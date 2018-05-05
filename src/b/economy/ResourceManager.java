package b.economy;

import java.util.ArrayList;

import com.google.common.collect.Multimap;

import b.structure.BuildingManager;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

public class ResourceManager {

	static int T_SUPPLY_VALUE = 16;
	static UnitType T_SD = UnitType.Terran_Supply_Depot;
	static UnitType T_SCV = UnitType.Terran_SCV;
	
	public static void updateResources(Game game, Player self, ArrayList<Integer> bResources,
			Multimap<UnitType, Integer> bArmyMap) {
		clearBResources(bResources);
		updateBResources(game, self, bResources, bArmyMap);
	}
	
	public static void clearBResources(ArrayList<Integer> bResources) {
		if ( bResources.size() != 0 ) {
			bResources.clear();
		}
		for (int i = 0; i < 6; i++) {
			bResources.add(0);			
		}
	}
	
	public static void updateBResources(Game game, Player self, ArrayList<Integer> bResources,
			Multimap<UnitType, Integer> bArmyMap) {
		// zero all elements
		clearBResources(bResources);
		
		// update bResources(0): actual minerals
		bResources.set(0, self.minerals());
					
		// update bResources(2): actual gas
		bResources.set(2,  self.gas());
		
		// update bResources(1): reserved minerals
		// update bResources(3): reserved gas
		for ( Integer SCVID : bArmyMap.get(T_SCV ) ) {
			Unit SCV = game.getUnit(SCVID);
			if ( SCV.isConstructing() && SCV.canAttack() ) {
				bResources.set(1, bResources.get(1) + SCV.getBuildType().mineralPrice());
				bResources.set(3, bResources.get(3) + SCV.getBuildType().gasPrice());
			}
		}
		
		// update bResources(4): actual supply
		bResources.set(4, self.supplyTotal());

		// update bResources(5): effective supply
		int numSupplyBeingBuilt = BuildingManager.getNumPlannedStruct(game, bArmyMap, T_SD) 
				+ BuildingManager.getNumConstructingStruct(game, bArmyMap, T_SD);
		bResources.set(5, bResources.get(4) + (T_SUPPLY_VALUE * numSupplyBeingBuilt) );
	
	}
	
	public static void addBuildingCost( ArrayList<Integer> bResources, UnitType struct ) {
		bResources.set( 1 , bResources.get(1) + struct.mineralPrice() );
		bResources.set( 3 , bResources.get(3) + struct.gasPrice() );
	}

	public static int getReservedMinerals(Game game, Multimap<UnitType, Integer> armyMap) {
		int reservedMineral = 0;
		
		for ( Integer SCVID : armyMap.get(T_SCV ) ) {
			Unit SCV = game.getUnit(SCVID);
			if ( SCV.isConstructing() && SCV.canAttack() ) {
				reservedMineral += SCV.getBuildType().mineralPrice();
			}
		}
		return reservedMineral;
	}
	
}
