/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class HideClusterEvent extends AEvent {

	private final ClusterElement elem;

	public HideClusterEvent(ClusterElement elem) {
		this.elem = elem;
	}

	/**
	 * @return the elem, see {@link #elem}
	 */
	public ClusterElement getElem() {
		return elem;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
