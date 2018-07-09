package map;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.WorkerManager;

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
			List<Position> scoutQueue ) {
		
		// assign a scout if there is no scout
		if ( bSpecialRoles.get( scoutRole ).size() == 0 ) {
			WorkerManager.assignScoutSCV(bSpecialRoles, bArmyMap);
		}
		Unit scout = getScout( game, bSpecialRoles );
		
		// Check if valid scout and supply > 12
		if ( scout != null && self.supplyUsed() > 24 ) {
			// TODO: check if under attack
			if ( !scout.isAttacking() ) {
				// if scoutQueue is NOT empty
				if ( !scoutQueue.isEmpty() ) {	
					// TODO: update scouting move behavior
					scout.attack( scoutQueue.get(0) );
				}
				// if scoutQueue is empty
				else if ( eStructPos.size() == 0 ) {
					// TODO: update scouting move behavior
					ScoutManager.scoutForUnknownEnemy(game, bArmyMap, eStructPos);
				}	
			}
			MapInformation.updateEnemyBuildingMemory(game, eStructPos);
		}

		// update scout queue
		if ( self.supplyUsed() > 24 && scoutQueue.size() == 1 ) {

			// if value in scoutQueue is seen, remove it
			// TODO: check if value 0 of scoutQueue is seen, then remove it
			if ( MapInformation.checkIfExpoIsExplored(game, bBasePos.get(1) ) ) {
				scoutQueue.remove( 0 );	
			}
		}

	}
	
	/**
	 * get scout unit from army multimap
	 * 
	 * @param game
	 * @param bArmyMap - bot's army multimap
	 * @return
	 */
	public static Unit getScout(Game game, Multimap<String, Integer> bRolesMap ) {
		Unit scout = null;
		for ( Integer scoutID : bRolesMap.get( scoutRole ) ) {
			scout = game.getUnit(scoutID);
		}
		return scout;
	}
	
	/**
	 * scout for enemy based on unexplored starting locations
	 * 
	 * @param game
	 * @param bArmyMap
	 * @param eStructPos
	 */
	public static void scoutForUnknownEnemy(Game game, 
			Multimap<UnitType, Integer> bArmyMap, 
			ArrayList<Position> eStructPos) {

		Unit scout = null;
		for ( Integer scoutID : bArmyMap.get(UnitType.Protoss_Scout ) ) {
			scout = game.getUnit(scoutID);
		}
		
		if ( scout != null ) {
			if ( !scout.isMoving() && eStructPos.size() == 0) {
				scout.move( MapInformation.getNearestUnexploredStartingLocation(game,scout.getPosition() ) );
			}
		}
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
		scoutQueue.add(bBasePos.get(1).getPosition() );	
	}
	
}
