package economy;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;
import structure.BuildingManager;

public class Resources {
	int minsActual = 0;
	int minsEffective = 0;
	int gasActual = 0;
	int gasEffective = 0;
	int supplyTotalActual = 0;
	int supplyTotalEffective = 0;
	int supplyUsed = 0;
		
	public void updateAll(Game game, Player self, Multimap<UnitType, Integer> bArmyMap ) {
		this.minsActual = self.minerals();
		this.minsEffective = this.minsActual;
		this.gasActual = self.gas();
		this.gasEffective = this.gasActual;
		this.supplyTotalActual = self.supplyTotal();
		this.supplyTotalEffective = this.supplyTotalActual;
		this.supplyUsed = self.supplyUsed();
		
		//update effective resources here:
		updateEffectiveResources(game, self, bArmyMap);
		halveSupplyValues();
	}
	
	public int getMinsActual() {
		return this.minsActual;
	}
	
	public int getMinsEffective() {
		return this.minsEffective;
	}
	
	public int getGasActual() {
		return this.gasActual;
	}
	
	public int getGasEffective() {
		return this.gasEffective;
	}
	
	public int getSupplyTotalActual() {
		return this.supplyTotalActual;
	}
	
	public int getSupplyTotalEffective() {
		return this.supplyTotalEffective;
	}
	
	public int getSupplyUsed() {
		return this.supplyUsed;
	}
	
	public boolean checkIfEnoughMins( int mins ) {
		return this.minsEffective - mins > 0;
	}
	
	public boolean checkIfEnoughGas( int gas ) {
		return this.gasEffective - gas > 0;
	}
	
	public void subFromMinsReserved( int mins ) {
		this.minsEffective = this.minsEffective - mins;
	}
	
	public void subFromGasReserved( int gas ) {
		this.gasEffective = this.gasEffective - gas;
	}
	
	public void addToEffectiveSupply( int supply ) {
		this.supplyTotalEffective = this.supplyTotalEffective + supply;
	}
	
	public boolean checkIfEnoughMinsAndGas( int minsCost, int gasCost ) {
		return (this.minsEffective - minsCost >= 0) && (this.gasEffective - gasCost >= 0);
	}
	
	public boolean enoughResourcesBuildUnit( UnitType uT ) {
		// check resources and supply
		if ( (this.minsEffective - uT.mineralPrice() < 0) || (this.gasEffective - uT.gasPrice() < 0) ) {
			return false;
		}
		//if ( this.supplyTotalActual - this.supplyUsed - uT.supplyRequired() < 0 ) {
		if ( this.supplyTotalEffective - this.supplyUsed - uT.supplyRequired() < 0 ) {
			return false;
		}
		return true;
	}
	
	public void addMinAndGas( int minsCost, int gasCost ) {
		this.minsEffective = this.minsEffective - minsCost;
		this.gasEffective = this.gasEffective - gasCost;
	}
	
	public void updateEffectiveResources( Game game, Player self, Multimap<UnitType,Integer> bArmyMap ) {
		// reserved minerals, reserved gas, 
		for ( Integer SCVID : bArmyMap.get(UnitType.Terran_SCV ) ) {
			Unit SCV = game.getUnit(SCVID);
			if ( SCV.isConstructing() && SCV.canAttack() ) {
				subFromMinsReserved( SCV.getBuildType().mineralPrice() );
				subFromGasReserved( SCV.getBuildType().gasPrice() );
			}
		}
		
		int numSupplyConstructing = BuildingManager.getNumPlannedStruct(game, bArmyMap, UnitType.Terran_Supply_Depot) 
				+ BuildingManager.getNumConstructingStruct(game, bArmyMap, UnitType.Terran_Supply_Depot);
		addToEffectiveSupply( 16 * numSupplyConstructing );
	}
	
	public void halveSupplyValues() {
		this.supplyTotalActual = this.supplyTotalActual / 2;
		this.supplyTotalEffective = this.supplyTotalEffective / 2;
		this.supplyUsed = this.supplyUsed / 2;
	}
	
	public boolean checkIfSixtyPercent( UnitType uT ) {
		return (this.minsEffective >= uT.mineralPrice()*.6) && (this.gasEffective >= uT.gasPrice()*.6);
	}

}
