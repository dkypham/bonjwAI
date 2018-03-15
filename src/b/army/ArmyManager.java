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
			Multimap<UnitType, Integer> armyMap, 
			Multimap<UnitType, Integer> structMap,
			List<BaseLocation> myBases, 
			ArrayList<Position> enemyBuildingMemory,
			ArrayList<Medic> medics ) {
		boolean underAttack = BonjwAIGame.updateUnderAttack(game, armyMap, structMap);
		updateMarines(game, self, armyMap, myBases, enemyBuildingMemory, underAttack);
	}
	
	public static void updateMarines(Game game, Player self,
			Multimap<UnitType, Integer> armyMap,
			List<BaseLocation> myBases,
			ArrayList<Position> enemyBuildingMemory,
			boolean underAttack ) {
		List<Integer> marineIDMap = (List<Integer>) armyMap.get(marine);
		for ( Integer marineID : marineIDMap ) {
			Unit uMarine = game.getUnit(marineID);
			
			// if marine has command issued, break
			if ( !uMarine.isIdle() ) {
				break;
			}
			
			// step back if during attack frame
			if (uMarine.isAttacking() ) {
				uMarine.stop();
				uMarine.move(myBases.get(0).getPosition());
				System.out.println("Issued stop command");
			}
			
			// if idle, then issue commands
			if ( uMarine.isIdle() ) {
				// if under attack
				if ( underAttack == true ) {
					Unit enemy = getNearbyEnemies(uMarine, 1000);
					if ( enemy != null ) {
						uMarine.attack(enemy.getPosition());
					}
				}	
				// if not under attack, but should attack
				else if ( marineIDMap.size() > 30 ) {
					if ( !uMarine.isAttacking() ) {
						for ( Position enemyPos : enemyBuildingMemory ) {
							uMarine.attack(enemyPos);
							break;
						}
					}
				}
				// if not under attack and should not attack
				else if ( uMarine.getDistance( myBases.get(1).getPosition()) > dFromPoint
						&& !uMarine.isAttacking()
						&& !uMarine.isUnderAttack() ) {
					uMarine.attack(myBases.get(1).getPosition());
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
	
