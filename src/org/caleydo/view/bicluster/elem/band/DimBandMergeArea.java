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

	private static final float ROTATION_RADIUS = 70;
	private static final float NEAREST_POINT_Y_DISTANCE = 10;
	private static final float BAND_CLUSTER_OFFSET = 20;
	double[] rotationMatrix = new double[4];
	private Vec2f centerDirection;
	private double centerAngle;

	public DimBandMergeArea(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> mergedIndices) {
		super(cluster, other, mergedIndices, cluster.getDimensionElementSize());
		calcClusterDirectionVectorsAndAngle();
		calculateRotationMatrix();
		setMergingAreaPoints();
	}

	private void calcClusterDirectionVectorsAndAngle() {
		Vec2f cLoc, oLoc;
		Vec2f cSize, oSize;
		cLoc = cluster.getAbsoluteLocation();
		oLoc = other.getAbsoluteLocation();
		cSize = cluster.getSize();
		oSize = other.getSize();
		Vec2f cCenter = cLoc.addScaled(0.5f, cSize);
		Vec2f oCenter = oLoc.addScaled(0.5f, oSize);
		centerDirection = oCenter.minus(cCenter);
		// if (cluster.getID().contains("bicluster3")
		// && other.getID().contains("bicluster19")) {
		// // System.out.println("halt -  dimbandmergeArea");
		// System.out.println(cLoc + " " + oLoc);
		// // System.out.println(cluster.getID() + " " + other.getID());
		// }
		centerDirection.normalize();
		// calcAngle();
		centerAngle = Math.atan(centerDirection.x() / -centerDirection.y());
		if (centerDirection.y() > 0)
			centerAngle = centerAngle + Math.PI;

	}

	private void calcAngle() {
		if (centerDirection.x() > 0) {
			if (centerDirection.y() < 0) {
				// 1
				centerAngle = -Math.atan(-centerDirection.y()
						/ centerDirection.x())
						+ 3 * Math.PI / 2;
			} else {
				// 2
				centerAngle = -Math.atan(centerDirection.x()
						/ centerDirection.y())
						+ Math.PI;
			}
		} else {
			if (centerDirection.y() < 0) {
				// 3
				centerAngle = Math.atan(-centerDirection.x()
						/ -centerDirection.y());
			} else {
				// 4
				centerAngle = -Math.atan(centerDirection.y()
						/ -centerDirection.x())
						+ Math.PI / 2;
			}
		}

	}

	private void calculateRotationMatrix() {

		rotationMatrix[0] = Math.cos(centerAngle);
		rotationMatrix[1] = Math.sin(centerAngle);
		rotationMatrix[2] = -Math.sin(centerAngle);
		rotationMatrix[3] = Math.cos(centerAngle);
	}

	protected void setMergingAreaPoints() {
		points = new Vec2f[4];
		points[2] = new Vec2f((float) -elementSize * nrOfIndices / 2, 0f);
		points[3] = new Vec2f((float) +elementSize * nrOfIndices / 2, 0f);
		points[0] = new Vec2f((float) -elementSize * nrOfIndices / 2,
				MERGING_AREA_LENGHT);
		points[1] = new Vec2f((float) +elementSize * nrOfIndices / 2,
				MERGING_AREA_LENGHT);
	}

	@Override
	protected Band getBand(List<Integer> subBandIndices) {
		List<Pair<Vec3f, Vec3f>> bandPoints = new ArrayList<>();
		findLeftestAndRightest(subBandIndices);
		boolean startsOnTop = isStartsOnTop();
		if (isStartsOnTop()) {
			bandPoints.add(pair((float) (leftest), (float) (startsOnTop ? 0
					: cluster.getSize().y()), (float) (rightest),
					(float) (startsOnTop ? 0 : cluster.getSize().y())));
			bandPoints.add(pair((float) (leftest),
					(float) (startsOnTop ? -BAND_CLUSTER_OFFSET : cluster
							.getSize().y() + BAND_CLUSTER_OFFSET),
					(float) (rightest),
					(float) (startsOnTop ? -BAND_CLUSTER_OFFSET : cluster
							.getSize().y() + BAND_CLUSTER_OFFSET)));
		} else {
			bandPoints.add(pair((float) (rightest),
					(float) (startsOnTop ? 0 : cluster.getSize().y()),(float) (leftest), (float) (startsOnTop ? 0
							: cluster.getSize().y())));
			bandPoints.add(pair((float) (rightest),
					(float) (startsOnTop ? -BAND_CLUSTER_OFFSET : cluster
							.getSize().y() + BAND_CLUSTER_OFFSET),(float) (leftest),
							(float) (startsOnTop ? -BAND_CLUSTER_OFFSET : cluster
									.getSize().y() + BAND_CLUSTER_OFFSET)));
		}
		int startIndex = getIndexOf(subBandIndices);
		int size = subBandIndices.size();
		List<Pair<Vec3f, Vec3f>> mergeAreaPoints = new ArrayList<>();
		mergeAreaPoints.add(pair((float) (points[0].x() + startIndex
				* elementSize),
				(float) (points[0].y() + MERGING_AREA_LENGHT / 2),
				(float) (points[0].x() + (startIndex + size) * elementSize),
				(float) (points[0].y() + MERGING_AREA_LENGHT / 2)));
		mergeAreaPoints.add(pair((float) (points[0].x() + startIndex
				* elementSize), (float) (points[0].y()),
				(float) (points[0].x() + (startIndex + size) * elementSize),
				(float) (points[0].y())));
//		mergeAreaPoints = mirrorOnY(mergeAreaPoints);
		mergeAreaPoints = translateForRotation(mergeAreaPoints);
		mergeAreaPoints = rotate(mergeAreaPoints);
		mergeAreaPoints = translateToClusterRelativeCoordinates(mergeAreaPoints);
		for (Pair<Vec3f, Vec3f> p : mergeAreaPoints)
			bandPoints.add(p);
		bandPoints = translateToClusterAbsoluteCoordinates(bandPoints);
		return TesselatedPolygons.band(bandPoints);
	}


	private int getIndexOf(List<Integer> subBandIndices) {
		int index = 0;
		int stopIndex = allIndices.indexOf(subBandIndices);
		int i = 0;
		for (List<Integer> list : allIndices) {
			if (i++ >= stopIndex)
				break;
			index += list.size();
		}
		return index;
	}

	// for debugging
	public Vec2f[] getPrintablePoints() {
		List<Pair<Vec3f, Vec3f>> pointPairs = new ArrayList<>();
		pointPairs.add(pair((float) (points[0].x()), (float) (points[0].y()),
				(float) (points[1].x()), (float) (points[1].y())));
		pointPairs.add(pair((float) (points[2].x()), (float) (points[2].y()),
				(float) (points[3].x()), (float) (points[3].y())));
		pointPairs = translateForRotation(pointPairs);
		pointPairs = rotate(pointPairs);
		pointPairs = translateToClusterRelativeCoordinates(pointPairs);
		pointPairs = translateToClusterAbsoluteCoordinates(pointPairs);
		Vec2f[] points = new Vec2f[4];
		points[0] = new Vec2f(pointPairs.get(0).getFirst().x(), pointPairs
				.get(0).getFirst().y());
		points[1] = new Vec2f(pointPairs.get(0).getSecond().x(), pointPairs
				.get(0).getSecond().y());
		points[2] = new Vec2f(pointPairs.get(1).getFirst().x(), pointPairs
				.get(1).getFirst().y());
		points[3] = new Vec2f(pointPairs.get(1).getSecond().x(), pointPairs
				.get(1).getSecond().y());
		return points;
	}

	// delivers indicator whether the bands directly leaving a cluster are
	// starting at the top or the bottom
	private boolean isStartsOnTop() {
		return centerDirection.y() < 0;
		// return true;
	}

	private float leftest;
	private float rightest;

	private void findLeftestAndRightest(List<Integer> list) {
		leftest = 2e30f;
		rightest = -2e30f;
		for (Integer i : list) {
			float pos = cluster.getDimPosOf(i);
			if (pos > rightest)
				rightest = pos;
			if (pos < leftest)
				leftest = pos;
		}
		rightest += elementSize;
	}

	@Override
	protected List<Pair<Vec3f, Vec3f>> getConnectionFromBand() {
		List<Pair<Vec3f, Vec3f>> subBandPoints = new ArrayList<>();
		Vec2f direction = new Vec2f(0, 1);
		// Vec2f zero = points[0].addScaled(MERGING_AREA_LENGHT/2, direction);
		// Vec2f one = points[1].addScaled(MERGING_AREA_LENGHT/2, direction);
		Vec2f zero = points[0];
		Vec2f one = points[1];

		if (isStartsOnTop()) {
			subBandPoints.add(pair(zero.x(), zero.y(), one.x(), one.y()));
			subBandPoints.add(pair(points[2].x(), points[2].y(), points[3].x(),
					points[3].y()));
		} else {
			subBandPoints.add(pair(one.x(), one.y(), zero.x(), zero.y()));
			subBandPoints.add(pair(points[3].x(), points[3].y(), points[2].x(),
					points[2].y()));
		}

		subBandPoints = translateForRotation(subBandPoints);
		subBandPoints = rotate(subBandPoints);
		subBandPoints = translateToClusterRelativeCoordinates(subBandPoints);
		subBandPoints = translateToClusterAbsoluteCoordinates(subBandPoints);
		return subBandPoints;
	}

	private List<Pair<Vec3f, Vec3f>> mirrorOnY(
			List<Pair<Vec3f, Vec3f>> toMirror) {
		List<Pair<Vec3f, Vec3f>> newPoints = new ArrayList<>();
		for (Pair<Vec3f, Vec3f> p : toMirror) {
			p.getFirst().setX(-p.getFirst().x());
			p.getSecond().setX(-p.getSecond().x());
			newPoints.add(p);
		}
		return newPoints;
	}

	
	private List<Pair<Vec3f, Vec3f>> translateToClusterRelativeCoordinates(
			List<Pair<Vec3f, Vec3f>> toTranslate) {
		List<Pair<Vec3f, Vec3f>> newAreaPoints = new ArrayList<>();
		Vec2f clusterSize = cluster.getSize();
		for (Pair<Vec3f, Vec3f> oldPair : toTranslate) {
			newAreaPoints.add(pair(
					oldPair.getFirst().x() + clusterSize.x() / 2, oldPair
							.getFirst().y(), oldPair.getSecond().x()
							+ clusterSize.x() / 2, oldPair.getSecond().y()));
		}
		return newAreaPoints;
	}

	private List<Pair<Vec3f, Vec3f>> translateToClusterAbsoluteCoordinates(
			List<Pair<Vec3f, Vec3f>> toTranslate) {
		Vec2f clusterLoc = cluster.getLocation();
		List<Pair<Vec3f, Vec3f>> newSubBandPoints = new ArrayList<>();
		for (Pair<Vec3f, Vec3f> oldPair : toTranslate) {
			newSubBandPoints.add(pair(oldPair.getFirst().x() + clusterLoc.x(),
					oldPair.getFirst().y() + clusterLoc.y(), oldPair
							.getSecond().x() + clusterLoc.x(), oldPair
							.getSecond().y() + clusterLoc.y()));
		}
		return newSubBandPoints;
	}

	private List<Pair<Vec3f, Vec3f>> rotate(List<Pair<Vec3f, Vec3f>> toRotate) {
		List<Pair<Vec3f, Vec3f>> newPoints = new ArrayList<>();

		for (Pair<Vec3f, Vec3f> oldPair : toRotate) {
			double newFirstX = rotationMatrix[0] * oldPair.getFirst().x()
					+ rotationMatrix[2] * oldPair.getFirst().y();
			double newFirstY = rotationMatrix[1] * oldPair.getFirst().x()
					+ rotationMatrix[3] * oldPair.getFirst().y();
			double newSecondX = rotationMatrix[0] * oldPair.getSecond().x()
					+ rotationMatrix[2] * oldPair.getSecond().y();
			double newSecondY = rotationMatrix[1] * oldPair.getSecond().x()
					+ rotationMatrix[3] * oldPair.getSecond().y();
			newPoints.add(pair((float) (newFirstX), (float) (newFirstY),
					(float) (newSecondX), (float) (newSecondY)));
		}
		return newPoints;
	}

	private List<Pair<Vec3f, Vec3f>> translateForRotation(
			List<Pair<Vec3f, Vec3f>> mergeAreaPoints) {
		List<Pair<Vec3f, Vec3f>> newPoints = new ArrayList<>();
		float translateAmount = isStartsOnTop() ? +ROTATION_RADIUS : +cluster
				.getSize().y() + ROTATION_RADIUS;
		for (Pair<Vec3f, Vec3f> p : mergeAreaPoints) {
			p.getFirst().setY(p.getFirst().y() - translateAmount);
			p.getSecond().setY(p.getSecond().y() - translateAmount);
			newPoints.add(p);
		}
		return newPoints;
	}

	protected Pair<Vec3f, Vec3f> pair(float x1, float y1, float x2, float y2) {
		Vec3f _1 = new Vec3f(x1, y1, 0);
		Vec3f _2 = new Vec3f(x2, y2, 0);
		return Pair.make(_1, _2);
	}

}
