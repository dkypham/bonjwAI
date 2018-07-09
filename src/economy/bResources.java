package economy;

public class bResources {
	int actualMinerals = -1;
	int reservedMinerals = 0;
	int actualGas = 0;
	int reservedGas = 0;
	int actualSupply = 0;
	int effectiveSupply = 0;
	int supplyUsed = 0;
	
	public void resetBResources() {
		this.actualMinerals = 1;
		this.reservedMinerals = 0;
		this.actualGas = 0;
		this.reservedGas = 0;
		this.actualSupply = 0;
		this.effectiveSupply = 0;
		this.supplyUsed = 0;
	}
	
	public int getActualMinerals() {
		return actualMinerals;
	}
	
	public int getEffectiveMinerals() {
		return reservedMinerals;
	}
	
	public int getActualGas() {
		return actualGas;
	}
	
	public int getEffectiveGas() {
		return reservedGas;
	}
	
	public int getActualSupply() {
		return actualSupply;
	}
	
	public int getEffectiveSupply() {
		return effectiveSupply;
	}
	
	public int getSupplyUsed() {
		return supplyUsed;
	}

}
