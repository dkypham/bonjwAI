package b.structure;

import java.util.ArrayList;
import java.util.List;

import bwapi.UnitType;

public class BuildingOrder {

	static UnitType SD = UnitType.Terran_Supply_Depot;
	static UnitType Barracks = UnitType.Terran_Barracks;
	static UnitType Refinery = UnitType.Terran_Refinery;
	static UnitType Factory = UnitType.Terran_Factory;
	static UnitType MachineShop = UnitType.Terran_Machine_Shop;
	
	public static void initializeBuildOrder(List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply ) {
		// default implementation: 1 Fact FE
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 8);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Barracks, 12);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Refinery, 12);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, SD, 15);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, Factory, 16);
		addToBuildOrder( buildOrderStruct, buildOrderSupply, MachineShop, 20);
	}
	
	public static void addToBuildOrder(List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			UnitType uT,
			Integer supply) {
		buildOrderStruct.add(uT);
		buildOrderSupply.add(supply);
	}
}
