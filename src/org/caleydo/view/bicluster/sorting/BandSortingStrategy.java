/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.Edge;
import org.caleydo.view.bicluster.elem.NormalClusterElement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class BandSortingStrategy implements ISortingStrategy {
	public static final ISortingStrategyFactory FACTORY = new ISortingStrategyFactory() {
		@Override
		public String getLabel() {
			return "Band";
		}

		@Override
		public ISortingStrategy create(NormalClusterElement cluster) {
			return new BandSortingStrategy(cluster);
		}
	};

	private final NormalClusterElement cluster;

	/**
	 * @param cluster
	 * @param dim
	 */
	public BandSortingStrategy(NormalClusterElement cluster) {
		this.cluster = cluster;
	}

	@Override
	public List<IntFloat> apply(List<IntFloat> list, EDimension dim) {
		List<Collection<Integer>> nonEmptyDimBands = new ArrayList<>();
		for (Edge edge : cluster.getOverlappingEdges(dim)) {
			nonEmptyDimBands.add(edge.getOverlapIndices(dim));
		}
		if (nonEmptyDimBands.isEmpty()) // early abort no bands nothing todo
			return list;

		BandSorting dimConflicts = new BandSorting(nonEmptyDimBands);

		Set<IntFloat> finalDimSorting = new LinkedHashSet<IntFloat>();

		ImmutableMap<Integer, IntFloat> byIndex = Maps.uniqueIndex(list, IntFloat.TO_INDEX);
		for (Integer i : dimConflicts) {
			finalDimSorting.add(byIndex.get(i));
		}
		// fill up rest
		finalDimSorting.addAll(list);
		return new ArrayList<>(finalDimSorting);
	}

	@Override
	public boolean needsResortAfterBandsUpdate() {
		return true;
	}
}
