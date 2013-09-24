/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.util.List;

import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.physics.Physics.Distance;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * tuned version of {@link ForceBasedLayout}
 *
 * @author Samuel Gratzl
 *
 */
public class ForceBasedLayoutTuned2 extends AForceBasedLayoutTuned {

	public ForceBasedLayoutTuned2(AllClustersElement parent) {
		super(parent);
		repulsion = 1000f;
		attractionFactor = 100f;
		borderForceFactor = 200f;
	}

	@Override
	public void forcedBasedLayout(List<ForcedBody> bodies, List<ForcedBody> toolBars, final int iterations, float w,
			float h) {

		// calculate the attraction based on the size of all overlaps
		int xOverlapSize = 0, yOverlapSize = 0;
		for (ForcedBody body : bodies) {
			if (body.isFocussed()) {
				body.setLocation(w * 0.5f, h * 0.5f);
			}
			ClusterElement v = body.asClusterElement();
			xOverlapSize += v.getDimTotalOverlaps();
			yOverlapSize += v.getRecTotalOverlaps();
		}

		for (int i = 0; i < iterations; i++) {
			double frameFactor = 1;
			forceDirectedLayout(bodies, toolBars, w, h, frameFactor, xOverlapSize, yOverlapSize);
		}
	}

	private void forceDirectedLayout(List<ForcedBody> bodies, List<ForcedBody> fixedBodies, float w, float h,
			double frameFactor, int overlapX, int overlapY) {

		final int size = bodies.size();

		for (int i = 0; i < size; ++i) { // Loop through Vertices
			final ForcedBody body = bodies.get(i);
			if (!body.isVisible())
				continue;
			// repulsion
			for (int j = i + 1; j < size; ++j) { // loop through other vertices
				final ForcedBody other = bodies.get(j);
				if (!other.isVisible())
					continue;
				// squared distance between "u" and "v" in 2D space
				// calculate the repulsion between two vertices
				final Distance distVec = body.distanceTo(other);
				final double distLength = distVec.getDistance();
				addRepulsion(body, other, distVec, distLength);
				addAttraction(body, other, distVec, distLength, overlapX, overlapY);
			}
			if (!body.isFixed()) { // don't waste time if the element is active
				addFrame(w, h, body);
				addGravity(w, h, body);
				addFixedBodyRespulsion(fixedBodies, body);
			}
		}

		applyForce(bodies, frameFactor, overlapX, overlapY);

	}


	private void applyForce(List<ForcedBody> bodies, double frameFactor, double attraction, int xOverlapSize) {
		// count forces together + apply + reset
		for (ForcedBody body : bodies) { // reset forces
			if (!body.isVisible() || body.isFixed()) {
				body.resetForce();
				continue;
			}
			final double repForceX = checkPlausibility(body.getRepForceX() * 20);
			final double repForceY = checkPlausibility(body.getRepForceY() * 20);
			double attForceX = checkPlausibility(body.getAttForceX() * 1);
			double attForceY = checkPlausibility(body.getAttForceY() * 1);

			final double frameForceX = 1 * body.getFrameForceX();
			final double frameForceY = 1 * body.getFrameForceY();

			double forceX = repForceX + attForceX + frameForceX;
			double forceY = repForceY + attForceY + frameForceY;

			// System.out.println("  Att: " + attForceX + " " + attForceY);
			// System.out.println("  Rep: " + repForceX + " " + repForceY);
			// System.out.println("  Fra: " + frameForceX + " " + frameForceY);
			// System.out.println("  Sum: " + forceX + " " + forceY);

			body.move(forceX, forceY);
			body.resetForce();
		}
	}

	private static void addFixedBodyRespulsion(List<ForcedBody> toolBars, final ForcedBody body) {
		for (ForcedBody toolbar : toolBars) {
			final Vec2d distVec = body.distanceTo(toolbar);
			final double distLength = distVec.length();
			double rsq = distLength * distLength * distLength;
			double xForce = 1.5f * distVec.x() / rsq;
			double yForce = 1.5f * distVec.y() / rsq;
			body.addRepForce(xForce, yForce);
		}
	}

