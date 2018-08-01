package structure;

import bwapi.TechType;
import bwapi.UnitType;

public class BuildOrderElement {
	
	UnitType uT;
	TechType tT;
	int supply;
	
	public BuildOrderElement( UnitType uT, Integer supply ) {
		this.uT = uT;
		this.tT = null;
		this.supply = supply;
	}
	
	public BuildOrderElement( TechType tT, Integer supply ) {
		this.uT = null;
		this.tT = tT;
		this.supply = supply;
	}
	
	public UnitType getUT() {
		return this.uT;
	}
	public TechType getTT() {
		return this.tT;
	}
	public Integer getSupply() {
		return this.supply;
	}
	
	
	public boolean isStruct() {
		if ( this.uT == null ) return false;
		return this.uT.isBuilding();
	}
	
	public boolean isUnit() {
		if ( this.uT == null ) return false;
		return !this.uT.isBuilding();
	}
	
	public boolean isTech() {
		return tT != null;
	}
}
