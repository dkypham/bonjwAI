package structure;

import java.util.List;

import bwapi.UnitType;
import economy.Resources;
import bwapi.Pair;
import bwapi.TilePosition;

/**
 * Used only for build openings
 * 
 *
 */
public class BuildOrder {

	static final int MAX_TIME_BUILD_ISSUED = 20;
	
	List<Pair<UnitType,Integer>> buildOrder;
	boolean buildIssued;
	int timeBuildIssued;
	TilePosition plannedBuildLocation;
	
	boolean completed;

	public BuildOrder( List<Pair<UnitType,Integer>> buildOrderChoice ) {
		this.buildOrder = buildOrderChoice;
		this.buildIssued = false;
		this.timeBuildIssued = -1;
		this.plannedBuildLocation = new TilePosition(-1,-1);
		this.completed = false;
	}
	
	public List<Pair<UnitType,Integer>> getBuildOrder() {
		return this.buildOrder;
	}
	
	public Pair<UnitType,Integer> getNextPairInBuildOrder() {
		return this.buildOrder.get(0);
	}
	
	public boolean matchesNextstruct( UnitType uT ) {
		return uT == this.buildOrder.get(0).first;
	}
	
	public void removeTopOfBuildOrder() {
		this.buildOrder.remove(0);
	}
	
	// check if buildOrderSupply is met
	public boolean checkIfSupplyMet( Resources bResources ) {
		return bResources.getSupplyUsed() >= this.buildOrder.get(0).second;
	}
	
	// check if build was issued
	public boolean checkIfBuildIssued() {
		return this.buildIssued;
	}
	
	public void setBuildIssuedTrue() {
		if ( this.buildIssued == false ) {
			this.buildIssued = true;
		}
		else {
			System.out.println("BuildOrder: buildIssued is already true.");
		}
	}
	public void setBuildIssuedFalse() {
		if ( this.buildIssued == true ) {
			this.buildIssued = false;
		}
		else {
			System.out.println("BuildOrder: buildIssued is already false.");
		}
	}
	
	
	public boolean isBuildIssued() {
		return this.buildIssued;
	}
	
	public void setTimeBuildIssued( int time ) {
		this.timeBuildIssued = time;
	}
	
	public boolean tooMuchTimeBuildIssued( int gameTime ) {
		return gameTime - this.timeBuildIssued > MAX_TIME_BUILD_ISSUED;
	}
	
	
	public static TilePosition getPlannedBuildLocation() {
		
		return null;
	}
	
	public boolean isCompleted() {
		return this.completed;
	}
	
}
