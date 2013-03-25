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
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;

/**
 * @author Michael Gillhofer
 *
 */
public class DimBandElement extends PickableGLElement implements BandElement {

	private static float[] color = Colors.BLUE.getRGBA();
	private static ConnectionBandRenderer bandRenderer = new ConnectionBandRenderer();

	{
		bandRenderer.init(GLContext.getCurrentGL().getGL2());
	}

	/**
	 * @param view
	 */
	public DimBandElement(GLElement first, GLElement second) {

		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;

	}

	boolean highlight = false;
	private ClusterElement first;
	private ClusterElement second;


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

		bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), points, highlight,
				highlight ? Colors.RED.getRGBA() : color, .5f);

		// super.renderImpl(g, w, h);
		// System.out.println(first.getId() + "/" + second.getId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElement#renderPickImpl(org.caleydo.core.view.opengl.layout2.GLGraphics,
	 * float, float)
	 */
	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), points, false, color, .5f);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.layout2.PickableGLElement#onClicked(org.caleydo.core.view.opengl.picking.Pick)
	 */
	@Override
	protected void onClicked(Pick pick) {
		highlight = !highlight;
		super.onClicked(pick);
	}

	@Override
	public void updatePosition() {

		double startDimBandScaleFactor = first.getSize().x() / (double) first.getNumberOfDimElements();
		double endDimBandScaleFactor = second.getSize().x() / (double) second.getNumberOfDimElements();
		int xOverlapSize = first.getxOverlap(second).size();
		if (xOverlapSize > 0) {
			points = addDimPointsToBand(xOverlapSize, startDimBandScaleFactor, endDimBandScaleFactor);
		}
		repaintAll();

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

	private Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}

}
