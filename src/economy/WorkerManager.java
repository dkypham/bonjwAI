package economy;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import idmap.MapUnitID;
import map.MapInformation;
import structure.BuildingManager;
import structure.BuildingPlacement;

public class WorkerManager {

	static int MAX_SCV_GAS_MINERS = 3;
	static int SUPPLY_VALUE_SD = 16;
	static int SUPPLY_VALUE_CC = 20;

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	static UnitType GasMiner = UnitType.Powerup_Terran_Gas_Tank_Type_1;
	
	static String uT_ScoutSCV = "Scout";
	static String uT_DefendSCV = "Defender";
	static String uT_RepairSCV ="Repairer";
	static String uT_BuildSCV = "Builder";
	static String uT_GasSCV = "GasMiner";
	
	public static void updateWorkerManager(Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Base> bBases ) {
		// make idle SCVs mine
		WorkerManager.makeIdleWorkersMine(game, bArmyMap);
		// 
		for ( Base bBase : bBases ) {
			// check if has refinery
			if ( bBase.isGasMinable() ) {
				// if not fully saturated gas, assign gas miners
				if ( bBase.getNumGasMiners() != 3 ) {
					assignGasMiners( game, bArmyMap, bRolesMap, bBase );
				}
			}
		}
	}
	
	public static void assignGasMiners( Game game, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap, Base bBase ) {
		int gasMinerCount = bBase.getNumGasMiners();
		for ( int i = gasMinerCount; i < MAX_SCV_GAS_MINERS; i++ ) {
			int gasMinerID = assignGasSCV(game, bRolesMap, bArmyMap);
			bBase.addToGasMinerIDs(gasMinerID);
			Unit gasMiner = game.getUnit( gasMinerID );
			gasMiner.gather( bBase.Refinery );
		}
	}
	
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
	
	public static int getFreeSCVID(Game game, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap ) {
		List<Integer> arraySCV = (List<Integer>) bArmyMap.get(UnitType.Terran_SCV);
		for ( Integer SCVID : arraySCV ) {
			Unit SCV = game.getUnit(SCVID);		
			if (SCV.isConstructing() == false 
					&& SCV.isCarryingMinerals() == false 
					&& SCV.isCarryingGas() == false
					&& !bRolesMap.containsEntry(uT_ScoutSCV, SCV.getID()) 
					&& !bRolesMap.containsEntry(uT_DefendSCV, SCV.getID())
					&& !bRolesMap.containsEntry(uT_RepairSCV, SCV.getID())
					&& !bRolesMap.containsEntry(uT_GasSCV, SCV.getID())
					&& !SCV.isConstructing()
					) {
				return SCVID;
			}
		}
		return -1;
	}
	
	public static boolean checkifSpecialRole(int SCVID, Multimap<String, Integer> bRolesMap,
			String specialRole ) {
		if ( bRolesMap.containsKey(specialRole) ) {
			return bRolesMap.containsEntry(specialRole, SCVID);
		}
		return false;
	}
	
	public static boolean checkIfScoutSCV(int SCVID, Multimap<String, Integer> bRolesMap) {
		if ( bRolesMap.containsKey( uT_ScoutSCV ) ) {
			return bRolesMap.containsEntry( uT_ScoutSCV , SCVID);
		}
		return false;
	}
	
	public static boolean checkIfGasSCV(int SCVID, Multimap<String, Integer> bRolesMap) {
		if ( bRolesMap.containsKey( uT_GasSCV ) ) {
			return bRolesMap.containsEntry( uT_GasSCV , SCVID);
		}
		return false;
	}
	
