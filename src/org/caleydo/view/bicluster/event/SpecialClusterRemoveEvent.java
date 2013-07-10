/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class SpecialClusterRemoveEvent extends ADirectedEvent {

	boolean isDimCluster;


	public SpecialClusterRemoveEvent(ClusterElement sender, boolean isDimCluster) {
		setSender(sender);
		this.isDimCluster = isDimCluster;
	}

	public boolean isDimCluster() {
		return isDimCluster;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
