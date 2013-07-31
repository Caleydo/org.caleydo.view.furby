/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.physics;

import java.awt.geom.Rectangle2D;

import org.caleydo.view.bicluster.util.Vec2d;

/**
 * @author Samuel Gratzl
 *
 */
public class Physics {
	/**
	 * @return
	 */
	public static boolean isApproximateRects() {
		return false; // change to true if you use circles
	}
	public static Vec2d distance(Rectangle2D a, Rectangle2D b) {
		// return circleDistance(a, b);
		return aabbDistance(a, b);
	}

	public static Vec2d aabbDistance(Rectangle2D a, Rectangle2D b) {
		final Vec2d distVec = new Vec2d();
		distVec.setX(a.getCenterX() - b.getCenterX());
		distVec.setY(a.getCenterY() - b.getCenterY());
		final double d = distVec.length();
		distVec.scale(1. / d); // aka normalize

		double r1; // = getRadius(a);
		double r2; // = getRadius(b);

		double ray_pos_x = b.getCenterX();
		double ray_pos_y = b.getCenterY();
		Vec2d ray_dir = distVec;
		r1 = d - raxBoxIntersection(ray_pos_x, ray_pos_y, ray_dir, a); // as starting from b
		r2 = raxBoxIntersection(ray_pos_x, ray_pos_y, ray_dir, b);

		final double d_real = d - r1 - r2;
		distVec.scale(d_real);
		return distVec;

	}

	/**
	 * computes the intersection point (as distance to the given ray) to the given box
	 *
	 * @param ray_pos
	 * @param ray_dir
	 * @param r
	 * @return
	 */
	static double raxBoxIntersection(double ray_pos_x, double ray_pos_y, Vec2d ray_dir, Rectangle2D r) {
		// http://www.scratchapixel.com/lessons/3d-basic-lessons/lesson-7-intersecting-simple-shapes/ray-box-intersection/
		final double min_x = r.getMinX();
		final double min_y = r.getMinY();
		final double max_x = r.getMaxX();
		final double max_y = r.getMaxY();
		double tmp;

		double tmin = 0;
		double tmax = 0;
		if (ray_dir.x() != 0) { // corner case = 0
			tmin = (min_x - ray_pos_x) / ray_dir.x();
			tmax = (max_x - ray_pos_x) / ray_dir.x();
			if (tmin > tmax) {
				tmp = tmin;
				tmin = tmax;
				tmax = tmp;
			}
		}
		if (ray_dir.y() != 0) { // corner case = 0
			double tymin = (min_y - ray_pos_y) / ray_dir.y();
			double tymax = (max_y - ray_pos_y) / ray_dir.y();
			if (tymin > tymax) {
				tmp = tymin;
				tymin = tymax;
				tymax = tmp;
			}
			if (ray_dir.x() != 0) {
				if ((tmin > tymax) || (tymin > tmax))
					return 0;
				if (tymin > tmin)
					tmin = tymin;
				if (tymax < tmax)
					tmax = tymax;
			} else {
				tmin = tymin;
				tmax = tymax;
			}
		}
		tmin = Math.abs(tmin);
		if (tmax < tmin)
			return tmax;
		return tmin;
	}

	private static Vec2d circleDistance(Rectangle2D a, Rectangle2D b) {
		Vec2d distVec = new Vec2d();
		distVec.setX(a.getCenterX() - b.getCenterX());
		distVec.setY(a.getCenterY() - b.getCenterY());

		double distance = distVec.length();
		double r1 = getRadius(a);
		double r2 = getRadius(b);
		distance -= r1 + r2;

		distVec.normalize();
		distVec.scale(distance);
		return distVec;
	}

	/**
	 * @param a
	 * @return
	 */
	private static double getRadius(Rectangle2D a) {
		return Math.max(a.getWidth(), a.getHeight()) * 0.5f;
	}

	public static void main(String[] args) {
		Rectangle2D a = new Rectangle2D.Double(0, 0, 2, 2);
		Rectangle2D b = new Rectangle2D.Double(4, 0, 2, 2);
		Rectangle2D c = new Rectangle2D.Double(0, 3, 2, 2);
		Rectangle2D d = new Rectangle2D.Double(3, 3, 2, 2);
		Rectangle2D e = new Rectangle2D.Double(4, 3, 2, 2);

		System.out.println("AABB\t\tcircle");
		System.out.println(String.format("%f\t%f", aabbDistance(a, b).length(), circleDistance(a, b).length()));
		System.out.println(String.format("%f\t%f", aabbDistance(a, c).length(), circleDistance(a, b).length()));
		System.out.println(String.format("%f\t%f", aabbDistance(a, d).length(), circleDistance(a, b).length()));
		System.out.println(String.format("%f\t%f", aabbDistance(a, e).length(), circleDistance(a, b).length()));
	}
}
