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

public class BuildingManager {

	int SUPPLY_VALUE = 16;
	
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
				Multimap<UnitType, Integer> armyMap,
				Multimap<UnitType, Integer> structMap,
				ArrayList<Integer> bResources ) {
		buildWorkers(game,self,armyMap,structMap,bResources);
		refineryManager(game,self,armyMap,structMap,bResources);
		academyManager(game,self,armyMap,structMap,bResources);
		barracksManager(game,self,armyMap,structMap,bResources);
		factoryManager(game,self,armyMap,structMap,bResources);
		buildingProduction(game,self,armyMap,structMap,bResources);
	}
	
	// COMMAND CENTER FUNCTIONS
	
	// function to build workers
	public static void buildWorkers(Game game, Player self, 
			Multimap<UnitType, Integer> armyMap, 
			Multimap<UnitType, Integer> structMap,
			ArrayList<Integer> bResources ) {
		int SCVcount = armyMap.get(UnitType.Terran_SCV).size();
		boolean needSupply = SupplyManager.needSupplyCheck(self, bResources.get(5));
		int reservedMinerals = bResources.get(1);	
		
		for ( Integer CCID : structMap.get(UnitType.Terran_Command_Center) ) {
			Unit CC = game.getUnit(CCID);
			if (CC.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false && 
					SCVcount < (16 * structMap.get(UnitType.Terran_Command_Center).size() ) ) {
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
	
	// TODO: Write a function that assigns first 16 SCVs to CC1, next 16 to CC2
	// using getUnitsInRadius or make func above only build scvs if num SCVs near CC is <16

	// Refinery
	public static void refineryManager(Game game, Player self,
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numRefinery = MapUnitID.getStructCount(game, armyMap, structMap, Refinery);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 24 && self.minerals() - reservedMinerals > 75
				&& numRefinery < 1
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Refinery
			WorkerManager.issueBuild(game, self, armyMap, structMap, Refinery);
		}
	}
	
	// Academy
	public static void academyManager(Game game, Player self,
				Multimap<UnitType, Integer> armyMap,
				Multimap<UnitType, Integer> structMap,
				ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, armyMap, structMap, Barracks);
		int numAcademy = MapUnitID.getStructCount(game, armyMap, structMap, Academy);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150
				&& numAcademy < 1 && numBarracks > 1
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Academy
			WorkerManager.issueBuild(game, self, armyMap, structMap, Academy);
		}
	}
	
	// Barracks
	public static void barracksManager(Game game, Player self,
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, armyMap, structMap, Barracks);
		int numAcademy = MapUnitID.getStructCount(game, armyMap, structMap, Academy);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150
				&& (numBarracks < 2 || numAcademy == 1) && numBarracks < 4
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Barracks
			WorkerManager.issueBuild(game, self, armyMap, structMap, Barracks);
		}
	}
	
	// Factory
	public static void factoryManager(Game game, Player self,
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap,
			ArrayList<Integer> bResources) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, armyMap, structMap, Barracks);
		int numFactory = MapUnitID.getStructCount(game, armyMap, structMap, Factory);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150 
				&& self.gas() > 100
				&& numBarracks >= 4 && numFactory < 2
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Factory
			WorkerManager.issueBuild(game, self, armyMap, structMap, Factory);
		}
	}
	
	// TRAIN UNITS
	public static void buildingProduction(Game game, Player self, 
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap,
			ArrayList<Integer> bResources) {
		int reservedMinerals = bResources.get(1);
		for ( Integer structID : structMap.values() ) {
			if ( structID == -1 ) {
				continue;
			}
			
			Unit struct = game.getUnit(structID);
			
			// train medics
			if ( struct.getType() == Barracks &&
					MapUnitID.getStructCount(game, armyMap, structMap, Academy) > 0 &&
					MapUnitID.getArmyCount(game, armyMap, Medic)  < MAX_MEDIC_COUNT &&
					MapUnitID.getArmyCount(game, armyMap, Marine) > 10 &&
					struct.isTraining() == false && 
					self.minerals() - reservedMinerals >= 50 &&
					self.gas() >= 25) {
				struct.train(UnitType.Terran_Medic);
			}
			
			// train marines
			if ( struct.getType() == Barracks && 
					MapUnitID.getArmyCount(game, armyMap, Marine) < MAX_MARINE_COUNT && 
					struct.isTraining() == false && 
					self.minerals() - reservedMinerals >= 50) {
				struct.train(UnitType.Terran_Marine);
			}
		}
	}
	
}
