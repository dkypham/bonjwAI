package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwta.BaseLocation;

public class BuildQueue {

	static final int MAX_TIME_BUILD_ISSUED = 20;
	
	ArrayList<UnitType> buildQueue;
	boolean isBuildIssued;
	int timeBuildIssued;
	TilePosition plannedBuildLocation;
	
	BuildQueue( BuildOrder bBuildOrder ) {
		this.buildQueue = new ArrayList<UnitType>();
		convertFromBuildOrder(this.buildQueue, bBuildOrder);
		
		this.plannedBuildLocation = new TilePosition(-1,-1);
	}
	
	public ArrayList<UnitType> getBuildQueue() {
		return this.buildQueue;
	}
	
	public void removeTopOfBuildOrder() {
		this.buildQueue.remove(0);
		this.plannedBuildLocation = new TilePosition(-1, -1);
	}
	
	public boolean checkIfBuildIssued() {
		return this.isBuildIssued;
	}
	
	public void setIsBuildIssuedTrue() {
		if ( this.isBuildIssued == false ) {
			this.isBuildIssued = true;
		}
		else {
			System.out.println("BuildQueue: buildIssued is already true.");
		}
	}
	public void setIsBuildIssuedFalse() {
		if ( this.isBuildIssued == true ) {
			this.isBuildIssued = false;
		}
		else {
			System.out.println("BuildQueue: buildIssued is already false.");
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
	
	public void convertFromBuildOrder( ArrayList<UnitType> structQueue, BuildOrder bBuildOrder ) {
		for ( BuildOrderElement beo : bBuildOrder.getBuildOrder() ) {
			if ( beo.isStruct() ) {
				this.buildQueue.add( beo.getUT() );
				bBuildOrder.removeTopOfBuildOrder();
			}
			else if ( beo.isTech() ) {
				bBuildOrder.removeTopOfBuildOrder();
			}
			else {
				bBuildOrder.removeTopOfBuildOrder();
			}
		}
	}
	
}
