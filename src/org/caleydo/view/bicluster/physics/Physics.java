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
	private static final double ENCLOSED_ELLIPSE_FACTOR = 0.5 * Math.sqrt(2);

	/**
	 * @return
	 */
	public static boolean isApproximateRects() {
		return false; // change to true if you use circles
	}

	public static Distance distance(Rectangle2D a, Rectangle2D b) {
		final Vec2d distVec = new Vec2d();
		distVec.setX(a.getCenterX() - b.getCenterX());
		distVec.setY(a.getCenterY() - b.getCenterY());
		final double d = distVec.length();
		distVec.scale(1. / d); // aka normalize

		final double r1 = ellipseRadius(distVec, a.getWidth() * ENCLOSED_ELLIPSE_FACTOR, a.getHeight()
				* ENCLOSED_ELLIPSE_FACTOR);
		final double r2 = ellipseRadius(distVec, b.getWidth() * ENCLOSED_ELLIPSE_FACTOR, b.getHeight()
				* ENCLOSED_ELLIPSE_FACTOR);
		final double d_real = d - r1 - r2;
		// // final double d_real = aabbDistance(a, b, distVec, d);
		// // final double d_real = ellipseDistance(a, b, distVec, d);
		// final double d_real = enclosedEllipseDistance(a, b, distVec, d);
		// // final double d_real = circleDistance(a, b, distVec, d);
		// // final double d_real = circleDiameterDistance(a, b, distVec, d);

		distVec.scale(d_real);
		return new Distance(distVec, d_real, r1, r2);
	}

	private static double aabbDistance(Rectangle2D a, Rectangle2D b, final Vec2d ray_dir, final double d) {
		double ray_pos_x = b.getCenterX();
		double ray_pos_y = b.getCenterY();
		double r1 = d - raxBoxIntersection(ray_pos_x, ray_pos_y, ray_dir, a); // as starting from b
		double r2 = raxBoxIntersection(ray_pos_x, ray_pos_y, ray_dir, b);

		final double d_real = d - r1 - r2;
		return d_real;
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


	private static double ellipseDistance(Rectangle2D a, Rectangle2D b, final Vec2d ray_dir, final double d) {
		double r1 = ellipseRadius(ray_dir, a.getWidth() * 0.5, a.getHeight() * 0.5);
		double r2 = ellipseRadius(ray_dir, b.getWidth() * 0.5, b.getHeight() * 0.5);
		final double d_real = d - r1 - r2;
		return d_real;
	}

	private static double ellipseRadius(Vec2d ray_dir, double a, double b) {
		// https://en.wikipedia.org/wiki/Ellipse#Polar_form_relative_to_center
		// r(\theta)=\frac{ab}{\sqrt{(b \cos \theta)^2 + (a\sin \theta)^2}}
		double r = (a * b) / (Math.sqrt(Math.pow(a * ray_dir.x(), 2) + Math.pow(b * ray_dir.y(), 2)));

		return r;
	}

	private static double enclosedEllipseDistance(Rectangle2D a, Rectangle2D b, final Vec2d ray_dir, final double d) {
		// FIXME compute from the diameter of the rect a ellipse that touches it
		final double factor = ENCLOSED_ELLIPSE_FACTOR;
		double r1 = ellipseRadius(ray_dir, a.getWidth() * factor, a.getHeight() * factor);
		double r2 = ellipseRadius(ray_dir, b.getWidth() * factor, b.getHeight() * factor);
		final double d_real = d - r1 - r2;
		return d_real;
	}

	private static double circleDistance(Rectangle2D a, Rectangle2D b, final Vec2d distVec, final double d) {
		double r1 = circleRadius(a);
		double r2 = circleRadius(b);
		final double d_real = d - r1 - r2;
		return d_real;
	}

	private static double circleRadius(Rectangle2D a) {
		return Math.max(a.getWidth(), a.getHeight()) * 0.5f;
	}

	private static double circleDiameterDistance(Rectangle2D a, Rectangle2D b, final Vec2d distVec, final double d) {
		double r1 = circleDiameterRadius(a);
		double r2 = circleDiameterRadius(b);
		final double d_real = d - r1 - r2;
		return d_real;
	}

	private static double circleDiameterRadius(Rectangle2D a) {
		return Math.sqrt(Math.pow(a.getWidth() * 0.5f, 2) + Math.pow(a.getHeight() * 0.5f, 2));

	}

	public static void main(String[] args) {
		Rectangle2D a = new Rectangle2D.Double(0, 0, 2, 2);
		Rectangle2D b = new Rectangle2D.Double(4, 0, 2, 2);
		Rectangle2D c = new Rectangle2D.Double(0, 3, 2, 2);
		Rectangle2D d = new Rectangle2D.Double(3, 3, 2, 2);
		Rectangle2D e = new Rectangle2D.Double(4, 3, 2, 2);
		Rectangle2D f = new Rectangle2D.Double(4, 3, 2, 3);

		System.out.println("AABB\t\tellipse\t\tenclosed\t\tcircle\t\tcircleDiameter");
		test(a, b);
		test(a, c);
		test(a, d);
		test(a, e);
		test(a, f);
	}

	private static void test(Rectangle2D a, Rectangle2D b) {
		final Vec2d distVec = new Vec2d();
		distVec.setX(a.getCenterX() - b.getCenterX());
		distVec.setY(a.getCenterY() - b.getCenterY());
		final double d = distVec.length();
		distVec.scale(1. / d); // aka normalize

		final double a_real = aabbDistance(a, b, distVec, d);
		final double e_real = ellipseDistance(a, b, distVec, d);
		final double e2_real = enclosedEllipseDistance(a, b, distVec, d);
		final double c_real = circleDistance(a, b, distVec, d);
		final double d_real = circleDiameterDistance(a, b, distVec, d);
		System.out.format("%f\t%f\t%f\t%f\t%f\n", a_real, e_real, e2_real, c_real, d_real);

	}

	public static final class Distance extends Vec2d {
		private final double distance;
		private final double r1;
		private final double r2;

		public Distance(Vec2d v, double distance, double r1, double r2) {
			super(v);
			this.distance = distance;
			this.r1 = r1;
			this.r2 = r2;
		}

		/**
		 * @return the r1, see {@link #r1}
		 */
		public double getR1() {
			return r1;
		}

		/**
		 * @return the r2, see {@link #r2}
		 */
		public double getR2() {
			return r2;
		}

		/**
		 * @return the distance, see {@link #distance}
		 */
		public double getDistance() {
			return distance;
		}

		public boolean isIntersection() {
			return distance < 0;
		}

	}
}
