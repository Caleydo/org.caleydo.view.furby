/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.sorting.EThresholdMode;

/**
 * @author user
 *
 */
public class LZThresholdChangeEvent extends AEvent {
	private final EDimension dim;
	private final float threshold;
	private final int numberThreshold;
	private final EThresholdMode mode;

	public LZThresholdChangeEvent(EDimension dim, float threshold, int numberThreshold, EThresholdMode mode) {
		this.threshold = threshold;
		this.numberThreshold = numberThreshold;
		this.dim = dim;
		this.mode = mode;
	}

	/**
	 * @return the mode, see {@link #mode}
	 */
	public EThresholdMode getMode() {
		return mode;
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return the threshold, see {@link #threshold}
	 */
	public float getThreshold() {
		return threshold;
	}

	/**
	 * @return the numberThreshold, see {@link #numberThreshold}
	 */
	public int getNumberThreshold() {
		return numberThreshold;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}


}
