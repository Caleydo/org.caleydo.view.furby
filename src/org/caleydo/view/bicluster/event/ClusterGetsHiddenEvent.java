/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

public class ClusterGetsHiddenEvent extends AEvent {

	private final String clusterID;

	public ClusterGetsHiddenEvent(String clusterID) {
		this.clusterID = clusterID;
	}

	public  String getClusterID(){
		return clusterID;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
