package ui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Color;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.WorkerManager;
import economy.Base;
import economy.Resources;
import economy.SupplyManager;
import idmap.MapUnitID;
import map.MapDraw;
import structure.BuildOrder;

public class DrawUI {

	public static void updateUI(Game game, Player self, Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap, ArrayList<Position> eBasePos,
			ArrayList<BaseLocation> bBasePos,
			Resources bResources,
			int[] productionMode, 
			int[] timeBuildIssued,
			List<Pair<Position, Position>> miningRegionsList,
			List<Base> bBases, BuildOrder bBuildOrder ) {
		
		// Column 1
		
		// Row 1+2: Name + APM
		game.drawTextScreen(10, 10, "Playing as " + "bonjwAI" + " - " + self.getRace());
		game.drawTextScreen(10, 20, "APM: " + game.getAPM() );
		
		// Row 3-8: bResources 
		game.drawTextScreen(10, 40, "mins Actual: " + bResources.getMinsActual() );
		game.drawTextScreen(10, 50, "mins Effective: " + bResources.getMinsEffective() );
		game.drawTextScreen(10, 60, "gas Actual: " + bResources.getGasActual() );
		game.drawTextScreen(10, 70, "gas Effective: " + bResources.getGasEffective() );
		game.drawTextScreen(10, 80, "supply total Actual: " + bResources.getSupplyTotalActual() );
		game.drawTextScreen(10, 90, "supply total Effective: " + bResources.getSupplyTotalEffective() );
		game.drawTextScreen(10, 100, "supply used: " + bResources.getSupplyUsed() );
		
		if ( bBuildOrder.nextIsStruct() ) {
			game.drawTextScreen(10,  120, "bBuildOrder: next struct: " + bBuildOrder.getNextStruct().toString() + 
					" at " + bBuildOrder.getBuildOrder().get(0).getSupply() + " supply.");
			game.drawTextScreen(10,  130, "buildIssued: " + bBuildOrder.getIsBuildIssued() );
			game.drawTextScreen(10,  140, "supplyMet: " + bBuildOrder.checkIfSupplyMet(bResources) );
		}
		else if ( bBuildOrder.nextIsTech() ) {
			game.drawTextScreen(10,  120, "bBuildOrder: next tech: " + bBuildOrder.getNextTech().toString() + 
					" at " + bBuildOrder.getBuildOrder().get(0).getSupply() + " supply.");
			game.drawTextScreen(10,  130, "buildIssued: " + bBuildOrder.getIsBuildIssued() );
			game.drawTextScreen(10,  140, "supplyMet: " + bBuildOrder.checkIfSupplyMet(bResources) );
		}
		else if ( bBuildOrder.nextIsUnit() ) {
			game.drawTextScreen(10,  120, "bBuildOrder: next unit: " + bBuildOrder.getNextUnit().toString() + 
					" at " + bBuildOrder.getBuildOrder().get(0).getSupply() + " supply.");
			game.drawTextScreen(10,  130, "buildIssued: " + bBuildOrder.getIsBuildIssued() );
			game.drawTextScreen(10,  140, "supplyMet: " + bBuildOrder.checkIfSupplyMet(bResources) );
		}
		
		game.drawTextScreen(10, 160, "needSupply: " + SupplyManager.needSupplyCheck(bResources));
		
		drawUnitID(game, bArmyMap);
	}
	
	public static void drawBuildingPlan( Game game,
			List<Position> drawStructPos,
			List<String> drawStructLabel ) {
		game.drawBoxMap(drawStructPos.get(0), 
				MapDraw.getBottomRightBuildZonePos(drawStructPos.get(0).toTilePosition(), UnitType.Terran_Supply_Depot.tileWidth(), UnitType.Terran_Supply_Depot.tileHeight() ), 
				Color.Green);
		game.drawTextMap(drawStructPos.get(0), drawStructLabel.get(0));
		game.drawBoxMap(drawStructPos.get(1), 
				MapDraw.getBottomRightBuildZonePos(drawStructPos.get(1).toTilePosition(), UnitType.Terran_Barracks.tileWidth(), UnitType.Terran_Barracks.tileHeight() ), 
				Color.Green);
		game.drawTextMap(drawStructPos.get(1), drawStructLabel.get(1));
	}
	
	public static void drawNextTech(Game game, List<Pair<TechType,Integer>> buildOrderTech ) {
		game.drawTextScreen(10, 110, "Next tech: " + buildOrderTech.get(0).first + " at " + 
				buildOrderTech.get(0).second + " supply.");
	}
	
	public static void drawUnitCounts(Game game, Player self, 
			Multimap<UnitType, Integer> bArmyMap ) {		
		int x = 175;
		int y = 20;
		for ( UnitType uT : bArmyMap.keySet() ) {
			game.drawTextScreen(x, y, "Number of " + uT + ": " + MapUnitID.getArmyCount(game, bArmyMap, uT));
			y += 10;
		}
	}
	
	public static void drawUnitIDs(Game game, Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap ) {
		int x = 350;
		int y = 20;
		for ( UnitType uT : bStructMap.keySet() ) {
			game.drawTextScreen(x, y, "Number of " + uT + ": " + 
					MapUnitID.getStructCount(game, bArmyMap, bStructMap, uT));
			y += 10;
		}
	}
	
	public static void drawInfo(Game game, Player self, Multimap<UnitType, Integer> armyMap, 
			Multimap<UnitType, Integer> structMap) {
		drawUnitVectors(game, armyMap);
	}
	
	// Draw vectors from unit to destination
	public static void drawUnitVectors(Game game, Multimap<UnitType, Integer> armyMap) {
		for (Integer uID : armyMap.values() ) {
			Unit myUnit = game.getUnit(uID);
			game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getOrder().toString());
			game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(),
					myUnit.getOrderTargetPosition().getX(), myUnit.getOrderTargetPosition().getY(), bwapi.Color.Black);
		}
	}
	
	public static void drawUnitID(Game game, Multimap<UnitType, Integer> bArmyMap ) {
		for (Integer uID : bArmyMap.values() ) {
			Unit myUnit = game.getUnit(uID);
			game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY()+10, Integer.toString(myUnit.getID()) );
		}
	}
	
}
