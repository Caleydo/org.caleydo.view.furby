package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.util.TesselatedBiClusterPolygons;

public class RecordBandFactory extends BandFactory {

	private static final float ROTATION_RADIUS = 50;
	private static final float BAND_CLUSTER_OFFSET = 20;
	private static final int SUBBAND_POLYGONS = 30;
	private static final int MAINBAND_POLYGONS = 50;
	private static final int SPLINE_POLYGONS = 20;
	private static final float SPLINE_RADIUS = 0.2f;

	private float[] rotationMatrixFirst = new float[4];
	private float[] rotationMatrixSecond = new float[4];
	private Vec2f firstDirection, secondDirection;
	private double firstAngle, secondAngle;

	private float firstAtTop, secondLeftest;
	private float firstAtBottom, secondRightest;
	private float firstElementSize, secondElementSize;

	public RecordBandFactory(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> firstSubIndices,
			List<List<Integer>> secondSubIndices, List<Integer> overlap) {
		super(cluster, other, firstSubIndices, secondSubIndices, cluster
				.getRecordElementSize(), overlap);
		calcClusterDirectionVectorsAndAngle();
		calculateRotationMatrixFirst();
		calculateRotationMatrixSecond();
		firstElementSize = first.getRecordElementSize();
		secondElementSize = second.getRecordElementSize();
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
		firstAngle = Math.atan(firstDirection.x() / -firstDirection.y())+Math.PI/2;
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

	// delivers indicator whether the bands directly leaving a cluster are
	// starting at Left or right
	private boolean isStartsLeftFirst() {
		return firstDirection.x() < 0;
		// return true;
	}

	private boolean isStartsRightSecond() {
		return secondDirection.x() < 0;
		// return true;
	}

	private void findTopAndBottomFirst(List<Integer> list) {
		firstAtTop = 2e30f;
		firstAtBottom = -2e30f;
		for (Integer i : list) {
			float pos = first.getRecPosOf(i);
			if (pos > firstAtBottom)
				firstAtBottom = pos;
			if (pos < firstAtTop)
				firstAtTop = pos;
		}
		firstAtBottom += firstElementSize;
	}

	private void findTopAndBottomSecond(List<Integer> list) {
		secondLeftest = 2e30f;
		secondRightest = -2e30f;
		for (Integer i : list) {
			float pos = second.getRecPosOf(i);
			if (pos > secondRightest)
				secondRightest = pos;
			if (pos < secondLeftest)
				secondLeftest = pos;
		}
		secondRightest += secondElementSize;
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
	protected Map<List<Integer>, Band> getNonSplitableBands() {
		Map<List<Integer>, Band> bandsMap = new IdentityHashMap<>();
		List<Vec2f> bandPoints = new ArrayList<>(4);

		// there is only one band from first cluster to the second cluster
		// area.
		// setUp the BandPoints variable with clusterAbsoluteCoordinates, so
		// it can be filled with the coordinates of the other cluster in the
		// other if statement.
		List<Vec2f> firstPoints = new ArrayList<>(2);
		findTopAndBottomFirst(allIndices);
		float middle = (firstAtTop + firstAtBottom) / 2;
		float xPos = isStartsLeftFirst() ? 0 : first.getSize().x();
		firstPoints.add(new Vec2f(xPos, middle));
		xPos = isStartsLeftFirst() ? -BAND_CLUSTER_OFFSET : first.getSize()
				.x() + BAND_CLUSTER_OFFSET;
		firstPoints.add(new Vec2f(xPos,middle));
		bandPoints = translateToClusterAbsoluteCoordinates(firstPoints, first);

		// there is only one band from the second cluster to the first
		// cluster area.
		// use coordinates provided in bandPoints from the first Cluster for
		// creating the band.
		List<Vec2f> secondPoints = new ArrayList<>(2);
		findTopAndBottomSecond(allIndices);
		middle = (secondLeftest + secondRightest) / 2;
		xPos = isStartsRightSecond() ? -BAND_CLUSTER_OFFSET : second.getSize()
				.x() + BAND_CLUSTER_OFFSET;
		secondPoints.add(new Vec2f(xPos, middle));
		xPos = isStartsRightSecond() ? 0 : second.getSize().x();
		secondPoints.add(new Vec2f(xPos, middle));
		secondPoints = translateToClusterAbsoluteCoordinates(secondPoints,
				second);
		bandPoints.addAll(secondPoints);
		bandsMap.put(allIndices, TesselatedBiClusterPolygons.band(bandPoints, 0,
				allIndices.size() / 2f * firstElementSize, allIndices.size() / 2f * secondElementSize, MAINBAND_POLYGONS));

		return bandsMap;
	}

	@Override
	protected Map<List<Integer>, Band> getSplitableBands() {
		Map<List<Integer>, Band> bandsMap = new IdentityHashMap<>();
		List<Vec2f> bandPoints = new ArrayList<>(4);
		if (firstIndices.size() == 1) {
			// there is only one band from first cluster to the second cluster
			// area.
			// setUp the BandPoints variable with clusterAbsoluteCoordinates, so
			// it can be filled with the coordinates of the other cluster in the
			// other if statement.
			List<Vec2f> firstPoints = new ArrayList<>(4);
			findTopAndBottomFirst(firstIndices.get(0));
			float middle = (firstAtTop + firstAtBottom) / 2;
			float xPos = isStartsLeftFirst() ? 0 : first.getSize().x();
			firstPoints.add(new Vec2f(xPos,middle));
			xPos = isStartsLeftFirst() ? -BAND_CLUSTER_OFFSET : first
					.getSize().x() + BAND_CLUSTER_OFFSET;
			firstPoints.add(new Vec2f(xPos, middle));
			bandPoints = translateToClusterAbsoluteCoordinates(firstPoints,
					first);
		} else {
			// band has to be split up into parts
			// create subBands and setUp the BandPoints variable with
			// clusterAbsoluteCoordinates, so it can be
			// filled with the coordinates of the other cluster.

			// to make sure bands are created in correct order
			if (isStartsLeftFirst()) {
				int mainBandIndex = allIndices.size();
				for (List<Integer> subBand : firstIndices) {
					mainBandIndex -= subBand.size();
					bandsMap.put(subBand,
							createSubBandFromFirst(subBand, mainBandIndex));
				}
			} else {
				int mainBandIndex = 0;
				for (List<Integer> subBand : firstIndices) {
					bandsMap.put(subBand,
							createSubBandFromFirst(subBand, mainBandIndex));
					mainBandIndex += subBand.size();
				}
			}

			List<Vec2f> mainPointsFirst = new ArrayList<>(4);
			mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
			mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT));
			mainPointsFirst = translateForRotation(mainPointsFirst);
			mainPointsFirst = rotate(mainPointsFirst, rotationMatrixFirst);
			if (!isStartsLeftFirst())
				mainPointsFirst = translateToRight(mainPointsFirst, first);
			findTopAndBottomFirst(allIndices);
			mainPointsFirst = translateForCenteringTheBand(mainPointsFirst,
					(firstAtTop + firstAtBottom) / 2);
			bandPoints = translateToClusterAbsoluteCoordinates(mainPointsFirst,
					first);

		}
		if (secondIndices.size() == 1) {
			// there is only one band from the second cluster to the first
			// cluster area.
			// use coordinates provided in bandPoints from the first Cluster for
			// creating the band.
			List<Vec2f> secondPoints = new ArrayList<>(2);
			findTopAndBottomSecond(secondIndices.get(0));
			float middle = (secondLeftest + secondRightest) / 2;
			float xPos = isStartsRightSecond() ? -BAND_CLUSTER_OFFSET : second
					.getSize().x() + BAND_CLUSTER_OFFSET;
			secondPoints.add(new Vec2f(xPos,middle));
			xPos = isStartsRightSecond() ? 0 : second.getSize().x();
			secondPoints.add(new Vec2f(xPos,middle));
			secondPoints = translateToClusterAbsoluteCoordinates(secondPoints,
					second);
			bandPoints.addAll(secondPoints);
			bandsMap.put(allIndices, TesselatedBiClusterPolygons.band(
					bandPoints, 0, allIndices.size() / 2f * firstElementSize,
					allIndices.size() / 2f * secondElementSize,
					MAINBAND_POLYGONS));
		} else {
			// band has to be split up into parts
			// create band from the other cluster with coordinates from
			// bandPoints and create subBands.

			// to make sure bands are created in correct order
			if (!isStartsRightSecond()) {
				int startIndex = 0;
				for (List<Integer> subBand : secondIndices) {
					bandsMap.put(subBand,
							createSubBandFromSecond(subBand, startIndex));
					startIndex += subBand.size();
				}
			} else {
				int startIndex = allIndices.size();
				for (List<Integer> subBand : secondIndices) {
					startIndex -= subBand.size();
					bandsMap.put(subBand,
							createSubBandFromSecond(subBand, startIndex));
				}
			}

			List<Vec2f> mainPointsSecond = new ArrayList<>();
			mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT));
			mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
			mainPointsSecond = translateForRotation(mainPointsSecond);
			mainPointsSecond = rotate(mainPointsSecond, rotationMatrixSecond);
			findTopAndBottomSecond(allIndices);
			if (!isStartsRightSecond())
				mainPointsSecond = translateToRight(mainPointsSecond, second);
			mainPointsSecond = translateForCenteringTheBand(mainPointsSecond,
					(secondLeftest + secondRightest) / 2);
			mainPointsSecond = translateToClusterAbsoluteCoordinates(
					mainPointsSecond, second);

