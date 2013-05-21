package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;



public class CreateBandsEvent extends AEvent {

	
	
	public CreateBandsEvent(GLElement sender) {
		this.setSender(sender);
	}
	
	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
