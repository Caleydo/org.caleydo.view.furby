/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author user
 *
 */
public class MaxThresholdChangeEvent extends AEvent {
	private final double newDimThreshold;
	private final double newRecThreshold;

	/**
	 *
	 */
	public MaxThresholdChangeEvent(double maxDim, double maxRec) {
		this.newDimThreshold = maxDim;
		this.newRecThreshold = maxRec;
	}

	/**
	 * @return the newDimThreshold, see {@link #newDimThreshold}
	 */
	public double getDimThreshold() {
		return newDimThreshold;
	}

	/**
	 * @return the newRecThreshold, see {@link #newRecThreshold}
	 */
	public double getRecThreshold() {
		return newRecThreshold;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
