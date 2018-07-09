package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import economy.ResourceManager;
import economy.SupplyManager;
import economy.WorkerManager;
import idmap.MapUnitID;
import math.MapMath;

/**
 * Building Manager, on each call
 *   - Check if struct in buildOrder needs to be built
 *   - If not, then check if tech in techTree needs to be built
 *   - If not, then check if reached end of buildOrder, then check if supply needs to be built
 *   - If not, then build a unit
 */
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
	
	/**
	 * Check if building needs to be built by checking buildOrderStruct and comparing supply.
	 * 
	 * 
	 * 
	 * @param game
	 * @param self
	 * @param bBasePos
	 * @param bArmyMap
	 * @param bRolesMap
	 * @param bStructMap
	 * @param productionMode
	 * @param bResources
	 * @param buildOrderStruct
	 * @param buildOrderTech
	 * @param mineralSetup
	 * @param timeBuildIssued
	 * @param miningRegionsList
	 * @param noBuildZones
	 */
	public static void buildingManagerWithBuildOrder( Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			int productionMode, ArrayList<Integer> bResources,
			
			List<Pair<TilePosition,TilePosition>> noBuildZones,
			//List<Pair<Position,Position>> miningRegionsList,
			
			ArrayList<BaseLocation> bBasePos,
			List<Pair<UnitType,Integer>> buildOrderStruct,
			List<Pair<TechType,Integer>> buildOrderTech,
			
			int mineralSetup,
			int[] timeBuildIssued,
			
			List<Pair<Position,Position>> miningRegionsList) {
		
		// check if something needs to be built at this supply
		if ( ResourceManager.getSupplyUsed(bResources) == buildOrderStruct.get(0).second*2 ) {
			// issue build
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bRolesMap, bStructMap, bResources, 
					buildOrderStruct.get(0).first, 
					productionMode,
					noBuildZones) ) {
				buildOrderStruct.set(0, new Pair<UnitType,Integer>( buildOrderStruct.get(0).first, 
						buildOrderStruct.get(0).second*-1) );
				//System.out.println(buildOrderStruct.get(0));
				timeBuildIssued[0] = game.elapsedTime();
			}
		}
		
		// if set only to >= then multiple SCVs go to build
		else if ( self.supplyUsed() >= buildOrderStruct.get(0).second*2 
				&& buildOrderStruct.get(0).second > 0 ) {
			// issue build
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bRolesMap, bStructMap, bResources, 
					buildOrderStruct.get(0).first, 
					productionMode,
					noBuildZones) ) {
				buildOrderStruct.set(0, new Pair<UnitType,Integer>( buildOrderStruct.get(0).first, 
						buildOrderStruct.get(0).second*-1) );
				timeBuildIssued[0] = game.elapsedTime();
			}
		}
		
		// check if tech needs to be built
		else if ( self.supplyUsed() >= buildOrderTech.get(0).second*2 ) {
			// issue build
			if ( TechManager.buildTech(game, self, bStructMap, bResources, 
					buildOrderTech.get(0).first ) ) {
				buildOrderTech.remove(0);
				return;
			}
			
			// need to build tech but did not, so return
			return;
		}
		
		// check if supply needed, then build supply depot
		else if ( buildOrderStruct.get(0).second == 500 && SupplyManager.needSupplyCheck(self, bResources.get(5)) ) {
			if ( buildStruct(game, self, bBasePos, mineralSetup, 
					bArmyMap, bRolesMap, bStructMap, bResources, 
					SD, 
					productionMode,
					noBuildZones) ) {
				return;
			}
		}
		
		// if not, issue build of ONE unit
		else {
			if ( buildOrderStruct.get(0).second < 0 ) {
				// do not build unit if building is queued
				
				// check if time since set negative is > 20 seconds, then make
				// it positive. Usually if this is the case, the SCV
				// did not actually build
				if ( game.elapsedTime() - timeBuildIssued[0] > 20 ) {
					buildOrderStruct.set(0, new Pair<UnitType,Integer>( buildOrderStruct.get(0).first, 
							buildOrderStruct.get(0).second*-1) );
				}
				return;
			}
			// issue build
			if ( productionMode == 0 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources, miningRegionsList);
			}
			else if ( productionMode == 1 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources, miningRegionsList);	
			}
			else if ( productionMode == 2 ) {
				buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources, miningRegionsList);	
			}
			
		}
	}
	
	/**
	 * Issue command to build structure
	 * 
	 * @param game
	 * @param self
	 * @param bBasePos
	 * @param mineralSetup
	 * @param bArmyMap
	 * @param bStructMap
	 * @param bResources
	 * @param struct
	 * @param productionMode
	 * @param noBuildZones
	 * @return
	 */
	public static boolean buildStruct( Game game, Player self, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources,
			UnitType struct,
			int productionMode,
			List<Pair<TilePosition,TilePosition>> noBuildZones
			) {
		// check if enough resources
		if ( !ResourceManager.checkIfEnoughResources(bResources, struct) ) {
			return false;
		}
		
		// if building type is SD
		if ( struct == SD ) {
			// first SD, use specific location
			if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, struct) == 0 ) {
				TilePosition pos = BuildingPlacement.getBuildPositionSD(game, bArmyMap, bRolesMap, bStructMap, 
						bBasePos, mineralSetup, noBuildZones);
				// if issue build command passes, update resource manager and exit
				if ( WorkerManager.issueBuildAtLocation(game, bArmyMap, bRolesMap, pos, SD) ) {
					// update bResources
					ResourceManager.addBuildingCost( bResources, struct );
					return true;
				}
			}
			else {
				ResourceManager.addBuildingCost( bResources, struct );
				return WorkerManager.issueBuild(game, self, bArmyMap, bRolesMap, bStructMap, struct, noBuildZones);				
			}
		}
		// if building type is Barracks
		if ( struct == Barracks ) {
			TilePosition pos = MapMath.findPosFirstBarracks(game, bBasePos.get(0), mineralSetup);
			if ( WorkerManager.issueBuildAtLocation(game, bArmyMap, bRolesMap, pos, Barracks) ) {
				ResourceManager.addBuildingCost( bResources, struct );

				return true;
			}
		}
		
		// if building type is an addon
		if ( struct == UnitType.Terran_Machine_Shop ) {
			for ( Integer factoryID : bStructMap.get(UnitType.Terran_Factory ) ) {
				Unit factory = game.getUnit(factoryID);
				if ( factory.canBuildAddon() && factory.isCompleted() ) {
					return factory.buildAddon( struct );
				}
			}
		}
		
		// if building type is a CC
		if ( struct == CC ) {
			return buildCC( game, self, bArmyMap, bRolesMap, bStructMap, bBasePos );
		}
		
		// general case
		if ( ResourceManager.checkIfEnoughResources(bResources, struct) ) {
			ResourceManager.addBuildingCost( bResources, struct );
			return WorkerManager.issueBuild(game, self, bArmyMap, bRolesMap, bStructMap, struct, noBuildZones);
		}
		return false;
	}
	
	public static int updateBuildStructTime( Game game ) {
		return game.elapsedTime();
	}
	
	public static void buildUnit( Game game, Player self, 			
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int productionMode, ArrayList<Integer> bResources,
			List<Pair<Position,Position>> miningRegionsList ) {
		if ( productionMode == 0 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, miningRegionsList) ) {
				return;
			}	
		}
		if ( productionMode == 1 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, miningRegionsList) ) {
				return;
			}
			if ( buildMarines(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
		}
		if ( productionMode == 2 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, miningRegionsList) ) {
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
		drawStructPos.add( (MapMath.findPosFirstSD(game, bBasePos.get(0), mineralSetup)).toPosition());
		drawStructLabel.add("First Supply Depot");
		drawStructPos.add( (MapMath.findPosFirstBarracks(game, bBasePos.get(0), mineralSetup)).toPosition());
		drawStructLabel.add("First Barracks");
	}

	public static boolean buildCC( Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<BaseLocation> bBasePos ) {
		int numCC = MapUnitID.getStructCount(game, bArmyMap, bStructMap, CC);
		return WorkerManager.issueBuildAtLocation(game, bArmyMap, bRolesMap, bBasePos.get(numCC).getTilePosition(), CC);
	}
	
	// function to build workers
	public static boolean buildWorkers(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources,
			List<Pair<Position,Position>> miningRegionsList ) {
		/*
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
		*/
		
		//iterate through each base
		// assumes CC array is in order of starting base->expo->2nd expo
		int baseNum = 0;
		boolean needSupply = SupplyManager.needSupplyCheck(self, bResources.get(5));
		int reservedMinerals = bResources.get(1);	
		for ( Integer CCID : bStructMap.get(UnitType.Terran_Command_Center ) ) {
			Unit CCUnit = game.getUnit(CCID);
			int mineralMiners = WorkerManager.getNumWorkersInBase(game, self, miningRegionsList.get(baseNum), bArmyMap).first;
			
			//System.out.println("CC ID: " + CCID + " has " + mineralMiners + " workers");
			//System.out.println("need supply: " + needSupply);
			if ( CCUnit.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false && mineralMiners < 20
					&& CCUnit.canTrain( UnitType.Terran_SCV ) ) {
				//System.out.println("trying to build scv from CC " + CCID);
				CCUnit.train(UnitType.Terran_SCV);		
				return true;
			}
			//System.out.println("Number of SCVs in base " + baseNum + ": " + mineralMiners );
			baseNum++;
		}
		return false;
	}
	
	// function to build marines
	public static boolean buildMarines(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources ) {
		//int marineCount = bArmyMap.get(UnitType.Terran_Marine).size();
		boolean needSupply = SupplyManager.needSupplyCheck(self, bResources.get(5));
		int reservedMinerals = bResources.get(1);	
		for ( Integer barracksID : bStructMap.get(UnitType.Terran_Barracks) ) {
			Unit barracks = game.getUnit(barracksID);
			if (barracks.isTraining() == false && (self.minerals()-reservedMinerals) >= 50 && needSupply == false 
					&& barracks.canTrain( UnitType.Terran_Marine )
					//&& marineCount < (16 * MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks) ) 
					) {
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
			Multimap<String, Integer> bRolesMap,
			ArrayList<Integer> bResources,
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			List<Pair<TilePosition,TilePosition>> noBuildZones) {
		// get number of supply depots
		int numSupply = MapUnitID.getStructCount(game, bArmyMap, bStructMap, SD);

		// if conditions met to build a supply depot
		if ( SupplyManager.needSupplyCheck(self,bResources.get(5)) && (bResources.get(0)-bResources.get(1)) >= 100 ) {
			// first supply depot position
			if ( numSupply == 0 ) {
				// find pos of first SD
				TilePosition pos = MapMath.findPosFirstSD(game, bBasePos.get(0), mineralSetup);
				// issue build at TilePosition found
				WorkerManager.issueBuildAtLocation(game, bArmyMap, bRolesMap, pos, SD);
			}
			else {
				// default build alg
				WorkerManager.issueBuild(game, self, bArmyMap, bRolesMap, bStructMap, SD, noBuildZones);
			}
		}
	}
	
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
