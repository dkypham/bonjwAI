package b.idmap;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import b.economy.WorkerManager;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;

public class MapUnitID {

	// This function adds a <Unit,Integer> value into unitIDMap
	public static void addToIDMap(Multimap<UnitType, Integer> unitIDMap, 
			Unit unit) {
		unitIDMap.put(unit.getType(), Integer.valueOf(unit.getID()));

	}
	
	// This function adds a <Unit,Integer> value into unitIDMap
	public static void addStructToIDMap(Multimap<UnitType, Integer> unitIDMap, 
			Unit unit, List<UnitType> buildOrderStruct, List<Integer> buildOrderSupply) {
		if ( unitIDMap.containsEntry(unit, -1) ) {
			unitIDMap.remove(unit, -1);
		}
		unitIDMap.put(unit.getType(), Integer.valueOf(unit.getID()));
		
		// remove from buildOrder list
		if ( unit.getType() == buildOrderStruct.get(0) ) {
			buildOrderStruct.remove(0);
			buildOrderSupply.remove(0);
		}

	}
	
	// This function removes a <Unit,Integer> value from unitIDMap
	public static void removeFromIDMap(
			Multimap<UnitType, Integer> unitIDMap, Unit unit) {
		unitIDMap.remove(unit.getType(), Integer.valueOf(unit.getID()));
		
		// handle case if unit that dies was scout
		if ( WorkerManager.checkIfScoutSCV(unit.getID(), unitIDMap) ) {
			unitIDMap.remove( UnitType.Protoss_Scout, Integer.valueOf(unit.getID()));	
		}
	}

	// This function takes an int unitID and returns a unit associated with that ID
	public static Unit getUnit( Game game, int unitID) {
		return game.getUnit(unitID);
	}

	public static int getStructCount(Game game, 
			Multimap<UnitType, Integer> armyMap,
			Multimap<UnitType, Integer> structMap,
			UnitType struct) {
		int structCount = structMap.get(struct).size();
		for ( Integer SCVID : armyMap.get(UnitType.Terran_SCV ) ) {
			Unit SCV = game.getUnit(SCVID);
			if ( SCV.isConstructing() && SCV.getBuildType() == struct && SCV.canAttack()) {
				structCount++;
			}
		}
		return structCount;
	}
	
	public static int getArmyCount(Game game, 
			Multimap<UnitType, Integer> armyMap,
			UnitType armyUnit) {
		return armyMap.get(armyUnit).size();
	}
	
}