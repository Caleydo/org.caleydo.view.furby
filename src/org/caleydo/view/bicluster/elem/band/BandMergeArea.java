package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec3f;

import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;

public abstract class BandMergeArea {

	protected static final float MERGING_AREA_LENGHT = 20;


	protected double elementSize=5;
	protected int nrOfIndices;
	protected List<List<Integer>> allIndices;
	protected ClusterElement cluster, other;
	protected Vec2f[] points;
	protected Vec2f areaCenter;

	public BandMergeArea(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> mergedIndices, double elementSize) {
		this.cluster = cluster;
		this.other = other;
		this.allIndices = mergedIndices;
		for (List<Integer> list : mergedIndices) {
			nrOfIndices += list.size();
		}
		this.elementSize = elementSize;

	}

	protected abstract Band getBand(List<Integer> subBandIndices);
	protected abstract List<Pair<Vec3f, Vec3f>> getConnectionFromBand();
}
