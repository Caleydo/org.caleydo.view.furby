/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.physics.Physics;
import org.caleydo.view.bicluster.physics.Physics.Distance;

/**
 * representation of a forced element
 *
 * @author Samuel Gratzl
 *
 */
class ForcedBody extends Rectangle2D {
	public static final int FLAG_FOCUSSED = 1 << 1;
	public static final int FLAG_HOVERED = 1 << 2;
	public static final int FLAG_DRAGGED = 1 << 3;
	public static final int FLAG_TOOLBAR = 1 << 4;

	private final int flags;
	private final IGLLayoutElement elem;

	public final double radiusX;
	public final double radiusY;

	private double attForceX = 0;
	private double attForceY = 0;

	private double repForceX = 0;
	private double repForceY = 0;

	private double frameForceX = 0;
	private double frameForceY = 0;

	private double centerX;
	private double centerY;

	public ForcedBody(IGLLayoutElement elem, int flags) {
		this.elem = elem;
		this.flags = flags;
		Vec2f location = ((flags & FLAG_TOOLBAR) != 0) ? elem.asElement().getAbsoluteLocation() : elem.getLocation();
		radiusX = elem.getWidth() * 0.5;
		radiusY = elem.getHeight() * 0.5;
		centerX = location.x() + radiusX;
		centerY = location.y() + radiusY;
	}

	/**
	 * @return the attForceX, see {@link #attForceX}
	 */
	public double getAttForceX() {
		return attForceX;
	}

	/**
	 * @return the attForceY, see {@link #attForceY}
	 */
	public double getAttForceY() {
		return attForceY;
	}

	/**
	 * @return the frameForceX, see {@link #frameForceX}
	 */
	public double getFrameForceX() {
		return frameForceX;
	}

	/**
	 * @return the frameForceY, see {@link #frameForceY}
	 */
	public double getFrameForceY() {
		return frameForceY;
	}

	/**
	 * @return the repForceX, see {@link #repForceX}
	 */
	public double getRepForceX() {
		return repForceX;
	}

	/**
	 * @return the repForceY, see {@link #repForceY}
	 */
	public double getRepForceY() {
		return repForceY;
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

	public int getRecOverlap(ForcedBody other) {
		ClusterElement o = other.asClusterElement();
		ClusterElement c = asClusterElement();
		return c.getRecOverlap(o);
	}

	public int getDimOverlap(ForcedBody other) {
		ClusterElement o = other.asClusterElement();
		ClusterElement c = asClusterElement();
		return c.getDimOverlap(o);
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

	public Distance distanceTo(ForcedBody other) {
		return Physics.distance(this, other);
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

	public boolean isDraggedOrFocussed() {
		return (flags & (FLAG_FOCUSSED | FLAG_DRAGGED)) != 0;
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

	public double y0() {
		return centerY - radiusY;
	}

	public double x0() {
		return centerX - radiusX;
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
		return radiusX * 2;
	}

	@Override
	public double getHeight() {
		return radiusY * 2;
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