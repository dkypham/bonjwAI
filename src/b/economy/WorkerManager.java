package b.economy;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.ai.BonjwAI;
import b.structure.BuildingPlacement;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class WorkerManager {

	static int MAX_SCV_GAS_MINERS = 1;
	static int SUPPLY_VALUE_SD = 16;
	static int SUPPLY_VALUE_CC = 20;

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	public static void updateWorkerManager(Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap) {
		WorkerManager.makeIdleWorkersMine(game, bArmyMap);
		if ( WorkerManager.getGasMinerCount(game, bArmyMap) < 1 ) {
			WorkerManager.makeWorkersMineGas(game, self, bArmyMap, bStructMap);
		}
	}

	// WORKER COUNTER METHODS GO HERE
	public static int getWorkerCount(Multimap<UnitType, Integer> bArmyMap) {
		return bArmyMap.get(UnitType.Terran_SCV).size();
	}
	
	// WORKER IDLE METHOD
	public static void makeIdleWorkersMine(Game game, Multimap<UnitType, Integer> bArmyMap) {
		List<Integer> arraySCVID = (List<Integer>) bArmyMap.get(UnitType.Terran_SCV);
		for ( Integer SCVID : arraySCVID) {
			Unit scv = game.getUnit(SCVID);
			if (scv.getType().isWorker() && scv.isIdle() && !scv.isConstructing() 
					&& !scv.isMoving()) {
				Unit closestMineral = null;
				for (Unit neutralUnit : game.neutral().getUnits()) {
					if (neutralUnit.getType().isMineralField()) {
						if (closestMineral == null
								|| scv.getDistance(neutralUnit) < scv.getDistance(closestMineral)) {
							closestMineral = neutralUnit;
						}
					}
				}
				if (closestMineral != null) {
					scv.gather(closestMineral, false);
				}
			}
		}
	}
	
	public static int getFreeSCVID(Game game, Multimap<UnitType, Integer> bArmyMap) {
		List<Integer> arraySCV = (List<Integer>) bArmyMap.get(UnitType.Terran_SCV);
		for ( Integer SCVID : arraySCV ) {
			Unit SCV = game.getUnit(SCVID);		
			if (SCV.isConstructing() == false && SCV.isCarryingMinerals() == false && 
					SCV.isCarryingGas() == false ) {
				return SCVID;
			}
		}
		return 0;
	}
	
	public static void issueBuild(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			UnitType building) {
		//if ( bStructMap.containsEntry(building,-1) ) {
		//	//System.out.println("" + building + "is already being built");
		//	return;
		//}
		
		Unit SCV = 	game.getUnit(getFreeSCVID(game,bArmyMap));
		SCV.stop();
		TilePosition buildTile = BuildingPlacement.getBuildTile(game, SCV, building,
				self.getStartLocation());
		if (buildTile != null) {
			SCV.build(building, buildTile);
			//bStructMap.put(building, -1);
			System.out.println("Issued order to build: " + building);
			return;
		}	
	}
	
	public static void issueBuildAtLocation(Game game,
			Multimap<UnitType, Integer> bArmyMap,
			TilePosition pos,
			UnitType building ) {
		//if ( bStructMap.containsEntry(building,-1) ) {
		//	//System.out.println("" + building + "is already being built");
		//	return;
		//}
		//game.drawTextMap( pos.getX(), pos.getY(), "build here2222222222");

		Unit SCV = 	game.getUnit(getFreeSCVID(game,bArmyMap));
		if ( SCV == null ) {
			System.out.println("Null SCV");
		}
		//SCV.stop();
		pos = game.getBuildLocation(building, pos, 1);
		//if ( !game.canBuildHere(pos,  SD, SCV, false)) {
		//	System.out.println("Cannot build here");
		//}
		SCV.build(building,pos);
		System.out.println("issued build order");
	}
	
	public static int getMineralMinerCount(Game game, Multimap<UnitType, Integer> armyMap) {
		int mineralMinerCount = 0;
		List<Integer> arraySCV = (List<Integer>) armyMap.get(UnitType.Terran_SCV);
		for ( Integer scvID : arraySCV ) {
			Unit scv = game.getUnit(scvID);		
			if ( scv.isGatheringMinerals() ) {
				mineralMinerCount++;
			}
			
		}	
		return mineralMinerCount;
	}

	public static int getGasMinerCount(Game game, Multimap<UnitType, Integer> armyMap) {
		int gasMinerCount = 0;
		List<Integer> arraySCV = (List<Integer>) armyMap.get(UnitType.Terran_SCV);
		for ( Integer scvID : arraySCV ) {
			Unit scv = game.getUnit(scvID);		
			if ( scv.isGatheringGas() ) {
				gasMinerCount++;
			}
			
		}	
		return gasMinerCount;
	}
	
	public static boolean checkIfRefineryExists(Player self) {
		for ( Unit myUnit : self.getUnits() ) {
			if ( myUnit.getType() == UnitType.Terran_Refinery ) {
				return true;
			}
		}
		return false;
	}

	// MAKE 3 WORKERS MINE GAS
	public static void makeWorkersMineGas(Game game, Player self, Multimap<UnitType, Integer> armyMap,
			 Multimap<UnitType, Integer> structMap) {
		int gasMinerCount = getGasMinerCount(game, armyMap);
		if ( gasMinerCount < MAX_SCV_GAS_MINERS && checkIfRefineryExists(self) ) {
			List<Integer> arraySCV = (List<Integer>) armyMap.get(UnitType.Terran_SCV);
			for ( Integer scvID : arraySCV ) {
				Unit scv = game.getUnit(scvID);		
				if ( !scv.isConstructing() && !scv.isCarryingMinerals() 
						&& !scv.isGatheringGas()) {
					List<Integer> arrayRefinery = (List<Integer>) structMap.get(UnitType.Terran_Refinery);
					Unit refinery = game.getUnit(arrayRefinery.get(0));
					scv.gather(refinery);
					//gasMinerCount++;
					return;
				}
				
			}
		}
	}
	
	
}
