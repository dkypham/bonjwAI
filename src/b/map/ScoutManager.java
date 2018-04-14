package b.map;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.ai.BonjwAI;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class ScoutManager {

	private static int scoutID = 0;
		
	public static void updateScoutManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, 
			ArrayList<Position> eStructPos, 
			ArrayList<BaseLocation> eBasePos) {
		
		if ( self.supplyUsed() > 18 ) {
			// if no enemies struct history seen
			if ( eStructPos.size() == 0 ) {
				// if no scout assigned
				if ( scoutID == 0 ) {
					scoutID = ScoutManager.assignScout(game, bArmyMap, scoutID);
				}
				// if scout assigned, scout
				if ( scoutID != 0 ) {
					ScoutManager.scoutForUnknownEnemy(game, scoutID, bArmyMap, eStructPos);
				}
			}	
			/*
			if ( eStructPos.size() != 0 && eBasePos.size() == 0 ) {
				eBasePos.add(MapInformation.getClosestStartLocation(eStructPos.get(0)));
				MapInformation.getTwoNearBases(game, eBasePos);
				System.out.println("Updated eBasePos!");
			}
			*/
			MapInformation.updateEnemyBuildingMemory(game, eStructPos);
		}
	}
	
	// assign a scout
	public static int assignScout(Game game, Multimap<UnitType, Integer> bArmyMap, int scoutID ) {
			for ( Integer scvID : bArmyMap.get(UnitType.Terran_SCV) ) {
				Unit SCV = game.getUnit(scvID);
				if ( !SCV.isConstructing() && !SCV.isCarryingMinerals() 
					&& !SCV.isGatheringGas() ) {
					return scvID;
				}
			}
			return 0;
	}
	
	// scout for enemy based on unexplored starting locations
	public static void scoutForUnknownEnemy(Game game, int scoutID, 
			Multimap<UnitType, Integer> bArmyMap, 
			ArrayList<Position> eStructPos) {
		Unit scout = game.getUnit(scoutID);
		if ( !scout.isMoving() && eStructPos.size() == 0) {
			scout.move( MapInformation.getNearestUnexploredStartingLocation(game,
					scout.getPosition() ) );
		}
	}
	
	// scout to next baselocation
	public static void scoutForNextBase(Game game, Unit scout) {
		Position baseLocation = MapInformation.getNearestUnexploredStartingLocation(game, scout.getPosition());
		if (baseLocation != null) {
			scout.move(baseLocation);
		}
	}
	
	/*
	public static Position updateEnemyPosition(Game game, Position enemyPosition) {
		for ( Unit u : game.enemy().getUnits() ) {
			if ( u != null && u.isVisible() ) {
				enemyPosition = u.getPosition();
				break;
			}
		}
		return enemyPosition;
	}
	*/
	
}
