package economy;

import java.util.ArrayList;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;

public class Base {

	static final int maxDistMins = 300;
	static final int maxDistMiners = 400;
	static final int numMinPatchesPerBase = 7;
	
	BaseLocation bL;
	Unit CC;
	Pair<Position,Position> miningRegion;
	
	int minMinMiners = 24; // TODO: write function to update based on number of patches
	int minGasMiners = 3;	// TODO: write function to update
	
	public Base( BaseLocation baseLocation ) {
		if ( bL != null || this.CC != null ) {
			System.out.println("Base: Overwriting old Base info");
		}
		
		this.bL = baseLocation;
	}
	public Base( BaseLocation baseLocation, Unit newCC ) {
		if ( bL != null || this.CC != null ) {
			System.out.println("Base: Overwriting old Base info");
		}
		
		this.bL = baseLocation;
		this.CC = newCC;
	}
	
	public Unit getCC() {
		return this.CC;
	}
	
	public void setCC( Unit newCC ) {
		this.CC = newCC;
	}
	
	public boolean isMinable( ) {
		return this.CC.isCompleted();
	}
	public int getNumMinMiners() {
		int numMinMiners = 0;
		for ( Unit nearbyU : this.CC.getUnitsInRadius(maxDistMiners) ) {
			if ( nearbyU.isGatheringMinerals() ) {
				numMinMiners++;
			}
		}
		return numMinMiners;
	}
	public int getNumGasMiners() { 
		int numGasMiners = 0;
		for ( Unit nearbyU : this.CC.getUnitsInRadius(maxDistMiners) ) {
			if ( nearbyU.isGatheringGas() ) {
				numGasMiners++;
			}
		}
		return numGasMiners;
	}
	
	public int getMinMinMiners() {
		return this.minMinMiners;
	}
	
	public int getMinGasMiners() {
		return this.minGasMiners;
	}

	public boolean initMiningRegion( Game game, Base base ) {
			
		return false;
	}
	
}
