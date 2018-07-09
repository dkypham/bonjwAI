package army;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitFilter;
import bwapi.UnitType;
import bwta.BaseLocation;
import economy.WorkerManager;
import map.MapInformation;

/**
 * This class controls army functions: rallying, individual unit behavior (ex: tank seigeing,
 * medic tagging), attacking.
 * @author Duc Pham
 * 
 * 7/4 - worker defense
 * 
 * Have an underAttack flag. On every frame, iterate over all units and check if any are being attacked.
 * If so, check if workers are being attacked. For those workers, check what's attacking it. If it is 
 * an opponent's miner, then send a designated defender SCV to attack that worker.
 * Check if under attack SCV is constructing a struct or mining.
 * 	- If constructing a struct, have onUnitDeath check if we need to send a new SCV to finish building
 * 	- If miner, then have a designated repair SCV repair damaged SCV.
 *	
 * 
 * asdf
 */
public class ArmyManager {

	public static int dFromPoint = 200;
	public static UnitType marine = UnitType.Terran_Marine;
	public static UnitType medic = UnitType.Terran_Medic;	
	public static UnitType tank = UnitType.Terran_Siege_Tank_Tank_Mode;
	
	public static int MEDIC_MIN_DISTANCE = 10;
	
	/**
	 * 
	 * @param game
	 * @param self
	 * @param bArmyMap
	 * @param bStructMap
	 * @param bBasePos
	 * @param enemyBuildingMemory
	 * @param rallyPoints
	 */
	public static void updateArmyManager(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap, 
			Multimap<String, Integer> bRolesMap,
			Multimap<UnitType, Integer> bStructMap,
			List<BaseLocation> bBasePos, 
			ArrayList<Position> enemyBuildingMemory,
			List<Pair<Position, Position>> rallyPoints ) {
		
		int rallyPointMode = updateRallyPoint(bStructMap);
		
		//boolean underAttack = updateUnderAttack(game, bArmyMap, bStructMap );
		/*
		 * underAttack ||
		 * 	0b1000 = (general) underAttack somewhere
		 * 	0b0100 = workers under attack
		 * 	0b0010 = army under attack
		 * 	0b0001 = structure under attack
		 */
		int underAttack = 0b000;
		
		// also update underAttackUnits array, all units currently under attack
		ArrayList<Integer> underAttackArray = new ArrayList<>();
		
		// updates underAttackArray as well
		underAttack = updateUnderAttack( game, bArmyMap, bStructMap, underAttackArray ); 
		boolean shouldAttack = updateShouldAttack(bArmyMap);
		
		// general base defense
		if ( checkIfUnderAttackGeneral(underAttack) ) {
			// if and SCV is under attack, check what is attacking it
			if ( checkIfUnderAttackSCV(underAttack) ) {
				if ( underAttackArray.size() == 1 ) {
					Unit underAttackSCV = game.getUnit(underAttackArray.get(0));
					List<Unit> nearbyEnemies = getNearbyEnemies( game, self, underAttackSCV.getPosition() );
					
					// determine if SCV is scouting at enemy base or mining at bonjwAI base
					// based on number of enemies seen (more than 1 = scouting, 1 = bonjwAI base)
					if ( nearbyEnemies.size() > 1 ) {
						
					}
					else if ( nearbyEnemies.size() == 1 ) {
						Unit enemyScout = nearbyEnemies.get(0);
						if ( MapInformation.isWorker( enemyScout ) ) {
							WorkerManager.defendAgainstSCout(game, bRolesMap, underAttackSCV, enemyScout);
							//System.out.println("getting attacked by enemy scout worker");
						}
					}
				} // end of if 1 enemy is attacking
			} // end of SCV is under attack
		} // end of underAttack is true
		
		else { // not under Attack, rally
			
		}
		
		//updateMarines(game, self, bArmymMap, bBasePos, enemyBuildingMemory, underAttack, rallyPointMode, 
		//		rallyPoints);
		//updateTanks(game, self, bArmymMap, bBasePos, enemyBuildingMemory, underAttack, rallyPointMode, 
		//		rallyPoints);
		updateArmy(game, self, bArmyMap, bBasePos, enemyBuildingMemory, underAttack, rallyPointMode, 
				rallyPoints, shouldAttack);
	}
	
