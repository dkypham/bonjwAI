package b.structure;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.UnitType;

public class BuildingOrder {

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType Barracks = UnitType.Terran_Barracks;
	static UnitType Refinery = UnitType.Terran_Refinery;
	static UnitType Factory = UnitType.Terran_Factory;
	static UnitType MachineShop = UnitType.Terran_Machine_Shop;
	static UnitType CC = UnitType.Terran_Command_Center;
	
	public static void initializeBuildOrder(List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply ) {
		// default implementation: 1 Fact FE
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 8);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Barracks, 12);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Refinery, 12);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 15);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Factory, 16);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, MachineShop, 20);
		//addToBuildOrder( buildOrderStruct, buildOrderSupply, UnitType.Terran_Siege_Tank_Tank_Mode, 22);
		//addToBuildOrder( buildOrderStruct, buildOrderSupply, TechType.Tank_Siege_Mode, 22);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 23);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, CC, 28);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 28);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Factory, 32);
		
		// so game doesn't crash when build order is finished
		addToBuildOrder( buildOrderStruct, buildOrderSupply, UnitType.Special_Terran_Flag_Beacon, 200);
	}
	
	public static void addToBuildOrder(List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			UnitType uT,
			Integer supply) {
		buildOrderStruct.add(uT);
		buildOrderSupply.add(supply);
	}
	
}
