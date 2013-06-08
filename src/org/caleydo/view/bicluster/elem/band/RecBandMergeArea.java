package org.caleydo.view.bicluster.elem.band;

import java.util.HashMap;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.band.BandElement.BandType;

public class RecBandMergeArea {

	private HashMap<List<Integer>, Integer> bandWidthMap;
	private ClusterElement cluster, other;

	public RecBandMergeArea(ClusterElement cluster, ClusterElement other) {
		this.cluster = cluster;
		this.other = other;
	}

	public Pair<Float, Float> getLeftMergePoint(List<Integer> band) {

		return null;
	}

	public Pair<Float, Float> getRightMergePoint(List<Integer> band) {

		return null;
	}
	
	public Pair<Float, Float> getBackVector() {
		return null;
	}
}
