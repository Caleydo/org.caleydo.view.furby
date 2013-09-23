/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.AToolBarElement;
import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.physics.Physics;
import org.caleydo.view.bicluster.physics.Physics.Distance;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * tuned version of {@link ForceBasedLayout}
 *
 * @author Samuel Gratzl
 *
 */
public class ForceBasedLayout2 extends AForceBasedLayout {
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

	public ForceBasedLayout2(AllClustersElement parent) {
		super(parent);
		repulsion = 2f;
		attractionFactor = 5f;
		borderForceFactor = 20f;
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

			final double attractionTotalOverlapFactor = attractionFactor / (xOverlapSize + yOverlapSize);

			int iterations = Math.max(1, computeNumberOfIterations(deltaTimeMs));
			IDoubleFunction normalize = DoubleFunctions.normalize(0, iterations);
			for (int i = 0; i < iterations; i++) {
				double iterationFactor = normalize.apply(i);
				forceDirectedLayout(bodies, toolBars, w, h, iterationFactor, attractionTotalOverlapFactor);
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

		{
			double toolbars = 0;
			for (ForcedBody tool : toolBars) {
				toolbars += tool.getWidth() * tool.getHeight();
			}
			double bo = 0;
			for (ForcedBody body : bodies) {
				if (!body.isVisible())
					continue;
				bo += body.getWidth() * body.getHeight();
			}
			double used = toolbars + bo;

			System.out.println("area: " + w + " " + h + " " + w * h);
			System.out.println("toolbars: " + toolbars);
			System.out.println("bodies: " + bo);
			System.err.println("filled: " + (used / (w * h)));
		}
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
			if (!frame.intersects(body) && !body.isFixed()) {
				// out of view -> add strong force to the center

				// FIXME better strategy
				// body.setLocation(Math.random() * w, Math.random() * h);
			}
		}
	}

	private void clearClusterCollisions(List<ForcedBody> bodies, List<ForcedBody> toolBars, float w, float h) {
		// for (ForcedBody body : bodies) {
		// if (!body.isVisible() || body.isFixed())
		// continue;
		// for (ForcedBody other : bodies) {
		// if (body == other || !other.isVisible() || other.isFixed())
		// continue;
		// if (body.intersects(other)) {
		// other.setLocation((other.centerX + 200) % w, (other.centerY + 200) % h);
		// }
		// }
		// for (ForcedBody toolbar : toolBars) {
		// if (body.intersects(toolbar)) {
		// body.setLocation((body.centerX - 200) % w, (body.centerY - 200) % h);
		// }
		// }
		// }

	}

	private void scaleView(List<ForcedBody> bodies, float w, float h) {
		final double wfactor = w / lastW;
		final double hfactor = h / lastH;
		for (ForcedBody body : bodies) {
			body.setLocation(body.centerX * wfactor, body.centerY * hfactor);
		}
	}

