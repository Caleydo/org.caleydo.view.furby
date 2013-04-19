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

import java.util.ArrayList;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;

/**
 * @author Michael Gillhofer
 *
 */
public class RecBandElement extends BandElement {

	private static float[] recBandColor = { 0.862f, 0.862f, 1f, 1f };

	/**
	 * @param view
	 */
	public RecBandElement(GLElement first, GLElement second, AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getRecOverlap(second), root.getSelectionMixin()
				.getRecordSelectionManager(), root, recBandColor);
	}

	@Override
	public void updatePosition() {
		overlap = first.getRecOverlap(second);
		int overlapSize = overlap.size();
		if (overlapSize > 0 && first.isVisible() && second.isVisible()) {
			setVisibility(EVisibility.PICKABLE);
			double endRecBandScaleFactor = second.getSize().y() / (double) second.getNumberOfRecElements();
			double startRecBandScaleFactor = first.getSize().y() / (double) first.getNumberOfRecElements();
			addPointsToBand(startRecBandScaleFactor, endRecBandScaleFactor);
			band = TesselatedPolygons.band(bandPoints).setDrawBandBordersOnFill(false);
			if (highlightPoints.size() > 0)
				highlightBand = TesselatedPolygons.band(highlightPoints).setDrawBandBordersOnFill(false);
		} else
			setVisibility(EVisibility.NONE);
		repaintAll();

		// bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), point, highlight, colorY, .5f);

	}

	private void addPointsToBand(double firRecScaFac, double secRecScaFac) {
		Vec2f fLoc = first.getLocation();
		Vec2f sLoc = second.getLocation();
		Vec2f fSize = first.getSize();
		Vec2f sSize = second.getSize();
		bandPoints = new ArrayList<>();
		highlightPoints = new ArrayList<>();
		int os = overlap.size();
		int hOS = highlightOverlap.size();
		if (hOS == 0)
			hOS = hoverOverlap.size();
		// if (second.getId().contains("bicluster17") && first.getId().contains("bicluster20"))
		// System.out.println("here");
		if (fLoc.x() < sLoc.x()) {
			// second right
			if (fLoc.x() + fSize.x() < sLoc.x()) {
				// second far at right
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y(), fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * hOS)));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS)));
					bandPoints.add(pair(fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * hOS),
							fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS), sLoc.x(),
							(float) (sLoc.y() + secRecScaFac * os)));

				} else {
					bandPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y(), fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * os)));
				}

			} else {
				// second in between
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS)));
					highlightPoints.add(pair(sLoc.x(), (float) (sLoc.y() - secRecScaFac * hOS), sLoc.x(), sLoc.y()));
					bandPoints.add(pair(fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS), fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), second.getLocation().x(), second.getLocation().y()));
				} else {
					bandPoints.add(pair(first.getLocation().x(), first.getLocation().y(), first.getLocation().x(),
							(float) (first.getLocation().y() + firRecScaFac * os)));
					bandPoints.add(pair(second.getLocation().x(), second.getLocation().y(), second.getLocation().x(),
							(float) (second.getLocation().y() + secRecScaFac * os)));
				}
			}

		} else {
			// second left
			if (sLoc.x() + sSize.x() < fLoc.x()) {
				// second far at left
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y(), sLoc.x() + sSize.x(),
							(float) (sLoc.y() + secRecScaFac * hOS)));
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS)));
					bandPoints.add(pair(sLoc.x() + sSize.x(), (float) (sLoc.y() + secRecScaFac * hOS),
							sLoc.x() + sSize.x(), (float) (sLoc.y() + secRecScaFac * os)));
					bandPoints.add(pair(fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS), fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
				} else {
					bandPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y(), sLoc.x() + sSize.x(),
							(float) (sLoc.y() + secRecScaFac * os)));
					bandPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * os)));
				}
			} else {
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * os)));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS)));
					bandPoints.add(pair(fLoc.x(), (float) (fLoc.y() + firRecScaFac * os), fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS), sLoc.x(),
							(float) (sLoc.y() + secRecScaFac * hOS)));
				} else {
					bandPoints.add(pair(first.getLocation().x(), first.getLocation().y(), first.getLocation().x(),
							(float) (first.getLocation().y() + firRecScaFac * os)));
					bandPoints.add(pair(second.getLocation().x(), second.getLocation().y(), second.getLocation().x(),
							(float) (second.getLocation().y() + secRecScaFac * os)));

				}
			}
		}
	}


	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();

	}




}
