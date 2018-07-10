package structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.Resources;

public class BuildingOrder {

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType Barracks = UnitType.Terran_Barracks;
	static UnitType Refinery = UnitType.Terran_Refinery;
	static UnitType Factory = UnitType.Terran_Factory;
	static UnitType MachineShop = UnitType.Terran_Machine_Shop;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	public static void initializeBuildOrder( List<Pair<UnitType,Integer>> buildOrderStruct ) {

		// default: 1 Face FE
		initOneFactFE( buildOrderStruct );
		
		// so game doesn't crash when build order is finished
		addToBuildOrder( buildOrderStruct, UnitType.Special_Terran_Flag_Beacon, 500);
	}
	
	public static void addToBuildOrder( List<Pair<UnitType,Integer>> buildOrderStruct, 
			UnitType uT, Integer supply) {
		buildOrderStruct.add( new Pair<UnitType,Integer>(uT,supply) );
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
	}
	
	// NEW FUNCS HERE
	public static boolean checkIfSupplyMet( Resources bResources, Integer structSupply ) {
		return bResources.getSupplyUsed() >= structSupply;
	}
	
	public static void setNextOrderNeg( List<Pair<UnitType,Integer>> buildOrderStruct ) {
		buildOrderStruct.set(0, new Pair<UnitType,Integer>( buildOrderStruct.get(0).first, 
				buildOrderStruct.get(0).second*-1) );
	}
	public static boolean isNextOrderNeg( List<Pair<UnitType,Integer>> buildOrderStruct ) {
		return buildOrderStruct.get(0).second < 0;
	}

}