	public static List<Unit> getNearbyEnemies( Game game, Player self, Position p ) {
		List<Unit> nearbyEnemies = new ArrayList<Unit>();
		for ( Unit nearbyU : game.getUnitsInRadius(p, 500) ) {
			if ( nearbyU.getPlayer().isEnemy(self) ) {
				nearbyEnemies.add( nearbyU );
			}
		}
		return nearbyEnemies;
	}
	
	private static int updateRallyPoint( Multimap<UnitType, Integer> bStructMap ) {
		// 0 means go to first rally point
		// 1 means go to second rally point...
		
		if ( bStructMap.get(UnitType.Terran_Command_Center ).size() == 2 ) {
			return 1; // 2nd rally point
		}
		
		return 0;
	}

	public static void updateArmy(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			List<BaseLocation> bBasePos,
			ArrayList<Position> enemyBuildingMemory,
			int underAttack, int rallyPointMode, List<Pair<Position, Position>> rallyPoints,
			boolean shouldAttack ) {
		
		List<Integer> armyList = new ArrayList<Integer>();
		armyList.addAll( bArmyMap.get(marine));
		armyList.addAll( bArmyMap.get(tank));
		
		for ( Integer armyUnitID : armyList ) {
			Unit uArmy = game.getUnit(armyUnitID);
			
			if ( !uArmy.canMove() ) {
				continue;
			}
			// if not under attack, go to rally point
			if ( checkIfUnderAttackGeneral(underAttack) ) {
				
				// if unit is attacking
				if ( shouldAttack == true ) {
					if ( !enemyBuildingMemory.isEmpty() ) {
						uArmy.attack( enemyBuildingMemory.get(0) );
					}
				}
				
				// if unit is not moving
				// rally, but do not interrupt current command
				else if ( !uArmy.isMoving() ) {
					// rallypoint 0
					if ( rallyPointMode == 0 ) {
						Position rallyPoint = MapInformation.retCenterOfPair(rallyPoints.get(0));
						if ( !MapInformation.checkIfInRegion( uArmy.getPosition() , rallyPoints.get(0) )) {
							uArmy.move( rallyPoint );
						}
					}
					// rallypoint 1
					else if ( rallyPointMode == 1 ) {
						Position rallyPoint = MapInformation.retCenterOfPair(rallyPoints.get(1));
						if ( !MapInformation.checkIfInRegion( uArmy.getPosition() , rallyPoints.get(1) )) {
							uArmy.move( rallyPoint );
						}
						
						// if unit is in rallypoint 1
						if ( MapInformation.checkIfInRegion( uArmy.getPosition() , rallyPoints.get(1) )) {
							// if unit is a tank
							if ( uArmy.getType() == UnitType.Terran_Siege_Tank_Tank_Mode ) {
								// if can siege, then siege
								if ( uArmy.canSiege() ) {
									uArmy.siege();
								}
							}
						}
						
					}
				}
				
				// 
			}

		}
	}
	
	/**
	 * Check bArmyMap to see if certain unit counts are met. Currently,
	 * 	- return true if marineCount > 10 and tankCount > 10
	 *  - else return false
	 * @param bArmyMap
	 * @return
	 */
	private static boolean updateShouldAttack(Multimap<UnitType, Integer> bArmyMap) {
		if ( bArmyMap.get(marine).size() > 10 && bArmyMap.get(tank).size() > 10 ) {
			return true;
		}
		return false;
	}
	
