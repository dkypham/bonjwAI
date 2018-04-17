package b.structure;

import java.util.ArrayList;
import java.util.List;

import b.ai.BonjwAI;
import bwapi.Game;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class BuildingPlacement {
	
	private static BuildingPlacement buildingPlacement = new BuildingPlacement();
	private Game game;
	
	private BuildingPlacement() {
		game = BonjwAI.mirror.getGame();
	}
	
	public static BuildingPlacement getInstance() {
		return buildingPlacement;
	}
		
	// NAIVE BUILDING PLACEMENT IMPLEMENTATION
	public static TilePosition getBuildTile(Game game, Unit builder, UnitType buildingType, TilePosition aroundTile) {
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

		if (buildingType == UnitType.Terran_Factory) {
			while ((maxDist < stopDist) && (ret == null)) {
				for (int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++) {
					for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++) {
						if (game.canBuildHere(new TilePosition(i, j), buildingType, builder, false)
								&& game.canBuildHere(new TilePosition(i+2, j), buildingType, builder, false) 
								&& game.canBuildHere(new TilePosition(i-2, j), buildingType, builder, false) ) {
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
								return new TilePosition(i, j);
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

		while ((maxDist < stopDist) && (ret == null)) {
			for (int i = aroundTile.getX() - maxDist; i <= aroundTile.getX() + maxDist; i++) {
				for (int j = aroundTile.getY() - maxDist; j <= aroundTile.getY() + maxDist; j++) {
					if (game.canBuildHere(new TilePosition(i, j), buildingType, builder, false)
							&& game.canBuildHere(new TilePosition(i-2, j), buildingType, builder, false) ) {
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
							return new TilePosition(i, j);
						}
						// creep for Zerg
						if (buildingType.requiresCreep()) {
							boolean creepMissing = false;
							for (int k = i; k <= i + buildingType.tileWidth(); k++) {
								for (int l = j; l <= j + buildingType.tileHeight(); l++) {
									if (!game.hasCreep(k, l))
										creepMissing = true;
									break;
								}
							}
							if (creepMissing)
								continue;
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
	// END OF BUILDING IMPLEMENTATION
	
	public static TilePosition getBuildTileNew(Game game, Unit builder, UnitType buildingType) {
		return builder.getTilePosition();
	}
	
	// Building implementation for 1st supply depot
	public static TilePosition getBuildPositionFirstSD(Game game, //Unit builder, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup ) {
		//int startingCC_X = bBasePos.get(0).getX();
		//int startingCC_Y = bBasePos.get(0).getY();
		
		int startingCC_X = bBasePos.get(0).getTilePosition().getX();
		int startingCC_Y = bBasePos.get(0).getTilePosition().getY();
		
		TilePosition pos = bBasePos.get(0).getTilePosition();
		
		if ( mineralSetup == 12 ) { 
			game.drawTextMap( startingCC_X, startingCC_Y, "build here");
		}		
		if ( mineralSetup == 3 ) { 
			pos = new TilePosition( startingCC_X - 3, startingCC_Y + 1);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build here222");
			}

		}		
		if ( mineralSetup == 6 ) { 
			game.drawTextMap( startingCC_X, startingCC_Y, "build here");
		}	
		
		if ( mineralSetup == 9 ) { 
			pos = new TilePosition( startingCC_X + 4, startingCC_Y - 1);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build here3");
				System.out.println( pos.getX() + " " + pos.getY() );
			}

		}
		
		
		return pos;
		
		//return builder.getTilePosition();
	}
	
	
	
	
	
	
	
	
	
	
}