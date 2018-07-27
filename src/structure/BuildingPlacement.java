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
	public static TilePosition getBuildTile(Game game, UnitType buildingType, TilePosition aroundTile,
			List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		TilePosition ret = null;
		int maxDist = 3;
		int stopDist = 40;

		// Refinery
		if (buildingType.isRefinery()) {
			for (Unit n : game.neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser)
						&& (Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist)
						&& (Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist))
					return n.getTilePosition();
			}
		}

		// if is addon, don't worry abbout it
		else if ( buildingType.isAddon() ) {
			return new TilePosition(-3,-3);
		}
		
		// if building type can build add on (not CC), make sure right two spaces are open
		else if (buildingType.canBuildAddon() && buildingType != CC ) {
			while ((maxDist < stopDist) && (ret == null)) {
				for (int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++) {
					for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++) {
						// if you can build here and two to the right (if there is space for the add on)
						if (game.canBuildHere(new TilePosition(i, j), buildingType)
								&& game.canBuildHere(new TilePosition(i+2, j), buildingType) ) {
							// units that are blocking the tile
							boolean unitsInWay = false;
							for (Unit u : game.getAllUnits()) {
								//if (u.getID() == builder.getID())
								//	continue;
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
					if (game.canBuildHere(new TilePosition(i, j), buildingType) ) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : game.getAllUnits()) {
							//if (u.getID() == builder.getID())
							//	continue;
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
	
	public static TilePosition getBuildTileCC(Game game, Multimap<UnitType, Integer> bStructMap, 
			ArrayList<BaseLocation> bBasePos, UnitType buildingType ) {
		int numCC = bStructMap.get(CC).size();
		return bBasePos.get(numCC).getTilePosition();
		
	}
	
	public static TilePosition getBuildTileSD(Game game, Multimap<UnitType, Integer> bStructMap, 
			UnitType buildingType, List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		Unit firstCC = MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC);
		int numSD = bStructMap.get(SD).size();
		if ( numSD == 0 ) {
			int mineralSetup = MapInformation.findMineralSetup(game, firstCC );
			return MapMath.findPosFirstSD(game, firstCC, mineralSetup);
		}
		return getBuildTile(game, SD, firstCC.getTilePosition(), noBuildZones);
		
	}
	
	public static TilePosition getBuildTileBarracks(Game game, Multimap<UnitType, Integer> bStructMap, 
			UnitType buildingType, List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		Unit firstCC = MapUnitID.getFirstUnitFromUnitMap(game,bStructMap,CC);
		int numBarracks = bStructMap.get(Barracks).size();
		if ( numBarracks == 0 ) {
			int mineralSetup = MapInformation.findMineralSetup(game, firstCC );
			return MapMath.findPosFirstBarracks(game, firstCC, mineralSetup);
		}
		return getBuildTile(game, Barracks, firstCC.getTilePosition(), noBuildZones);
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
	
	public static TilePosition getPlannedBuildLocation(Game game,
			Multimap<UnitType, Integer> bStructMap, UnitType structType,
			ArrayList<BaseLocation> bBasePos,
			List<Pair<TilePosition,TilePosition>> noBuildZones ) {
		TilePosition buildPos = null;
		
		// SD
		if ( structType == SD ) {
			return getBuildTileSD(game, bStructMap, SD, noBuildZones);
		}
		
		// Barracks
		if ( structType == Barracks ) {
			return getBuildTileBarracks(game, bStructMap, Barracks, noBuildZones);
		}		
		
		// CC
		if ( structType == CC ) {
			return getBuildTileCC(game, bStructMap, bBasePos, CC);
		}
		
		// else
		Unit startingCC = MapUnitID.getFirstUnitFromUnitMap(game,  bStructMap,  CC);
		return getBuildTile(game, structType, startingCC.getTilePosition(), noBuildZones);
		
	}
	
}