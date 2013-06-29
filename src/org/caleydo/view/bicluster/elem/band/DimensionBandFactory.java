package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class DimensionBandFactory extends BandFactory {

	private static final float ROTATION_RADIUS = 35;
	private static final float NEAREST_POINT_Y_DISTANCE = 10;
	private static final float BAND_CLUSTER_OFFSET = 30;
	private static final int SUBBAND_POLYGONS = 10;
	private static final int MAINBAND_POLYGONS = 50;

	private float[] rotationMatrixFirst = new float[4];
	private float[] rotationMatrixSecond = new float[4];
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
		calculateRotationMatrixFirst();
		calculateRotationMatrixSecond();
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

		secondAngle = firstAngle;
		firstAngle = firstAngle + Math.PI;

		// if (first.getID().contains("bicluster3")
		// && second.getID().contains("bicluster17")) {
		// System.out.println(firstAngle);
		// // System.out.println("halt -  dimbandmergeArea");
		// // System.out.println(cLoc + " " + oLoc);
		// // System.out.println(cluster.getID() + " " + other.getID());
		// }

	}

	private void calculateRotationMatrixFirst() {
		rotationMatrixFirst[0] = (float) Math.cos(firstAngle);
		rotationMatrixFirst[1] = (float) Math.sin(firstAngle);
		rotationMatrixFirst[2] = (float) -Math.sin(firstAngle);
		rotationMatrixFirst[3] = (float) Math.cos(firstAngle);
	}

	private void calculateRotationMatrixSecond() {
		rotationMatrixSecond[0] = (float) Math.cos(secondAngle);
		rotationMatrixSecond[1] = (float) Math.sin(secondAngle);
		rotationMatrixSecond[2] = (float) -Math.sin(secondAngle);
		rotationMatrixSecond[3] = (float) Math.cos(secondAngle);
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
		Map<List<Integer>, Band> bandsMap = new IdentityHashMap<>();
		List<Vec2f> bandPoints = new ArrayList<>(4);
		if (firstIndices.size() == 1) {
			// there is only one band from first cluster to the second cluster
			// area.
			// setUp the BandPoints variable with clusterAbsoluteCoordinates, so
			// it can be filled with the coordinates of the other cluster in the
			// other if statement.
			List<Vec2f> firstPoints = new ArrayList<>(4);
			findLeftestAndRightestFirst(firstIndices.get(0));
			float middle = (firstLeftest + firstRightest) / 2;
			float yPos = isStartsOnTopFirst() ? 0 : first.getSize().y();
			firstPoints.add(new Vec2f(middle, yPos));
			yPos = isStartsOnTopFirst() ? -BAND_CLUSTER_OFFSET : first
					.getSize().y() + BAND_CLUSTER_OFFSET;
			firstPoints.add(new Vec2f(middle, yPos));
			bandPoints = translateToClusterAbsoluteCoordinates(firstPoints,
					first);
		} else {
			// band has to be split up into parts
			// create subBands and setUp the BandPoints variable with
			// clusterAbsoluteCoordinates, so it can be
			// filled with the coordinates of the other cluster.
			int startIndex = 0;
			for (List<Integer> subBand: firstIndices) {
				bandsMap.put(subBand, createSubBandFromFirst(subBand, startIndex));
				startIndex += subBand.size();
			}
			
			List<Vec2f> mainPointsFirst = new ArrayList<>(4);
			mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
			mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT));
			mainPointsFirst = translateForRotation(mainPointsFirst);
			mainPointsFirst = rotate(mainPointsFirst, rotationMatrixFirst);
			if (!isStartsOnTopFirst())
				mainPointsFirst = translateToBottom(mainPointsFirst, first);
			findLeftestAndRightestFirst(allIndices);
			mainPointsFirst = translateForCenteringTheBand(mainPointsFirst,
					(firstLeftest + firstRightest) / 2);
			bandPoints = translateToClusterAbsoluteCoordinates(
					mainPointsFirst, first);
			
		}
		float mainBandWidth = (float) (allIndices.size()/2f*elementSize);
		if (secondIndices.size() == 1) {
			// there is only one band from the second cluster to the first
			// cluster area.
			// use coordinates provided in bandPoints from the first Cluster for
			// creating the band.
			List<Vec2f> secondPoints = new ArrayList<>(2);
			findLeftestAndRightestSecond(secondIndices.get(0));
			float middle = (secondLeftest + secondRightest) / 2;
			float yPos = isStartsOnTopSecond() ? -BAND_CLUSTER_OFFSET : second.getSize()
					.y() + BAND_CLUSTER_OFFSET;
			secondPoints.add(new Vec2f(middle, yPos));
			yPos = isStartsOnTopSecond() ? 0 : second.getSize().y();
			secondPoints.add(new Vec2f(middle, yPos));
			secondPoints = translateToClusterAbsoluteCoordinates(secondPoints,
					second);
			bandPoints.addAll(secondPoints);
			bandsMap.put(allIndices, TesselatedPolygons.band(bandPoints, 0, mainBandWidth, MAINBAND_POLYGONS));
		} else {
			// band has to be split up into parts
			// create band from the other cluster with coordinates from
			// bandPoints and create subBands.
			int startIndex = 0;
			for (List<Integer> subBand : secondIndices) {
				bandsMap.put(subBand, createSubBandFromSecond(subBand, startIndex));
				startIndex +=subBand.size();
			}
			
			List<Vec2f> mainPointsSecond = new ArrayList<>();
			mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT));
			mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
			mainPointsSecond = translateForRotation(mainPointsSecond);
			mainPointsSecond = rotate(mainPointsSecond, rotationMatrixSecond);
			findLeftestAndRightestSecond(allIndices);
			if (!isStartsOnTopSecond())
				mainPointsSecond = translateToBottom(mainPointsSecond, second);
			mainPointsSecond = translateForCenteringTheBand(mainPointsSecond,
					(secondLeftest + secondRightest) / 2);
			mainPointsSecond = translateToClusterAbsoluteCoordinates(
					mainPointsSecond, second);
			
			bandPoints.addAll(mainPointsSecond);
			bandsMap.put(allIndices, TesselatedPolygons.band(bandPoints, 0, mainBandWidth, MAINBAND_POLYGONS));
		}

		return bandsMap;
	}

	private Band createSubBandFromSecond(List<Integer> subBand, int startIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	private Band createSubBandFromFirst(List<Integer> subBand, int startIndex) {
		List<Vec2f> bandPointsClusterRelative = new ArrayList<>(4);
		float yPos = isStartsOnTopFirst() ? 0 : first.getSize().y();
		bandPointsClusterRelative.add(new Vec2f((float) ((startIndex + subBand
				.size() / 2f) * elementSize), yPos));
		yPos = isStartsOnTopFirst() ? -BAND_CLUSTER_OFFSET : first.getSize()
				.y() + BAND_CLUSTER_OFFSET;
		bandPointsClusterRelative.add(new Vec2f((float) ((startIndex + subBand
				.size() / 2f) * elementSize), yPos));

		List<Vec2f> bandPoints = new ArrayList<>(2);
		bandPoints.add(new Vec2f(
				(float) ((startIndex + subBand.size() / 2f) * elementSize), 0));
		bandPoints.add(new Vec2f(
				(float) ((startIndex + subBand.size() / 2f) * elementSize),
				MERGING_AREA_LENGHT / 2));
		bandPoints = translateForRotation(bandPoints);
		bandPoints = rotate(bandPoints, rotationMatrixFirst);
		if (!isStartsOnTopFirst())
			bandPoints = translateToBottom(bandPoints, first);
		findLeftestAndRightestFirst(subBand);
		bandPoints = translateForCenteringTheBand(bandPoints,
				(firstLeftest + firstRightest) / 2);
		// now bandPoints contains cluster relative coordinates

		bandPointsClusterRelative.addAll(bandPoints);
		bandPointsClusterRelative = translateToClusterAbsoluteCoordinates(
				bandPoints, first);
		return TesselatedPolygons.band(bandPointsClusterRelative, 0,
				(float) (subBand.size() * elementSize / 2), SUBBAND_POLYGONS);
	}

	private Band createMainBand() {
		int size = getMainBandSize();
		List<Vec2f> mainPointsFirst = new ArrayList<>(4);
		mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
		mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT));
		mainPointsFirst = translateForRotation(mainPointsFirst);
		mainPointsFirst = rotate(mainPointsFirst, rotationMatrixFirst);
		if (!isStartsOnTopFirst())
			mainPointsFirst = translateToBottom(mainPointsFirst, first);
		findLeftestAndRightestFirst(allIndices);
		mainPointsFirst = translateForCenteringTheBand(mainPointsFirst,
				(firstLeftest + firstRightest) / 2);
		mainPointsFirst = translateToClusterAbsoluteCoordinates(
				mainPointsFirst, first);

		List<Vec2f> mainPointsSecond = new ArrayList<>();
		mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT));
		mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
		mainPointsSecond = translateForRotation(mainPointsSecond);
		mainPointsSecond = rotate(mainPointsSecond, rotationMatrixSecond);
		findLeftestAndRightestSecond(allIndices);
		if (!isStartsOnTopSecond())
			mainPointsSecond = translateToBottom(mainPointsSecond, second);
		mainPointsSecond = translateForCenteringTheBand(mainPointsSecond,
				(secondLeftest + secondRightest) / 2);
		mainPointsSecond = translateToClusterAbsoluteCoordinates(
				mainPointsSecond, second);

		mainPointsFirst.addAll(mainPointsSecond);
		return TesselatedPolygons.band(mainPointsFirst, 0, (float) (size
				* elementSize / 2), MAINBAND_POLYGONS);
	}

	private List<Vec2f> translateToBottom(List<Vec2f> toTranslate,
			ClusterElement cluster) {
		Vec2f offset = new Vec2f(0, cluster.getSize().y());
		for (Vec2f v : toTranslate) {
			v.add(offset);
		}
		return toTranslate;
	}

	private List<Vec2f> translateForCenteringTheBand(List<Vec2f> toMove,
			float offset) {
		Vec2f offsetV = new Vec2f(offset, 0);
		for (Vec2f v : toMove) {
			v.add(offsetV);
		}
		return toMove;
	}

	private List<Vec2f> rotate(List<Vec2f> toRotate, float[] rotationMatrix) {
		for (Vec2f v : toRotate) {
			v.setX(v.x() * rotationMatrix[0] + v.y() * rotationMatrix[2]);
			v.setY(v.x() * rotationMatrix[1] + v.y() * rotationMatrix[3]);
		}
		return toRotate;
	}

	// moves all Vec's for ROTATION_RADIUS upwards
	private List<Vec2f> translateForRotation(List<Vec2f> toTranslate) {
		Vec2f xOffset = new Vec2f(0, ROTATION_RADIUS);
		for (Vec2f v : toTranslate) {
			v.add(xOffset);
		}
		return toTranslate;
	}

	private int getMainBandSize() {
		int accu = 0;
		for (List<Integer> l : firstIndices) {
			accu += l.size();
		}
		return accu;
	}

	private Band createSubBandWithOneIndex(List<Integer> subBand) {
		// Points from first Cluster
		List<Vec2f> firstPoints = new ArrayList<>(4);
		findLeftestAndRightestFirst(subBand);
		float middle = (firstLeftest + firstRightest) / 2;
		float yPos = isStartsOnTopFirst() ? 0 : first.getSize().y();
		firstPoints.add(new Vec2f(middle, yPos));
		yPos = isStartsOnTopFirst() ? -BAND_CLUSTER_OFFSET : first.getSize()
				.y() + BAND_CLUSTER_OFFSET;
		firstPoints.add(new Vec2f(middle, yPos));
		firstPoints = translateToClusterAbsoluteCoordinates(firstPoints, first);

		// Points from second Cluster
		List<Vec2f> secondPoints = new ArrayList<>(2);
		findLeftestAndRightestSecond(subBand);
		middle = (secondLeftest + secondRightest) / 2;
		yPos = isStartsOnTopSecond() ? -BAND_CLUSTER_OFFSET : second.getSize()
				.y() + BAND_CLUSTER_OFFSET;
		secondPoints.add(new Vec2f(middle, yPos));
		yPos = isStartsOnTopSecond() ? 0 : second.getSize().y();
		secondPoints.add(new Vec2f(middle, yPos));
		secondPoints = translateToClusterAbsoluteCoordinates(secondPoints,
				second);

		firstPoints.addAll(secondPoints);
		return TesselatedPolygons.band(firstPoints, 0, (float) elementSize / 2,
				100);
	}

}
