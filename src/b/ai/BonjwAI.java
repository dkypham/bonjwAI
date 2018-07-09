package b.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import b.army.ArmyManager;
import b.economy.ResourceManager;
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

/**
 * BonjwAI: Starcraft BroodWar bot using BWAPI 4.1.2
 * @author Duc Pham
 *
 */
public class BonjwAI extends DefaultBWListener {

	public static Mirror mirror = new Mirror();
	private Game game;
	private Player self;
	
	/* Managers section */
	
	/* Persistent data section */
	private Multimap<UnitType, Integer> bArmyMap = ArrayListMultimap.create();	// b.ai army units
	private Multimap<String, Integer> bRolesMap = ArrayListMultimap.create();	// b.ai units w/ roles
	private Multimap<UnitType, Integer> bStructMap = ArrayListMultimap.create();// b.ai structs

	private ArrayList<Position> eStructPos = new ArrayList<Position>();			// enemy structs
	private ArrayList<BaseLocation> bBasePos = new ArrayList<BaseLocation>();	// b.ai baselocations
	private ArrayList<BaseLocation> eBasePos = new ArrayList<BaseLocation>();	// enemy baselocations

	private ArrayList<Integer> bResources = new ArrayList<Integer>();			// b.ai lists

	// mineralSetup - tells us the configuration of the minerals relative to CC
	private int mineralSetup = -1;
	
	private List<Pair<UnitType,Integer>> buildOrderStruct = new ArrayList<Pair<UnitType,Integer>>();
	private List<Pair<TechType,Integer>> buildOrderTech = new ArrayList<Pair<TechType,Integer>>();
	
	private List<Position> drawStructPos = new ArrayList<Position>();
	private List<String> drawStructLabel = new ArrayList<String>();

	private List<Position> scoutQueue = new ArrayList<Position>();
	private List<Chokepoint> chokepointList = new ArrayList<Chokepoint>();
	private List<Pair<Position,Position>> miningRegionsList = new ArrayList<Pair<Position,Position>>();
	int[] timeBuildIssued = {0};
	//boolean[] underAttack = {false};
	private List<Pair<TilePosition,TilePosition>> noBuildZones = new ArrayList<Pair<TilePosition,TilePosition>>();
	
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
		
		mineralSetup = MapInformation.initMapInfo(game, bStructMap, bBasePos, mineralSetup);
		System.out.println("Mineral setup: " + mineralSetup);
		miningRegionsList.add( MapInformation.initResourceZone2(game, bBasePos.get(0) ) );	
		
		ScoutManager.initializeScoutQueue(scoutQueue, bBasePos );
		BuildingOrder.initializeBuildOrder( buildOrderStruct );
		TechManager.initializeTechOrder( buildOrderTech);
		BuildingManager.getBuildingPlan(game, self, bArmyMap, bStructMap, drawStructPos, drawStructLabel, mineralSetup, bBasePos);
		MapInformation.initializeMapInformation( noBuildZones, rallyPoints, miningRegionsList, chokepointList, bBasePos );
		
	}

	public void onUnitMorph(Unit u) {
		if ( u.getType() == UnitType.Terran_Refinery ) {
			MapUnitID.addToIDMap(bStructMap, u);
			if ( buildOrderStruct.get(0).first == UnitType.Terran_Refinery ) {
				buildOrderStruct.remove(0);
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
		case 1:	// unit
			MapUnitID.addToIDMap(bArmyMap, u);
			break;
		case 2: // structure
			MapUnitID.addStructToIDMap(bStructMap, u, buildOrderStruct);
			BuildingPlacement.addToNoBuildZone(noBuildZones, u);
			break;
		default:
			break;
		}
	}

	public void onUnitDestroy(Unit u) {
		switch (BonjwAIGame.switchFlags(self, u)) {
		case 1: // unit
			MapUnitID.removeFromIDMap(bArmyMap, u);
			if (u.getType() == UnitType.Terran_SCV) {	// remove from special roles map as well
				MapUnitID.removeRoleSCV(bRolesMap, u);
			}
			break;
		case 2: // structure
			MapUnitID.removeFromIDMap(bStructMap, u);
			break;
		default:
			break;
		}
	}

	@Override
	public void onFrame() {	

		// check if SCV roles have been assigned yet
		// can't put this in OnStart() b/c bArmyMap isn't init yet
		if ( !WorkerManager.checkIfAllSCVRolesAssigned(bRolesMap) ) { // assigns if false
			WorkerManager.fillSCVRoles(bRolesMap, bArmyMap);
		}
		
		/**
		 * Resource Manager, updates resources: minerals, gas, and supply.
		 */
		ResourceManager.updateResources(game, self, bResources, bArmyMap);
		
		/**
		 * Scout Manager, updates scouting behavior
		 */
		ScoutManager.updateScoutManager(game, self, bArmyMap, bRolesMap, eStructPos, bBasePos, scoutQueue);

		/**
		 * Building Manager, updates building behavior
		 */
		if ( game.getFrameCount() % 16 == 0 ) {
			BuildingManager.buildingManager( game, self, bBasePos, bArmyMap, bRolesMap, bStructMap, 
					productionMode, bResources, buildOrderStruct, buildOrderTech, mineralSetup, 
					timeBuildIssued, miningRegionsList, noBuildZones );
		}

		/**
		 * Update productionMode
		 */
		productionMode = BuildingManager.updateProductionMode(game, bArmyMap, bArmyMap, productionMode);
		
		/**
		 * Army Manager, updates army
		 */
		ArmyManager.updateArmyManager(game, self, bArmyMap, bRolesMap, bStructMap, bBasePos, eStructPos, rallyPoints);

		/**
		 * Worker Manager, updates workers
		 */
		WorkerManager.updateWorkerManager(game, self, bArmyMap, bRolesMap, bStructMap);
		
		/**
		 * Map Information, updates known map information
		 */
		if ( game.getFrameCount() % 100 == 0 ) {
			MapInformation.updateMapInformation(game, miningRegionsList, bBasePos, noBuildZones);
		}
		
		// Implement without persistent data
		/**
		 * Drawing
		 */
		MapDraw.drawMapInformation(game, bBasePos, eBasePos, miningRegionsList, rallyPoints, noBuildZones);		
		DrawUI.updateUI(game, self, bArmyMap, bStructMap, eStructPos, bBasePos, bResources, buildOrderStruct, 
				buildOrderTech, drawStructPos, drawStructLabel, productionMode, 
				timeBuildIssued, miningRegionsList );
		
	}

	public void onEnd() {

	}

}
