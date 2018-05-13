package b.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.ai.BonjwAIGame;
import b.idmap.MapUnitID;
import b.map.MapDraw;
import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class DrawUI {

	public static void updateUI(Game game, Player self, Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap, ArrayList<Position> eBasePos,
			ArrayList<Integer> bResources,
			List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			List<Position> drawStructPos,
			List<String> drawStructLabel,
			int productionMode ) {
		game.drawTextScreen(10, 10, "Playing as " + "bonjwAI" + " - " + self.getRace());
		game.drawTextScreen(10, 20, "APM: " + game.getAPM() );
		
		game.drawTextScreen(10, 40, "aM: " + bResources.get(0) );
		game.drawTextScreen(10, 50, "rM: " + bResources.get(1) );
		game.drawTextScreen(10, 60, "aG: " + bResources.get(2) );
		game.drawTextScreen(10, 70, "rG: " + bResources.get(3) );
		game.drawTextScreen(10, 80, "aS: " + bResources.get(4) );
		game.drawTextScreen(10, 90, "eS: " + bResources.get(5) );
		
		drawNextBuilding(game, buildOrderStruct, buildOrderSupply);
		
		game.drawTextScreen(10, 120, "Number of eBuildings seen: " + eBasePos.size());
		
		int offset = 10;
		for( int ID : bArmyMap.get(UnitType.Powerup_Terran_Gas_Tank_Type_1) ) {
			game.drawTextScreen(10, 120 + offset, "Gas Miner: " + Integer.toString(ID) );
			offset += 10;
		}		
		
		game.drawTextScreen(10,  160, "Production Mode: " + productionMode );
		
		drawUnitCounts(game, self, bArmyMap);
		drawUnitIDs(game, bArmyMap, bStructMap);
		
		drawInfo(game, self, bArmyMap, bStructMap);
	
		drawBuildingPlan(game, drawStructPos, drawStructLabel);
		drawUnitID(game, bArmyMap );
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
	
	public static void drawNextBuilding(Game game, List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply) {
		game.drawTextScreen(10, 100, "Next building: " + buildOrderStruct.get(0) + " at " + 
			buildOrderSupply.get(0) + " supply.");
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
