package idmap;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Unit;
import bwapi.UnitType;
import economy.WorkerManager;

public class MapUnitID {

	static String uT_ScoutSCV = "Scout";
	static String uT_DefendSCV = "Defender";
	static String uT_RepairSCV ="Repairer";
	
	// This function adds a <Unit,Integer> value into unitIDMap
	public static void addToIDMap(Multimap<UnitType, Integer> unitIDMap, Unit unit) {
		unitIDMap.put(unit.getType(), Integer.valueOf(unit.getID()));

	}
	
	// This function adds a <Unit,Integer> value into unitIDMap
	public static void addToIDMapSpecial(Multimap<UnitType, Integer> unitIDMap, Unit unit, 
			UnitType specialUnitType) {
		unitIDMap.put(specialUnitType, Integer.valueOf(unit.getID()));

	}
	
	// This function adds a <Unit,Integer> value into unitIDMap
	public static void addStructToIDMap(Multimap<UnitType, Integer> unitIDMap, 
			Unit unit ) {
		if ( unitIDMap.containsEntry(unit, -1) ) {
			unitIDMap.remove(unit, -1);
		}
		unitIDMap.put(unit.getType(), Integer.valueOf(unit.getID()));

	}
	
	// This function removes a <Unit,Integer> value from unitIDMap
	public static void removeFromIDMap(
			Multimap<UnitType, Integer> unitIDMap, Unit unit) {
		unitIDMap.remove(unit.getType(), Integer.valueOf(unit.getID()));
		
	}

	public static void removeRoleSCV( Multimap<String, Integer> bRolesMap, Unit SCV ) {
		//System.out.println("SCV with ID: " + SCV.getID() + " destroyed");
		if ( bRolesMap.containsEntry(uT_ScoutSCV, SCV.getID() )) {
			bRolesMap.remove( uT_ScoutSCV, SCV.getID() );
		}
		if ( bRolesMap.containsEntry(uT_DefendSCV, SCV.getID() )) {
			bRolesMap.remove( uT_DefendSCV, SCV.getID() );	
		}
		if ( bRolesMap.containsEntry(uT_RepairSCV, SCV.getID() )) {
			bRolesMap.remove( uT_RepairSCV, SCV.getID() );		
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
	
	public static Unit getStruct(Game game, Multimap<UnitType, Integer> bStructMap, UnitType struct) {
		for ( Integer structID : bStructMap.get( struct ) ) {
			Unit structUnit = getUnit( game, structID);
			if ( structUnit.isCompleted() ) {
				return structUnit;
			}
		}
		return null;
	}
	
	public static Unit getFirstUnitFromUnitMap( Game game, Multimap<UnitType, Integer> map, UnitType uT ) {
		return game.getUnit( Iterables.get( (map.get(uT)), 0) );
	}
	
	public static Unit getFirstUnitFromRolesMap( Game game, Multimap<String, Integer> map, String role ) {
		return game.getUnit( Iterables.get( (map.get(role)), 0) );
	}
	
	public static boolean isInjured( Unit u ) {
		return u.getHitPoints() != u.getInitialHitPoints();
	}
	
	// NEW FUNCTIONS BELOW
	
}