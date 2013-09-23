/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.EDimension;

public class SpecialClusterRemoveEvent extends ADirectedEvent {

	private final boolean isDimCluster;

	public SpecialClusterRemoveEvent(ClusterElement sender, boolean isDimCluster) {
		setSender(sender);
		this.isDimCluster = isDimCluster;
	}

	public boolean isDimCluster() {
		return isDimCluster;
	}

	public EDimension getDimension() {
		return isDimCluster ? EDimension.DIMENSION : EDimension.RECORD;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
