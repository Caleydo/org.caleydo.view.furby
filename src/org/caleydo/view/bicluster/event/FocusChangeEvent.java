/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class FocusChangeEvent extends AEvent {

	private boolean focus;

	public FocusChangeEvent(ClusterElement sender, boolean focus) {
		this.focus = focus;
		this.setSender(sender);
	}

	public ClusterElement getCluster() {
		return (ClusterElement) getSender();
	}

	public boolean gotFocus() {
		return focus;
	}

	public boolean lostFocus() {
		return !focus;
	}


	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
