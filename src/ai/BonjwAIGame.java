package ai;

import bwapi.Player;
import bwapi.Unit;


public class BonjwAIGame {
	
	// Switch flags
	static int switchFlagOther = 0;
	static int switchArmy = 1;
	static int switchStructure = 2;
	
	public static int switchFlags(Player self, Unit u) {
		if ( (u.getType().isBuilding() && u.getType().getRace() == self.getRace() ) ) {
			return switchStructure;
		}
		else if ( u.getType().getRace() == self.getRace() ){
			return switchArmy;
		}
		else {
			return switchFlagOther;
		}
	}
	
	

	
}
