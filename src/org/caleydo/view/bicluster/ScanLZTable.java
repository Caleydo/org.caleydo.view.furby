/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.view.bicluster.sorting.FuzzyClustering;
import org.caleydo.view.bicluster.sorting.IntFloat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class ScanLZTable implements Callable<List<FuzzyClustering>> {
	private final Table lOrZ;

	public ScanLZTable(Table lOrZ) {
		this.lOrZ = lOrZ;
	}

	@Override
	public List<FuzzyClustering> call() throws Exception {
		final int rows = lOrZ.depth();
		final int clusters = lOrZ.size();

		List<FuzzyClustering> l = new ArrayList<>(clusters);
		for (int i = 0; i < clusters; ++i) {
			ImmutableSortedSet.Builder<IntFloat> b = ImmutableSortedSet.orderedBy(IntFloat.BY_PROBABILITY);
			for (int j = 0; j < rows; ++j) {
				float p = lOrZ.getRaw(i, j);
				b.add(new IntFloat(j, p));
			}
			l.add(new FuzzyClustering(b.build()));
		}
		return ImmutableList.copyOf(l);
	}
}
