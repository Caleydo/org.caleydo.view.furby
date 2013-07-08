/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.concurrent;

import java.util.List;

/**
 * @author user
 *
 */
public class ScanResult {

	private final List<Integer> indices;
	private final double max;
	private final double min;

	public ScanResult(List<Integer> indices, double max, double min) {
		this.indices = indices;
		this.max = max;
		this.min = min;
	}

	/**
	 * @return the indices, see {@link #indices}
	 */
	public List<Integer> getIndices() {
		return indices;
	}

	/**
	 * @return the max, see {@link #max}
	 */
	public double getMax() {
		return max;
	}

	public double getMin(){
		return min;
	}
}