	private void forceDirectedLayout(List<ForcedBody> bodies, List<ForcedBody> fixedBodies, float w, float h,
			double iterationFactor, double attraction) {

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
				final Distance distVec = body.distanceTo(other, iterationFactor);
				final double distLength = distVec.getDistance();
				addRepulsion(body, other, distVec, distLength);
				addAttraction(body, other, distVec, distLength);
			}
			if (!body.isFixed()) { // don't waste time if the element is active
				addFrame(w, h, body, iterationFactor);
				addFixedBodyRespulsion(fixedBodies, body, iterationFactor);
			}
		}

		// count forces together + apply + reset
		for (ForcedBody body : bodies) { // reset forces
			if (!body.isVisible() || body.isFixed()) {
				body.resetForce();
				continue;
			}
			final double repForceX = checkPlausibility(body.repForceX * repulsion);
			final double repForceY = checkPlausibility(body.repForceY * repulsion);
			double attForceX = checkPlausibility(body.attForceX * attraction);
			double attForceY = checkPlausibility(body.attForceY * attraction);

			final double frameForceX = iterationFactor * body.frameForceX;
			final double frameForceY = iterationFactor * body.frameForceY;

			double forceX = repForceX + attForceX + frameForceX;
			double forceY = repForceY + attForceY + frameForceY;

//			while ((forceX * forceX + forceY * forceY) > 20 * 20) {
//				forceX *= 0.1;
//				forceY *= 0.1;
//			}

			// System.out.println(body.elem + ": ");
			// System.out.println("  Att: " + attForceX + " " + attForceY);
			// System.out.println("  Rep: " + repForceX + " " + repForceY);
			// System.out.println("  Fra: " + body.frameForceX + " " + body.frameForceY);
			// System.out.println("  Sum: " + forceX + " " + forceY);
			body.move(forceX, forceY);
			body.resetForce();
		}

	}

	private static void addFixedBodyRespulsion(List<ForcedBody> toolBars, final ForcedBody body, double iterationFactor) {
		for (ForcedBody toolbar : toolBars) {
			final Distance distVec = body.distanceTo(toolbar, iterationFactor);
			final double distLength = Math.max(MIN_REPULSION_DISTANCE, distVec.getDistance());
			double rsq = distLength * distLength;
			double xForce = 1.5f * distVec.x() / rsq;
			double yForce = 1.5f * distVec.y() / rsq;
			body.addRepForce(xForce, yForce);
		}
	}

	private void addFrame(float w, float h, final ForcedBody body, double iterationFactor) {
		Vec2d distFromTopLeft = getDistanceFromTopLeft(body, w, h);
		Vec2d distFromBottomRight = getDistanceFromBottomRight(body, w, h); // positive

		double xForce = 0;
		xForce += frameForce(distFromTopLeft.x());
		xForce -= frameForce(distFromBottomRight.x());
		double yForce = 0;
		yForce += frameForce(distFromTopLeft.y());
		yForce -= frameForce(distFromBottomRight.y());

		xForce *= iterationFactor;
		yForce *= iterationFactor;
		body.addFrameForce(checkPlausibility(xForce), checkPlausibility(yForce));
	}

	private double frameForce(double value) {
		if (value < 0) {
			return value;
		} else
			return Math.exp(borderForceFactor / value);
	}

	private static void addRepulsion(final ForcedBody body, final ForcedBody other, final Vec2d distVec,
			double distLength) {
		distLength = Math.max(MIN_REPULSION_DISTANCE, distLength);
		double rsq = distLength * distLength;
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

	private static class ForcedBody extends Rectangle2D {
		public static final int FLAG_FOCUSSED = 1 << 1;
		public static final int FLAG_HOVERED = 1 << 2;
		public static final int FLAG_DRAGGED = 1 << 3;
		public static final int FLAG_TOOLBAR = 1 << 4;

		private final int flags;
		private final IGLLayoutElement elem;
		private final Vec2f size;
		private final double radiusX;
		private final double radiusY;

		private double attForceX = 0;
		private double attForceY = 0;

		private double repForceX = 0;
		private double repForceY = 0;

		private double frameForceX = 0;
		private double frameForceY = 0;

		private double centerX;
		private double centerY;
		// scale of radius
		private double scaleFactor = 1.;

		public ForcedBody(IGLLayoutElement elem, int flags) {
			this.elem = elem;
			this.flags = flags;
			Vec2f location = ((flags & FLAG_TOOLBAR) != 0) ? elem.asElement().getAbsoluteLocation() : elem
					.getLocation();
			this.size = new Vec2f(elem.getWidth(), elem.getHeight());
			radiusX = size.x() * 0.5;
			radiusY = size.y() * 0.5;
			centerX = location.x() + radiusX;
			centerY = location.y() + radiusY;
		}

		public void move(double x, double y) {
			centerX += x;
			centerY += y;
		}

		public int getOverlap(ForcedBody other) {
			ClusterElement o = other.asClusterElement();
			ClusterElement c = asClusterElement();
			int rsize = c.getRecOverlap(o);
			int csize = c.getDimOverlap(o);
			return rsize + csize;
		}

		public void addAttForce(double attX, double attY) {
			attForceX += attX;
			attForceY += attY;
		}

		public void addRepForce(double repX, double repY) {
			repForceX += repX;
			repForceY += repY;
		}

		public void addFrameForce(double xForce, double yForce) {
			frameForceX += xForce;
			frameForceY += yForce;
		}

		public Distance distanceTo(ForcedBody other, double iterationFactor) {
			other.scaleFactor = this.scaleFactor = Math.max(0.2, iterationFactor);
			Distance d = Physics.distance(this, other);
			other.scaleFactor = this.scaleFactor = 1;
			return d;
		}

		public void resetForce() {
			attForceX = 0;
			attForceY = 0;
			repForceX = 0;
			repForceY = 0;
			frameForceX = 0;
			frameForceY = 0;
		}

		/**
		 * @return
		 */
		public ClusterElement asClusterElement() {
			assert !isToolBar();
			return (ClusterElement) elem.asElement();
		}

		public boolean isFocussed() {
			return (flags & FLAG_FOCUSSED) != 0;
		}

		public boolean isFixed() {
			return (flags & (FLAG_FOCUSSED | FLAG_DRAGGED | FLAG_HOVERED)) != 0;
		}

		public boolean isToolBar() {
			return (flags & FLAG_TOOLBAR) != 0;
		}

		private double y0() {
			return centerY - radiusY * scaleFactor;
		}

		private double x0() {
			return centerX - radiusX * scaleFactor;
		}

		/**
		 * set the new location
		 *
		 * @param damping
		 *
		 * @return the squared distance moved
		 */
		public double apply(double damping) {
			assert !isToolBar();
			Vec2f ori = elem.getLocation();
			double x0 = x0();
			double y0 = y0();
			// where we want to be
			if (damping < 1) {
				double dx = x0 - ori.x();
				double dy = y0 - ori.y();
				x0 = ori.x() + dx * damping;
				y0 = ori.y() + dy * damping;
			}
			// really set the location
			elem.setLocation((float) x0, (float) y0);

			// distance moved
			x0 -= ori.x();
			y0 -= ori.y();
			return x0 * x0 + y0 * y0;
		}

		public void setLocation(double x, double y) {
			centerX = x;
			centerY = y;
		}

		public boolean isVisible() {
			GLElement elem = this.elem.asElement();
			return elem.getVisibility().doRender() && elem.getParent() != null;
		}

		@Override
		public void setRect(double x, double y, double w, double h) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int outcode(double x, double y) {
			int out = 0;
			if (this.getWidth() <= 0) {
				out |= OUT_LEFT | OUT_RIGHT;
			} else if (x < this.x0()) {
				out |= OUT_LEFT;
			} else if (x > this.x0() + this.getWidth()) {
				out |= OUT_RIGHT;
			}
			if (this.getHeight() <= 0) {
				out |= OUT_TOP | OUT_BOTTOM;
			} else if (y < this.y0()) {
				out |= OUT_TOP;
			} else if (y > this.y0() + this.getHeight()) {
				out |= OUT_BOTTOM;
			}
			return out;
		}

		@Override
		public Rectangle2D createIntersection(Rectangle2D r) {
			Rectangle2D dest = new Rectangle2D.Double();
			Rectangle2D.intersect(this, r, dest);
			return dest;
		}

		@Override
		public Rectangle2D createUnion(Rectangle2D r) {
			Rectangle2D dest = new Rectangle2D.Double();
			Rectangle2D.union(this, r, dest);
			return dest;
		}

		@Override
		public double getX() {
			return x0();
		}

		@Override
		public double getY() {
			return y0();
		}

		@Override
		public double getWidth() {
			return size.x() * scaleFactor;
		}

		@Override
		public double getHeight() {
			return size.y() * scaleFactor;
		}

		@Override
		public double getCenterX() {
			return centerX;
		}

		@Override
		public double getCenterY() {
			return centerY;
		}

		@Override
		public boolean isEmpty() {
			return (getWidth() <= 0.0 || getHeight() <= 0.0);
		}

		@Override
		public String toString() {
			return String.format("%s %2f %2f", elem.toString(), centerX, centerY);
		}
	}
}
