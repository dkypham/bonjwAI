package army;

import bwapi.Unit;

public class Marine {
	int ID;
	Unit target;
	
	public void assignTarget( Unit newTarget) {
		target = newTarget;
	}

	public int getID() {
		return ID;
	}
	
	public Unit getTarget() {
		return target;
	}
}
