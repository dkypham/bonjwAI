package b.army;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import b.ai.BonjwAI;
import b.ai.BonjwAIGame;
import b.structure.BuildingPlacement;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class ArmyManager {
	private static ArmyManager armyManager = new ArmyManager();
	private Game game;
	private static Player self;
	
	private ArmyManager() {
		game = BonjwAI.mirror.getGame();
		self = game.self();
	}
	
	public static ArmyManager getInstance() {
		return armyManager;
	}
	
	public static int dFromPoint = 200;
	public static UnitType marine = UnitType.Terran_Marine;
	public static UnitType medic = UnitType.Terran_Medic;	
	
	public static int MEDIC_MIN_DISTANCE = 10;
	
	// whole army handler
	public static void updateArmyManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmymMap, 
			Multimap<UnitType, Integer> bStructMap,
			List<BaseLocation> bBasePos, 
			ArrayList<Position> enemyBuildingMemory,
			List<Position> rallyPoints ) {
		int rallyPointMode = updateRallyPoint(bStructMap);
		
		boolean underAttack = BonjwAIGame.updateUnderAttack(game, bArmymMap, bStructMap);
		updateMarines(game, self, bArmymMap, bBasePos, enemyBuildingMemory, underAttack, rallyPointMode, 
				rallyPoints);
	}
	
	private static int updateRallyPoint( Multimap<UnitType, Integer> bStructMap ) {
		// 0 means go to first rally point
		// 1 means go to second rally point...
		
		if ( bStructMap.get(UnitType.Terran_Command_Center ).size() == 2 ) {
			return 1; // 2nd rally point
		}
		
		return 0;
	}

	public static void updateMarines(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			List<BaseLocation> bBasePos,
			ArrayList<Position> enemyBuildingMemory,
			boolean underAttack, int rallyPointMode, List<Position> rallyPoints ) {
		
		List<Integer> marineIDMap = (List<Integer>) bArmyMap.get(marine);

		for ( Integer marineID : marineIDMap ) {
			Unit uMarine = game.getUnit(marineID);

			// if not under attack, go to rally point
			if ( underAttack == false ) {
				if ( rallyPointMode == 0 ) {
					if ( BWTA.getGroundDistance( uMarine.getTilePosition() , rallyPoints.get(0).toTilePosition() ) > 50) {
						uMarine.attack( rallyPoints.get(0) );
					}
				}
				else if ( rallyPointMode == 1 ) {
					if ( BWTA.getGroundDistance( uMarine.getTilePosition() , rallyPoints.get(1).toTilePosition() ) > 50) {
						uMarine.attack( rallyPoints.get(1) );
					}
				}
			}

		}
	}
	
	public void updateMedics(
			ArrayList<Medic> medics ) {
		for (Medic medic : medics ) {
			Unit uMedic = game.getUnit(medic.ID);
			if ( !uMedic.isFollowing() || medic.getTarget() == null || !medic.getTarget().exists() ) {
				medic.setHealTarget( getNearestMarine(uMedic, 10000) );
				uMedic.follow(medic.getTarget());
				System.out.println("assigned targegt for medic");
			}
		}
	}
	
	public static Unit getNearbyEnemies( Unit unit, int dist) {
		for ( Unit potEnemy : unit.getUnitsInRadius(dist)) {
			if ( self.isEnemy(potEnemy.getPlayer())) {
				return potEnemy;
			}
		}
		System.out.println("ArmyManager:getNearbyEnemies error: no enemy found");
		return null;
	}
	
	public Unit getNearestMarine(Unit unit, int dist) {
		ArrayList<Unit> marines = new ArrayList<Unit>();
		for ( Unit u : game.getAllUnits() ) {
			if (u.getType() == UnitType.Terran_Marine) {
				marines.add(u);
			}
		}
		
		int minDistance = -1;
		int maxDistance = dist;
		Unit closestMarine = null;
		
		if ( marines == null ) {
			// break
		}
		
		for ( Unit marine : marines ) {
			// don't follow a marine being constructed and is already being followed
			if ( marine.isCompleted()) {
				if ( marine.getDistance(unit) < minDistance || minDistance == -1 ) {
					minDistance = marine.getDistance(marine);
					closestMarine = marine;
				}
			}
		}
		return closestMarine;
	}
}
	
