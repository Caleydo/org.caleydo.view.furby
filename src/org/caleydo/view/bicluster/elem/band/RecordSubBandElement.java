package org.caleydo.view.bicluster.elem.band;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class RecordSubBandElement extends RecordBandElement {

	private ClusterElement cluster;
	private BandElement anchor;
	private List<Integer> indices;
	private RecBandMergeArea mergeArea;


	public RecordSubBandElement(GLElement cluster, RecordBandElement anchor,
			AllBandsElement root, List<Integer> indices, RecBandMergeArea mergeArea) {
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
		topBandClusterPos = 100000f;
		bottomBandClusterPos = 0;
		for (Integer i : indices) {
			float pos = cluster.getDimPosOf(i);
			if (pos < topBandClusterPos)
				topBandClusterPos = pos;
			if (pos > bottomBandClusterPos)
				bottomBandClusterPos = pos;
		}
	}

}
