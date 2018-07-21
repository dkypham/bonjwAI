package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.WorkerManager;
import idmap.MapUnitID;
import map.MapInformation;
import math.MapMath;

public class BuildingPlacement {
	
	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	static UnitType Barracks = UnitType.Terran_Barracks;
	
	// NAIVE BUILDING PLACEMENT IMPLEMENTATION
	public static TilePosition getBuildTile(Game game, Unit builder, UnitType buildingType, TilePosition aroundTile,
			List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		TilePosition ret = null;
		int maxDist = 3;
		int stopDist = 40;

		// Refinery, Assimilator, Extractor
		if (buildingType.isRefinery()) {
			for (Unit n : game.neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser)
						&& (Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist)
						&& (Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist))
					return n.getTilePosition();
			}
		}

		// if building type is a factory, make sure right two spaces are open
		if (buildingType == UnitType.Terran_Factory || buildingType == UnitType.Terran_Command_Center || buildingType == UnitType.Terran_Starport) {
			while ((maxDist < stopDist) && (ret == null)) {
				for (int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++) {
					for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++) {
						// if you can build here and two to the right (if there is space for the add on)
						if (game.canBuildHere(new TilePosition(i, j), buildingType, builder, false)
								&& game.canBuildHere(new TilePosition(i+2, j), buildingType, builder, false) ) {
							// units that are blocking the tile
							boolean unitsInWay = false;
							for (Unit u : game.getAllUnits()) {
								if (u.getID() == builder.getID())
									continue;
								if ((Math.abs(u.getTilePosition().getX() - i) < 4)
										&& (Math.abs(u.getTilePosition().getY() - j) < 4))
									unitsInWay = true;
							}
							if (!unitsInWay) {
								if ( !isInNoBuildZone( new TilePosition(i,j), buildingType, noBuildZones ) ) {
									return new TilePosition(i, j);
								}
							}
						}
					}
				}
				maxDist += 2;
			}

			if (ret == null)
				game.printf("Unable to find suitable build position for " + buildingType.toString());
			return ret;
		}
		 
		// if building doesn't require addon
		// maxDist = (iterative) how far we are searching, stopDist = (set) max dist we will searched
		// ret = return tilePosition
		while ((maxDist < stopDist) && (ret == null)) {
			// search around the tile in the argument
			for (int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++) {
				for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++) {
					// if you can build here
					if (game.canBuildHere(new TilePosition(i, j), buildingType, builder, false) ) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : game.getAllUnits()) {
							if (u.getID() == builder.getID())
								continue;
							if ((Math.abs(u.getTilePosition().getX() - i) < 4)
									&& (Math.abs(u.getTilePosition().getY() - j) < 4))
								unitsInWay = true;
						}
						if (!unitsInWay) {
							// final check, if in any of the noBuildZones
							if ( !isInNoBuildZone( new TilePosition(i,j), buildingType, noBuildZones ) ) {
								return new TilePosition(i, j);
							}
						}
					}
				}
			}
			// iterate maxDist
			maxDist += 2;
		}

		if (ret == null)
			game.printf("Unable to find suitable build position for " + buildingType.toString());
		return ret;
	}
	// END OF NAIVE BUILDING IMPLEMENTATION
	
	public static TilePosition getBuildTileNew(Game game, Unit builder, UnitType buildingType) {
		return builder.getTilePosition();
	}
	
	public static TilePosition getBuildPositionSD(Game game, Multimap<UnitType, Integer> bArmyMap,
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			ArrayList<BaseLocation> bBasePos, int mineralSetup,
			List<Pair<TilePosition,TilePosition>> noBuildZones ) {

		// getBuildPositionFirstSD
		if ( MapUnitID.getStructCount(game, bArmyMap, bStructMap, SD) == 0 ) {
			return MapMath.findPosFirstSD(game, MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC), mineralSetup);	
		}
		// else
		// naive build
		
		Unit SCV = 	game.getUnit(WorkerManager.getFreeSCVID(game,bArmyMap, bRolesMap));
		
		return getBuildTile(game, SCV, SD, bBasePos.get(0).getTilePosition(), noBuildZones );
	}	
	
	// return true if IS overlapping/is in a build zone
	public static boolean isInNoBuildZone( TilePosition buildTile, UnitType building, List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		TilePosition btTL = new TilePosition( buildTile.getX(), buildTile.getY() );
		TilePosition btBR = new TilePosition( buildTile.getX() + building.tileWidth(), buildTile.getY() + building.tileHeight());
		
		//System.out.println("Region to check: TL = " + btTL + ", BR = " + btBR);
		for ( Pair<TilePosition,TilePosition> potZone : noBuildZones ) {	
			// check if in bounds
			TilePosition potZoneTL = potZone.first;
			TilePosition potZoneBR = potZone.second;
			
			//System.out.print("test region is: TL = " + potZoneTL + ", BR = " + potZoneBR + " is: ");
			if ( btTL.getX() < potZoneBR.getX() 	// A.X1 < B.X2
					&& btBR.getX() > potZoneTL.getX()	// A.X2 > B.X1
					&& btTL.getY() < potZoneBR.getY()	// A.Y1 < B.Y2
					&& btBR.getY() > potZoneTL.getY() ) {	// A.Y2 > B.Y1
				//System.out.println("NOT VALID");
				return true;
			}
			//else {
				//System.out.println("valid");
			//}
			
		}
		return false;
	}
	
	public static void addToNoBuildZone( List<Pair<TilePosition,TilePosition>> noBuildZones, Unit building ) {
		UnitType uT = building.getType();
		
		if ( uT == UnitType.Terran_Comsat_Station || uT == UnitType.Terran_Machine_Shop || uT == UnitType.Terran_Control_Tower ) {
			return;
		}
		
		if ( uT == UnitType.Terran_Command_Center || uT == UnitType.Terran_Factory ) {
			addAddOnToNoBuildZone( noBuildZones, building);
		}
		
		TilePosition topLeft = building.getTilePosition();
		TilePosition botRight = new TilePosition( building.getTilePosition().getX() + (uT.width()/32) + 1,building.getTilePosition().getY() + (uT.height()/32) + 1 );
		noBuildZones.add( new Pair<TilePosition,TilePosition>( topLeft, botRight ));
	}
	
	public static void addAddOnToNoBuildZone( List<Pair<TilePosition,TilePosition>> noBuildZones, Unit building ) {
		TilePosition topLeft = new TilePosition( building.getTilePosition().getX() + 4, building.getTilePosition().getY() + 1 );
		TilePosition botRight = new TilePosition( topLeft.getX() + 2, topLeft.getY() + 2 );
		noBuildZones.add( new Pair<TilePosition,TilePosition>( topLeft, botRight ));
	}
	
	// IMPROVED METHODS BELOW
	public static TilePosition findBuildLocation( Multimap<UnitType, Integer> bStructMap,
			ArrayList<BaseLocation> bBasePos,
			List<Pair<TilePosition,TilePosition>> noBuildZones, UnitType struct) {
		// condition for SD
		if ( struct == SD ) {
			if ( bStructMap.get(SD).size() == 0 ) {
				// method for first depot position, ignore noBuildZone
			}
			else {
				// generic alg, use noBuildZone
			}
		}
		
		// condition for Barracks
		else if ( struct == Barracks ) {
			if ( bStructMap.get(Barracks).size() == 0 ) {
				// method for first depot position, ignore noBuildZone
			}
			else {
				// generic alg, use noBuildZone
			}
		}
		
		// conditions for expos
		else if ( struct == CC ) {
			// method for expanding, ignore noBuildZone
		}
		
		else if ( struct.isAddon() ) {
			// method for building addons
		}
		
		else { // catch all
			
		}
		
		return null;
	}

	public static TilePosition getPlannedBuildLocation(Game game, Multimap<UnitType, Integer> bStructMap, 
			UnitType structType) {
		TilePosition buildPos = null;
		
		// 1 SD
		if ( bStructMap.get(SD).size() == 0 ) {
			Unit firstCC = MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC);
			int mineralSetup = MapInformation.findMineralSetup(game, firstCC );
			return MapMath.findPosFirstSD(game, firstCC, mineralSetup);
		}
		
		// 1 Barracks
		if ( bStructMap.get(Barracks).size() == 0 ) {
			Unit firstCC = MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC);
			int mineralSetup = MapInformation.findMineralSetup(game, firstCC );
			return MapMath.findPosFirstBarracks(game, firstCC, mineralSetup);
		}		
		
		// Reg SD
		
		
		// Reg Barracks
		
		
		return buildPos;
	}
	
}