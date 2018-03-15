package b.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.economy.SupplyManager;
import b.idmap.MapUnitID;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

public class BonjwAIGame {
	
	// Switch flags
	static int switchFlagOther = 0;
	static int switchArmy = 1;
	static int switchStructure = 2;
	
	public static int switchFlags(Player self, Unit u) {
		if ( (u.getType().isBuilding() && u.getType().getRace() == self.getRace() ) ) {
			return switchStructure;
		}
		else if ( u.getType().getRace() == self.getRace() ){
			return switchArmy;
		}
		else {
			return switchFlagOther;
		}
	}
	
	
	public static void initializeArmyMap(Multimap<UnitType, Integer> armyMap) {
		// TODO
	} 
	
	public static void initializeStructMap(Multimap<UnitType, Integer> structMap) {
		// TODO
	}
	
	public static boolean updateUnderAttack(Game game,
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap) {
		for (Integer uID : armyMap.values() )  {
			if ( game.getUnit(uID).isUnderAttack() ) {
				return true;
			}
		}
		for (Integer uID : structMap.values() )  {
			if ( game.getUnit(uID).isUnderAttack() ) {
				return true;
			}
		}
		return false;
	}
	

	
}
