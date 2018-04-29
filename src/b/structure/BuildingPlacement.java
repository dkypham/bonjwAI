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
		// relative to startingCC location
		int startingCC_X = bBasePos.get(0).getTilePosition().getX();
		int startingCC_Y = bBasePos.get(0).getTilePosition().getY();
		
		// default pos
		TilePosition pos = bBasePos.get(0).getTilePosition();
		
		// minerals above CC
		if ( mineralSetup == 12 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y + 3);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build first supply depot here");
				//System.out.println( pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals right of CC
		if ( mineralSetup == 3 ) { 
			pos = new TilePosition( startingCC_X - 3, startingCC_Y + 1);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build first supply depot here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
			//System.out.println("BuildingPlacement: SD position at" + pos);
		}		
		// minerals bottom of CC
		if ( mineralSetup == 6 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y - 3);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build first supply depot here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals left of CC
		if ( mineralSetup == 9 ) { 
			pos = new TilePosition( startingCC_X + 4, startingCC_Y - 1);
			pos = game.getBuildLocation(UnitType.Terran_Supply_Depot,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Supply_Depot)) {
				System.out.println("Cannot build first supply depot here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
			//System.out.println("BuildingPlacement: SD position at" + pos);
		}
		
		if ( pos == null ) {
			System.out.println("Null value with first supply depot position");
		}
		
		return pos;
	}
	
	// Building implementation for 1st supply depot
	public static TilePosition getBuildPositionFirstBarracks(Game game, //Unit builder, 
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup ) {
		// relative to startingCC location
		int startingCC_X = bBasePos.get(0).getTilePosition().getX();
		int startingCC_Y = bBasePos.get(0).getTilePosition().getY();
		
		// default pos
		TilePosition pos = bBasePos.get(0).getTilePosition();
		
		// minerals above CC
		if ( mineralSetup == 12 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y + 3);
			pos = game.getBuildLocation(UnitType.Terran_Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Barracks)) {
				System.out.println("Cannot build first barracks here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals right of CC
		if ( mineralSetup == 3 ) { 
			pos = new TilePosition( startingCC_X - 4, startingCC_Y + 3);
			pos = game.getBuildLocation(UnitType.Terran_Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Barracks)) {
				System.out.println("Cannot build first barracks here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
			//System.out.println("BuildingPlacement: barracks position at" + pos);
		}		
		// minerals bottom of CC
		if ( mineralSetup == 6 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y - 3);
			pos = game.getBuildLocation(UnitType.Terran_Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Barracks)) {
				System.out.println("Cannot build first barracks here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals left of CC
		if ( mineralSetup == 9 ) { 
			pos = new TilePosition( startingCC_X + 7, startingCC_Y - 1);
			pos = game.getBuildLocation(UnitType.Terran_Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, UnitType.Terran_Barracks)) {
				System.out.println("Cannot build first barracks here");
				System.out.println( pos.getX() + " " + pos.getY() );
			}
			//System.out.println("BuildingPlacement: barracks position at" + pos);
		}
		
		if ( pos == null ) {
			System.out.println("Null value with first barracks position");
		}
		return pos;
	}
	
}