package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import economy.ResourceManager;
import economy.Resources;
import idmap.MapUnitID;

public class TechManager {

	public static void initializeTechOrder( List<Pair<TechType,Integer>> buildOrderTech ) {

		// default implementation: 1 Fact FE
		initOneFactFE( buildOrderTech );
		
		// so game doesn't crash when build order is finished
		addToTechOrder( buildOrderTech, TechType.None, 500);
	}

	// Tech Orders here
	
	public static void initOneFactFE( List<Pair<TechType,Integer>> buildOrderTech ) {
		addToTechOrder( buildOrderTech, TechType.Tank_Siege_Mode, 22);
	}
	
	public static void initOneRaxFE( List<Pair<TechType,Integer>> buildOrderTech ) {
		addToTechOrder( buildOrderTech, TechType.Tank_Siege_Mode, 41);
	}
	
	public static void addToTechOrder(List<Pair<TechType,Integer>> buildOrderTech, TechType tT, Integer supply) {
		buildOrderTech.add( new Pair<TechType,Integer>(tT, supply) );
	}
	
	// NEW FUNCS HERE
	public static boolean checkIfSupplyMet( Resources bResources, Integer techSupply ) {
		return bResources.getSupplyUsed() >= techSupply;
	}

	public static boolean buildTech(Game game, Multimap<UnitType, Integer> bStructMap,
			Resources bResources, TechType techType) {
		// first check if enough resources
		if ( bResources.checkIfEnoughMinsAndGas(techType.mineralPrice(), techType.gasPrice()) ) {
			// get struct that researches the tech
			Unit struct = MapUnitID.getStruct(game,  bStructMap, techType.whatResearches());
			if ( struct == null ) {
				System.out.println("TechManager: Could not find building to research: " + techType );
				return false;
			}
			if ( struct.canResearch( techType ) ) {
				return struct.research(techType); // return true if building starts researching tech
			}
		}	
		return false;
	}
	
}
