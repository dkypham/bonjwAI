package b.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.idmap.MapUnitID;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

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
	public static boolean needSupplyCheck(Player self, int effectiveSupply) {
		boolean needSupply = false;

		// need supply at 8/10
		if (self.supplyTotal() <= 20) {
			if (effectiveSupply - self.supplyUsed() <= 2) {
				needSupply = true;
			} 
		}
		else if (self.supplyTotal() <= 60) {
			if (effectiveSupply - self.supplyUsed() < 4) {
				needSupply = true;
			}
		}
		// 30 to 50 supply: need supply at -6
		else if (self.supplyTotal() < 100) {
			if (effectiveSupply - self.supplyUsed() < 12) {
				needSupply = true;
			}
		}
		// 50 to 100 supply: need supply at -8
		else if (self.supplyTotal() < 200) {
			if (effectiveSupply - self.supplyUsed() < 16) {
				needSupply = true;
			}
		}
		// 100 to 150 supply: need supply at -8
		else if (self.supplyTotal() <= 400) {
			if (effectiveSupply - self.supplyUsed() < 20) {
				needSupply = true;
			}
		}	
		else {
			needSupply = false;
		}
		
		return needSupply;
	}
}
