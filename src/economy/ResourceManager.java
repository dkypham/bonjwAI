package economy;

import java.util.ArrayList;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import structure.BuildingManager;

/**
 * Resource Manager, on each call
 * 
 * Updates bot's resource information: minerals, gas, and supply. Also accounts for planned resource state 
 * (minerals/gas after queued buildings are built, supply after queued supply depots are built)
 * 
 * bResources array:
 * 
 * [0] - actual minerals: actual minerals bot has
 * 
 * [1] - reserved minerals: amount of minerals bot is queued to spend
 * 
 * [2] - actual gas: actual gas bot has
 * 
 * [3] - reserved gas: amount of gas bot is queued to spend
 * 
 * [4] - actual supply: actual supply bot has
 * 
 * [5] - effective supply: amount of supply bot is queued to have
 * 
 * [6] - supply used: amount of supply bot is currently using
 */
public class ResourceManager {

	static int T_SUPPLY_VALUE = 16;
	static UnitType T_SD = UnitType.Terran_Supply_Depot;
	static UnitType T_SCV = UnitType.Terran_SCV;
	
	/*
	 * Update the bResources array, by first clearing old values, and then
	 * calculating new values (necessary to update reserved resources)
	 */
	public static void updateResources(Game game, Player self, ArrayList<Integer> bResources, 
			Multimap<UnitType, Integer> bArmyMap) {
		clearBResources(bResources);
		updateBResources(game, self, bResources, bArmyMap);
	}
	
	/**
	 * Clear all values in bResources array
	 * @param bResources - arrayList containing resource values
	 */
	public static void clearBResources(ArrayList<Integer> bResources) {
		if ( bResources.size() != 0 ) {
			bResources.clear();
		}
		for (int i = 0; i < 7; i++) {
			bResources.add(0);			
		}
	}
	
	/**
	 * Update all values in bResources array
	 * @param game
	 * @param self
	 * @param bResources - arrayList containing resource values
	 * @param bArmyMap - MultiMap containing unitType/unitIDs for all army units
	 */
	public static void updateBResources(Game game, Player self, ArrayList<Integer> bResources,
			Multimap<UnitType, Integer> bArmyMap) {		
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
		// calculate based on planned 
		int numSupplyBeingBuilt = BuildingManager.getNumPlannedStruct(game, bArmyMap, T_SD) 
				+ BuildingManager.getNumConstructingStruct(game, bArmyMap, T_SD);
		bResources.set(5, bResources.get(4) + (T_SUPPLY_VALUE * numSupplyBeingBuilt) );
		
		// update bResources(6): supply used
		bResources.set(6, self.supplyUsed() );
	
	}
	
	/**
	 * Calculate queued resources, passes in structType and adds to values in
	 * bResources
	 * @param bResources - arrayList containing resource values
	 * @param structType - type of structure to be built
	 */
	public static void addBuildingCost( ArrayList<Integer> bResources, UnitType structType ) {
		bResources.set( 1 , bResources.get(1) + structType.mineralPrice() );
		bResources.set( 3 , bResources.get(3) + structType.gasPrice() );
	}
	
	/**
	 * Determine if bot has enough resources to build structType
	 * @param bResources - arrayList containing resource values
	 * @param structType - type of struct to be built
	 * @return - true if there are enough resources to build building (accounting for
	 * queued resource expenditures)
	 */
	public static boolean checkIfEnoughResources( ArrayList<Integer> bResources, UnitType structType ) {
		// if actual minerals - reserved minerals < mineral price
		if ( bResources.get(0) - bResources.get(1) < structType.mineralPrice() ) {
			return false;
		}
		// if actual gas - reserved gas < gas price
		if ( bResources.get(2) - bResources.get(3) < structType.gasPrice() ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Determine if bot has enough resources to build techType
	 * @param bResources - arrayList containing resource values
	 * @param tech - type of tech to be built
	 * @return
	 */
	public static boolean checkIfEnoughResourcesTech( ArrayList<Integer> bResources, TechType tech ) {
		// if actual minerals - reserved minerals < mineral price
		if ( bResources.get(0) - bResources.get(1) < tech.mineralPrice() ) {
			return false;
		}
		// if actual gas - reserved gas < gas price
		if ( bResources.get(2) - bResources.get(3) < tech.gasPrice() ) {
			return false;
		}
		return true;
	}
	
	public static int getActualMinerals( ArrayList<Integer> bResources ) {
		return bResources.get(0);
	}
	
	public static int getEffectiveMinerals( ArrayList<Integer> bResources ) {
		return bResources.get(1);
	}
	
	public static int getActualGas( ArrayList<Integer> bResources ) {
		return bResources.get(2);
	}
	
	public static int getEffectiveGas( ArrayList<Integer> bResources ) {
		return bResources.get(3);
	}
	
	public static int getActualSupply( ArrayList<Integer> bResources ) {
		return bResources.get(4);
	}
	
	public static int getEffectiveSupply( ArrayList<Integer> bResources ) {
		return bResources.get(5);
	}
	
	public static int getSupplyUsed( ArrayList<Integer> bResources ) {
		return bResources.get(6);
	}
	
}
