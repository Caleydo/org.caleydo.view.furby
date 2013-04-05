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
import java.util.LinkedList;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.layout2.GLElement;

/**
 * @author Michael Gillhofer
 *
 */
public class DimBandElement extends BandElement {

	private static float[] dimBandColor = Colors.NEUTRAL_GREY.getRGBA();

	/**
	 * @param view
	 */
	public DimBandElement(GLElement first, GLElement second, AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getDimOverlap(second), root.getSelectionMixin()
				.getDimensionSelectionManager(), root, dimBandColor);
	}



	@Override
	public void updatePosition() {
		overlap = first.getDimOverlap(second);
		int overlapSize = overlap.size();
		if (overlapSize > 0 && first.isVisible() && second.isVisible()) {
			visible = true;
			double endDimBandScaleFactor = second.getSize().x() / (double) second.getNumberOfDimElements();
			double startDimBandScaleFactor = first.getSize().x() / (double) first.getNumberOfDimElements();
			addPointsToBand(startDimBandScaleFactor, endDimBandScaleFactor);
		} else
			visible = false;
		repaintAll();

	}

	private void addPointsToBand(double firDimScaFac, double secDimScFac) {
		Vec2f fLoc = first.getLocation();
		Vec2f sLoc = second.getLocation();
		Vec2f fSize = first.getSize();
		Vec2f sSize = second.getSize();
		bandPoints = new ArrayList<>();
		highlightPoints = new ArrayList<>();
		int os = overlap.size();
		int hOS = highlightOverlap.size();
		if (fLoc.y() < sLoc.y()) {
			// first on top
			if (fLoc.y() + fSize.y() < sLoc.y()) {
				// second far at the bottom
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y() + fSize.y(), (float) (fLoc.x() + firDimScaFac * hOS),
							fLoc.y() + fSize.y()));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * hOS), sLoc.y()));
					bandPoints.add(pair((float) (fLoc.x() + firDimScaFac * hOS), fLoc.y() + fSize.y(),
							(float) (fLoc.x() + firDimScaFac * os), fLoc.y() + fSize.y()));
					bandPoints.add(pair((float) (sLoc.x() + secDimScFac * hOS), sLoc.y(),
							(float) (sLoc.x() + secDimScFac * os), sLoc.y()));
				} else {
					bandPoints.add(pair(fLoc.x(), fLoc.y() + fSize.y(), (float) (fLoc.x() + firDimScaFac * os),
							fLoc.y() + fSize.y()));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * os), sLoc.y()));
				}
			} else {
				// second in between
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * hOS), fLoc.y()));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * hOS), sLoc.y()));
					bandPoints.add(pair((float) (fLoc.x() + firDimScaFac * hOS), fLoc.y(),
							(float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
					bandPoints.add(pair((float) (sLoc.x() + secDimScFac * hOS), sLoc.y(),
							(float) (sLoc.x() + secDimScFac * os), sLoc.y()));

				} else {
					bandPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * os), sLoc.y()));
				}
			}

		} else {
			// second on top
			if (sLoc.y() + sSize.y() < fLoc.y()) {
				// second far at the top
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x(), sLoc.y() + sSize.y(), (float) (sLoc.x() + secDimScFac * hOS),
							sLoc.y() + sSize.y()));
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * hOS), fLoc.y()));
					bandPoints.add(pair((float) (sLoc.x() + secDimScFac * hOS), sLoc.y() + sSize.y(),
							(float) (sLoc.x() + secDimScFac * os), sLoc.y() + sSize.y()));
					bandPoints.add(pair((float) (fLoc.x() + firDimScaFac * hOS), fLoc.y(),
							(float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
				} else {
					bandPoints.add(pair(sLoc.x(), sLoc.y() + sSize.y(), (float) (sLoc.x() + secDimScFac * os), sLoc.y()
							+ sSize.y()));
					bandPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
				}
			} else {
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * hOS), fLoc.y()));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * hOS), sLoc.y()));
					bandPoints.add(pair((float) (fLoc.x() + firDimScaFac * hOS), fLoc.y(),
							(float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
					bandPoints.add(pair((float) (sLoc.x() + secDimScFac * hOS), sLoc.y(),
							(float) (sLoc.x() + secDimScFac * os), sLoc.y()));
				} else {
					bandPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firDimScaFac * os), fLoc.y()));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secDimScFac * os), sLoc.y()));
				}
			}
		}
	}

	private Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}



	@Override
	public void highlightSelectionOverlapWith(BandElement b) {
		highlightOverlap = new ArrayList<>();
		if (b instanceof DimBandElement) {
			List<Integer> highList = new LinkedList<>(overlap);
			highList.retainAll(b.overlap);
			highlightOverlap = highList;
		}
		updatePosition();

	}


	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireDimensionSelectionDelta();

	}

}
