package b.map;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.ai.BonjwAI;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class ScoutManager {
		
	public static void updateScoutManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, 
			ArrayList<Position> eStructPos, 
			ArrayList<BaseLocation> eBasePos, 
			ArrayList<BaseLocation> bBasePos,
			List<Position> scoutQueue,
			List<Pair<Position,Position>> miningRegionsList ) {
		
		// if no scout assigned
		if ( bArmyMap.get( UnitType.Protoss_Scout ).size() == 0 ) {
			ScoutManager.assignScout(game, bArmyMap);
		}
		
		Unit scout = getScout( game, bArmyMap );
		
		if ( scout != null && self.supplyUsed() > 24 ) {
			if ( !scout.isAttacking() ) {
				if ( !scoutQueue.isEmpty() ) {	
					scout.attack( scoutQueue.get(0) );
				}
				// if no enemies struct history seen
				else if ( eStructPos.size() == 0 && !scout.isAttacking() ) {
					ScoutManager.scoutForUnknownEnemy(game, bArmyMap, eStructPos);
				}	
			}
			
			MapInformation.updateEnemyBuildingMemory(game, eStructPos);
		}
		
		// update scout queue
		//if ( game.getFrameCount() % 100 == 0 ) {
			if ( self.supplyTotal() > 24 && scoutQueue.size() == 1 ) {
				if ( MapInformation.checkIfExpoIsExplored(game, bBasePos.get(1) ) ) {
					
					scoutQueue.remove( 0 );	
				}
			}
		//}
	}
	
	// assign a scout
	public static boolean assignScout(Game game, Multimap<UnitType, Integer> bArmyMap ) {
			for ( Integer scvID : bArmyMap.get(UnitType.Terran_SCV) ) {
				Unit SCV = game.getUnit(scvID);
				if ( !SCV.isConstructing() && !SCV.isCarryingMinerals() 
					&& !SCV.isGatheringGas() ) {
					// add scout to bArmyMap
					bArmyMap.put(UnitType.Protoss_Scout, SCV.getID() );
					return true;
				}
			}
			return false;
	}
	
	public static Unit getScout(Game game, Multimap<UnitType, Integer> bArmyMap ) {
		Unit scout = null;
		for ( Integer scoutID : bArmyMap.get(UnitType.Protoss_Scout ) ) {
			scout = game.getUnit(scoutID);
		}
		return scout;
	}
	
	// scout for enemy based on unexplored starting locations
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
	
	// scout to next baselocation
	public static void scoutForNextBase(Game game, Unit scout) {
		Position baseLocation = MapInformation.getNearestUnexploredStartingLocation(game, scout.getPosition());
		if (baseLocation != null) {
			scout.move(baseLocation);
		}
	}

	public static void initializeScoutQueue(List<Position> scoutQueue, ArrayList<BaseLocation> bBasePos) {
		scoutQueue.add(bBasePos.get(1).getPosition() );	
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