	private void addFrame(float w, float h, final ForcedBody body) {
		Vec2d distFromTopLeft = getDistanceFromTopLeft(body, w, h);
		Vec2d distFromBottomRight = getDistanceFromBottomRight(body, w, h);
		final double left = distFromTopLeft.x() - 20;
		final double top = distFromTopLeft.y() - 20;
		final double right = -distFromBottomRight.x() - 20;
		final double bottom = -distFromBottomRight.y() - 20;

		double xForce = 0;
		double yForce = 0;

		final double border = 1. / (Math.min(w, h) * 0.15);

		xForce += borderForce(left, border);
		xForce -= borderForce(right, border);

		yForce += borderForce(top, border);
		yForce -= borderForce(bottom, border);

		body.addFrameForce(xForce, yForce);
	}

	/**
	 * small force in the center
	 *
	 * @param w
	 * @param h
	 * @param body
	 */
	private void addGravity(float w, float h, ForcedBody body) {
		final double cx = body.getCenterX() - w * 0.5;
		final double cy = body.getCenterY() - h * 0.5;

		final double centerForce = -0.001;
		body.addFrameForce(cx * centerForce, cy * centerForce);
	}

	/**
	 * compute the border force, such as it will look like
	 *
	 * <pre>
	 * \
	 *   \
	 *     \ real border, sharp drop off
	 *      \
	 *       \
	 *        ____
	 * </pre>
	 *
	 * @param dist
	 *            distance to border
	 * @param dropOff
	 * @return
	 */
	private static double borderForce(final double dist, final double dropOff) {
		final double k = .1;
		final int pow = 3;
		final double c = pow;

		if (dist < 0) {
			return c - dist * k;
		} else {
			double t = 1 - Math.min(dist * dropOff, 1);
			return c * pow(t, pow);
		}
	}

	/**
	 * @param max
	 * @param i
	 * @return
	 */
	private static double pow(double v, int i) {
		switch (i) {
		case 0:
			return 1;
		case 1:
			return v;
		case 2:
			return v * v;
		case 3:
			return v * v * v;
		case 4:
			double v2 = v * v;
			return v2 * v2;
		case 5:
			v2 = v * v;
			return v2 * v2 * v;
		case 6:
			v2 = v * v * v;
			return v2 * v2;
		default:
			return Math.pow(v, i);
		}
	}

	private static void addRepulsion(final ForcedBody body, final ForcedBody other, final Distance distVec,
			double distLength) {
		double repX;
		double repY;
		// min distance for two elements
		final double min_distance = (distVec.getR1() + distVec.getR2()) * 0.25;

		Vec2d v;
		if (distVec.isIntersection())
			v = distVec.times(-1); // as the dist vec has the wrong direction
		else
			v = distVec;
		if (distLength < min_distance) // at least the min distance
			distLength = min_distance;

		double rsq = distLength * distLength;
		repX = v.x() / rsq;
		repY = v.y() / rsq;
		// as distance symmetrical
		if (Double.isNaN(repX))
			System.err.println();
		body.addRepForce(repX, repY);
		other.addRepForce(-repX, -repY);
	}

	private static void addAttraction(final ForcedBody body, final ForcedBody other, final Distance distVec,
			final double distLength, int overlapX, int overlapY) {
		if (distVec.isIntersection())
			return;
		int yoverlap = body.getRecOverlap(other);
		int xoverlap = body.getDimOverlap(other);
		addAttractionX(body, other, distVec, distLength, overlapX / (double) xoverlap);
		addAttractionX(body, other, distVec, distLength, overlapY / (double) yoverlap);
	}

	private static void addAttractionX(final ForcedBody body, final ForcedBody other, final Distance distVec,
			final double distLength, double overlap) {
		if (!Double.isNaN(overlap) && !Double.isInfinite(overlap) && overlap > 0) {
			// counting the attraction
			double factor = overlap / distLength;
			double accX = distVec.x() * factor;
			double accY = distVec.y() * factor;
			// as distance symmetrical
			body.addAttForce(-accX, -accY);
			other.addAttForce(accX, accY);
		}
	}

	private static double checkPlausibility(double v) {
		if (v > 1e4)
			return 1e4;
		if (v < -1e4)
			return -1e4;
		return v;
	}

	@Override
	protected void initialLayout(List<ForcedBody> bodies, float w, float h) {
		final int rowCount = (int) (Math.sqrt(bodies.size())) + 1;
		int i = 0;

		float hFactor = (h - 200) / rowCount;
		float wFactor = (w - 300) / (bodies.size() / rowCount + 1);
		for (ForcedBody child : bodies) {
			int row = i / rowCount;
			int col = (i % rowCount);
			child.setLocation(row * wFactor + 200, col * hFactor + 100);
			i++;
		}
	}
}
