package b.army;

import bwapi.Unit;

public class Medic {
	int ID;
	Unit healTarget;
	
	public Medic(int newID) {
		ID = newID;
	}
	
	public void setHealTarget( Unit newTarget) {
		this.healTarget = newTarget;
	}

	public int getID() {
		return ID;
	}
	
	public Unit getTarget() {
		return this.healTarget;
	}

}
