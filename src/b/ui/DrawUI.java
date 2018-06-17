package b.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.ai.BonjwAIGame;
import b.economy.WorkerManager;
import b.idmap.MapUnitID;
import b.map.MapDraw;
import b.map.MapInformation;
import bwapi.Color;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class DrawUI {

	public static void updateUI(Game game, Player self, Multimap<UnitType, Integer> bArmyMap, 
			Multimap<UnitType, Integer> bStructMap, ArrayList<Position> eBasePos,
			ArrayList<BaseLocation> bBasePos,
			ArrayList<Integer> bResources,
			List<UnitType> buildOrderStruct,
			List<Integer> buildOrderSupply,
			List<TechType> techTreeTech,
			List<Integer> techTreeSupply,
			List<Position> drawStructPos,
			List<String> drawStructLabel,
			int productionMode, 
			int[] timeBuildIssued,
			List<Pair<Position, Position>> miningRegionsList ) {
		
		// Column 1
		
		// Row 1+2: Name + APM
		game.drawTextScreen(10, 10, "Playing as " + "bonjwAI" + " - " + self.getRace());
		game.drawTextScreen(10, 20, "APM: " + game.getAPM() );
		
		// Row 3-8: bResources 
		//game.drawTextScreen(10, 40, "aM: " + bResources.get(0) );
		//game.drawTextScreen(10, 50, "rM: " + bResources.get(1) );
		//game.drawTextScreen(10, 60, "aG: " + bResources.get(2) );
		//game.drawTextScreen(10, 70, "rG: " + bResources.get(3) );
		//game.drawTextScreen(10, 80, "aS: " + bResources.get(4) );
		//game.drawTextScreen(10, 90, "eS: " + bResources.get(5) );
		
		// Rows 3-8 now for testing info
		//game.drawTextScreen( 10,  40, "Is first expo explored?: " + MapInformation.checkIfExpoIsExplored(game, bBasePos.get(1) ) );
		game.drawTextScreen( 10,  40, "current game time: " + game.elapsedTime() );
		game.drawTextScreen( 10,  50, "timeBuildIssued: " + timeBuildIssued[0] );
		
		
		Pair<Integer,Integer> mainBaseWorkersDist = WorkerManager.getNumWorkersInBase(game, self, miningRegionsList.get(0), bArmyMap);
		game.drawTextScreen( 10,  60, "main base mineral miners: " + mainBaseWorkersDist.first);
		game.drawTextScreen( 10,  70, "main base gas miners: " + mainBaseWorkersDist.second);		
		
		if ( miningRegionsList.size() == 2 ) { 
			Pair<Integer,Integer> secondBaseWorkersDist = WorkerManager.getNumWorkersInBase(game, self, miningRegionsList.get(1), bArmyMap);
			game.drawTextScreen( 10,  80, "second base mineral miners: " + secondBaseWorkersDist.first);
			game.drawTextScreen( 10,  90, "second base gas miners: " + secondBaseWorkersDist.second);	
		}
		
		// Row 9-10: buildOrderStruct + buildOrderSupply
		drawNextBuilding(game, buildOrderStruct, buildOrderSupply);
		drawNextTech(game, techTreeTech, techTreeSupply);
		
		// Row 12:
		game.drawTextScreen(10, 130, "Number of eBuildings seen: " + eBasePos.size());
		
		// Row 13-16: Gas Miner IDs 
		int offset = 10;
		for( int ID : bArmyMap.get(UnitType.Powerup_Terran_Gas_Tank_Type_1) ) {
			game.drawTextScreen(10, 140 + offset, "Gas Miner: " + Integer.toString(ID) );
			offset += 10;
		}		
		
		//game.drawTextScreen(10,  160, "Production Mode: " + productionMode );
		
		// Column 2: Unit counts
		drawUnitCounts(game, self, bArmyMap);
		
		// Column 3: Building counts
		drawUnitIDs(game, bArmyMap, bStructMap);
		
		
		// In game: draw unit vectors
		drawInfo(game, self, bArmyMap, bStructMap);
		// In game: draw build order zones
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
	
	public static void drawNextTech(Game game, List<TechType> techTreeTech,
			List<Integer> techTreeSupply) {
		game.drawTextScreen(10, 110, "Next tech: " + techTreeTech.get(0) + " at " + 
				techTreeSupply.get(0) + " supply.");
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
