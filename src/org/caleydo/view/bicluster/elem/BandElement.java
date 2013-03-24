/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLContext;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;

/**
 * @author user
 *
 */
public class BandElement extends GLElement {


	static float[] colorY = Colors.BLUE.getRGBA();
	static float[] colorX = Colors.GREEN.getRGBA();
	private ConnectionBandRenderer bandRenderer = new ConnectionBandRenderer();

	/**
	 * @param view
	 */
	public BandElement(AGLView view, AllBandsElement bands, boolean dimBand, List<Integer> overlap, GLElement first,
			GLElement second) {
		this.view = view;
		this.root = bands;
		this.dimBand = dimBand;
		this.overlap = overlap;
		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;
		bandRenderer.init(GLContext.getCurrentGL().getGL2());

	}


	private AllBandsElement root;
	private final AGLView view;

	private ClusterElement first;
	private ClusterElement second;
	private boolean dimBand;
	private List<Integer> overlap;

	private List<Pair<Point2D, Point2D>> points;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElementAdapter#renderImpl(org.caleydo.core.view.opengl.layout2.GLGraphics,
	 * float, float)
	 */
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		boolean highlight = false;
		float[] color = dimBand ? colorY : colorX;
		// System.out.println(dimBand);
		bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), points, highlight, color, .5f);

		// super.renderImpl(g, w, h);
		// System.out.println(first.getId() + "/" + second.getId());
	}


	public void updatePosition() {
		if (dimBand) {
			double startDimBandScaleFactor = first.getSize().x() / (double) first.getNumberOfDimElements();
			double endDimBandScaleFactor = second.getSize().x() / (double) second.getNumberOfDimElements();
			int xOverlapSize = first.getxOverlap(second).size();
			if (xOverlapSize > 0) {
				points = addDimPointsToBand(xOverlapSize, startDimBandScaleFactor, endDimBandScaleFactor);
			}
		} else {
			double endRecBandScaleFactor = second.getSize().y() / (double) second.getNumberOfRecElements();
			double startRecBandScaleFactor = first.getSize().y() / (double) first.getNumberOfRecElements();
			int yOverlapSize = first.getyOverlap(second).size();
			if (yOverlapSize > 0) {
				points = addRecPointsToBand(yOverlapSize, startRecBandScaleFactor, endRecBandScaleFactor);
			}
		}
		repaint();

		// bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), point, highlight, colorY, .5f);

	}

	private List<Pair<Point2D, Point2D>> addDimPointsToBand(int xOS, double firDimScaFac, double secDimScFac) {

		Vec2f fLoc = first.getLocation();
		Vec2f sLoc = second.getLocation();
		Vec2f fSize = first.getSize();
		Vec2f sSize = second.getSize();
		List<Pair<Point2D, Point2D>> points = new ArrayList<>();
		setLocation(1, 1);
		setSize(2, 2);
		if (fLoc.y() < sLoc.y()) {
			// first on top
			if (fLoc.y() + fSize.y() < sLoc.y()) {
				// second far at the bottom
				points.add(pair(fLoc.x(), fLoc.y() + fSize.y(), (float) (fLoc.x() + firDimScaFac * xOS), fLoc.y()
						+ fSize.y()));
				points.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * xOS), sLoc.y()));
			} else {
				// second in between
				points.add(pair(first.getLocation().x(), first.getLocation().y(),
						(float) (first.getLocation().x() + firDimScaFac * xOS), first.getLocation().y()));
				points.add(pair(second.getLocation().x(), second.getLocation().y(),
						(float) (second.getLocation().x() + secDimScFac * xOS), second.getLocation().y()));
			}

		} else {
			// second on top
			if (sLoc.y() + sSize.y() < fLoc.y()) {
				// second far at the top
				points.add(pair(sLoc.x(), sLoc.y() + sSize.y(), (float) (sLoc.x() + secDimScFac * xOS), sLoc.y()
						+ sSize.y()));
				points.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * xOS), fLoc.y()));
			} else {
				points.add(pair(first.getLocation().x(), first.getLocation().y(),
						(float) (first.getLocation().x() + firDimScaFac * xOS), first.getLocation().y()));
				points.add(pair(second.getLocation().x(), second.getLocation().y(),
						(float) (second.getLocation().x() + secDimScFac * xOS), second.getLocation().y()));
			}
		}
		return points;
	}

	private List<Pair<Point2D, Point2D>> addRecPointsToBand(int yOS, double firRecScaFac, double secRecScaFac) {
		Vec2f fLoc = first.getLocation();
		Vec2f sLoc = second.getLocation();
		Vec2f fSize = first.getSize();
		Vec2f sSize = second.getSize();
		List<Pair<Point2D, Point2D>> points = new ArrayList<>();
		if (fLoc.x() < sLoc.x()) {
			// second right
			if (fLoc.x() + fSize.x() < sLoc.x()) {
				// second far at right
				points.add(pair(fLoc.x() + fSize.x(), fLoc.y(), fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac
						* yOS)));
				points.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * yOS)));
			} else {
				// second in between
				points.add(pair(first.getLocation().x(), first.getLocation().y(), first.getLocation().x(),
						(float) (first.getLocation().y() + firRecScaFac * yOS)));
				points.add(pair(second.getLocation().x(), (float) (second.getLocation().y() - secRecScaFac * yOS),
						second.getLocation().x(), second.getLocation().y()));
			}

		} else {
			// second left
			if (sLoc.x() + sSize.x() < fLoc.x()) {
				// second far at left
				points.add(pair(sLoc.x() + sSize.x(), sLoc.y(), sLoc.x() + sSize.x(), (float) (sLoc.y() + secRecScaFac
						* yOS)));
				points.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * yOS)));
			} else {
				points.add(pair(first.getLocation().x(), first.getLocation().y(),
						(float) (first.getLocation().x() + firRecScaFac * yOS), first.getLocation().y()));
				points.add(pair(second.getLocation().x(), second.getLocation().y(),
						(float) (second.getLocation().x() + secRecScaFac * yOS), second.getLocation().y()));
			}
		}
		return points;
	}

	private Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}



}
