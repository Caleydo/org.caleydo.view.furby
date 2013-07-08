/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.concurrent;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.util.execution.SafeCallable;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;

/**
 * @author user
 *
 */
public class ScanProbabilityMatrix implements SafeCallable<ScanResult> {
	private final float threshold;
	private final Table table;
	private final int bcNr;
	private final ASortingStrategy strategy;

	public ScanProbabilityMatrix(float threshold, Table t, int bcNr,
			ASortingStrategy strat) {
		this.threshold = threshold;
		this.table = t;
		this.bcNr = bcNr;
		this.strategy = strat;
	}

	private ScanResult scanProbTable() {

		final int tablesize = table.depth(); // table.getRowIDList().size();
		double max = 0;
		double min = 6e20;
		for (int nr = 0; nr < tablesize; nr++) {
			float p;
			p = (float) table.getRaw(bcNr, nr);
			if (Math.abs(p) > threshold) {
				strategy.addIndex(nr);
			}
			if (p > max)
				max = p;
			if (p < min)
				min = p;
		}

		List<Integer> indices = new ArrayList<>();
		for (Integer i : strategy) {
			indices.add(i);
		}
		return new ScanResult(indices, max, min);
	}

	@Override
	public ScanResult call() {
		return scanProbTable();

	}

}
