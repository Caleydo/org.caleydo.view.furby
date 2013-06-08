package org.caleydo.view.bicluster.elem.band;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class DimensionSubBandElement extends DimensionBandElement {

	private ClusterElement cluster;
	private BandElement anchor;
	private List<Integer> indices;
	private DimBandMergeArea mergeArea;

	public DimensionSubBandElement(GLElement cluster, DimensionBandElement anchor,
			AllBandsElement root, List<Integer> indices, DimBandMergeArea mergeArea) {
		super(cluster, anchor.second, root);
		this.cluster = (ClusterElement) cluster;
		this.anchor = anchor;
		this.indices = indices;
		this.mergeArea = mergeArea;
	}

	@Override
	public void update() {
		updateLeftestAndRightest();
		createBand();
	}

	private void createBand() {

		band = TesselatedPolygons.band(bandPoints);
	}

	private void updateLeftestAndRightest() {
		leftBandClusterPos = 100000f;
		rightBandClusterPos = 0;
		for (Integer i : indices) {
			float pos = cluster.getDimPosOf(i);
			if (pos < leftBandClusterPos)
				leftBandClusterPos = pos;
			if (pos > rightBandClusterPos)
				rightBandClusterPos = pos;
		}
	}

}
