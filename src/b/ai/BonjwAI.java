package b.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.army.ArmyManager;
import b.army.Medic;
import b.economy.ResourceManager;
import b.economy.SupplyManager;
import b.economy.WorkerManager;
import b.idmap.MapUnitID;
import b.map.MapDraw;
import b.map.MapInformation;
import b.map.ScoutManager;
import b.structure.BuildingManager;
import b.structure.BuildingPlacement;
import b.ui.DrawUI;
import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class BonjwAI extends DefaultBWListener {

	public static Mirror mirror = new Mirror();
	private Game game;
	private Player self;
	
	public static BuildingPlacement bpInstance;
	public static ArmyManager armyManager;
	
	private Multimap<UnitType, Integer> bArmyMap = ArrayListMultimap.create();
	private Multimap<UnitType, Integer> bStructMap = ArrayListMultimap.create();
	private ArrayList<Position> eStructPos = new ArrayList<Position>();
	private ArrayList<BaseLocation> bBasePos = new ArrayList<BaseLocation>();
	private ArrayList<BaseLocation> eBasePos = new ArrayList<BaseLocation>();
	private ArrayList<Integer> bResources = new ArrayList<Integer>();
	private ArrayList<Medic> medics = new ArrayList<Medic>();
	
	private List<Integer> resourceZone = new ArrayList<Integer>();
	private List<Integer> buildZone = new ArrayList<Integer>();
	
	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	@Override
	public void onStart() {
		game = mirror.getGame();
		self = game.self();
		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();
		BWTA.buildChokeNodes();
		System.out.println("Map data ready");
		
		// Enable user input
		game.enableFlag(1);
		// Enable map hack
		// game.sendText("black sheep wall");
		//game.sendText("power overwhelming");
		
		game.setLocalSpeed(15);
		
		bpInstance = BuildingPlacement.getInstance();
		armyManager = ArmyManager.getInstance();
		
		BonjwAIGame.initializeArmyMap(bArmyMap);
		BonjwAIGame.initializeStructMap(bStructMap);
		MapInformation.findNearBasePos(game, bBasePos);	
		
		System.out.println ( MapInformation.findMineralSetup(game, bBasePos.get(0)) );
	}

	public void onUnitMorph(Unit u) {
		if ( u.getType() == UnitType.Terran_Refinery ) {
			MapUnitID.addToIDMap(bStructMap, u);
		}
		if ( u.getType() == UnitType.Resource_Vespene_Geyser ) {
			MapUnitID.removeFromIDMap(bStructMap, u);
		}
	}
	
	@Override
	// when a unit is starting to be built
	public void onUnitCreate(Unit u) {
		if (u.getType() == UnitType.Terran_Medic) {
			Medic medic = new Medic(u.getID());
			medics.add(medic);
			System.out.println("size of medics: " + medics.size() );
		}
		switch (BonjwAIGame.switchFlags(self, u)) {
		case 1:
			MapUnitID.addToIDMap(bArmyMap, u);
			break;
		case 2:
			MapUnitID.addStructToIDMap(bStructMap, u);
			break;
		default:
			break;
		}
	}

	public void onUnitDestroy(Unit u) {
		switch (BonjwAIGame.switchFlags(self, u)) {
		case 1:
			MapUnitID.removeFromIDMap(bArmyMap, u);
			break;
		case 2:
			MapUnitID.removeFromIDMap(bStructMap, u);
			break;
		default:
			break;
		}
	}

	@Override
	public void onFrame() {	
		
		// b.economy driver
		
		// Resource Manager:
		/*
		 * Variables:
		 * --bResources	>	array holding self resource values
		 * 		0	>	actual minerals
		 * 		1	>	actual gas
		 * 		2	>	reserved minerals (queued for construction)
		 * 		3	>	reserved gas (queued for construction)	
		 * 		4	>	actual supply
		 * 		5	>	reserved supply (queued for construction)
		 * 
		 * Iterative functions:
		 * --clearBResources()	>	clear array of all values (reset)
		 * --updateBRresources()	>	calculate each index value
		 * --getReservedMinerals()	>	calculated reserved minerals
		 * 
		 */
		ResourceManager.updateResources(game, self, bResources, bArmyMap);
		
		// Scout Manager:
		/*
		 * Variables:
		 * --eStructPos	>	array holding position of enemy structures
		 * 		contains any buildings seen, no specific ordering
		 * 
		 * --eBasePos	>	array holding base/expansion positions of enemy
		 * 		0	>	enemy main base
		 * 		1 	>	enemy first expo
		 * 		2	> 	enemy second expo
		 * 		..	>	..
		 * 
		 * Iterative functions:
		 * --scoutForUnknownEnemy	> if eStructPos is empty, then scout for an enemy struct
		 * 
		 */
		ScoutManager.updateScoutManager(game, self, bArmyMap, eStructPos, eBasePos);

		// Supply Manager:
		/*
		 * Variables:
		 * --
		 * 
		 * Iterative functions:
		 * --needSupplyCheck()	>	step function determining if supply needs to be built
		 */
		SupplyManager.updateSupplyManager(game, self, bArmyMap, bStructMap , bResources);

		// Building Manager:
		/*
		 * Variables:
		 * --
		 * 
		 * Iterative functions:
		 * --buildWorkers()	>	build workers based on number of command centers
		 * --refineryManager()	>	manage number of gas miners per refinery
		 * --academyManager()	>	build one academy when conditions are met
		 * --barracksManager()	>	build five barracks when conditions are met
		 * --factoryManager()	>	build two factories when conditions are met
		 * --buildingProduction()	>	build marines/medics when conditions are met
		 */
		BuildingManager.buildingManager(game, self, bArmyMap, bStructMap, bResources);
		
		// Army Manager:
		/*
		 * Variables:
		 * --underAttack	>	flag that checks if any unit is under attack
		 * 
		 * Iterative functions:
		 * --updateMarines()	>	marine targeting and micro
		 * 
		 */
		ArmyManager.updateArmyManager(game, self, bArmyMap, bStructMap, bBasePos, 
				eStructPos, medics);
		//armyManager.updateMedics(medics);
		
		
		// Worker Manager:
		/*
		 * Variables:
		 * -- 
		 * 
		 * Iterative functions:
		 * --makeIdleWorkersMine()	>	make idle SCVS mine minerals
		 * --makeWorkersMineGas()	> 	count number of gas miners, then assign some if low
		 */
		WorkerManager.updateWorkerManager(game, self, bArmyMap, bStructMap);
		
		
		//MapInformation.initMapInfo(game, bStructMap, resourceZone, buildZone);
		//MapDraw.drawMapInformation(game, bBasePos, eBasePos, resourceZone);

		//DrawUI.updateUI(game, self, bArmyMap, bStructMap, eStructPos, bResources);
		
		// test
				
	}

	public void onEnd() {

	}

}