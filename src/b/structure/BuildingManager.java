package b.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.ai.BonjwAIGame;
import b.economy.ResourceManager;
import b.economy.SupplyManager;
import b.economy.WorkerManager;
import b.idmap.MapUnitID;
import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
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
	/*
	public static void buildingManager( Game game, Player self,
				Multimap<UnitType, Integer> bArmyMap,
				Multimap<UnitType, Integer> bStructMap,
				ArrayList<Integer> bResources, 
				ArrayList<BaseLocation> bBasePos,
				int mineralSetup,
				List<Position> drawStructPos,
				List<String> drawStructLabel,
				List<UnitType> buildOrderStruct,
				List<Integer> buildOrderSupply) {
		//getBuildingPlan(game,self,bArmyMap, bStructMap, drawStructPos,drawStructLabel,mineralSetup, bBasePos);
		buildWorkers(game,self,bArmyMap,bStructMap,bResources);
		//updateSupplyManager(game,self,bArmyMap,bStructMap,bResources, bBasePos, mineralSetup);
		//refineryManager(game,self,bArmyMap,bStructMap,bResources);
		//academyManager(game,self,bArmyMap,bStructMap,bResources);
		//barracksManager(game,self,bArmyMap,bStructMap,bResources, bBasePos, mineralSetup);
		//factoryManager(game,self,bArmyMap,bStructMap,bResources);
	}
	*/
	public static void buildingManager( Game game, Player self,
			ArrayList<BaseLocation> bBasePos,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int productionMode, ArrayList<Integer> bResources,
			List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			int mineralSetup) {
		// check if something needs to be built at this supply
		if ( self.supplyUsed() == buildOrderSupply.get(0)*2 ) {
			// issue build
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bStructMap, bResources, 
					buildOrderStruct.get(0) ) ) {
				//buildOrderStruct.remove(0);
				//buildOrderSupply.remove(0);
				buildOrderSupply.set(0, buildOrderSupply.get(0)*-1);
				System.out.println(buildOrderStruct.get(0));
			}
		}
		// if not, issue build of ONE unit
		else {
			// issue build
			if ( productionMode == 0 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources);
			}
			
		}
	}
	
	public static boolean buildStruct( Game game, Player self, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources,
			UnitType struct ) {
		// if building type is SD
		if ( struct == SD && checkIfEnoughResources(bResources, struct) ) {
			// first SD
			if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, struct) == 0 ) {
				TilePosition pos = BuildingPlacement.getBuildPositionSD(game, bArmyMap, bStructMap, bBasePos, mineralSetup);
				if ( WorkerManager.issueBuildAtLocation(game, bArmyMap, pos, SD) ) {
					// update bResources
					ResourceManager.addBuildingCost( bResources, struct );
					return true;
				}
			}
			else {
				System.out.println("trying to build 2nd depot");
				ResourceManager.addBuildingCost( bResources, struct );
				return WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, struct);				
			}
		}
		if ( struct == Barracks && checkIfEnoughResources(bResources, struct) ) {
			TilePosition pos = BuildingPlacement.getBuildPositionFirstBarracks(game, bBasePos, mineralSetup);
			if ( WorkerManager.issueBuildAtLocation(game, bArmyMap, pos, Barracks) ) {
				ResourceManager.addBuildingCost( bResources, struct );
				return true;
			}
		}
		
		// general case
		if ( checkIfEnoughResources(bResources, struct) ) {
			ResourceManager.addBuildingCost( bResources, struct );
			return WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, struct);
		}
		return false;
	}
	
	public static boolean checkIfEnoughResources( ArrayList<Integer> bResources, 
			UnitType struct ) {
		// check minerals
		if ( bResources.get(0) - bResources.get(2) < struct.mineralPrice() ) {
			return false;
		}
		if ( bResources.get(1) - bResources.get(3) < struct.gasPrice() ) {
			return false;
		}
		
		// check gas
		
		return true;
	}
	
	public static void buildUnit( Game game, Player self, 			
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int buildingMode, ArrayList<Integer> bResources ) {
		if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources) ) {
			return;
		}
	}
	
	// COMMAND CENTER FUNCTIONS
	
	// method to find build locations
	public static void getBuildingPlan(Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap, List<Position> drawStructPos,
			List<String> drawStructLabel, int mineralSetup, ArrayList<BaseLocation> bBasePos) {
		drawStructPos.add( (BuildingPlacement.getBuildPositionFirstSD(game, bBasePos, mineralSetup)).toPosition());
		drawStructLabel.add("First Supply Depot");
		drawStructPos.add( (BuildingPlacement.getBuildPositionFirstBarracks(game, bBasePos, mineralSetup)).toPosition());
		drawStructLabel.add("First Barracks");
	}
	
	// Given an array for order of buildings and array with supplies, issue a build order for that building if
	// given supply is met
	public static void issueBuildOrder(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap, 
			List<Position> drawStructPos,
			List<String> drawStructLabel, 
			int mineralSetup, 
			ArrayList<BaseLocation> bBasePos,
			List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			ArrayList<Integer> bResources ) {
		for ( int i = 0; i < buildOrderStruct.size(); i++ ) {
			// if supply used < supplyTotal 
			if ( (self.supplyTotal() * 2) < buildOrderSupply.get(i) ) {
				// production
				buildingProduction(game,self,bArmyMap,bStructMap,bResources);
				break;
			}
			
		}
		
	}

	// function to build workers
	public static boolean buildWorkers(Game game, Player self, 
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
				return true;
			}
		}
		return false;
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
				WorkerManager.issueBuildAtLocation(game, bArmyMap, pos, SD);
			}
			else {
				// default build alg
				WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, SD);
			}
		}
	}
	/*
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
			ArrayList<Integer> bResources,
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup) {
		// IMPLEMENT build barracks when minerals > 150
		int numBarracks = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks);
		int numAcademy = MapUnitID.getStructCount(game, bArmyMap, bStructMap, Academy);
		int reservedMinerals = bResources.get(1);
		
		if ( self.supplyUsed() > 20 && self.minerals() - reservedMinerals > 150
				&& (numBarracks < 2 || numAcademy == 1) && numBarracks < 4
				&& SupplyManager.needSupplyCheck(self, bResources.get(5)) == false ) {
			// build Barracks
			if ( numBarracks == 0 ) {
				TilePosition pos = BuildingPlacement.getBuildPositionFirstBarracks(game, bBasePos, mineralSetup);
				// issue build at TilePosition found
				WorkerManager.issueBuildAtLocation(game, bArmyMap, pos, Barracks);
			}
			else {
				WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, Barracks);
			}
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
	*/
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
