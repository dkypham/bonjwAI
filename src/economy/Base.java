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
	Unit Refinery;
	ArrayList<Integer> gasMinerIDs;
	
	int minMinMiners = 24; // TODO: write function to update based on number of patches
	int minGasMiners = 3;	// TODO: write function to update
	
	public Base( BaseLocation baseLocation ) {
		if ( bL != null || this.CC != null ) {
			System.out.println("Base: Overwriting old Base info");
		}
		
		this.bL = baseLocation;
		this.Refinery = null;
		this.gasMinerIDs = new ArrayList<Integer>();
	}
	public Base( BaseLocation baseLocation, Unit newCC ) {
		if ( bL != null || this.CC != null ) {
			System.out.println("Base: Overwriting old Base info");
		}
		
		this.bL = baseLocation;
		this.CC = newCC;
		this.Refinery = null;
		this.gasMinerIDs = new ArrayList<Integer>();
	}
	
	public Unit getCC() {
		return this.CC;
	}
	
	public void setCC( Unit newCC ) {
		this.CC = newCC;
	}
	
	public void assignRefinery( Unit newRefinery ) {
		this.Refinery = newRefinery;
	}
	
	public boolean isGasMinable() {
		if ( this.Refinery == null ) {
			return false;
		}
		return this.Refinery.isCompleted();
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
		/*
		int numGasMiners = 0;
		for ( Unit nearbyU : this.CC.getUnitsInRadius(maxDistMiners) ) {
			if ( nearbyU.isGatheringGas() ) {
				numGasMiners++;
			}
		}
		return numGasMiners;
		*/
		return this.gasMinerIDs.size();
	}
	
	public void addToGasMinerIDs( int ID ) {
		this.gasMinerIDs.add( ID );
	}
	
	public int getMinMinMiners() {
		return this.minMinMiners;
	}
	
	public int getMinGasMiners() {
		return this.minGasMiners;
	}
	
}
