package b.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
import b.structure.BuildingOrder;
import b.structure.BuildingPlacement;
import b.structure.TechManager;
import b.ui.DrawUI;
import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class BonjwAI extends DefaultBWListener {

	public static Mirror mirror = new Mirror();
	private Game game;
	private Player self;
	
	/* Managers section */
	public static BuildingPlacement bpInstance;
	public static ArmyManager armyManager;
	
	/* Persistent data section */
	// bArmyMap - IDs of all of our army units grouped by UnitType
	private Multimap<UnitType, Integer> bArmyMap = ArrayListMultimap.create();
	// bStructMap - IDs all of our buildings grouped by UnitType
	private Multimap<UnitType, Integer> bStructMap = ArrayListMultimap.create();
	// eStructPos - positions of enemy buildings seen
	private ArrayList<Position> eStructPos = new ArrayList<Position>();
	// bBasePos - list of BaseLocations for bonjwAI to build bases
	private ArrayList<BaseLocation> bBasePos = new ArrayList<BaseLocation>();
	// eBasePos - list of BaseLocations enemy is predicted to build at
	private ArrayList<BaseLocation> eBasePos = new ArrayList<BaseLocation>();
	// bResources - list of bonjwAI's resources defined in ResourceManager
	private ArrayList<Integer> bResources = new ArrayList<Integer>();

	// mineralSetup - tells us the configuration of the minerals relative to CC
	private int mineralSetup = -1;
	
	// resourceZone - coordinates for resources
	private Pair<Position,Position> resourceZone = new Pair<Position,Position>();
	
	private List<Position> drawStructPos = new ArrayList<Position>();
	private List<String> drawStructLabel = new ArrayList<String>();
	
	private List<UnitType> buildOrderStruct = new ArrayList<UnitType>();
	private List<Integer> buildOrderSupply = new ArrayList<Integer>();
	
	private List<TechType> techTreeTech = new ArrayList<TechType>();
	private List<Integer> techTreeSupply = new ArrayList<Integer>();
	
	private List<Position> scoutQueue = new ArrayList<Position>();
	
	private List<Chokepoint> chokepointList = new ArrayList<Chokepoint>();
	
	int[] timeBuildIssued = {0};
	
	// building stuff
	int productionMode = 0; // 0 to start (SCVs only), 1 for SCVS+Marines, 2 for SCVs+Marines+Medics
	
	private List< Pair<Position,Position> > rallyPoints = new ArrayList< Pair<Position,Position> >();
	
	// testing/temp variables
	TilePosition firstSDPos = null;
	
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
		
		/* Enable user input */
		game.enableFlag(1);

		/* Enable hacks for single player testing */
		//game.sendText("black sheep wall");
		//game.sendText("power overwhelming");
		
		/* Enable game speed */
		game.setLocalSpeed(15);
		
		/* Define manager instances here */
		bpInstance = BuildingPlacement.getInstance();
		armyManager = ArmyManager.getInstance();
		
		/* On start functions */
		
		/*
		 * initMapInfo:
		 * -findNearBasePos: populate first 3 indices of bBasePos
		 * -initResourceZone: find nobuild zone
		 * -findMineralSetup: find position of minerals relative to startingCC
		 */
		mineralSetup = MapInformation.initMapInfo(game, bStructMap, bBasePos, mineralSetup);
		System.out.println("Mineral setup: " + mineralSetup);
		resourceZone = MapInformation.initResourceZone2(game, bBasePos.get(0) );
		
		ScoutManager.initializeScoutQueue(scoutQueue, bBasePos );
		BuildingOrder.initializeBuildOrder(buildOrderStruct, buildOrderSupply);
		TechManager.initializeTechOrder(techTreeTech, techTreeSupply);
		BuildingManager.getBuildingPlan(game, self, bArmyMap, bStructMap, drawStructPos, drawStructLabel, mineralSetup, bBasePos);
		MapInformation.initializeRallyPoints( rallyPoints, bBasePos.get(0).getTilePosition(), bBasePos.get(1).getTilePosition() );
	
		// testing
		MapInformation.initializeChokepointList(chokepointList, bBasePos.get(0).getTilePosition());

	}

	public void onUnitMorph(Unit u) {
		if ( u.getType() == UnitType.Terran_Refinery ) {
			MapUnitID.addToIDMap(bStructMap, u);
			if ( buildOrderStruct.get(0) == UnitType.Terran_Refinery ) {
				buildOrderStruct.remove(0);
				buildOrderSupply.remove(0);
			}
		}
		if ( u.getType() == UnitType.Resource_Vespene_Geyser ) {
			MapUnitID.removeFromIDMap(bStructMap, u);
		}
	}
	
	@Override
	// when a unit is starting to be built
	public void onUnitCreate(Unit u) {
		switch (BonjwAIGame.switchFlags(self, u)) {
		case 1:
			MapUnitID.addToIDMap(bArmyMap, u);
			break;
		case 2:
			MapUnitID.addStructToIDMap(bStructMap, u, buildOrderStruct, buildOrderSupply);
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
		 * --clearBResources():	clear array of all values (reset)
		 * --updateBRresources(): calculate each index value
		 * --getReservedMinerals():	calculated reserved minerals
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
		 * --scoutForUnknownEnemy	> 	if eStructPos is empty, then scout for an 
		 * 								enemy struct
		 * 
		 */
		ScoutManager.updateScoutManager(game, self, bArmyMap, eStructPos, eBasePos, bBasePos, scoutQueue);

		// Supply Manager:
		/*
		 * Variables:
		 * --
		 * 
		 * Iterative functions:
		 * --needSupplyCheck()	>	step function determining if supply needs to 
		 * 							be built
		 */
		//SupplyManager.updateSupplyManager(game, self, bArmyMap, bStructMap ,
		//		bResources);

		// Building Manager:
		/*
		 * Variables:
		 * --
		 * 
		 * Iterative functions:
		 * --buildWorkers()	>	build workers based on number of command centers
		 * --buildingProduction()	>	build marines/medics when conditions are met
		 */
		//BuildingManager.buildingManager(game, self, bArmyMap, bStructMap, bResources, bBasePos, mineralSetup, drawStructPos, drawStructLabel, buildOrderStruct, buildOrderSupply);
		BuildingManager.buildingManager( game, self, bBasePos, bArmyMap, bStructMap, productionMode, bResources, buildOrderStruct, buildOrderSupply, techTreeTech, techTreeSupply, mineralSetup, timeBuildIssued );
		productionMode = BuildingManager.updateProductionMode(game, bArmyMap, bArmyMap, productionMode);
		
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
				eStructPos, rallyPoints);
		//armyManager.updateMedics(medics);
		
		
		// Worker Manager:
		/*
		 * Variables:
		 * -- 
		 * 
		 * Iterative functions:
		 * --makeIdleWorkersMine()	>	make idle SCVS mine minerals
		 * --makeWorkersMineGas()	> 	count number of gas miners, then assign 
		 * 								some if low
		 */
		WorkerManager.updateWorkerManager(game, self, bArmyMap, bStructMap);

		// Implement without persistent data	
		MapDraw.drawMapInformation(game, bBasePos, eBasePos, resourceZone, rallyPoints);		
		DrawUI.updateUI(game, self, bArmyMap, bStructMap, eStructPos, bBasePos, bResources, buildOrderStruct, buildOrderSupply, techTreeTech, techTreeSupply, drawStructPos, drawStructLabel, productionMode, timeBuildIssued );
	
		//testing
		//MapDraw.drawChokePointRegion(game, chokepointList );
		MapDraw.drawRallyPoints(game, rallyPoints);
		//System.out.println(buildOrderStruct.get(0));
		//System.out.println(buildOrderSupply.get(0));
		
		//game.drawTextMap( , arg1);

	}

	public void onEnd() {

	}

}