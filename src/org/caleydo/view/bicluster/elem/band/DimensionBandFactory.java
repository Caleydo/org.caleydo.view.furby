package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class DimensionBandFactory extends BandFactory {

	private static final float ROTATION_RADIUS = 30;
	private static final float NEAREST_POINT_Y_DISTANCE = 10;
	private static final float BAND_CLUSTER_OFFSET = 20;
	double[] rotationMatrix = new double[4];
	private Vec2f firstDirection, secondDirection;
	private double firstAngle, secondAngle;

	private float firstLeftest, secondLeftest;
	private float firstRightest, secondRightest;
	private float firstTotalSize, secondTotalSize; // = leftest-rigtest

	public DimensionBandFactory(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> firstSubIndices,
			List<List<Integer>> secondSubIndices, List<Integer> overlap) {
		super(cluster, other, firstSubIndices, secondSubIndices, cluster
				.getDimensionElementSize(), overlap);
		calcClusterDirectionVectorsAndAngle();
		calculateRotationMatrix();
	}

	private void calcClusterDirectionVectorsAndAngle() {
		Vec2f cLoc, oLoc;
		Vec2f cSize, oSize;
		cLoc = first.getAbsoluteLocation();
		oLoc = second.getAbsoluteLocation();
		cSize = first.getSize();
		oSize = second.getSize();
		Vec2f cCenter = cLoc.addScaled(0.5f, cSize);
		Vec2f oCenter = oLoc.addScaled(0.5f, oSize);
		firstDirection = oCenter.minus(cCenter);
		firstDirection.normalize();
		secondDirection = new Vec2f(-firstDirection.x(), -firstDirection.y());
		// calcAngle();
		firstAngle = Math.atan(firstDirection.x() / -firstDirection.y());
		if (firstDirection.y() > 0)
			firstAngle = firstAngle + Math.PI;

		secondAngle = Math.atan(secondDirection.x() / -secondDirection.y());
		if (secondDirection.y() > 0)
			secondAngle = secondAngle + Math.PI;

		// if (first.getID().contains("bicluster3")
		// && second.getID().contains("bicluster17")) {
		// System.out.println(firstAngle);
		// // System.out.println("halt -  dimbandmergeArea");
		// // System.out.println(cLoc + " " + oLoc);
		// // System.out.println(cluster.getID() + " " + other.getID());
		// }

	}

	private void calculateRotationMatrix() {
		rotationMatrix[0] = Math.cos(firstAngle);
		rotationMatrix[1] = Math.sin(firstAngle);
		rotationMatrix[2] = -Math.sin(firstAngle);
		rotationMatrix[3] = Math.cos(firstAngle);
	}

	private int getIndexOf(List<List<Integer>> source, List<Integer> subBand) {
		int index = 0;
		int stopIndex = source.indexOf(subBand);
		int i = 0;
		for (List<Integer> list : source) {
			if (i++ >= stopIndex)
				break;
			index += list.size();
		}
		return index;
	}

	// delivers indicator whether the bands directly leaving a cluster are
	// starting at the top or the bottom
	private boolean isStartsOnTopFirst() {
		return firstDirection.y() < 0;
		// return true;
	}

	private boolean isStartsOnTopSecond() {
		return secondDirection.y() < 0;
		// return true;
	}

	private void findLeftestAndRightestFirst(List<Integer> list) {
		firstLeftest = 2e30f;
		firstRightest = -2e30f;
		for (Integer i : list) {
			float pos = first.getDimPosOf(i);
			if (pos > firstRightest)
				firstRightest = pos;
			if (pos < firstLeftest)
				firstLeftest = pos;
		}
		firstRightest += elementSize;
		firstTotalSize = firstRightest - firstLeftest;
	}

	private void findLeftestAndRightestSecond(List<Integer> list) {
		secondLeftest = 2e30f;
		secondRightest = -2e30f;
		for (Integer i : list) {
			float pos = second.getDimPosOf(i);
			if (pos > secondRightest)
				secondRightest = pos;
			if (pos < secondLeftest)
				secondLeftest = pos;
		}
		secondRightest += elementSize;
		secondTotalSize = secondRightest - secondLeftest;
	}

	private List<Vec2f> translateToClusterAbsoluteCoordinates(
			List<Vec2f> bandPoints, ClusterElement cluster) {
		Vec2f clusterLoc = cluster.getLocation();
		for (Vec2f v : bandPoints) {
			v.add(clusterLoc);
		}
		return bandPoints;
	}

	@Override
	protected Map<List<Integer>, Band> getBands() {
		Map<List<Integer>, Band> bandsMap = new HashMap<>();
		// first
		for (List<Integer> subBand : firstIndices) {
//			bandsMap.put(subBand, createSubBandFromFirst(subBand));
		}
		// second
		for (List<Integer> subBand : secondIndices) {
//			bandsMap.put(subBand, createSubBandFromSecond(subBand));
		}
		// connectionBand
		if (firstIndices.size() > 0) {
			// there is at least one band which has to be split up
			// --> a connection band has to be drawn
//			bandsMap.put(allIndices, createMainBand());
		}
		// singleElementBands
		for (List<Integer> singleElement : singleElementIndices) {
			bandsMap.put(singleElement,
					createSubBandWithOneIndex(singleElement));
		}
		return bandsMap;
	}

	private Band createMainBand() {
		// TODO Auto-generated method stub
		return null;
	}

	private Band createSubBandFromSecond(List<Integer> subBand) {
		// TODO Auto-generated method stub
		return null;
	}

	private Band createSubBandFromFirst(List<Integer> subBand) {
		// TODO Auto-generated method stub
		return null;
	}

	private Band createSubBandWithOneIndex(List<Integer> subBand) {
		
		//Points from first Cluster
		List<Vec2f> firstPoints = new ArrayList<>();
		findLeftestAndRightestFirst(subBand);
		float middle = (firstLeftest + firstRightest) / 2;
		float yPos = isStartsOnTopFirst() ? 0 : first.getSize().y();
		firstPoints.add(new Vec2f(middle, yPos));
		yPos = isStartsOnTopFirst() ? -BAND_CLUSTER_OFFSET : first.getSize().y() + BAND_CLUSTER_OFFSET;
		firstPoints.add(new Vec2f(middle, yPos));
		firstPoints = translateToClusterAbsoluteCoordinates(firstPoints, first);

		//Points from second Cluster
		List<Vec2f> secondPoints = new ArrayList<>();
		findLeftestAndRightestSecond(subBand);
		middle = (secondLeftest + secondRightest) / 2;
		yPos = isStartsOnTopSecond() ? -BAND_CLUSTER_OFFSET : second.getSize().y() + BAND_CLUSTER_OFFSET;
		secondPoints.add(new Vec2f(middle, yPos));
		yPos = isStartsOnTopSecond() ? 0 : second.getSize().y();
		secondPoints.add(new Vec2f(middle, yPos));
		secondPoints = translateToClusterAbsoluteCoordinates(secondPoints, second);
		
		firstPoints.addAll(secondPoints);
		return TesselatedPolygons.band(firstPoints, 0, (float)elementSize/2, 100);
	}

}
