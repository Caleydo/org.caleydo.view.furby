/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class MouseOverClusterEvent extends AEvent {

	private boolean in;

	public MouseOverClusterEvent(ClusterElement hoveredElement, boolean b) {
		setSender(hoveredElement);
		in = b;
	}


	public boolean isMouseOver(){
		return in;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
