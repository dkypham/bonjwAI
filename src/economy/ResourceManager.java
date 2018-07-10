package economy;

import java.util.ArrayList;

import com.google.common.collect.Multimap;

import bwapi.Game;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import structure.BuildingManager;

/**
 * Resource Manager, on each call
 * 
 * Updates bot's resource information: minerals, gas, and supply. Also accounts for planned resource state 
 * (minerals/gas after queued buildings are built, supply after queued supply depots are built)
 * 
 * bResources array:
 * 
 * [0] - actual minerals: actual minerals bot has
 * 
 * [1] - reserved minerals: amount of minerals bot is queued to spend
 * 
 * [2] - actual gas: actual gas bot has
 * 
 * [3] - reserved gas: amount of gas bot is queued to spend
 * 
 * [4] - actual supply: actual supply bot has
 * 
 * [5] - effective supply: amount of supply bot is queued to have
 * 
 * [6] - supply used: amount of supply bot is currently using
 */
public class ResourceManager {

	static int T_SUPPLY_VALUE = 16;
	static UnitType T_SD = UnitType.Terran_Supply_Depot;
	static UnitType SCV = UnitType.Terran_SCV;

}
