package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Color;
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
import economy.Base;
import economy.Resources;
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

	static final String uT_BuildSCV = "Builder";
	
	static final int SUPPLY_VALUE_SD = 16;
	static final int SUPPLY_VALUE_CC = 20;

	static final UnitType SD = UnitType.Terran_Supply_Depot;
	static final UnitType CC = UnitType.Terran_Command_Center;
	
	static final UnitType Refinery = UnitType.Terran_Refinery;
	static final UnitType Academy = UnitType.Terran_Academy;
	static final UnitType Barracks = UnitType.Terran_Barracks;
	static final UnitType Factory = UnitType.Terran_Factory;
	
	static final UnitType SCV = UnitType.Terran_SCV;
	static final UnitType Marine = UnitType.Terran_Marine;
	static final UnitType Medic = UnitType.Terran_Medic;
	static final UnitType TankTank = UnitType.Terran_Siege_Tank_Tank_Mode;
	
	public static boolean buildStruct( Game game, Player self, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			Resources bResources,
			UnitType struct,
			List<Pair<TilePosition,TilePosition>> noBuildZones,
			BuildOrder bBuildOrder
			) {
		// check if enough resources
		if ( !bResources.checkIfEnoughMinsAndGas(struct.mineralPrice(), struct.gasPrice()) ) {
			return false;
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
			return buildCC( game, self, bArmyMap, bRolesMap, bStructMap, bBasePos, bBuildOrder );
		}
		
		// general case
		if ( bResources.checkIfEnoughMinsAndGas(struct.mineralPrice(), struct.gasPrice() )) {
			bResources.addMinAndGas( struct.mineralPrice(), struct.gasPrice() );
			return WorkerManager.issueBuild(game, self, bArmyMap, bRolesMap, bStructMap, struct, noBuildZones);
		}
		return false;
	}
	
	public static boolean issueBuildAtLocation(Game game,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			TilePosition pos,
			UnitType building,
			BuildOrder bBuildOrder ) {
		Unit buildSCV = MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, uT_BuildSCV); 
		return buildSCV.build(building,bBuildOrder.getPlannedBuildLocation());
	}
	
	public static int updateBuildStructTime( Game game ) {
		return game.elapsedTime();
	}
	
	public static boolean buildCC( Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<BaseLocation> bBasePos,
			BuildOrder bBuildOrder) {
		int numCC = MapUnitID.getStructCount(game, bArmyMap, bStructMap, CC);
		return issueBuildAtLocation(game, bArmyMap, bRolesMap, bBuildOrder.getPlannedBuildLocation(), 
				CC, bBuildOrder );
	}
	
	// function to build marines
	public static boolean buildMarines(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			Resources bResources ) {
		//int marineCount = bArmyMap.get(UnitType.Terran_Marine).size();
		boolean needSupply = SupplyManager.needSupplyCheck(bResources);
		int reservedMinerals = bResources.getMinsActual() - bResources.getMinsEffective();
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
			Resources bResources ) {
		// check if enough resources
		if ( !bResources.checkIfEnoughMinsAndGas(TankTank.mineralPrice(), TankTank.gasPrice() ) ) {
			return false;
		}
		
		// TODO: add supply check
		if ( SupplyManager.needSupplyCheck(bResources) ) {
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
			Resources bResources,
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup,
			List<Pair<TilePosition,TilePosition>> noBuildZones,
			BuildOrder bBuildOrder ) {
		// get number of supply depots
		int numSupply = MapUnitID.getStructCount(game, bArmyMap, bStructMap, SD);

		// if conditions met to build a supply depot
		if ( SupplyManager.needSupplyCheck(bResources) && ( bResources.getMinsEffective() ) >= 100 ) {
			// first supply depot position
			if ( numSupply == 0 ) {
				// find pos of first SD
				TilePosition pos = MapMath.findPosFirstSD(game, MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC), mineralSetup);
				// issue build at TilePosition found
				issueBuildAtLocation(game, bArmyMap, bRolesMap, bBuildOrder.getPlannedBuildLocation(), 
						SD, bBuildOrder );
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

	public static void updateProductionMode( Game game,
			Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int[] productionMode ) {
		if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, Barracks) >= 1) {
			productionMode[0] = 1;
		}
		// tanks
		if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, Factory) >= 1) {
			productionMode[0] = 2;
		}
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
	 
	// NEW FUNCTIONS BELOW
	public static void buildingManagerWithBuildOrder( Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			int[] productionMode, Resources bResources,			
			List<Pair<TilePosition,TilePosition>> noBuildZones,
			ArrayList<BaseLocation> bBasePos,			
			List<Pair<Position,Position>> miningRegionsList,		
			ArrayList<Base> bBases,
			BuildOrder bBuildOrder ) {
	
		//  update bBuildOrder building placement
		if ( bBuildOrder.nextIsStruct() && bBuildOrder.nextBuildLocationInvalid() ) {
			bBuildOrder.updatePlannedBuildLocation(game, bStructMap, bBuildOrder.getNextStruct(), bBasePos, noBuildZones  );
			System.out.println("BuildingManager: Next build placement" + bBuildOrder.getPlannedBuildLocation() );
		}
		
		// supply met and no build issued
		if ( bBuildOrder.checkIfSupplyMet(bResources) && !bBuildOrder.checkIfBuildIssued() ) {
			// if next element is a struct
			if ( bBuildOrder.getBuildOrder().get(0).isStruct() ) { // is struct
				UnitType structType = bBuildOrder.getNextStruct();
				// struct is add on
				if ( structType.isAddon() ) {
					if ( bResources.enoughResourcesBuildUnit(structType) ) {
						for ( Integer buildsAddOnID : bStructMap.get(structType.whatBuilds().first)) {
							Unit buildsAddOnStruct = game.getUnit(buildsAddOnID);
							if ( buildsAddOnStruct.canBuildAddon(structType) ) {
								buildsAddOnStruct.buildAddon(structType);
								return;
							}
						}
					}
					return;
				}
				
				// if enough resources, try to build
				if ( bResources.enoughResourcesBuildUnit(structType) ) {
					// struct is building
					if ( issueBuildAtLocation(game, bArmyMap, bRolesMap, bBuildOrder.getPlannedBuildLocation(), 
							structType, bBuildOrder )) {
						// build command was issued
						bResources.addMinAndGas( structType.mineralPrice(), structType.gasPrice() );
						bBuildOrder.setIsBuildIssuedTrue();
						bBuildOrder.setTimeBuildIssued( game.elapsedTime() );	// get time build command is issued for error checking
						return;
					}
				}
				// if not enough resources check if 60 percent to move SCV
				else if ( bResources.checkIfSixtyPercent(structType) ) {
					// move build scv to position if not already
					Unit buildSCV = MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, uT_BuildSCV);
					if ( buildSCV.isGatheringMinerals() ) { // if still mining, make patrol
						System.out.println("Sending build SCV to patrol");
						Pair<TilePosition,TilePosition> buildArea = MapMath.findBuildingArea(
								bBuildOrder.getPlannedBuildLocation(), structType);
						// make patrol area tighter
						Position topLeft = buildArea.first.toPosition();
						Position botRight = buildArea.second.toPosition();
						int upDist = botRight.getY() - topLeft.getY();
						int sideDist = botRight.getX() - botRight.getX();
						topLeft = new Position((int)(topLeft.getX() + sideDist*.3),(int)(topLeft.getY() + upDist*.3));
						botRight = new Position((int)(botRight.getX() - sideDist*.3),(int)(botRight.getY() - upDist*.3));
						game.drawBoxMap(topLeft,botRight,Color.Black);
						buildSCV.move( topLeft );
						buildSCV.patrol( botRight, true);
					}
				}
				
				
			}	
			// if next element is a tech
			else if ( bBuildOrder.getBuildOrder().get(0).isTech() ) {
				TechType techType = bBuildOrder.getNextTech();
				if ( buildTech(game, bStructMap, bResources, bBuildOrder.getBuildOrder().get(0).getTT() )) {
					bBuildOrder.removeTopOfBuildOrder();
				}
			}
		}
		
		// generic case ( buildOrderStruct supply + tech reads 500 )
		
		// else build UNIT
		else {
			if ( bBuildOrder.checkIfBuildIssued() ) { // do not build unit if building is queued
				// check if time since set negative is > 20 seconds, then make it positive. 
				// Usually if this is the case, the SCV did not sucessfully build
				if ( bBuildOrder.tooMuchTimeBuildIssued(game.elapsedTime() )) {
					if ( bBuildOrder.getIsBuildIssued() ) {
						bBuildOrder.setIsBuildIssuedFalse(); // make it neg to pos		
					}
					else { 
						System.out.println("BuildingManager: More than 20 seconds since build command was issued, but "
								+ "buildOrderStruct has a positive supply value");
					}
				}
				return;
			}
;
			// issue build
			buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources, bBases);		
		}
	}
	
	public static void buildUnit( Game game, Player self, Multimap<UnitType, Integer> bArmyMap,
			Multimap<UnitType, Integer> bStructMap,
			int[] productionMode, Resources bResources,
			List<Base> bBases ) {
		if ( productionMode[0] == 0 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, bBases) ) {
				return;
			}	
		}
		if ( productionMode[0] == 1 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, bBases) ) {
				return;
			}
			if ( buildMarines(game, self, bArmyMap, bStructMap, bResources) ) {
				return;
			}
		}
		if ( productionMode[0] == 2 ) {
			if ( buildWorkers(game, self, bArmyMap, bStructMap, bResources, bBases) ) {
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
	
	public static boolean buildWorkers(Game game, Player self, Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap,
			Resources bResources,
			List<Base> bBases ) {
		// first check if it is possible: resources + supply
		if ( !bResources.enoughResourcesBuildUnit(SCV) ) {
			return false;
		}
		if ( SupplyManager.needSupplyCheck(bResources) ) {
			return false;
		}

		for ( Base bBase : bBases ) {
			if ( bBase.getNumMinMiners() < bBase.getMinMinMiners() ) { // not enough min miners
				Unit CC = bBase.getCC();
				if (CC.isTraining()) continue;
				if (CC.train(SCV)) {
					//CC.train(SCV);
					return true;	// tell buildingManager that we built a unit this frame
				}
			}
		}
		return false; // did not train SCV
	}
	
	public static boolean buildTech(Game game, Multimap<UnitType, Integer> bStructMap,
			Resources bResources, TechType tT) {
		// first check if enough resources
		if ( bResources.checkIfEnoughMinsAndGas(tT.mineralPrice(), tT.gasPrice()) ) {
			// get struct that researches the tech
			Unit struct = MapUnitID.getStruct(game,  bStructMap, tT.whatResearches());
			if ( struct == null ) {
				//System.out.println("TechManager: Could not find building to research: " + tT );
				return false;
			}
			if ( struct.canResearch( tT ) ) {
				return struct.research(tT); // return true if building starts researching tech
			}
		}	
		return false;
	}
	
	public static void buildingManagerWithBuildQueue( Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			int[] productionMode, Resources bResources,			
			List<Pair<TilePosition,TilePosition>> noBuildZones,
			ArrayList<BaseLocation> bBasePos,			
			List<Pair<Position,Position>> miningRegionsList,		
			ArrayList<Base> bBases,
			BuildQueue bBuildQueue ) {
	
		//  update bBuildOrder building placement
		if (  bBuildQueue.nextBuildLocationInvalid() ) {
			bBuildQueue.updatePlannedBuildLocation(game, bStructMap, bBuildQueue.getBuildQueue().get(0), bBasePos, noBuildZones  );
			System.out.println("BuildingManager: Next build placement" + bBuildQueue.getPlannedBuildLocation() );
		}
		
		// supply met and no build issued
		if ( !bBuildQueue.checkIfBuildIssued() ) {
			// if next element is a struct
			if ( bBuildQueue.getBuildQueue().get(0).isBuilding() ) { // is struct
				UnitType structType = bBuildQueue.getBuildQueue().get(0);
				// struct is add on
				if ( structType.isAddon() ) {
					if ( bResources.enoughResourcesBuildUnit(structType) ) {
						for ( Integer buildsAddOnID : bStructMap.get(structType.whatBuilds().first)) {
							Unit buildsAddOnStruct = game.getUnit(buildsAddOnID);
							if ( buildsAddOnStruct.canBuildAddon(structType) ) {
								buildsAddOnStruct.buildAddon(structType);
								return;
							}
						}
					}
					return;
				}
				
				// if enough resources, try to build
				if ( bResources.enoughResourcesBuildUnit(structType) ) {
					// struct is building
					if ( issueBuildAtLocation(game, bArmyMap, bRolesMap, bBuildQueue.getPlannedBuildLocation(), 
							structType, bBuildQueue )) {
						// build command was issued
						bResources.addMinAndGas( structType.mineralPrice(), structType.gasPrice() );
						bBuildQueue.setIsBuildIssuedTrue();
						bBuildQueue.setTimeBuildIssued( game.elapsedTime() );	// get time build command is issued for error checking
						return;
					}
				}
				// if not enough resources check if 60 percent to move SCV
				else if ( bResources.checkIfSixtyPercent(structType) ) {
					// move build scv to position if not already
					Unit buildSCV = MapUnitID.getFirstUnitFromRolesMap(game, bRolesMap, uT_BuildSCV);
					if ( buildSCV.isGatheringMinerals() ) { // if still mining, make patrol
						System.out.println("Sending build SCV to patrol");
						Pair<TilePosition,TilePosition> buildArea = MapMath.findBuildingArea(
								bBuildQueue.getPlannedBuildLocation(), structType);
						// make patrol area tighter
						Position topLeft = buildArea.first.toPosition();
						Position botRight = buildArea.second.toPosition();
						int upDist = botRight.getY() - topLeft.getY();
						int sideDist = botRight.getX() - botRight.getX();
						topLeft = new Position((int)(topLeft.getX() + sideDist*.3),(int)(topLeft.getY() + upDist*.3));
						botRight = new Position((int)(botRight.getX() - sideDist*.3),(int)(botRight.getY() - upDist*.3));
						game.drawBoxMap(topLeft,botRight,Color.Black);
						buildSCV.move( topLeft );
						buildSCV.patrol( botRight, true);
					}
				}
				
				
			}	
			// if next element is a tech
			/*
			else if ( bBuildQueue.getBuildOrder().get(0).isTech() ) {
				TechType techType = bBuildQueue.getNextTech();
				if ( buildTech(game, bStructMap, bResources, bBuildQueue.getBuildOrder().get(0).getTT() )) {
					bBuildQueue.removeTopOfBuildOrder();
				}
			}
			*/
		}
		
		// generic case ( buildOrderStruct supply + tech reads 500 )
		
		// else build UNIT
		else {
			if ( bBuildQueue.checkIfBuildIssued() ) { // do not build unit if building is queued
				// check if time since set negative is > 20 seconds, then make it positive. 
				// Usually if this is the case, the SCV did not sucessfully build
				if ( bBuildQueue.tooMuchTimeBuildIssued(game.elapsedTime() )) {
					if ( bBuildQueue.getIsBuildIssued() ) {
						bBuildQueue.setIsBuildIssuedFalse(); // make it neg to pos		
					}
					else { 
						System.out.println("BuildingManager: More than 20 seconds since build command was issued, but "
								+ "buildOrderStruct has a positive supply value");
					}
				}
				return;
			}
;
			// issue build
			buildUnit(game,self,bArmyMap,bStructMap,productionMode, bResources, bBases);		
		}
	}
}