			bandPoints.addAll(mainPointsSecond);
			bandsMap.put(allIndices, TesselatedBiClusterPolygons.band(bandPoints, 0,
					allIndices.size() / 2f * firstElementSize,
					allIndices.size() / 2f * secondElementSize, MAINBAND_POLYGONS));
		}

		return bandsMap;
	}

	private Band createSubBandFromSecond(List<Integer> subBand, int startIndex) {
		findTopAndBottomSecond(subBand);
		float startPosY = (secondLeftest + secondRightest) / 2;
		List<Vec2f> bandPointsClusterRelative = new ArrayList<>(4);
		float xPos = isStartsRightSecond() ? 0 : second.getSize().y();
		bandPointsClusterRelative.add(new Vec2f(xPos,startPosY));
		xPos = isStartsRightSecond() ? -BAND_CLUSTER_OFFSET : second.getSize()
				.y() + BAND_CLUSTER_OFFSET;
		bandPointsClusterRelative.add(new Vec2f(xPos, startPosY));

		List<Vec2f> bandPoints = new ArrayList<>(2);
		startPosY = (float) ((startIndex + subBand.size() / 2f - allIndices
				.size() / 2f) * secondElementSize);
		bandPoints.add(new Vec2f(0,startPosY));
		bandPoints.add(new Vec2f(MERGING_AREA_LENGHT / 2,startPosY));
		bandPoints = translateForRotation(bandPoints);
		bandPoints = rotate(bandPoints, rotationMatrixSecond);
		if (!isStartsRightSecond())
			bandPoints = translateToRight(bandPoints, second);
		findTopAndBottomSecond(allIndices);
		bandPoints = translateForCenteringTheBand(bandPoints,
				(secondLeftest + secondRightest) / 2f);
		// now bandPoints contains cluster relative coordinates

		bandPointsClusterRelative.addAll(bandPoints);
		bandPointsClusterRelative = translateToClusterAbsoluteCoordinates(
				bandPointsClusterRelative, second);
		return TesselatedPolygons.band(bandPointsClusterRelative, 0,
				(float) subBand.size() * (float) secondElementSize / 2f,
				SUBBAND_POLYGONS);
	}

	public List<Vec2f> getTestPointsSecond() {
		List<Vec2f> mainPointsSecond = new ArrayList<>(6);
		mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
		mainPointsSecond.add(new Vec2f(0, MERGING_AREA_LENGHT));
		mainPointsSecond.add(new Vec2f((float) (allIndices.size()
				* secondElementSize / -2f), MERGING_AREA_LENGHT / 2));
		mainPointsSecond.add(new Vec2f((float) (allIndices.size()
				* secondElementSize / 2f), MERGING_AREA_LENGHT / 2));
		mainPointsSecond.add(new Vec2f((float) (allIndices.size()
				* secondElementSize / -2f), MERGING_AREA_LENGHT));
		mainPointsSecond.add(new Vec2f((float) (allIndices.size()
				* secondElementSize / 2f), MERGING_AREA_LENGHT));
		mainPointsSecond = translateForRotation(mainPointsSecond);
		mainPointsSecond = rotate(mainPointsSecond, rotationMatrixSecond);
		if (!isStartsRightSecond())
			mainPointsSecond = translateToRight(mainPointsSecond, second);
		findTopAndBottomSecond(allIndices);
		mainPointsSecond = translateForCenteringTheBand(mainPointsSecond,
				(secondLeftest + secondRightest) / 2);
		mainPointsSecond = translateToClusterAbsoluteCoordinates(
				mainPointsSecond, second);
		return mainPointsSecond;

	}

	public List<Vec2f> getTestPointsFirst() {
		List<Vec2f> mainPointsFirst = new ArrayList<>(6);
		mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT / 2));
		mainPointsFirst.add(new Vec2f(0, MERGING_AREA_LENGHT));
		mainPointsFirst.add(new Vec2f((float) (allIndices.size()
				* firstElementSize / -2f), MERGING_AREA_LENGHT / 2));
		mainPointsFirst.add(new Vec2f((float) (allIndices.size()
				* firstElementSize / 2f), MERGING_AREA_LENGHT / 2));
		mainPointsFirst.add(new Vec2f((float) (allIndices.size()
				* firstElementSize / -2f), MERGING_AREA_LENGHT));
		mainPointsFirst.add(new Vec2f((float) (allIndices.size()
				* firstElementSize / 2f), MERGING_AREA_LENGHT));
		mainPointsFirst = translateForRotation(mainPointsFirst);
		mainPointsFirst = rotate(mainPointsFirst, rotationMatrixFirst);
		if (!isStartsLeftFirst())
			mainPointsFirst = translateToRight(mainPointsFirst, first);
		findTopAndBottomFirst(allIndices);
		mainPointsFirst = translateForCenteringTheBand(mainPointsFirst,
				(firstAtTop + firstAtBottom) / 2);
		mainPointsFirst = translateToClusterAbsoluteCoordinates(
				mainPointsFirst, first);
		return mainPointsFirst;

	}

	private Band createSubBandFromFirst(List<Integer> subBand, int startIndex) {
		findTopAndBottomFirst(subBand);
		float middle = (firstAtTop + firstAtBottom) / 2;
		List<Vec2f> bandPointsClusterRelative = new ArrayList<>(4);
		float xPos = isStartsLeftFirst() ? 0 : first.getSize().x();
		bandPointsClusterRelative.add(new Vec2f(xPos,middle));
		xPos = isStartsLeftFirst() ? -BAND_CLUSTER_OFFSET : first.getSize()
				.x() + BAND_CLUSTER_OFFSET;
		bandPointsClusterRelative.add(new Vec2f(xPos, middle));

		List<Vec2f> bandPoints = new ArrayList<>(2);
		middle = (float) ((startIndex + subBand.size() / 2f - allIndices
				.size() / 2f) * firstElementSize);
		bandPoints.add(new Vec2f(0,middle));
		bandPoints.add(new Vec2f(MERGING_AREA_LENGHT / 2,middle ));
		bandPoints = translateForRotation(bandPoints);
		bandPoints = rotate(bandPoints, rotationMatrixFirst);
		if (!isStartsLeftFirst())
			bandPoints = translateToRight(bandPoints, first);
		findTopAndBottomFirst(allIndices);
		bandPoints = translateForCenteringTheBand(bandPoints,
				(firstAtTop + firstAtBottom) / 2f);
		// now bandPoints contains cluster relative coordinates

		bandPointsClusterRelative.addAll(bandPoints);
		bandPointsClusterRelative = translateToClusterAbsoluteCoordinates(
				bandPointsClusterRelative, first);
		return TesselatedPolygons.band(bandPointsClusterRelative, 0,
				(float) subBand.size() * (float) firstElementSize / 2f,
				SUBBAND_POLYGONS);

	}

	// moves the vertices to the bottom of the cluster
	// v.x += cluster.getSize().x();
	private List<Vec2f> translateToRight(List<Vec2f> toTranslate,
			ClusterElement cluster) {
		Vec2f offset = new Vec2f(cluster.getSize().x(), 0);
		for (Vec2f v : toTranslate) {
			v.add(offset);
		}
		return toTranslate;
	}

	// moves all vertices to the right to make sure the band starts in the
	// center of the leftest and rightest index
	private List<Vec2f> translateForCenteringTheBand(List<Vec2f> toMove,
			float offset) {
		Vec2f offsetV = new Vec2f(0, offset);
		for (Vec2f v : toMove) {
			v.add(offsetV);
		}
		return toMove;
	}

	private List<Vec2f> rotate(List<Vec2f> toRotate, float[] rotationMatrix) {
		for (Vec2f v : toRotate) {
			float oldX = v.x();
			float oldY = v.y();
			v.setX(oldX * rotationMatrix[0] + oldY * rotationMatrix[2]);
			v.setY(oldX * rotationMatrix[1] + oldY * rotationMatrix[3]);
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

	@Override
	protected Map<Integer, Band> getConnectionsSplines() {
		Map<Integer, Band> bandsMap = new IdentityHashMap<>();
		if (allIndices.size() == 1)
			return bandsMap;
		int firstIndex = 0;
		int secondIndex = 0;
		for (List<Integer> list : firstIndices) {
			for (Integer i : list) {
				secondIndex = getIndex(secondIndices, i);
				secondIndex = isStartsRightSecond() ? allIndices.size()
						- secondIndex - 1 : secondIndex;
				int firstDelivery = isStartsLeftFirst() ? allIndices.size()
						- firstIndex - 1 : firstIndex;
				bandsMap.put(
						i,
						createBSplineBandFromIndex(i, firstDelivery,
								secondIndex));
				firstIndex++;
			}
		}
		bandsMap.clear();
		return bandsMap;
	}

	private int getIndex(List<List<Integer>> source, int target) {
		int index = 0;
		for (List<Integer> list : source) {
			for (Integer i : list) {
				if (target == i)
					return index;
				index++;
			}
		}
		// shouldn't be executed
		return index;
	}

	private Band createBSplineBandFromIndex(Integer i, int firstIndex,
			int secondIndex) {
		List<Vec2f> finalPoints = new ArrayList<>();
		if (firstIndices.size() == 1) {
			List<Vec2f> firstPoints = new ArrayList<>(4);
			firstAtTop = first.getRecPosOf(i);
			firstAtBottom = firstAtTop + (float) firstElementSize;
			float middle = (firstAtTop + firstAtBottom) / 2;
			float yPos = isStartsLeftFirst() ? 0 : first.getSize().y();
			firstPoints.add(new Vec2f(middle, yPos));
			yPos = isStartsLeftFirst() ? -BAND_CLUSTER_OFFSET : first
					.getSize().y() + BAND_CLUSTER_OFFSET;
			firstPoints.add(new Vec2f(middle, yPos));
			finalPoints = translateToClusterAbsoluteCoordinates(firstPoints,
					first);
		} else {

			firstAtTop = first.getRecPosOf(i);
			firstAtBottom = firstAtTop + (float) firstElementSize;
			float startPosX = (firstAtTop + firstAtBottom) / 2;
			List<Vec2f> bandPointsClusterRelative = new ArrayList<>();
			float yPos = isStartsLeftFirst() ? 0 : first.getSize().y();
			bandPointsClusterRelative.add(new Vec2f(startPosX, yPos));
			yPos = isStartsLeftFirst() ? -BAND_CLUSTER_OFFSET : first
					.getSize().y() + BAND_CLUSTER_OFFSET;
			bandPointsClusterRelative.add(new Vec2f(startPosX, yPos));

			List<Vec2f> bandPoints = new ArrayList<>(2);
			startPosX = (float) ((firstIndex + 0.5f - allIndices.size() / 2f) * firstElementSize);
			bandPoints.add(new Vec2f(startPosX, 0));
			bandPoints.add(new Vec2f(startPosX, MERGING_AREA_LENGHT / 2));
			bandPoints = translateForRotation(bandPoints);
			bandPoints = rotate(bandPoints, rotationMatrixFirst);
			if (!isStartsLeftFirst())
				bandPoints = translateToRight(bandPoints, first);
			findTopAndBottomFirst(allIndices);
			bandPoints = translateForCenteringTheBand(bandPoints,
					(firstAtTop + firstAtBottom) / 2f);
			// now bandPoints contains cluster relative coordinates

			bandPointsClusterRelative.addAll(bandPoints);
			finalPoints = translateToClusterAbsoluteCoordinates(
					bandPointsClusterRelative, first);

			// Points on the main band
			List<Vec2f> mainPointsFirst = new ArrayList<>(4);
			mainPointsFirst.add(new Vec2f(startPosX, MERGING_AREA_LENGHT / 2));
			mainPointsFirst.add(new Vec2f(startPosX, MERGING_AREA_LENGHT));
			mainPointsFirst = translateForRotation(mainPointsFirst);
			mainPointsFirst = rotate(mainPointsFirst, rotationMatrixFirst);
			if (!isStartsLeftFirst())
				mainPointsFirst = translateToRight(mainPointsFirst, first);
			findTopAndBottomFirst(allIndices);
			mainPointsFirst = translateForCenteringTheBand(mainPointsFirst,
					(firstAtTop + firstAtBottom) / 2);
			mainPointsFirst = translateToClusterAbsoluteCoordinates(
					mainPointsFirst, first);
			finalPoints.addAll(mainPointsFirst);

		}

		// TODO add the central crossing point

		if (secondIndices.size() == 1) {
			// there is only one band from the second cluster to the first
			// cluster area.
			// use coordinates provided in bandPoints from the first Cluster for
			// creating the band.
			List<Vec2f> secondPoints = new ArrayList<>(2);
			secondLeftest = second.getRecPosOf(i);
			secondRightest = secondLeftest + (float) secondElementSize;
			float middle = (secondLeftest + secondRightest) / 2;
			float yPos = isStartsRightSecond() ? -BAND_CLUSTER_OFFSET : second
					.getSize().y() + BAND_CLUSTER_OFFSET;
			secondPoints.add(new Vec2f(middle, yPos));
			yPos = isStartsRightSecond() ? 0 : second.getSize().y();
			secondPoints.add(new Vec2f(middle, yPos));
			secondPoints = translateToClusterAbsoluteCoordinates(secondPoints,
					second);
			finalPoints.addAll(secondPoints);
			return TesselatedPolygons.band(finalPoints, 0, SPLINE_RADIUS,
					SPLINE_POLYGONS);
		} else {

			float startPosX = (float) ((secondIndex + 0.5f - allIndices.size() / 2f) * secondElementSize);

			List<Vec2f> mainPointsSecond = new ArrayList<>();
			mainPointsSecond.add(new Vec2f(startPosX, MERGING_AREA_LENGHT));
			mainPointsSecond.add(new Vec2f(startPosX, MERGING_AREA_LENGHT / 2));
			mainPointsSecond = translateForRotation(mainPointsSecond);
			mainPointsSecond = rotate(mainPointsSecond, rotationMatrixSecond);
			if (!isStartsRightSecond())
				mainPointsSecond = translateToRight(mainPointsSecond, second);
			findTopAndBottomSecond(allIndices);
			mainPointsSecond = translateForCenteringTheBand(mainPointsSecond,
					(secondLeftest + secondRightest) / 2);
			mainPointsSecond = translateToClusterAbsoluteCoordinates(
					mainPointsSecond, second);
			finalPoints.addAll(mainPointsSecond);

			secondLeftest = second.getRecPosOf(i);
			secondRightest = secondLeftest + (float) secondElementSize;
			startPosX = (secondLeftest + secondRightest) / 2;
			List<Vec2f> bandPointsClusterRelative = new ArrayList<>(4);
			float yPos = isStartsRightSecond() ? -BAND_CLUSTER_OFFSET : second
					.getSize().y() + BAND_CLUSTER_OFFSET;
			bandPointsClusterRelative.add(new Vec2f(startPosX, yPos));
			yPos = isStartsRightSecond() ? 0 : second.getSize().y();
			bandPointsClusterRelative.add(new Vec2f(startPosX, yPos));

			List<Vec2f> bandPoints = new ArrayList<>(2);
			startPosX = (float) ((secondIndex + 0.5f - allIndices.size() / 2f) * secondElementSize);
			bandPoints.add(new Vec2f(startPosX, MERGING_AREA_LENGHT / 2));
			bandPoints.add(new Vec2f(startPosX, 0));
			bandPoints = translateForRotation(bandPoints);
			bandPoints = rotate(bandPoints, rotationMatrixSecond);
			if (!isStartsRightSecond())
				bandPoints = translateToRight(bandPoints, second);
			findTopAndBottomSecond(allIndices);
			bandPoints = translateForCenteringTheBand(bandPoints,
					(secondLeftest + secondRightest) / 2f);
			// now bandPoints contains cluster relative coordinates

			bandPoints.addAll(bandPointsClusterRelative);
			bandPoints = translateToClusterAbsoluteCoordinates(bandPoints,
					second);
			finalPoints.addAll(bandPoints);
			return TesselatedPolygons.band(finalPoints, 0, SPLINE_RADIUS,
					SPLINE_POLYGONS);
		}

	}

}
