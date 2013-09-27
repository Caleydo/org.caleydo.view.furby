/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * tuned version of {@link ForceBasedLayout}
 *
 * @author Samuel Gratzl
 *
 */
public class ForceBasedLayoutTuned extends AForceBasedLayoutTuned {

	public ForceBasedLayoutTuned(AllClustersElement parent) {
		super(parent);
	}

	@Override
	public void forcedBasedLayout(List<ForcedBody> bodies, List<ForcedBody> toolBars, final int iterations, float w,
			float h) {
		bringClustersBackToFrame(bodies, w, h);
		clearClusterCollisions(bodies, toolBars, w, h);

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
		final double attraction = attractionFactor / (xOverlapSize + yOverlapSize);

		// TODO: magic numbers?
		xOverlapSize *= 2;
		yOverlapSize /= 3;

		for (int i = 0; i < iterations; i++) {
			double frameFactor = 1;
			forceDirectedLayout(bodies, toolBars, w, h, frameFactor, attraction, xOverlapSize);
		}
	}

	private void bringClustersBackToFrame(List<ForcedBody> bodies, float w, float h) {
		Rectangle2D frame = new Rectangle2D.Float(0, 0, w, h);
		for (ForcedBody body : bodies) {
			if (!frame.intersects(body))
				body.setLocation(Math.random() * w, Math.random() * h);
		}
	}

	private void clearClusterCollisions(List<ForcedBody> bodies, List<ForcedBody> toolBars, float w, float h) {
		for (ForcedBody body : bodies) {
			if (!body.isVisible())
				continue;
			for (ForcedBody other : bodies) {
				if (body == other || !other.isVisible() || other.isDraggedOrFocussed())
					continue;
				if (body.intersects(other)) {
					other.setLocation((other.getCenterX() + 200) % w, (other.getCenterY() + 200) % h);
				}
			}
			for (ForcedBody toolbar : toolBars) {
				if (body.intersects(toolbar)) {
					body.setLocation((body.getCenterX() - 200) % w, (body.getCenterY() - 200) % h);
				}
			}
		}

	}

	/**
	 * @param children2
	 * @param w
	 * @param h
	 * @param attraction
	 */
	private void forceDirectedLayout(List<ForcedBody> bodies, List<ForcedBody> fixedBodies, float w, float h,
			double frameFactor, double attraction, int xOverlapSize) {

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
				final Vec2d distVec = body.distanceTo(other);
				final double distLength = distVec.length();
				addRepulsion(body, other, distVec, distLength);
				addAttraction(body, other, distVec, distLength);
			}
			if (!body.isFixed()) { // don't waste time if the element is active
				addFrame(w, h, body);
				addFixedBodyRespulsion(fixedBodies, body);
			}
		}

		applyForce(bodies, frameFactor, attraction, xOverlapSize);

	}

	private void applyForce(List<ForcedBody> bodies, double frameFactor, double attraction, int xOverlapSize) {
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
			if (xOverlapSize < 3) {
				attForceX *= 0.2;
				attForceY *= 0.2;
			}
			final double frameForceX = frameFactor * body.getFrameForceX();
			final double frameForceY = frameFactor * body.getFrameForceY();

			double forceX = repForceX + attForceX + frameForceX;
			double forceY = repForceY + attForceY + frameForceY;

			while ((forceX * forceX + forceY * forceY) > 20 * 20) {
				forceX *= 0.1;
				forceY *= 0.1;
			}
			// System.out.println(body.elem + ": ");
			// System.out.println("  Att: " + attForceX + " " + attForceY);
			// System.out.println("  Rep: " + repForceX + " " + repForceY);
			// System.out.println("  Fra: " + body.frameForceX + " " + body.frameForceY);
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
		double xForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.x()));
		xForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.x()));
		double yForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.y()));
		yForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.y()));
		body.addFrameForce(checkPlausibility(xForce), checkPlausibility(yForce));
	}

	private static void addRepulsion(final ForcedBody body, final ForcedBody other, final Vec2d distVec,
			final double distLength) {
		double rsq = distLength * distLength * distLength;
		double repX = distVec.x() / rsq;
		double repY = distVec.y() / rsq;
		// as distance symmetrical
		body.addRepForce(repX, repY);
		other.addRepForce(-repX, -repY);
	}

	private static void addAttraction(final ForcedBody body, final ForcedBody other, final Vec2d distVec,
			final double distLength) {
		int overlap = body.getOverlap(other);
		if (overlap > 0) {
			// counting the attraction
			double accX = distVec.x() * (overlap) / distLength;
			double accY = distVec.y() * (overlap) / distLength;
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

	@Override
	protected void initialPosition(ForcedBody body, float w, float h, List<ForcedBody> bodies) {
		body.setLocation(Math.random() * w - 2 * body.radiusX + body.radiusX, Math.random() * h - 2 * body.radiusY
				+ body.radiusY);
	}
}
