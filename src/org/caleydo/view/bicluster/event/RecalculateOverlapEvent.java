package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;

public class RecalculateOverlapEvent extends AEvent {

	final boolean isGlobal;
	final boolean dimBands;
	final boolean recBands;
	
	public boolean isGlobal(){
		return isGlobal;
	}

	public boolean isDimBandEnabled() {
		return dimBands;
	}
	
	public boolean isRecBandEnabled() {
		return recBands;
	}
	
	public RecalculateOverlapEvent(GLElement sender, boolean isGlobal, boolean dimBands, boolean recBands) {
		this.setSender(sender);
		this.isGlobal = isGlobal;
		this.dimBands = dimBands;
		this.recBands = recBands;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

	
}
