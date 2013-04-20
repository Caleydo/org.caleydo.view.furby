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

import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;

/**
 * @author Michael Gillhofer
 * 
 */
public class DimBandElement extends BandElement {

	private static float[] dimBandColor = Colors.NEUTRAL_GREY.getRGBA();

	/**
	 * @param view
	 */
	public DimBandElement(GLElement first, GLElement second,
			AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getDimOverlap(second),
				root.getSelectionMixin().getDimensionSelectionManager(), root,
				dimBandColor);
	}

	@Override
	public void updatePosition() {
		overlap = first.getDimOverlap(second);
		int overlapSize = overlap.size();
		if (overlapSize > 0 && first.isVisible() && second.isVisible()) {
			setVisibility(EVisibility.PICKABLE);
			double endDimBandScaleFactor = second.getSize().x()
					/ (double) second.getNumberOfDimElements();
			double startDimBandScaleFactor = first.getSize().x()
					/ (double) first.getNumberOfDimElements();
			addPointsToBand(startDimBandScaleFactor, endDimBandScaleFactor);
			band = TesselatedPolygons.band(bandPoints)
					.setDrawBandBordersOnFill(false);
			if (highlightPoints.size() > 0)
				highlightBand = TesselatedPolygons.band(highlightPoints)
						.setDrawBandBordersOnFill(false);
		} else
			setVisibility(EVisibility.NONE);
		repaintAll();

	}

	private void addPointsToBand(double firDimScaFacDouble,
			double secDimScaFacDouble) {
		float firDimScaFac = (float) firDimScaFacDouble;
		float secDimScaFac = (float) secDimScaFacDouble;
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

//		if (first.getID().contains("bicluster16")
//				&& second.getID().contains("bicluster13")) {
//			System.out.println("Stop");
//		}
//		boolean isFirstBandSplit = isBandSplitted(first);
//		boolean isSecondBandSplit = isBandSplitted(second);
		// first/second-BandDelta for moving the band to the correct sample in
		// the heatmap
		float fBD = /*isFirstBandSplit ? 0 : */findLeftestOverlapIndex(first);
		float sBD = /*isSecondBandSplit ? 0 : */findLeftestOverlapIndex(second);
		float fdx = fBD * firDimScaFac;
		float sdx = sBD * secDimScaFac;
		if (fLoc.y() < sLoc.y()) {
			// first on top
			if (fLoc.y() + fSize.y() < sLoc.y()) {
				// second far at the bottom
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fdx,
							fLoc.y() + fSize.y(), fLoc.x() + firDimScaFac
									* (hOS + fBD), fLoc.y() + fSize.y()));
					highlightPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (hOS + sBD), sLoc.y()));
					bandPoints.add(pair(fLoc.x() + firDimScaFac * (hOS + fBD),
							fLoc.y() + fSize.y(), fLoc.x() + firDimScaFac
									* (os + fBD), fLoc.y() + fSize.y()));
					bandPoints.add(pair(sLoc.x() + secDimScaFac * (hOS + sBD),
							sLoc.y(), sLoc.x() + secDimScaFac * (os + sBD),
							sLoc.y()));
				} else {
					bandPoints.add(pair(fLoc.x() + fdx, fLoc.y() + fSize.y(),
							fLoc.x() + firDimScaFac * (os + fBD), fLoc.y()
									+ fSize.y()));
					bandPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (os + sBD), sLoc.y()));
				}
			} else {
				// second in between
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (hOS + fBD), fLoc.y()));
					highlightPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (hOS + sBD), sLoc.y()));
					bandPoints.add(pair(fLoc.x() + firDimScaFac * (hOS + fBD),
							fLoc.y(), fLoc.x() + firDimScaFac * (os + fBD),
							fLoc.y()));
					bandPoints.add(pair(sLoc.x() + secDimScaFac * (hOS + sBD),
							sLoc.y(), sLoc.x() + secDimScaFac * (os + sBD),
							sLoc.y()));

				} else {
					bandPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (os + fBD), fLoc.y()));
					bandPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (os + sBD), sLoc.y()));
				}
			}

		} else {
			// second on top
			if (sLoc.y() + sSize.y() < fLoc.y()) {
				// second far at the top
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x() + sdx,
							sLoc.y() + sSize.y(), sLoc.x() + secDimScaFac
									* (hOS + sBD), sLoc.y() + sSize.y()));
					highlightPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (hOS + fBD), fLoc.y()));
					bandPoints.add(pair(sLoc.x() + secDimScaFac * (hOS + sBD),
							sLoc.y() + sSize.y(), sLoc.x() + secDimScaFac
									* (os + sBD), sLoc.y() + sSize.y()));
					bandPoints.add(pair(fLoc.x() + firDimScaFac * (hOS + fBD),
							fLoc.y(), fLoc.x() + firDimScaFac * (os + fBD),
							fLoc.y()));
				} else {
					bandPoints.add(pair(sLoc.x() + sdx, sLoc.y() + sSize.y(),
							sLoc.x() + secDimScaFac * (os + sBD), sLoc.y()
									+ sSize.y()));
					bandPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (os + fBD), fLoc.y()));
				}
			} else {
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (hOS + fBD), fLoc.y()));
					highlightPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (hOS + sBD), sLoc.y()));
					bandPoints.add(pair(fLoc.x() + firDimScaFac * (hOS + fBD),
							fLoc.y(), fLoc.x() + firDimScaFac * (os + fBD),
							fLoc.y()));
					bandPoints.add(pair(sLoc.x() + secDimScaFac * (hOS + sBD),
							sLoc.y(), sLoc.x() + secDimScaFac * (os + sBD),
							sLoc.y()));
				} else {
					bandPoints.add(pair(fLoc.x() + fdx, fLoc.y(), fLoc.x()
							+ firDimScaFac * (os + fBD), fLoc.y()));
					bandPoints.add(pair(sLoc.x() + sdx, sLoc.y(), sLoc.x()
							+ secDimScaFac * (os + sBD), sLoc.y()));
				}
			}
		}
	}

	private int findLeftestOverlapIndex(ClusterElement element) {
		int smallest = 1000000;
		for (Integer i : overlap) {
			int cur = element.getDimIndexOf(i);
			if (cur < smallest)
				smallest = cur;
		}
		return smallest;
	}

	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireDimensionSelectionDelta();
	}

	protected boolean isBandSplitted(ClusterElement cluster) {
		return !cluster.isContinuousDimSequenze(overlap);
	}

}
