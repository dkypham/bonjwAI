package b.map;

import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class MapDraw {

	public static void drawMapInformation(Game game, List<BaseLocation> bBasePos, 
			List<BaseLocation> eBasePos, List<Pair<Position,Position>> miningRegionsList,
			List<Pair<Position, Position>> rallyPoints ) {
		drawBBasePos(game, bBasePos);
		if (eBasePos.size() != 0) {
			MapDraw.drawEBasePos(game, eBasePos);
		}
		drawResourceZone(game, miningRegionsList);
		//drawAllChokepoints(game);
		drawRallyPoints( game, rallyPoints );
	}
	
	public static void drawBBasePos(Game game, 
			List<BaseLocation> bBasePos) {
		game.drawTextMap(bBasePos.get(0).getX(), bBasePos.get(0).getY() - 10, "bMain");
		game.drawTextMap(bBasePos.get(1).getX(), bBasePos.get(1).getY() - 10, "bFirstExpo");
		game.drawTextMap(bBasePos.get(2).getX(), bBasePos.get(2).getY() - 10, "bSecondExpo");
	}
	
	public static void drawEBasePos(Game game, 
			List<BaseLocation> eBasePos) {
		game.drawTextMap(eBasePos.get(0).getX(), eBasePos.get(0).getY() - 10, "eMain");
		game.drawTextMap(eBasePos.get(1).getX(), eBasePos.get(1).getY() - 10, "eFirstExpo");
		game.drawTextMap(eBasePos.get(2).getX(), eBasePos.get(2).getY() - 10, "eSecondExpo");
	}
	
	public static void drawResourceZone(Game game, List<Pair<Position,Position>> miningRegionsList ) {
		game.drawBoxMap( miningRegionsList.get(0).first, miningRegionsList.get(0).second, Color.Green );
		
		if ( miningRegionsList.size() == 2 ) {
			//System.out.println("Drawing 2nd mining region");
			game.drawBoxMap( miningRegionsList.get(1).first, miningRegionsList.get(1).second, Color.Blue );
		}
		
	}
	
	public static void drawBuildingAreas(Game game, List<BaseLocation> myBases) {
		
	}
	
	// given top left TP, get position of bottom right TP so that we can use game.drawBoxScreen
	public static Position getBottomRightBuildZonePos( TilePosition tpos, int right, int below ) {
		TilePosition tempTpos = new TilePosition( tpos.getX() + right, tpos.getY() + below );
		return tempTpos.toPosition();
	}
	
	public static void drawAllChokepoints( Game game ) {
		for ( Chokepoint chokepoint : BWTA.getChokepoints() ) {
			game.drawTextMap( chokepoint.getPoint(), "Chokepoint");
		}
	}
	
	public static void drawRallyPoints( Game game, List<Pair<Position, Position>> rallyPoints ) {
		for ( Pair<Position,Position> pair : rallyPoints ) {
			game.drawBoxMap( pair.first, pair.second, Color.Red );
			game.drawTextMap( MapInformation.retCenterOfPair(pair), "rallyPoint" );
		}
	}
	
	public static void drawChokePointRegion ( Game game, List<Chokepoint> chokepointList ) {
		for ( Chokepoint choke : chokepointList ) {
			Position leftSide = choke.getSides().first;
			Position rightSide = choke.getSides().second;
			game.drawBoxMap( new Position( leftSide.getX() - 60, leftSide.getY() - 60 ),
					new Position( rightSide.getX() + 60, rightSide.getY() + 60 ), Color.Green );
		}
	}
}
