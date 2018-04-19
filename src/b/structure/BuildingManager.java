package b.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.ai.BonjwAIGame;
import b.economy.SupplyManager;
import b.economy.WorkerManager;
import b.idmap.MapUnitID;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class BuildingManager {

	static int SUPPLY_VALUE_SD = 16;
	static int SUPPLY_VALUE_CC = 20;

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	static UnitType Refinery = UnitType.Terran_Refinery;
	static UnitType Academy = UnitType.Terran_Academy;
	static UnitType Barracks = UnitType.Terran_Barracks;
	static UnitType Factory = UnitType.Terran_Factory;
	
	static UnitType Marine = UnitType.Terran_Marine;
	static UnitType Medic = UnitType.Terran_Medic;
	static int MAX_MARINE_COUNT = 45;
	static int MAX_MEDIC_COUNT = 8;
	
	// BUILDING MANAGER
	public static void buildingManager( Game game, Player self,
				Multimap<UnitType, Integer> bArmyMap,
				Multimap<UnitType, Integer> bStructMap,
				ArrayList<Integer> bResources, 
				ArrayList<BaseLocation> bBasePos,
				int mineralSetup ) {
		buildWorkers(game,self,bArmyMap,bStructMap,bResources);
		updateSupplyManager(game,self,bArmyMap,bStructMap,bResources, bBasePos, mineralSetup);
		refineryManager(game,self,bArmyMap,bStructMap,bResources);
		academyManager(game,self,bArmyMap,bStructMap,bResources);
		barracksManager(game,self,bArmyMap,bStructMap,bResources);
		factoryManager(game,self,bArmyMap,bStructMap,bResources);
		buildingProduction(game,self,bArmyMap,bStructMap,bResources);
	}
	
	// COMMAND CENTER FUNCTIONS
	
	// function to build workers
	public static void buildWorkers(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources ) {
		int SCVcount = bArmyMap.get(UnitType.Terran_SCV).size();
		boolean needSupply = SupplyManager.needSupplyCheck(self, bResources.get(5));
		int reservedMinerals = bResources.get(1);	
		
		for ( Integer CCID : bStructMap.get(UnitType.Terran_Command_Center) ) {
			Unit CC = game.getUnit(CCID);
			if (CC.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false && 
					SCVcount < (16 * bStructMap.get(UnitType.Terran_Command_Center).size() ) ) {
				CC.train(UnitType.Terran_SCV);
			}
		}
	}
	
	public static int getNumPlannedStruct(Game game, Multimap<UnitType, Integer> bArmyMap, UnitType struct) {
		int structCount = 0;
		for ( int scvID : bArmyMap.get(UnitType.Terran_SCV ) ) {
			Unit SCV = game.getUnit(scvID);
			if ( SCV.isConstructing() && SCV.getBuildType() == struct && SCV.canAttack()) {
				structCount++;
			}
		}
		return structCount;
	}
	
	public static int getNumConstructingStruct(Game game, Multimap<UnitType, Integer> bArmyMap, UnitType struct) {
		int structCount = 0;
		for ( int scvID : bArmyMap.get(UnitType.Terran_SCV ) ) {
			Unit SCV = game.getUnit(scvID);
			if ( SCV.isConstructing() && SCV.getBuildType() == struct && !SCV.canAttack()) {
				structCount++;
			}
		}
		return structCount;
	}

	public static void updateSupplyManager(Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources,
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup ) {
		// get number of supply depots
		int numSupply = MapUnitID.getStructCount(game, bArmyMap, bStructMap, SD);

		// if conditions met to build a supply depot
		if ( SupplyManager.needSupplyCheck(self,bResources.get(5)) && (bResources.get(0)-bResources.get(1)) >= 100 ) {
			// first supply depot position
			if ( numSupply == 0 ) {
				// find pos of first SD
				TilePosition pos = BuildingPlacement.getBuildPositionFirstSD(game, bBasePos, mineralSetup);
				// issue build at TilePosition found
				WorkerManager.issueBuildAtLocation(game, bArmyMap, pos);
			}
			else {
				// default build alg
				WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, SD);
			}
		}
	}
	
	// TODO: Write a function that assigns first 16 SCVs to CC1, next 16 to CC2
	// using getUnitsInRadius or make func above only build scvs if num SCVs near CC is <16

	// Refinery
	public static void refineryManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numRefinery = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Refinery);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 24 && self.minerals() - reservedMinerals > 75
				&& numRefinery < 1
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Refinery
			WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, Refinery);
			System.out.println("Num refinery" + numRefinery);
		}
	}
	
	// Academy
	public static void academyManager(Game game, Player self,
				Multimap<UnitType, Integer> bArmyMap,
				Multimap<UnitType, Integer> bStructMap,
				ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks);
		int numAcademy = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Academy);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150
				&& numAcademy < 1 && numBarracks > 1
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Academy
			WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, Academy);
		}
	}
	
	// Barracks
	public static void barracksManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks);
		int numAcademy = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Academy);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150
				&& (numBarracks < 2 || numAcademy == 1) && numBarracks < 4
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Barracks
			WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, Barracks);
		}
	}
	
	// Factory
	public static void factoryManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks);
		int numFactory = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Factory);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150 
				&& self.gas() > 100
				&& numBarracks >= 4 && numFactory < 2
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Factory
			WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, Factory);
		}
	}
	
	// TRAIN UNITS
	public static void buildingProduction(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources) {
		int reservedMinerals = bResources.get(1);
		for ( Integer structID : bStructMap.values() ) {
			if ( structID == -1 ) {
				continue;
			}
			
			Unit struct = game.getUnit(structID);
			
			// train medics
			if ( struct.getType() == Barracks &&
					MapUnitID.getStructCount(game, bArmyMap, bStructMap, Academy) > 0 &&
					MapUnitID.getArmyCount(game, bArmyMap, Medic)  < MAX_MEDIC_COUNT &&
					MapUnitID.getArmyCount(game, bArmyMap, Marine) > 10 &&
					struct.isTraining() == false && 
					self.minerals() - reservedMinerals >= 50 &&
					self.gas() >= 25) {
				struct.train(UnitType.Terran_Medic);
			}
			
			// train marines
			if ( struct.getType() == Barracks && 
					MapUnitID.getArmyCount(game, bArmyMap, Marine) < MAX_MARINE_COUNT && 
					struct.isTraining() == false && 
					self.minerals() - reservedMinerals >= 50) {
				struct.train(UnitType.Terran_Marine);
			}
		}
	}
	
}
