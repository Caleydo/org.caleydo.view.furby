package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class DimBandMergeArea extends BandMergeArea {

	protected static final float NEAREST_POINT_X_DISTANCE = 50;
	protected static final float NEAREST_POINT_Y_DISTANCE = 10;

	public DimBandMergeArea(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> mergedIndices) {
		super(cluster, other, mergedIndices, cluster.getDimensionElementSize());
		setMergingAreaPoints();
	}

	protected void setMergingAreaPoints() {
		points = new Vec2f[4];
		Vec2f cLoc = cluster.getLocation();
		Vec2f cSize = cluster.getSize();
		Vec2f cCenter = cLoc.addScaled(0.5f, cSize);
		Vec2f first = cCenter.copy();
		points[0] = first;
		Vec2f second = cCenter.copy();
		points[1] = second;
		Vec2f third = cCenter.copy();
		points[2] = third;
		Vec2f fourth = cCenter.copy();
		points[3] = fourth;
		first.setX((float) (cCenter.x() - nrOfIndices * elementSize / 2));
		third.setX((float) (cCenter.x() - nrOfIndices * elementSize / 2));
		second.setX((float) (cCenter.x() + nrOfIndices * elementSize / 2));
		fourth.setX((float) (cCenter.x() + nrOfIndices * elementSize / 2));
		first.setY((float) (cCenter.x() - MERGING_AREA_LENGHT / 2));
		second.setY((float) (cCenter.x() - MERGING_AREA_LENGHT / 2));
		third.setY((float) (cCenter.x() + MERGING_AREA_LENGHT / 2));
		fourth.setY((float) (cCenter.x() + MERGING_AREA_LENGHT / 2));
	}

	@Override
	protected Band getBand(List<Integer> subBandIndices) {
		List<Pair<Vec3f, Vec3f>> bandPoints = new ArrayList<>();
		int startIndex = allIndices.indexOf(subBandIndices);
		int size = subBandIndices.size();
		findLeftestAndRightest(subBandIndices);
		
		

		bandPoints = translate(bandPoints);
		bandPoints = rotate(bandPoints);
		return TesselatedPolygons.band(bandPoints);
	}

	private float leftest;
	private float rightest;

	private void findLeftestAndRightest(List<Integer> list) {
		leftest = 2e30f;
		rightest = -2e30f;
		for (Integer i: list) {
			float pos = cluster.getDimPosOf(i);
			if (pos > rightest) rightest = pos;
			if (pos < leftest) leftest = pos;
		}
	}


	private List<Pair<Vec3f, Vec3f>> translate(
			List<Pair<Vec3f, Vec3f>> bandPoints) {
		return bandPoints;
		// TODO Auto-generated method stub

	}

	private List<Pair<Vec3f, Vec3f>> rotate(List<Pair<Vec3f, Vec3f>> bandPoints) {
		return bandPoints;
		// TODO Auto-generated method stub

	}

	@Override
	protected Vec2f[] getConnectionFromBand() {
		Vec2f[] subBandPoints = new Vec2f[4];
		subBandPoints[2] = points[0];
		subBandPoints[3] = points[1];
		subBandPoints[0] = points[0].addScaled(-MERGING_AREA_LENGHT / 2,
				direction);
		subBandPoints[1] = points[1].addScaled(-MERGING_AREA_LENGHT / 2,
				direction);
		return subBandPoints;
	}

}
