/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;

import java.util.List;
import java.util.Map;

import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;

public abstract class BandFactory {

	protected static final float MERGING_AREA_LENGHT = 20;

//	protected double elementSize = 5;
	/*
	 * firstIndices contains all Indices of SubBands from the first cluster with
	 * subBand.size > 1
	 *
	 * secondIndices contains all Indices of SubBands from the second cluster
	 * with subBand.size > 1
	 *
	 * singleElementIndices --> contains all Indices of SubBands with
	 * subBand.size == 1
	 */
	protected List<List<Integer>> firstIndices, secondIndices;
	protected ClusterElement first, second;
	protected List<Integer> allIndices;

	public BandFactory(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> firstSubIndices,
			List<List<Integer>> secondSubIndices, double elementSize,
			List<Integer> overlap) {
		this.first = cluster;
		this.second = other;
		this.firstIndices = firstSubIndices;
		this.secondIndices = secondSubIndices;
//		this.elementSize = elementSize;
		this.allIndices = overlap;
	}

	protected static void calculateRotationMatrix(float[] m, double angle) {
		final float cos = (float) Math.cos(angle);
		final float sin = (float) Math.sin(angle);

		m[0] = cos;
		m[1] = sin;
		m[2] = -sin;
		m[3] = cos;
	}

	protected abstract Map<List<Integer>, Band> getSplitableBands();

	protected abstract Band getSimpleBand();

	protected abstract Map<Integer, List<Vec2f>> getConnectionsSplines();
}
