/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.util;

import gleem.linalg.Vec2f;


/**
 * @author Michael Gillhofer; mostly copied from {@link Vec2f}
 */
public class Vec2d {
	private double x;
	private double y;

	public Vec2d() {
	}


	public Vec2d(Vec2d arg) {
		this(arg.x, arg.y);
	}

	public Vec2d(double x, double y) {
		set(x, y);
	}

	/**
	 * @param iSizef
	 */
	public Vec2d(Vec2f iSizef) {
		this.x = iSizef.x();
		this.y = iSizef.y();
	}

	public Vec2d copy() {
		return new Vec2d(this);
	}

	public void set(Vec2d arg) {
		set(arg.x, arg.y);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** Sets the ith component, 0 <= i < 2 */
	public void set(int i, double val) {
		switch (i) {
		case 0:
			x = val;
			break;
		case 1:
			y = val;
			break;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	/** Gets the ith component, 0 <= i < 2 */
	public double get(int i) {
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double dot(Vec2d arg) {
		return x * arg.x + y * arg.y;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return this.dot(this);
	}

	public void normalize() {
		double len = length();
		if (len == 0.0f)
			return;
		scale(1.0f / len);
	}

	/** Returns this * val; creates new vector */
	public Vec2d times(double val) {
		Vec2d tmp = new Vec2d(this);
		tmp.scale(val);
		return tmp;
	}

	/** this = this * val */
	public void scale(double val) {
		x *= val;
		y *= val;
	}

	/** Returns this + arg; creates new vector */
	public Vec2d plus(Vec2d arg) {
		Vec2d tmp = new Vec2d();
		tmp.add(this, arg);
		return tmp;
	}

	/** this = this + b */
	public void add(Vec2d b) {
		add(this, b);
	}

	/** this = a + b */
	public void add(Vec2d a, Vec2d b) {
		x = a.x + b.x;
		y = a.y + b.y;
	}

	/** Returns this + s * arg; creates new vector */
	public Vec2d addScaled(double s, Vec2d arg) {
		Vec2d tmp = new Vec2d();
		tmp.addScaled(this, s, arg);
		return tmp;
	}

	/** this = a + s * b */
	public void addScaled(Vec2d a, double s, Vec2d b) {
		x = a.x + s * b.x;
		y = a.y + s * b.y;
	}

	/** Returns this - arg; creates new vector */
	public Vec2d minus(Vec2d arg) {
		Vec2d tmp = new Vec2d();
		tmp.sub(this, arg);
		return tmp;
	}

	/** this = this - b */
	public void sub(Vec2d b) {
		sub(this, b);
	}

	/** this = a - b */
	public void sub(Vec2d a, Vec2d b) {
		x = a.x - b.x;
		y = a.y - b.y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

}

