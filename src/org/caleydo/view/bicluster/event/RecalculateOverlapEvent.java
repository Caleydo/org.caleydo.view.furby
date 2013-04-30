package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;

public class RecalculateOverlapEvent extends AEvent {

	final boolean isGlobal;
	
	public boolean isGlobal(){
		return isGlobal;
	}

	public RecalculateOverlapEvent(GLElement sender, boolean isGlobal) {
		this.setSender(sender);
		this.isGlobal = isGlobal;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
