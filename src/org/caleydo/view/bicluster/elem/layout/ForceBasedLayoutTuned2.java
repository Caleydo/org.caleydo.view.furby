/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.util.List;
import java.util.Random;

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
	private final Random r = new Random();

	public ForceBasedLayoutTuned2(AllClustersElement parent) {
		super(parent);
		repulsion = 1000f;
		attractionFactor = 100f;
		borderForceFactor = 200f;
	}

	@Override
	public void forcedBasedLayout(List<ForcedBody> bodies, List<ForcedBody> fixed, final int iterations, float w,
			float h) {

		// calculate the attraction based on the size of all overlaps
		int dimOverlapSize = 0, recOverlapSize = 0;
		double areaFilled = 0;
		for (ForcedBody fix : fixed) {
			areaFilled += fix.getArea();
		}
		for (ForcedBody body : bodies) {
			if (body.isFocussed()) {
				body.setLocation(w * 0.5f, h * 0.5f);
			}
			ClusterElement v = body.asClusterElement();
			dimOverlapSize += v.getDimTotalOverlaps();
			recOverlapSize += v.getRecTotalOverlaps();

			areaFilled += body.getArea();
			if (body.isFixed())
				fixed.add(body); // handle fixed bodies as fixed objects
		}

		areaFilled /= (w * h);

		for (int i = 0; i < iterations; i++) {
			double frameAlpha = 1;
			forceDirectedLayout(bodies, fixed, w, h, frameAlpha, dimOverlapSize, recOverlapSize, areaFilled);
		}
	}

	private void forceDirectedLayout(List<ForcedBody> bodies, List<ForcedBody> fixedBodies, float w, float h,
			double frameAlpha, int overlapDim, int overlapRec, double areaFilled) {

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
				addAttraction(body, other, distVec, distLength, overlapDim, overlapRec);
			}
			if (!body.isFixed()) { // don't waste time if the element is active
				addFrame(w, h, body, frameAlpha);
				addGravity(w, h, body);
				addFixedBodyRespulsion(fixedBodies, body);
			}
		}

		applyForce(bodies, frameAlpha, overlapDim, overlapRec, areaFilled);

	}


	private void applyForce(List<ForcedBody> bodies, double frameFactor, int overlapDim, int overlapRec,
			double areaFilled) {
		// use the area filled as an indicator how repulsive the elements should be
		// the less filled the more repulsion
		// the less filled the less attraction
		final double attraction = clamp(0.20 * areaFilled, 0.01, 0.1);
		final double repulsion = clamp((1 - areaFilled) * 50, 5, 80);

		// count forces together + apply + reset
		for (ForcedBody body : bodies) { // reset forces
			if (!body.isVisible() || body.isFixed()) {
				body.resetForce();
				continue;
			}
			final double repForceX = checkPlausibility(body.getRepForceX() * repulsion);
			final double repForceY = checkPlausibility(body.getRepForceY() * repulsion);
			double attForceX = checkPlausibility(body.getAttForceX() * attraction);
			double attForceY = checkPlausibility(body.getAttForceY() * attraction);

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

	private double clamp(double v, double min, double max) {
		if (v < min)
			return min;
		if (v > max)
			return max;
		return v;
	}

	private static void addFixedBodyRespulsion(List<ForcedBody> toolBars, final ForcedBody body) {
		for (ForcedBody toolbar : toolBars) {
			final Distance distVec = body.distanceTo(toolbar);
			addRepulsion(body, toolbar, distVec, distVec.getDistance());
		}
	}

	private void addFrame(float w, float h, final ForcedBody body, double frameAlpha) {
		Vec2d distFromTopLeft = getDistanceFromTopLeft(body, w, h);
		Vec2d distFromBottomRight = getDistanceFromBottomRight(body, w, h);

		// in earlier frames simulate a larger space (twice as large in the first iteration)
		final double offsetX = 20 - w * (1 - frameAlpha);
		final double offsetY = 20 - h * (1 - frameAlpha);

		final double left = distFromTopLeft.x() - offsetX;
		final double top = distFromTopLeft.y() - offsetY;
		final double right = -distFromBottomRight.x() - offsetX;
		final double bottom = -distFromBottomRight.y() - offsetY;

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

		double scale = (body.isFixed() || other.isFixed() ? 2 : 1);
		scale /= (distLength * distLength);
		repX = v.x() * scale;
		repY = v.y() * scale;
		// as distance symmetrical
		if (Double.isNaN(repX))
			System.err.println();
		body.addRepForce(repX, repY);
		other.addRepForce(-repX, -repY);
	}

	private static void addAttraction(final ForcedBody body, final ForcedBody other, final Distance distVec,
			final double distLength, int dimOverlapTotal, int recOverlapTotal) {
		if (distVec.isIntersection()) // too close
			return;
		final int dimOverlap = body.getDimOverlap(other);
		final int recOverlap = body.getRecOverlap(other);
		final int totalOverlap = recOverlap + dimOverlap;

		if (totalOverlap <= 0) // no overlap
			return;

		final double recPercent = recOverlap == 0 ? 0 : recOverlap / (double) recOverlapTotal;
		final double dimPercent = dimOverlap == 0 ? 0 : dimOverlap / (double) dimOverlapTotal;

		// central force
		{
			double factor = 0.5 * (recPercent + dimPercent);
			double accX = distVec.x() * factor;
			double accY = distVec.y() * factor;
			// as distance symmetrical
			body.addAttForce(-accX, -accY);
			other.addAttForce(accX, accY);
		}

		final double offset = 20; // [px]
		// overlap specific force
		if (recOverlap > 0) { // rec overlap connector in x dimension
			double b_l = body.x0() - offset;
			double b_r = body.x1() + offset;
			double o_l = other.x0() - offset;
			double o_r = other.x1() + offset;

			double y = body.getCenterY() - body.getCenterY();
			addAttractionConnector(body, other, recPercent, b_l - o_r, y, b_r - o_l, y);
		}

		if (dimOverlap > 0) {
			double b_l = body.y0() - offset;
			double b_r = body.y1() + offset;
			double o_l = other.y0() - offset;
			double o_r = other.y1() + offset;

			double x = body.getCenterX() - body.getCenterX();
			addAttractionConnector(body, other, recPercent, x, b_l - o_r, x, b_r - o_l);
		}
	}

	private static void addAttractionConnector(final ForcedBody body, final ForcedBody other, double factor,
			final double x1, double y1, final double x2, double y2) {

		final double d0 = x1 * x1 + y1 * y1;
		final double d1 = x2 * x2 + y2 * y2;

		Vec2d d;
		if (d0 < d1)
			d = new Vec2d(x1, y1);
		else
			d = new Vec2d(x2, y1);

		// factor /= d.length();

		double accX = d.x() * factor;
		double accY = d.y() * factor;
		// as distance symmetrical
		body.addAttForce(-accX, -accY);
		other.addAttForce(accX, accY);
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
		for(ForcedBody body : bodies) {
			initialPosition(body, w, h, bodies);
		}
	}

	@Override
	protected void initialPosition(ForcedBody body, float w, float h, List<ForcedBody> bodies) {
		for(ForcedBody neighbor : body.neighbors(bodies)) {
			if (!neighbor.isInvalid()) { //near the first valid neighbor
				int rec = body.getRecOverlap(neighbor);
				int dim = body.getDimOverlap(neighbor);
				double offsetX = (body.radiusX + neighbor.radiusX) * 1.2;
				double offsetY = (body.radiusX + neighbor.radiusX) * 1.2;
				if (rec <= 0)
					offsetX = 0;
				if (dim <= 0)
					offsetY = 0;
				body.setLocation(neighbor.getCenterX() + offsetX * (r.nextBoolean() ? 1 : -1),
						neighbor.getCenterY() + offsetY * (r.nextBoolean() ? 1 : -1));
				return;
			}
		}
		body.setLocation(r.nextDouble() * w - 2 * body.radiusX + body.radiusX, r.nextDouble() * h - 2
				* body.radiusY + body.radiusY);
	}
}
