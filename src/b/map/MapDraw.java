package b.map;

import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BaseLocation;

public class MapDraw {

	public static void drawMapInformation(Game game, List<BaseLocation> bBases, 
			List<BaseLocation> eBases, List<Integer> resourceZone) {
		drawBBasePos(game, bBases);
		if (eBases.size() != 0) {
			MapDraw.drawEBasePos(game, eBases);
		}
		drawResourceZone(game, resourceZone);
	}
	
	public static void drawBBasePos(Game game, 
			List<BaseLocation> myBases) {
		game.drawTextMap(myBases.get(0).getX(), myBases.get(0).getY() - 10, "bMain");
		game.drawTextMap(myBases.get(1).getX(), myBases.get(1).getY() - 10, "bFirstExpo");
		game.drawTextMap(myBases.get(2).getX(), myBases.get(2).getY() - 10, "bSecondExpo");
	}
	
	public static void drawEBasePos(Game game, 
			List<BaseLocation> myBases) {
		game.drawTextMap(myBases.get(0).getX(), myBases.get(0).getY() - 10, "eMain");
		game.drawTextMap(myBases.get(1).getX(), myBases.get(1).getY() - 10, "eFirstExpo");
		game.drawTextMap(myBases.get(2).getX(), myBases.get(2).getY() - 10, "eSecondExpo");
	}
	
	public static void drawResourceZone(Game game, List<Integer> resourceZone) {
		game.drawBoxMap( resourceZone.get(0), resourceZone.get(1), resourceZone.get(2),
				resourceZone.get(3), Color.Green );
	}
	
	public static void drawBuildingAreas(Game game, List<BaseLocation> myBases) {
		
	}
	
	// given top left TP, get position of bottom right TP so that we can use game.drawBoxScreen
	public static Position getBottomRightBuildZonePos( TilePosition tpos, int right, int below ) {
		TilePosition tempTpos = new TilePosition( tpos.getX() + right, tpos.getY() + below );
		return tempTpos.toPosition();
	}
	
}