	public static boolean issueBuild(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			UnitType struct,
			List<Pair<TilePosition,TilePosition>> noBuildZones) {
		//if ( bStructMap.containsEntry(building,-1) ) {
		//	//System.out.println("" + building + "is already being built");
		//	return;
		//}
		
		int freeSCVID = getFreeSCVID(game,bArmyMap,bRolesMap);
		if ( freeSCVID == -1 ) {
			return false; // no valid SCV found
		}
		Unit SCV = game.getUnit(freeSCVID);
		
		SCV.stop();
		TilePosition buildTile = BuildingPlacement.getBuildTile(game, struct,
				self.getStartLocation(), noBuildZones);
		if (buildTile != null) {
			if ( SCV.build(struct, buildTile) ) {
				//bStructMap.put(building, -1);
				System.out.println("Issued order to build: " + struct);
				return true;
			}
		}
		return false;
	}
	
	public static boolean issueBuildAtLocation(Game game,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			TilePosition pos,
			UnitType building ) {
		//if ( bStructMap.containsEntry(building,-1) ) {
		//	//System.out.println("" + building + "is already being built");
		//	return;
		//}
		//game.drawTextMap( pos.getX(), pos.getY(), "build here2222222222");

		int freeSCVID = getFreeSCVID(game,bArmyMap,bRolesMap);
		if ( freeSCVID == -1 ) {
			return false; // no valid SCV found
		}
		Unit SCV = game.getUnit(freeSCVID);
		
		//SCV.stop();
		pos = game.getBuildLocation(building, pos, 1);
		//if ( !game.canBuildHere(pos,  SD, SCV, false)) {
		//	System.out.println("Cannot build here");
		//}
		return SCV.build(building,pos);
		//System.out.println("issued build order");
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
	
	public static boolean checkIfRefineryExists(Player self) {
		for ( Unit myUnit : self.getUnits() ) {
			if ( myUnit.getType() == UnitType.Terran_Refinery ) {
				return true;
			}
		}
		return false;
	}
	
	public static Pair<Integer,Integer> getNumWorkersInBase( Game game, Player self, Pair<Position,Position> miningRegion,
			Multimap<UnitType, Integer> bArmyMap ) {
		List<Integer> arrayValidSCV = new ArrayList<Integer>();
		List<Integer> arraySCV = (List<Integer>) bArmyMap.get(UnitType.Terran_SCV);
		for ( Integer SCVID : arraySCV ) {
			if ( MapInformation.checkIfInRegion( game.getUnit(SCVID).getPosition() , miningRegion  ) ) {
				arrayValidSCV.add( SCVID );
			}
			
		}
		int numMineralMiners = 0;
		int numGasMiners = 0;
		for ( Integer validSCVID : arrayValidSCV ) {
			Unit SCV = game.getUnit(validSCVID);
			if ( SCV.isGatheringMinerals() ) {
				numMineralMiners++;
			}
			if ( SCV.isGatheringGas() ) {
				numGasMiners++;
			}
		}
		return new Pair<Integer,Integer>(numMineralMiners, numGasMiners);
	}
	
	/**
	 * Returns ID of an SCV that is not a scout, defender, or repairer
	 * @param game
	 * @param bArmyMap
	 * @return
	 */
	public static int getSCVwithNoRole( Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		ArrayList<Integer> invalidIDs = new ArrayList<Integer>();
		for ( Integer scoutID : bRolesMap.get(uT_ScoutSCV )) {
			invalidIDs.add(scoutID);
		}
		for ( Integer scoutID : bRolesMap.get(uT_RepairSCV )) {	// repair
			invalidIDs.add(scoutID);
		}
		for ( Integer scoutID : bRolesMap.get(uT_DefendSCV )) { // defend
			invalidIDs.add(scoutID);
		}
		for ( Integer scoutID : bRolesMap.get(uT_BuildSCV )) { // build
			invalidIDs.add(scoutID);
		}
		for ( Integer scoutID : bRolesMap.get(uT_GasSCV )) { // gas
			invalidIDs.add(scoutID);
		}
		for ( Integer scvID : bArmyMap.get(UnitType.Terran_SCV) ) {
			if ( invalidIDs.contains(scvID) ) {	// if this ID is assigned a role, go to next one
				continue;
			}
			return scvID;
		}
		return -1; // error
	}
	
	/**
	 * Check if SCVs have been assigned roles yet.
	 * 
	 * @param bSpecialRoles
	 * @return
	 */
	public static boolean checkIfAllSCVRolesAssigned( Multimap<String, Integer> bRolesMap ) {
		if ( bRolesMap.get(uT_ScoutSCV).isEmpty() || bRolesMap.get(uT_DefendSCV).isEmpty()
				|| bRolesMap.get(uT_RepairSCV).isEmpty() || bRolesMap.get(uT_BuildSCV).isEmpty() ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Assign SCV roles
	 * 
	 * @param bArmyMap
	 */
	public static void fillSCVRoles( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		if ( bRolesMap.get( uT_ScoutSCV ).isEmpty() ) {
			assignScoutSCV( game, bRolesMap, bArmyMap );
		}
		if ( bRolesMap.get( uT_DefendSCV ).isEmpty() ) {
			assignDefendSCV( game, bRolesMap, bArmyMap );
		}
		if ( bRolesMap.get( uT_RepairSCV ).isEmpty() ) {
			assignRepairSCV( game, bRolesMap, bArmyMap );
		}
		if ( bRolesMap.get( uT_BuildSCV ).isEmpty() ) {
			assignBuildSCV( game, bRolesMap, bArmyMap );
		}
		/*
		System.out.println("reg SCV ID: " + bArmyMap.get(UnitType.Terran_SCV));
		System.out.println("scout SCV ID: " + bRolesMap.get(uT_ScoutSCV));
		System.out.println("defend SCV ID: " + bRolesMap.get(uT_DefendSCV));
		System.out.println("repair SCV ID: " + bRolesMap.get(uT_RepairSCV));
		*/
	}
	
	public static void assignScoutSCV( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		int freeSCVID = getFreeSCVID( game, bArmyMap, bRolesMap );
		bRolesMap.put( uT_ScoutSCV , freeSCVID );
		System.out.println("Assiged SCV with ID as scout: " + bRolesMap.get(uT_ScoutSCV));
	}
	public static void assignDefendSCV( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		int freeSCVID = getFreeSCVID( game, bArmyMap, bRolesMap );
		bRolesMap.put( uT_DefendSCV , freeSCVID );
		System.out.println("Assiged SCV with ID as defender: " + bRolesMap.get(uT_DefendSCV));
	}
	public static void assignRepairSCV( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		int freeSCVID = getFreeSCVID( game, bArmyMap, bRolesMap );
		bRolesMap.put( uT_RepairSCV , freeSCVID );
		System.out.println("Assiged SCV with ID as repairer: " + bRolesMap.get(uT_RepairSCV));
	}
	public static void assignBuildSCV( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		int freeSCVID = getFreeSCVID( game, bArmyMap, bRolesMap );
		bRolesMap.put( uT_BuildSCV , freeSCVID );
		System.out.println("Assiged SCV with ID as builder: " + bRolesMap.get(uT_BuildSCV));
	}
	
	public static int assignGasSCV( Game game, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bArmyMap ) {
		int freeSCVID = getFreeSCVID( game, bArmyMap, bRolesMap );
		bRolesMap.put( uT_GasSCV , freeSCVID );
		System.out.println("Assiged SCV with ID as gasMiner: " + freeSCVID);
		return freeSCVID;
	}
	
	public static void defendAgainstSCout( Game game, Multimap<String, Integer> bRolesMap, Unit underAttackSCV,
			Unit enemyScout ) {
		Unit defender = MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, uT_DefendSCV);
		Unit repairer = MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, uT_RepairSCV);
		
		// defender behavior
		if ( !defender.isAttacking() ) {
			defender.attack( enemyScout );
		}
		
		// repairer behavior
		if ( !repairer.isRepairing() ) {
			if ( repairer.getID() != underAttackSCV.getID() && MapUnitID.isInjured(underAttackSCV)) {
				repairer.repair(underAttackSCV);
			}
		}
	}
	
	
}
