package ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import army.ArmyManager;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import economy.Base;
import economy.Resources;
import economy.WorkerManager;
import idmap.MapUnitID;
import map.MapDraw;
import map.MapInformation;
import map.ScoutManager;
import structure.BuildOrder;
import structure.BuildingManager;
import structure.ListOfBuildOrders;
import structure.BuildingPlacement;
import ui.DrawUI;

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
	
	private ArrayList<Base> bBases = new ArrayList<Base>();
	private Resources bResources = new Resources();
	
	private List<Position> scoutQueue = new ArrayList<Position>();
	private List<Chokepoint> chokepointList = new ArrayList<Chokepoint>();
	private List<Pair<Position,Position>> miningRegionsList = new ArrayList<Pair<Position,Position>>();
	int[] timeBuildIssued = {0};
	private List<Pair<TilePosition,TilePosition>> noBuildZones = new ArrayList<Pair<TilePosition,TilePosition>>();
	
	boolean[] underAttack = {false};
	
	// building stuff
	int[] productionMode = {0}; // 0 to start (SCVs only), 1 for SCVS+Marines, 2 for SCVs+Marines+Medics
	
	private List< Pair<Position,Position> > rallyPoints = new ArrayList< Pair<Position,Position> >();
		
	BuildOrder bBuildOrder;
	
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
		
		MapInformation.initMapInfo(game, bStructMap, bBasePos);
		miningRegionsList.add( MapInformation.initResourceZone2(game, bBasePos.get(0) ) );	
		
		ScoutManager.initializeScoutQueue(scoutQueue, bBasePos );
		MapInformation.initializeMapInformation( noBuildZones, rallyPoints, miningRegionsList, chokepointList, bBasePos );
		
		// NEW FUNCTIONS BELOW
		bBuildOrder = new BuildOrder( ListOfBuildOrders.chooseBuildOrder( game.enemy().getRace() ));
	}

	public void onUnitMorph(Unit u) {
		// case when refinery is built
		if ( u.getType() == UnitType.Terran_Refinery ) {
			MapUnitID.addToIDMap(bStructMap, u);
			if ( !bBuildOrder.isCompleted() ) { // still using build order
				if ( bBuildOrder.matchesNextstruct(u.getType())) {
					// clear refinery from buildOrder
					bBuildOrder.removeTopOfBuildOrder();
					// remove builder and assign new one
					bRolesMap.put( "GasMiner" , u.getBuildUnit().getID() ); // assign generic role
					bRolesMap.get("Builder").clear();
					WorkerManager.assignBuildSCV(game, bRolesMap, bArmyMap);
				}
			}
			
			// add to relevant bBase
			for ( Base bBase : bBases ) {
				if ( u.getDistance(bBase.getCC()) < 500 ) {
					bBase.assignRefinery( u ); // assign refinery to base
					bBase.addToGasMinerIDs( u.getBuildUnit().getID() ); // assign gas miner to base
				}
			}
		}
		// case when refinery is destroyed
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
			MapUnitID.addStructToIDMap(bStructMap, u);
			if ( !bBuildOrder.isCompleted() ) { // still using build order
				if ( bBuildOrder.matchesNextstruct(u.getType())) {
					// clear top
					bBuildOrder.removeTopOfBuildOrder();
					// remove builder and assign new one
					bRolesMap.get("Builder").clear();
					WorkerManager.assignBuildSCV(game, bRolesMap, bArmyMap);
				}
			}
			BuildingPlacement.addToNoBuildZone(noBuildZones, u);
			if ( u.getType() == UnitType.Terran_Command_Center ) {
				bBases.add( new Base( bBasePos.get(bStructMap.get(UnitType.Terran_Command_Center).size()), u ));
			}
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
			WorkerManager.fillSCVRoles(game, bRolesMap, bArmyMap);
		}
		
		/**
		 * bResources, updates resources: minerals, gas, and supply.
		 */
		bResources.updateAll(game, self, bArmyMap);
		
		/**
		 * Scout Manager, updates scouting behavior
		 */
		ScoutManager.updateScoutManager(game, self, bArmyMap, bRolesMap, eStructPos, bBasePos, scoutQueue, underAttack);

		/**
		 * Building Manager, updates building behavior
		 */
		
		// if buildOrder is still in use
		if ( !bBuildOrder.isCompleted() ) {
			BuildingManager.buildingManagerWithBuildOrder( game, self, bArmyMap, bRolesMap, bStructMap, productionMode,
						bResources, noBuildZones, bBasePos, 
						miningRegionsList, bBases, bBuildOrder );
		}
		
		/**
		 * Update productionMode
		 */
		BuildingManager.updateProductionMode(game, bArmyMap, bArmyMap, productionMode);
		
		/**
		 * Army Manager, updates army
		 */
		ArmyManager.updateArmyManager(game, self, bArmyMap, bRolesMap, bStructMap, bBasePos, eStructPos, rallyPoints);

		/**
		 * Worker Manager, updates workers
		 */
		WorkerManager.updateWorkerManager(game, self, bArmyMap, bRolesMap, bStructMap, bBases);
		
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
		MapDraw.drawMapInformation(game, bBasePos, eBasePos, miningRegionsList, rallyPoints, noBuildZones,
				bBuildOrder );		
		DrawUI.updateUI(game, self, bArmyMap, bStructMap, eStructPos, bBasePos, bResources,
				productionMode, 
				timeBuildIssued, miningRegionsList, bBases, bBuildOrder );
		
	}

	public void onEnd() {

	}

}
