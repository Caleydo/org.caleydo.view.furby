/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.toolbar.AToolBarElement;
import org.caleydo.view.bicluster.physics.Physics.Distance;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * tuned version of {@link ForceBasedLayout}
 *
 * @author Samuel Gratzl
 *
 */
public class ForceBasedLayout3 extends AForceBasedLayout {
	/**
	 *
	 */
	private static final double MIN_REPULSION_DISTANCE = 0.1;

	private boolean isInitLayoutDone = false;
	float lastW, lastH;

	/**
	 * counts the number of continuous "another round" calls, used for damping
	 */
	private int continousLayoutDuration = 0;

	public ForceBasedLayout3(AllClustersElement parent) {
		super(parent);
	}
	@Override
	public boolean forceBasedLayout(List<? extends IGLLayoutElement> children, float w, float h, int deltaTimeMs) {
		List<ForcedBody> bodies = toForcedBodies(children);
		List<ForcedBody> toolBars = toToolBarBodies(parent.getToolbars());

		if (!isInitLayoutDone && !children.isEmpty()) {
			initialLayout(bodies, w, h);
			isInitLayoutDone = true;
		} else {
			if (lastW > w || lastH > h)
				scaleView(bodies, w, h);
			lastW = w;
			lastH = h;

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

			int iterations = Math.max(1, computeNumberOfIterations(deltaTimeMs));
			for (int i = 0; i < iterations; i++) {
				double frameFactor = 1;
				forceDirectedLayout(bodies, toolBars, w, h, frameFactor, attraction, xOverlapSize);
			}
		}
		double totalDistanceSquared = 0;
		double damping = time2damping(continousLayoutDuration);
		for (ForcedBody body : bodies)
			totalDistanceSquared += body.apply(damping);

		final boolean anotherRound = totalDistanceSquared > 5 * 5;
		if (anotherRound)
			continousLayoutDuration += deltaTimeMs;
		else
			continousLayoutDuration = 0;
		return anotherRound;
	}

	/**
	 * @param duration
	 * @return
	 */
	private static double time2damping(int duration) {
		if (duration == 0)
			return 1;
		final double maxTime = 5000; // ms
		final double minDamping = 0.00;
		final double maxDamping = 1;
		double factor = Math.max(minDamping, Math.min(maxDamping, 1 - (duration / maxTime)));
		return factor;
	}

	/**
	 * @param toolbars
	 * @return
	 */
	private List<ForcedBody> toToolBarBodies(List<AToolBarElement> toolbars) {
		List<ForcedBody> b = new ArrayList<>(toolbars.size());
		for (AToolBarElement elem : toolbars) {
			if (!elem.isVisible())
				continue;
			b.add(new ForcedBody(GLElementAccessor.asLayoutElement(elem), ForcedBody.FLAG_TOOLBAR));
		}
		return b;
	}

	/**
	 * @param children
	 * @return
	 */
	private List<ForcedBody> toForcedBodies(List<? extends IGLLayoutElement> children) {
		List<ForcedBody> b =new ArrayList<>(children.size());
		for(IGLLayoutElement elem : children) {
			b.add(toForcedBody(elem));
		}
		return b;
	}

	private ForcedBody toForcedBody(IGLLayoutElement elem) {
		int flags = 0;
		GLElement glelem = elem.asElement();
		if (parent.getFocussedElement() == glelem)
			flags |= ForcedBody.FLAG_FOCUSSED;
		if (parent.getDraggedElement() == glelem)
			flags |= ForcedBody.FLAG_DRAGGED;
		if (parent.getHoveredElement() == glelem)
			flags |= ForcedBody.FLAG_HOVERED;
		return new ForcedBody(elem, flags);
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

	private void scaleView(List<ForcedBody> bodies, float w, float h) {
		final double wfactor = w / lastW;
		final double hfactor = h / lastH;
		for (ForcedBody body : bodies) {
			body.setLocation(body.getCenterX() * wfactor, body.getCenterY() * hfactor);
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
				final Distance distVec = body.distanceTo(other);
				final double distLength = distVec.getDistance();
				addRepulsion(body, other, distVec, distLength);
				addAttraction(body, other, distVec, distLength);
			}
			if (!body.isFixed()) { // don't waste time if the element is active
				addFrame(w, h, body);
				addFixedBodyRespulsion(fixedBodies, body);
			}
		}

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
			final Distance distVec = body.distanceTo(toolbar);
			final double distLength = Math.max(MIN_REPULSION_DISTANCE, distVec.getDistance());
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
		if (overlap > 0 && distLength > 0) {
			// counting the attraction
			double factor = (overlap) / distLength;
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

	private static Vec2d getDistanceFromTopLeft(ForcedBody body, float w, float h) {
		return new Vec2d(body.x0(), body.y0());
	}

	private static Vec2d getDistanceFromBottomRight(ForcedBody body, float w, float h) {
		return new Vec2d(w - body.getMaxX(), h - body.getMaxY());
	}

	private void initialLayout(List<ForcedBody> bodies, float w, float h) {
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
