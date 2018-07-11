package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.Resources;

public class ListOfBuildOrders {

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType Barracks = UnitType.Terran_Barracks;
	static UnitType Refinery = UnitType.Terran_Refinery;
	static UnitType Factory = UnitType.Terran_Factory;
	static UnitType MachineShop = UnitType.Terran_Machine_Shop;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	// choose build order here
	public static List<Pair<UnitType,Integer>> chooseBuildOrder( Race enemyRace ) {
		List<Pair<UnitType,Integer>> buildOrderStruct = new ArrayList<Pair<UnitType,Integer>>();
		
		if ( enemyRace == Race.Terran) initOneFactFE( buildOrderStruct );
		else if ( enemyRace == Race.Protoss ) initOneFactFE( buildOrderStruct );
		else if ( enemyRace == Race.Zerg ) initOneFactFE( buildOrderStruct ) ;
		else if ( enemyRace == Race.Random ) initOneFactFE( buildOrderStruct );
		else System.out.println("ListOfBuildOrders: Cannot choose buildOrder based on enemyRace");
		
		return buildOrderStruct;
	}
	
	// DEFAULT BUILD ORDERS HERE
	public static void initOneFactFE( List<Pair<UnitType,Integer>> buildOrderStruct ) {
		addToBuildOrder( buildOrderStruct, SD			, 9);
		addToBuildOrder( buildOrderStruct, Barracks		, 12);
		addToBuildOrder( buildOrderStruct, Refinery		, 12);
		addToBuildOrder( buildOrderStruct, SD			, 15);
		addToBuildOrder( buildOrderStruct, Factory		, 16); // take two scvs off gas
		addToBuildOrder( buildOrderStruct, MachineShop	, 20); // put two scvs back on gas
		addToBuildOrder( buildOrderStruct, SD			, 23);
		addToBuildOrder( buildOrderStruct, CC			, 28);
		addToBuildOrder( buildOrderStruct, SD			, 28);
		addToBuildOrder( buildOrderStruct, Factory		, 32);
		addToBuildOrder( buildOrderStruct, UnitType.Special_Terran_Flag_Beacon, 500);
	}
	
	public static void initOneRaxFE( List<Pair<UnitType,Integer>> buildOrderStruct ) {
		addToBuildOrder( buildOrderStruct, SD			, 9);
		addToBuildOrder( buildOrderStruct, Barracks		, 11);
		addToBuildOrder( buildOrderStruct, CC			, 16);
		addToBuildOrder( buildOrderStruct, Refinery		, 16);	
		addToBuildOrder( buildOrderStruct, SD			, 16);
		addToBuildOrder( buildOrderStruct, Factory		, 21);
		addToBuildOrder( buildOrderStruct, SD			, 28);
		addToBuildOrder( buildOrderStruct, MachineShop	, 30);
		addToBuildOrder( buildOrderStruct, Factory		, 32);
		addToBuildOrder( buildOrderStruct, MachineShop	, 39);
		addToBuildOrder( buildOrderStruct, UnitType.Special_Terran_Flag_Beacon, 500);
	}
	
	// NEW FUNCS HERE
	public static void addToBuildOrder( List<Pair<UnitType,Integer>> buildOrderStruct, 
			UnitType uT, Integer supply) {
		buildOrderStruct.add( new Pair<UnitType,Integer>(uT,supply) );
	}
}
