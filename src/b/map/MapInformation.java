package b.map;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class MapInformation {
	
	public static int initMapInfo(Game game, Multimap<UnitType, Integer> bStructMap,
			List<Integer> resourceZone, 
			//List<Integer> buildZone,
			ArrayList<BaseLocation> bBasePos,
			int mineralSetup ) {
		initResourceZone(game, resourceZone, bBasePos);
		//initBuildZone(game, resourceZone, bStructMap);
		findNearBasePos(game, bBasePos);
		return findMineralSetup(game, bBasePos, mineralSetup);
	}
	
	// Populate bBases based on base visibility
	// Finds starting space, expansion 1, expansion 2
	public static void findNearBasePos(Game game, ArrayList<BaseLocation> baseList) {
		for ( BaseLocation b : BWTA.getStartLocations() ) {
			if ( isExplored(game,b.getPoint()) ) {
				baseList.add(b);
			}
		}
		// get second index
		baseList.add(getClosestUniqueBase(baseList));
    	// get third index
		baseList.add(getClosestUniqueBase(baseList)); 	
	}
	
	// checks if a Position is explored
	public static boolean isExplored(Game game, Position position) {
		return game.isExplored(position.toTilePosition());
	}
	
	// checks if 
	public static BaseLocation getClosestUniqueBase( List<BaseLocation> myBases ) {
		double distance = -1;
		double tempDistance = -1;
		BaseLocation potentialBase = myBases.get(0);
		
		for ( BaseLocation b : BWTA.getBaseLocations() ) {
			if ( !(myBases.contains(b)) ) {
				tempDistance = BWTA.getGroundDistance(myBases.get(0).getTilePosition(), 
						b.getTilePosition());
				if ( distance == -1 || tempDistance < distance ) {
					potentialBase = b;
					distance = tempDistance;
				}
			}
		}
		return potentialBase;
	}
	
	// Define 4 setups
	// 12 -> minerals above main
	// 3 -> minerals right of main
	// 6 -> minerals below main
	// 9 -> minerals left of main
	public static int findMineralSetup( Game game, List<BaseLocation> bBasePos,
			int mineralSetup ) {
		int startingCC_X = bBasePos.get(0).getX();
		int startingCC_Y = bBasePos.get(0).getY();
		
		boolean allAbove = true;
		boolean allRight = true;
		boolean allBelow = true;
		boolean allLeft = true;
		
		for ( Unit nUnit : game.neutral().getUnits() ) {
			if ( (nUnit.getType().isMineralField() && nUnit.isVisible() )) {
				if ( nUnit.getX() < startingCC_X ) {
					allRight = false;
				}
				if ( nUnit.getX() > startingCC_X ) {
					allLeft = false;
				}
				if ( nUnit.getY() < startingCC_Y ) {
					allAbove = false;
				}
				if ( nUnit.getY() > startingCC_Y ) {
					allBelow = false;
				}
				
			}
		}
		if ( allAbove == true ) {
			return mineralSetup = 12;
		}
		if ( allRight == true ) {
			return mineralSetup = 3;
		}
		if ( allBelow == true ) {
			return mineralSetup = 6;
		}
		if ( allLeft == true ) {
			return mineralSetup = 9;
		}
		
		System.out.println(mineralSetup);
		
		// error
		return -2;
	}
	
	public static void initResourceZone(Game game, List<Integer> resourceZone,
			List<BaseLocation> bBasePos) {
		int lx = -1, mx = -1, ly = -1, my = -1;
		
		// iterate over mineral fields and gas geysers
		for ( Unit nUnit : game.neutral().getUnits() ) {
			if ( (nUnit.getType().isMineralField() || nUnit.getType().isRefinery())
					&& nUnit.isVisible() ) {
				if ( nUnit.getX() < lx || lx == -1 ) {
					lx = nUnit.getX();
				}
				if ( nUnit.getX() > mx || mx == -1 ) {
					mx = nUnit.getX();
				}
				if ( nUnit.getY() < ly || ly == -1 ) {
					ly = nUnit.getY();
				}
				if ( nUnit.getY() > my || my == -1 ) {
					my = nUnit.getY();
				}
			}
		}
		
		int startingCC_X = bBasePos.get(0).getX();
		int startingCC_Y = bBasePos.get(0).getY();
		if ( startingCC_X < lx) {
			lx = startingCC_X;
		}
		if ( startingCC_X > mx) {
			mx = startingCC_X;
		}
		if ( startingCC_Y < ly) {
			ly = startingCC_Y;
		}
		if ( startingCC_Y > my) {
		}
		
		// left, top, right, bottom
		resourceZone.add(lx);
		resourceZone.add(ly);
		resourceZone.add(mx);
		resourceZone.add(my);
		//return resourceZone;
	}

	
	// bonjwAI baselocations
	private static List<Position> getStartingLocations(Game game) {
		List<Position> BaseLocationArray = new ArrayList<Position>();

		for (BaseLocation b : BWTA.getBaseLocations()) {
			if (b.isStartLocation()) {
				BaseLocationArray.add(b.getPosition());
			}
		}
		return BaseLocationArray;
	}

	public static Position getNearestUnexploredStartingLocation(Game game, Position position) {
		if (position == null) {
			return null;
		}
		// Get list of all starting locations
		List<Position> startingLocations = getStartingLocations(game);

		// For every location...
		for (Position baseLocation : startingLocations) {
			if (!isExplored(game, baseLocation)) {
				return baseLocation;
			}

		}
		return null;
	}
	
	public static BaseLocation getClosestStartLocation( Position pos ) {
		double distance = -1;
		double tempDistance = 0;
		BaseLocation potentialStartLoc = null;
		
		for ( BaseLocation startLoc : BWTA.getStartLocations() ) {
			tempDistance = BWTA.getGroundDistance(pos.toTilePosition(), startLoc.getTilePosition());			
			if ( distance == -1 || tempDistance < distance) {
				distance = tempDistance;
				potentialStartLoc = startLoc;
			}
		}
		return potentialStartLoc;
	}
	
	public static void getTwoNearBases( Game game, ArrayList<BaseLocation> baseList) {
		// get second index
		baseList.add(getClosestUniqueBase(baseList));
    	// get third index
		baseList.add(getClosestUniqueBase(baseList)); 
	}
	
	public static void updateEnemyBuildingMemory(Game game, List<Position> enemyBuildingMemory) {
		// always loop over all currently visible enemy units (even though this
		// set is usually empty)
		for (Unit u : game.enemy().getUnits()) {
			// if this unit is in fact a building
			if (u.getType().isBuilding()) {
				// check if we have it's position in memory and add it if we
				// don't
				if (!enemyBuildingMemory.contains(u.getPosition()))
					enemyBuildingMemory.add(u.getPosition());
			}
		}

		// loop over all the positions that we remember
		for (Position p : enemyBuildingMemory) {
			// compute the TilePosition corresponding to our remembered Position
			// p
			TilePosition tileCorrespondingToP = new TilePosition(p.getX() / 32, p.getY() / 32);

			// if that tile is currently visible to us...
			if (game.isVisible(tileCorrespondingToP)) {

				// loop over all the visible enemy buildings and find out if at
				// least
				// one of them is still at that remembered position
				boolean buildingStillThere = false;
				for (Unit u : game.enemy().getUnits()) {
					if ((u.getType().isBuilding()) && (u.getPosition() == p)) {
						buildingStillThere = true;
						break;
					}
				}

				// if there is no more any building, remove that position from
				// our memory
				if (buildingStillThere == false) {
					enemyBuildingMemory.remove(p);
					break;
				}
			}
		}
	}
	
	
	
}
