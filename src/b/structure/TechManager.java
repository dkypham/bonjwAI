package b.structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.economy.ResourceManager;
import b.idmap.MapUnitID;
import bwapi.Game;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;

public class TechManager {

	public static void initializeTechOrder(List<TechType> techTreeTech,
			List<Integer> techTreeSupply ) {
		// default implementation: 1 Fact FE
		addToTechOrder( techTreeTech, techTreeSupply, TechType.Tank_Siege_Mode, 22);

		// so game doesn't crash when build order is finished
		addToTechOrder( techTreeTech, techTreeSupply, TechType.None, 200);
	}
	 
	public static void addToTechOrder(List<TechType> techTreeTech,
			List<Integer> techTreeSupply,
			TechType tT,
			Integer supply) {
		techTreeTech.add(tT);
		techTreeSupply.add(supply);
	}

	public static boolean buildTech(Game game, Player self, Multimap<UnitType, Integer> bStructMap,
			ArrayList<Integer> bResources, TechType tech) {
		if ( ResourceManager.checkIfEnoughResourcesTech(bResources, tech) ) {
			Unit struct = MapUnitID.getStruct(game,  bStructMap, tech.whatResearches());
			if ( struct == null ) {
				System.out.println("Null value reached in buildTech for " + tech);
				return false;
			}
			System.out.println("trying to research tech 1");
			if ( struct.canResearch( tech ) ) {
				System.out.println("trying to research tech 2");
				return struct.research(tech);
			}
		}	
		return false;
	}
	
}
