package math;

import bwapi.Game;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwta.BaseLocation;

public class MapMath {

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	static UnitType Barracks = UnitType.Terran_Barracks;
	
	// Building implementation for 1st supply depot
	public static TilePosition findPosFirstSD(Game game, BaseLocation startingBL, int mineralSetup ) {
		// relative to startingCC location
		int startingCC_X = startingBL.getTilePosition().getX();
		int startingCC_Y = startingBL.getTilePosition().getY();
			
		// default pos
		TilePosition pos = startingBL.getTilePosition();
		
		// minerals above CC
		if ( mineralSetup == 12 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y + 3);
			pos = game.getBuildLocation(SD,pos, 1);		
			if ( !game.canBuildHere(pos, SD)) {
				System.out.println( "MapMath: cannot build first SD at: " + pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals right of CC
		if ( mineralSetup == 3 ) { 
			pos = new TilePosition( startingCC_X - 3, startingCC_Y + 1);
			pos = game.getBuildLocation(SD,pos, 1);		
			if ( !game.canBuildHere(pos, SD)) {
				System.out.println( "MapMath: cannot build first SD at: " + pos.getX() + " " + pos.getY() );
			}
		}		
		// minerals bottom of CC
		if ( mineralSetup == 6 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y - 3);
			pos = game.getBuildLocation(SD,pos, 1);		
			if ( !game.canBuildHere(pos, SD)) {
				System.out.println( "MapMath: cannot build first SD at: " + pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals left of CC
		if ( mineralSetup == 9 ) { 
			pos = new TilePosition( startingCC_X + 4, startingCC_Y - 1);
			pos = game.getBuildLocation(SD,pos, 1);		
			if ( !game.canBuildHere(pos, SD)) {
				System.out.println( "MapMath: cannot build first SD at: " + pos.getX() + " " + pos.getY() );
			}
		}
		if ( pos == null ) {
			System.out.println("MapMath: Null value finding first supply depot position");
		}
		return pos;	
	}
		
	// Building implementation for 1st supply depot
	public static TilePosition findPosFirstBarracks(Game game,  BaseLocation startingBL, int mineralSetup ) {
		// relative to startingCC location
		int startingCC_X = startingBL.getTilePosition().getX();
		int startingCC_Y = startingBL.getTilePosition().getY();
		
		// default pos
		TilePosition pos = startingBL.getTilePosition();
		
		// minerals above CC
		if ( mineralSetup == 12 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y + 3);
			pos = game.getBuildLocation(Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, Barracks)) {
				System.out.println( "MapMath: cannot build first Barracks at: " + pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals right of CC
		if ( mineralSetup == 3 ) { 
			pos = new TilePosition( startingCC_X - 4, startingCC_Y + 3);
			pos = game.getBuildLocation(Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, Barracks)) {
				System.out.println( "MapMath: cannot build first Barracks at: " + pos.getX() + " " + pos.getY() );
			}
		}		
		// minerals bottom of CC
		if ( mineralSetup == 6 ) { 
			pos = new TilePosition( startingCC_X, startingCC_Y - 3);
			pos = game.getBuildLocation(Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, Barracks)) {
				System.out.println( "MapMath: cannot build first Barracks at: " + pos.getX() + " " + pos.getY() );
			}
		}	
		// minerals left of CC
		if ( mineralSetup == 9 ) { 
			pos = new TilePosition( startingCC_X + 7, startingCC_Y - 1);
			pos = game.getBuildLocation(Barracks,pos, 1);		
			if ( !game.canBuildHere(pos, Barracks)) {
				System.out.println( "MapMath: cannot build first Barracks at: " + pos.getX() + " " + pos.getY() );
			}
		}
		return pos;
	}
	
	
}
