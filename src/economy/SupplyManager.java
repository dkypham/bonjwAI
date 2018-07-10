package economy;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;
import idmap.MapUnitID;

public class SupplyManager {

	static int SUPPLY_VALUE_SD = 16;
	static int SUPPLY_VALUE_CC = 20;

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	/*
	public static int manageSupply(Game game, Player self, Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap, int effectiveSupply, int reservedMinerals,
			ArrayList<Integer> bResources) {
		effectiveSupply = SupplyManager.updateEffectiveSupply(game, armyMap, structMap);
		if ( needSupplyCheck(self,effectiveSupply) && (self.minerals()-reservedMinerals) >= 100 ) {
			WorkerManager.issueBuild(game, self, armyMap, structMap, SD);
		}
		effectiveSupply = SupplyManager.updateEffectiveSupply(game, armyMap, structMap);
		return effectiveSupply;
	}
	*/
	public static int updateEffectiveSupply(Game game, 
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap) {
		return (SUPPLY_VALUE_SD * MapUnitID.getStructCount(game, armyMap, structMap, SD) +
				SUPPLY_VALUE_CC * MapUnitID.getStructCount(game, armyMap, structMap, CC));
	}

	// step function for when supply depots should be built
	public static boolean needSupplyCheck( Resources bResources) {
		boolean needSupply = false;
		int supplyUsed = bResources.getSupplyUsed();
		int supplyTotalEffective = bResources.getSupplyTotalEffective();
		
		// need supply at 8/10
		if (supplyUsed <= 10) {
			if (supplyTotalEffective - supplyUsed <= 1) {
				needSupply = true;
			} 
		}
		else if (supplyUsed <= 30) {
			if (supplyTotalEffective - supplyUsed < 2) {
				needSupply = true;
			}
		}
		// 30 to 50 supply: need supply at -6
		else if (supplyUsed < 50) {
			if (supplyTotalEffective - supplyUsed < 6) {
				needSupply = true;
			}
		}
		// 50 to 100 supply: need supply at -8
		else if (supplyUsed < 100) {
			if (supplyTotalEffective - supplyUsed < 8) {
				needSupply = true;
			}
		}
		// 100 to 150 supply: need supply at -8
		else if (supplyUsed <= 200) {
			if (supplyTotalEffective - supplyUsed < 10) {
				needSupply = true;
			}
		}	
		else {
			needSupply = false;
		}
		
		return needSupply;
	}
}