	// should have a target (i.e. rally target, defend target, attack target)
	// define micro behavior towards target
	public static void updateMarines(Game game, Player self, ArrayList<Integer> marines, Position target ) {
		//List<Integer> marineIDMap = (List<Integer>) bArmyMap.get(marine);

		for ( Integer marineID : marines ) {
			//Unit uMarine = game.getUnit(marineID);
				System.out.println("test");
			// action specifically for marine

		}
	}
	
	public void updateMedics(Game game, ArrayList<Medic> medics ) {
		for (Medic medic : medics ) {
			Unit uMedic = game.getUnit(medic.ID);
			if ( !uMedic.isFollowing() || medic.getTarget() == null || !medic.getTarget().exists() ) {
				medic.setHealTarget( getNearestMarine(game, uMedic, 10000) );
				uMedic.follow(medic.getTarget());
				System.out.println("assigned targegt for medic");
			}
		}
	}
	
	public static void updateTanks(Game game, Player self,
			Multimap<UnitType, Integer> bArmyMap,
			List<BaseLocation> bBasePos,
			ArrayList<Position> enemyBuildingMemory,
			boolean underAttack, int rallyPointMode, List<Pair<Position, Position>> rallyPoints ) {
		
		List<Integer> tankIDmap = (List<Integer>) bArmyMap.get(tank);

		for ( Integer tankID : tankIDmap ) {
			Unit uTank = game.getUnit(tankID);
			// action specifically for tanks

		}
	}
	
	public static Unit getNearbyEnemies(Player self, Unit unit, int dist) {
		for ( Unit potEnemy : unit.getUnitsInRadius(dist)) {
			if ( self.isEnemy(potEnemy.getPlayer())) {
				return potEnemy;
			}
		}
		System.out.println("ArmyManager:getNearbyEnemies error: no enemy found");
		return null;
	}
	
	public Unit getNearestMarine(Game game, Unit unit, int dist) {
		ArrayList<Unit> marines = new ArrayList<Unit>();
		for ( Unit u : game.getAllUnits() ) {
			if (u.getType() == UnitType.Terran_Marine) {
				marines.add(u);
			}
		}
		
		int minDistance = -1;
		Unit closestMarine = null;
		
		
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
	
	/**
	 * Go through all units in bArmyMap. 
	 * Return true if any units or structs are under attack, false if not.
	 * 
	 * @param game
	 * @param self
	 * @param bArmyMap
	 */
	public static int updateUnderAttack( Game game, Multimap<UnitType,Integer> bArmyMap,
			Multimap<UnitType,Integer> bStructMap, ArrayList<Integer> underAttackArray ) {
		// clear array before updating
		underAttackArray.clear();
		
		int underAttack = 0b0000;
		for (Integer uID : bArmyMap.values() )  {		
			
			if ( game.getUnit(uID).isUnderAttack() ) {
				underAttackArray.add( uID );
				//System.out.println("added to under attack units");
				if ( game.getUnit(uID).getType() == UnitType.Terran_SCV ) {
					underAttack = underAttack | 0b1100;	// general and SCVs
				}
				else {
					underAttack = underAttack | 0b1010; // general and army
				}
			}
		}
		for (Integer uID : bStructMap.values() )  {
			if ( game.getUnit(uID).isUnderAttack() ) {
				underAttack = underAttack | 0b1001;	// general and structure
			}
		}
		//System.out.println("Underattack value: " + underAttack);
		return underAttack;
	}
	
	// bitwise flag checkers
	// return true if is under attack
	public static boolean checkIfUnderAttackGeneral( int underAttack ) {
		return ((underAttack & (1 << 3)) != 0 );
	}
	
	public static boolean checkIfUnderAttackSCV( int underAttack ) {
		return ((underAttack & (1 << 2)) != 0 );
	}
	
	public static boolean checkIfUnderAttackArmy( int underAttack ) {
		return ((underAttack & (1 << 1)) != 0 );
	}
	
	public static boolean checkIfUnderAttackStructure( int underAttack ) {
		return ((underAttack & (1 << 0)) != 0 );
	}
}
	
