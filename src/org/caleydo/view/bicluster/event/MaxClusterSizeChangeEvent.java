/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author Michael Gillhofer
 * 
 */
public class MaxClusterSizeChangeEvent extends AEvent {

	float dim, rec;

	public float getMaxDimensionSize() {
		return dim;
	}

	public float getMaxRecordSize() {
		return rec;
	}

	public MaxClusterSizeChangeEvent(float maxDimSize, float maxRecSize) {
		this.dim = maxDimSize;
		this.rec = maxRecSize;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
