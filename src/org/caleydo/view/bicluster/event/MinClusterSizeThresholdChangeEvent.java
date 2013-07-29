/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

public class MinClusterSizeThresholdChangeEvent extends AEvent {

	private final float minClusterSize;

	public MinClusterSizeThresholdChangeEvent(float minClusterSize) {
		this.minClusterSize = minClusterSize;
	}

	public float getMinClusterSize() {
		return minClusterSize;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
