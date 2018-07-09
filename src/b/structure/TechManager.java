package b.structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.economy.ResourceManager;
import b.idmap.MapUnitID;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;

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
	
	public static void addToTechOrder(List<Pair<TechType,Integer>> buildOrderTech,
			TechType tT,
			Integer supply) {
		buildOrderTech.add( new Pair<TechType,Integer>(tT, supply) );
	}
	
	public static boolean buildTech(Game game, Player self, Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources, TechType tech) {
		// first check if enough resources
		if ( ResourceManager.checkIfEnoughResourcesTech(bResources, tech) ) {
			// get struct that researches the tech
			Unit struct = MapUnitID.getStruct(game,  bStructMap, tech.whatResearches());
			// if no such struct, then return false
			if ( struct == null ) {
				//System.out.println("Null value reached in buildTech for " + tech);
				return false;
			}
			//System.out.println("trying to research tech 1");
			if ( struct.canResearch( tech ) ) {
				System.out.println("trying to research tech 2");
				return struct.research(tech);
			}
		}	
		return false;
	}
	
}
