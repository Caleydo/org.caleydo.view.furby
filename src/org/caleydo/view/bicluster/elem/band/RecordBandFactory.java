package org.caleydo.view.bicluster.elem.band;

import java.util.List;
import java.util.Map;

import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class RecordBandFactory extends BandFactory {

	protected static final float NEAREST_POINT_X_DISTANCE = 50;
	protected static final float NEAREST_POINT_Y_DISTANCE = 50;
	
	
	public RecordBandFactory(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> firstSubIndices,
			List<List<Integer>> secondSubIndices, double elementSize, List<Integer> overlap) {
		super(cluster, other, firstSubIndices, secondSubIndices, elementSize, overlap);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected Map<List<Integer>, Band> getBands() {
		// TODO Auto-generated method stub
		return null;
	}



	
}
