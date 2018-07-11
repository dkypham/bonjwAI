package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.Resources;

public class ListOfBuildOrders {

	static final UnitType SD = UnitType.Terran_Supply_Depot;
	static final UnitType BARRACKS = UnitType.Terran_Barracks;
	static final UnitType REFINERY = UnitType.Terran_Refinery;
	static final UnitType FACTORY = UnitType.Terran_Factory;
	static final UnitType MACHINESHOP = UnitType.Terran_Machine_Shop;
	static final UnitType CC = UnitType.Terran_Command_Center;
	
	static final TechType SIEGE = TechType.Tank_Siege_Mode;
	
	// choose build order here
	public static List<BuildOrderElement> chooseBuildOrder( Race enemyRace ) {
		List<BuildOrderElement> buildOrderList = new ArrayList<BuildOrderElement>();
		
		if 		(enemyRace == Race.Terran) 	initOneFactFE( buildOrderList );
		else if (enemyRace == Race.Protoss)	initOneFactFE( buildOrderList );
		else if (enemyRace == Race.Zerg) 	initOneFactFE( buildOrderList ) ;
		else if (enemyRace == Race.Random) 	initOneFactFE( buildOrderList );
		else System.out.println("ListOfBuildOrders: Cannot choose buildOrder based on enemyRace");
		
		return buildOrderList;
	}
	
	public static void addUnitToBO( List<BuildOrderElement> buildOrderList, UnitType uT, Integer supply ) {
		buildOrderList.add( new BuildOrderElement(uT, supply) );
	}
	
	public static void addTechToBO( List<BuildOrderElement> buildOrderList, TechType tT, Integer supply ) {
		buildOrderList.add( new BuildOrderElement(tT, supply) );
	}
	
	// DEFAULT BUILD ORDERS HERE
	public static void initOneFactFE( List<BuildOrderElement> buildOrderList ) {
		addUnitToBO( buildOrderList, SD				, 9);
		addUnitToBO( buildOrderList, BARRACKS		, 12);
		addUnitToBO( buildOrderList, REFINERY		, 12);
		addUnitToBO( buildOrderList, SD				, 15);
		addUnitToBO( buildOrderList, FACTORY		, 16); // take two scvs off gas
		addUnitToBO( buildOrderList, MACHINESHOP	, 20); // put two scvs back on gas
		addTechToBO( buildOrderList, SIEGE			, 22);
		addUnitToBO( buildOrderList, SD				, 23);
		addUnitToBO( buildOrderList, CC				, 28);
		addUnitToBO( buildOrderList, SD				, 28);
		addUnitToBO( buildOrderList, FACTORY		, 32);
		addUnitToBO( buildOrderList, UnitType.Special_Terran_Flag_Beacon, 500);
	}
	
	public static void initOneRaxFE( List<BuildOrderElement> buildOrderList ) {
		addUnitToBO( buildOrderList, SD			, 9);
		addUnitToBO( buildOrderList, BARRACKS		, 11);
		addUnitToBO( buildOrderList, CC			, 16);
		addUnitToBO( buildOrderList, REFINERY		, 16);	
		addUnitToBO( buildOrderList, SD			, 16);
		addUnitToBO( buildOrderList, FACTORY		, 21);
		addUnitToBO( buildOrderList, SD			, 28);
		addUnitToBO( buildOrderList, MACHINESHOP	, 30);
		addUnitToBO( buildOrderList, FACTORY		, 32);
		addUnitToBO( buildOrderList, MACHINESHOP	, 39);
		addUnitToBO( buildOrderList, UnitType.Special_Terran_Flag_Beacon, 500);
	}
	
}
