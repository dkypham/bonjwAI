package map;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.WorkerManager;
import idmap.MapUnitID;

/**
 * Scout Manager, on each call
 * 
 * Assign a scout (SCV) if there is no scout.
 * 
 * Check if supply > 12, scout is not attacking, bot is not under attack,
 * and there are no enemy buildings seen. If so, then scout.
 * 
 */
public class ScoutManager {
		
	static String scoutRole = "Scout";
	
	public static void updateScoutManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<String, Integer> bSpecialRoles, 
			ArrayList<Position> eStructPos, 
			ArrayList<BaseLocation> bBasePos, // then remove this
			List<Position> scoutQueue,
			boolean[] underAttack) {
		// don't update scout if under attack
		if ( underAttack[0] == true ) {
			return;
		}
		
		// assign a scout if there is no scout
		if ( bSpecialRoles.get( scoutRole ).size() == 0 ) {
			WorkerManager.assignScoutSCV(game, bSpecialRoles, bArmyMap);
		}
		Unit scout = getScout( game, bSpecialRoles );
		
		// Check if valid scout and supply > 12
		if ( scout != null && self.supplyUsed() > 24 ) {
			// scout queue not empty, then scout it
			if ( scoutQueue.size() != 0 ) {
				scout.attack( scoutQueue.get(0) );
			}
			// if no enemy structs scouted and nothing in queue, generic scout
			else if ( eStructPos.size() == 0 ) {
				ScoutManager.scoutForUnknownEnemy(game, scout);
			}
			else if ( scout.getDistance(bBasePos.get(0).getPoint()) > 700 ) {
				scout.move( bBasePos.get(0).getPoint() );
			}
		}

		// update scout queue
		if ( self.supplyUsed() > 24 && scoutQueue.size() != 0 ) {
			// if value in scoutQueue is seen, remove it
			if ( isPosScouted(game, scoutQueue.get(0)) ) {
				scoutQueue.remove( 0 );	
			}
		}
		MapInformation.updateEnemyBuildingMemory(game, eStructPos);
	}
	
	/**
	 * get scout unit from army multimap
	 * 
	 * @param game
	 * @param bArmyMap - bot's army multimap
	 * @return
	 */
	public static Unit getScout(Game game, Multimap<String, Integer> bRolesMap ) {
		return MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, scoutRole);
	}
	
	/**
	 * scout for enemy based on unexplored starting locations
	 * 
	 * @param game
	 * @param bArmyMap
	 * @param eStructPos
	 */
	public static void scoutForUnknownEnemy(Game game, Unit scout ) {
		scout.move( MapInformation.getNearestUnexploredStartingLocation(game,scout.getPosition() ) );
	}
	
	/**
	 * scout to next baselocation
	 * @param game
	 * @param scout
	 */
	public static void scoutForNextBase(Game game, Unit scout) {
		Position baseLocation = MapInformation.getNearestUnexploredStartingLocation(game, scout.getPosition());
		if (baseLocation != null) {
			scout.move(baseLocation);
		}
	}
	
	public static boolean isPosScouted( Game game, Position pos ) {
		TilePosition tp = pos.toTilePosition();
		TilePosition farRight = new TilePosition(tp.getX() + 1, tp.getY());
		TilePosition farLeft = new TilePosition(tp.getX() - 1, tp.getY());
		TilePosition farUp = new TilePosition(tp.getX(), tp.getY() + 1);
		TilePosition farDown = new TilePosition(tp.getX(), tp.getY() - 1);
		//return ( game.isExplored(tp) && game.isExplored(farRight) && game.isExplored(farLeft) 
		//		&& game.isExplored(farLeft) && game.isExplored(farDown) );
		return game.isExplored(tp);
	}

	/**
	 * initialize scoutQueue
	 * 
	 * Currently the only value is bonjwAI's first expo.
	 * TODO: update to scout specific spots on specific maps/matchups
	 * 
	 * @param scoutQueue
	 * @param bBasePos
	 */
	public static void initializeScoutQueue(List<Position> scoutQueue, ArrayList<BaseLocation> bBasePos) {
		// add first expansion
		for ( Unit minPatches : bBasePos.get(1).getMinerals() ) {
			scoutQueue.add(minPatches.getPosition());
		}	
		for ( Unit gasPatches : bBasePos.get(1).getGeysers() ) {
			scoutQueue.add(gasPatches.getPosition());
		}
	}
	
}
