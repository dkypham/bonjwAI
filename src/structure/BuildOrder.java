package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.UnitType;
import bwta.BaseLocation;
import economy.Resources;
import bwapi.Game;
import bwapi.Pair;
import bwapi.TechType;
import bwapi.TilePosition;

/**
 * Used only for build openings
 * 
 *
 */
public class BuildOrder {

	static final int MAX_TIME_BUILD_ISSUED = 20;
	
	List<BuildOrderElement> buildOrder;
	boolean isBuildIssued;
	int timeBuildIssued;
	TilePosition plannedBuildLocation;
	
	boolean completed;

	public BuildOrder( List<BuildOrderElement> buildOrderChoice ) {
		this.buildOrder = buildOrderChoice;
		this.isBuildIssued = false;
		this.timeBuildIssued = -1;
		this.plannedBuildLocation = new TilePosition(-1,-1);
		this.completed = false;
	}
	
	public List<BuildOrderElement> getBuildOrder() {
		return this.buildOrder;
	}
	
	public boolean matchesNextstruct( UnitType uT ) {
		if ( uT.isBuilding() ) {
			return uT == this.buildOrder.get(0).getUT();
		}
		return false;
	}
	
	public boolean nextIsStruct() {
		return this.buildOrder.get(0).isStruct();
	}
	
	public UnitType getNextStruct() {
		return this.buildOrder.get(0).getUT();
	}
	
	public void removeTopOfBuildOrder() {
		this.buildOrder.remove(0);
		this.plannedBuildLocation = new TilePosition(-1, -1);
	}
	
	// check if buildOrderSupply is met
	public boolean checkIfSupplyMet( Resources bResources ) {
		return bResources.getSupplyUsed() >= this.buildOrder.get(0).getSupply();
	}
	
	// check if build was issued
	public boolean checkIfBuildIssued() {
		return this.isBuildIssued;
	}
	
	public void setIsBuildIssuedTrue() {
		if ( this.isBuildIssued == false ) {
			this.isBuildIssued = true;
		}
		else {
			System.out.println("BuildOrder: buildIssued is already true.");
		}
	}
	public void setIsBuildIssuedFalse() {
		if ( this.isBuildIssued == true ) {
			this.isBuildIssued = false;
		}
		else {
			System.out.println("BuildOrder: buildIssued is already false.");
		}
	}
	
	
	public boolean getIsBuildIssued() {
		return this.isBuildIssued;
	}
	
	public void setTimeBuildIssued( int time ) {
		this.timeBuildIssued = time;
	}
	
	public boolean tooMuchTimeBuildIssued( int gameTime ) {
		return (gameTime - this.timeBuildIssued) > MAX_TIME_BUILD_ISSUED;
	}
	
	
	public TilePosition getPlannedBuildLocation() {
		return this.plannedBuildLocation;
	}
	
	public boolean nextBuildLocationInvalid() {
		return this.plannedBuildLocation.getX() == -1;
	}
	
	public void updatePlannedBuildLocation( Game game, Multimap<UnitType, Integer> bStructMap, UnitType structType,
			ArrayList<BaseLocation> bBasePos, List<Pair<TilePosition,TilePosition>> noBuildZones) {
		this.plannedBuildLocation = BuildingPlacement.getPlannedBuildLocation( game, bStructMap, structType, 
				bBasePos, noBuildZones );
	}
	
	public boolean isCompleted() {
		return this.completed;
	}

	public TechType getNextTech() {
		return this.buildOrder.get(0).getTT();
	}
	
}
