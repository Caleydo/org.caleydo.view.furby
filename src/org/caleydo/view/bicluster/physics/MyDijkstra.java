/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.physics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.caleydo.view.bicluster.elem.ClusterElement;

import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class MyDijkstra {
	/**
	 * compute the min distance between the clusters
	 *
	 * @param source
	 * @param target
	 * @param maxDistance
	 * @param recBands
	 * @param dimBands
	 * @return the distance or maxDistance +1 for invalid or not existing
	 */
	public static int minDistance(final ClusterElement source, final ClusterElement target, int maxDistance,
			boolean recBands, boolean dimBands) {
		if (source == target)
			return 0;
		final int invalid = Math.min(maxDistance + 1, Integer.MAX_VALUE);
		if (maxDistance <= 0)
			return invalid;
		if (hasNeighbor(source, target, recBands, dimBands))
			return 1;
		if (maxDistance < 2)
			return invalid;
		Set<ClusterElement> done = Sets.newIdentityHashSet();
		Deque<Distanced> queue = new ArrayDeque<>(10);
		queue.add(new Distanced(0, source));
		done.add(source);
		Distanced act;
		while( (act = queue.pollFirst()) != null) {
			if (hasNeighbor(act.elem, target, recBands, dimBands))
				return act.distance + 1;
			if (act.distance >= maxDistance) // stop here to add its neighors
				continue;
			if (dimBands)
				addAll(done, queue, act.distance, act.elem.getDimOverlappingNeighbors());
			if (recBands)
				addAll(done, queue, act.distance, act.elem.getRecOverlappingNeighbors());
		}
		return invalid;
	}

	private static void addAll(Set<ClusterElement> done, Deque<Distanced> queue, int distance,
			Iterable<ClusterElement> neighbors) {
		for (ClusterElement neighbor : neighbors) {
			if (!done.contains(neighbor) && neighbor.isVisible()) {
				done.add(neighbor);
				queue.add(new Distanced(distance + 1, neighbor));
			}
		}
	}

	private static class Distanced {
		final int distance;
		final ClusterElement elem;

		public Distanced(int distance, ClusterElement elem) {
			this.distance = distance;
			this.elem = elem;
		}
	}

	private static boolean hasNeighbor(final ClusterElement source, final ClusterElement target, boolean recBands,
			boolean dimBands) {
		return (dimBands && source.getDimOverlap(target) > 0) || (recBands && source.getRecOverlap(target) > 0);
	}
}
