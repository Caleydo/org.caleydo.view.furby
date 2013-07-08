package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;

public class FocusChangeEvent extends AEvent {

	public FocusChangeEvent(GLElement sender) {
		this.setSender(sender);
	}
	
	
	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
