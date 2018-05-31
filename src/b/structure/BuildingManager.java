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
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
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
			List<TechType> techTreeTech,
			List<Integer> techTreeSupply,
			int mineralSetup,
			int[] timeBuildIssued ) {
		// check if something needs to be built at this supply
		if ( self.supplyUsed() == buildOrderSupply.get(0)*2 ) {
			
			// issue build
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bStructMap, bResources, 
					buildOrderStruct.get(0), 
					productionMode ) ) {
				buildOrderSupply.set(0, buildOrderSupply.get(0)*-1);
				//System.out.println(buildOrderStruct.get(0));
				timeBuildIssued[0] = game.elapsedTime();
			}
		}
		
		// if set only to >= then multiple SCVs go to build
		else if ( self.supplyUsed() >= buildOrderSupply.get(0)*2 
				&& buildOrderSupply.get(0) > 0 ) {
			// issue build
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bStructMap, bResources, 
					buildOrderStruct.get(0), 
					productionMode ) ) {
				buildOrderSupply.set(0, buildOrderSupply.get(0)*-1);
				timeBuildIssued[0] = game.elapsedTime();
			}
		}
		
		// check if tech needs to be built
		else if ( self.supplyUsed() >= techTreeSupply.get(0)*2 ) {
			// issue build
			if ( TechManager.buildTech(game, self,
					bStructMap, 
					bResources, 
					techTreeTech.get(0) ) ) {
				techTreeTech.remove(0);
				techTreeSupply.remove(0);
				return;
			}
		}
		
		// if not, issue build of ONE unit
		else {
			if ( buildOrderSupply.get(0) < 0 ) {
				// do not build unit if building is queued
				
				// check if time since set negative is > 20 seconds, then make
				// it positive. Usually if this is the case, the SCV
				// did not actually build
				if ( game.elapsedTime() - timeBuildIssued[0] > 20 ) {
					buildOrderSupply.set(0, buildOrderSupply.get(0)*-1);
				}
				return;
			}
			// issue build
			if ( productionMode == 0 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources);
			}
			else if ( productionMode == 1 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources);	
			}
			else if ( productionMode == 2 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources);
				
			}
			/*
			else if ( productionMode == 2 ) {
				if ( !self.hasResearched(TechType.Tank_Siege_Mode) ) {
					Unit machineShop = MapUnitID.getStruct(game, bStructMap, UnitType.Terran_Machine_Shop);
					if ( machineShop != null ) {
						machineShop.research( TechType.Tank_Siege_Mode);
					}
				}
			}
			*/
			
		}
	}
	
	public static boolean buildStruct( Game game, Player self, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources,
			UnitType struct,
			int productionMode
			) {
		// check if enough resources
		if ( !ResourceManager.checkIfEnoughResources(bResources, struct) ) {
			return false;
		}
		
		// if building type is SD
		if ( struct == SD ) {
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
		if ( struct == Barracks ) {
			TilePosition pos = BuildingPlacement.getBuildPositionFirstBarracks(game, bBasePos, mineralSetup);
			if ( WorkerManager.issueBuildAtLocation(game, bArmyMap, pos, Barracks) ) {
				ResourceManager.addBuildingCost( bResources, struct );

				return true;
			}
		}
		
		// special case: addon
		if ( struct == UnitType.Terran_Machine_Shop ) {
			for ( Integer factoryID : bStructMap.get(UnitType.Terran_Factory ) ) {
				Unit factory = game.getUnit(factoryID);
				if ( factory.canBuildAddon() ) {
					return factory.buildAddon( struct );
				}
			}
		}
		
		if ( struct == CC ) {
			return buildCC( game, self, bArmyMap, bStructMap, bBasePos );
		}
		
		// general case
		if ( ResourceManager.checkIfEnoughResources(bResources, struct) ) {
			ResourceManager.addBuildingCost( bResources, struct );
			return WorkerManager.issueBuild(game, self, bArmyMap, bStructMap, struct);
		}
		return false;
	}
	
	public static int updateBuildStructTime( Game game ) {
		return game.elapsedTime();
	}
	
	public static void buildUnit( Game game, Player self, 			
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int productionMode, ArrayList<Integer> bResources ) {
		if ( productionMode == 0 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}	
		}
		if ( productionMode == 1 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
			if ( buildMarines(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
		}
		if ( productionMode == 2 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
			if ( buildTanks(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
			if ( buildMarines(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
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

	public static boolean buildCC( Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<BaseLocation> bBasePos ) {
		int numCC = MapUnitID.getStructCount(game, bArmyMap, bStructMap, CC);
		return WorkerManager.issueBuildAtLocation(game, bArmyMap, bBasePos.get(numCC).getTilePosition(), CC);
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
			Unit CCUnit = game.getUnit(CCID);
			if (CCUnit.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false && 
					SCVcount < (16 * MapUnitID.getStructCount(game, bArmyMap, bStructMap, CC)) ) {
				CCUnit.train(UnitType.Terran_SCV);
				return true;
			}
		}
		return false;
	}
	
	// function to build marines
	public static boolean buildMarines(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources ) {
		int marineCount = bArmyMap.get(UnitType.Terran_Marine).size();
		boolean needSupply = SupplyManager.needSupplyCheck(self, bResources.get(5));
		int reservedMinerals = bResources.get(1);	
		for ( Integer barracksID : bStructMap.get(UnitType.Terran_Barracks) ) {
			Unit barracks = game.getUnit(barracksID);
			if (barracks.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false && 
					marineCount < (16 * MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks) ) ) {
				barracks.train(UnitType.Terran_Marine);
				return true;
			}
		}
		return false;
	}
	
	// function to build tanks
	public static boolean buildTanks(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources ) {
		// check if enough resources
		if ( !ResourceManager.checkIfEnoughResources(bResources, UnitType.Terran_Siege_Tank_Tank_Mode) ) {
			return false;
		}
		
		// TODO: add supply check
		if ( SupplyManager.needSupplyCheck(self, bResources.get(5)) ) {
			return false; // return false if needSupplyCheck returns true
		}

		// if tank count needs to be restricted
		//int tankCount = bArmyMap.get(UnitType.Terran_Siege_Tank_Tank_Mode).size();	
		
		// build tank from factories
		for ( Integer factoryID : bStructMap.get(UnitType.Terran_Factory) ) {
			Unit factory = game.getUnit(factoryID);
			if ( factory.isTraining() == false 
					//&& tankCount < (16 * MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks) ) 
					&& factory.canTrain( UnitType.Terran_Siege_Tank_Tank_Mode ) ) {
				factory.train(UnitType.Terran_Siege_Tank_Tank_Mode );
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

	// TODO: Write a function that assigns first 16 SCVs to CC1, next 16 to CC2
	// using getUnitsInRadius or make func above only build scvs if num SCVs near CC is <16
	
	public static boolean isAddOn( UnitType struct ) {
		if ( struct == UnitType.Terran_Machine_Shop || struct == UnitType.Terran_Comsat_Station
				|| struct == UnitType.Terran_Control_Tower ) {
			return true;
		}
		return false;
	}

	public static int updateProductionMode( Game game,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int productionMode ) {
		if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks) >= 1) {
			productionMode = 1;
		}
		// tanks
		if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, Factory) >= 1) {
			productionMode = 2;
		}
		
		return productionMode;
	}
	
	public static Unit getNearestBuildingToUnit( Game game, Multimap<UnitType, Integer> bStructMap, Unit unit, UnitType structType ) {
		int distance = -1;
		Unit closestStruct = null;
		for ( int structID : bStructMap.get(structType) ) {
			Unit struct = game.getUnit(structID);
			int structDistance = (int) BWTA.getGroundDistance(unit.getTilePosition(), struct.getTilePosition());
			if ( (distance == -1) || structDistance < distance ) {
				distance = structDistance;
				closestStruct = struct;
			}
		}
		
		return closestStruct;
	}
	 
}
