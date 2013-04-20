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

	private void addPointsToBand(double firRecScaFacDouble, double secRecScaFacDouble) {
		float firRecScaFac = (float) firRecScaFacDouble;
		float secRecScaFac = (float) secRecScaFacDouble;
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
//		if (first.getID().contains("bicluster22")&& second.getID().contains("bicluster7")){
//			System.out.println("Stop");
//		}
		
		boolean isBandSplitFirst = isBandSplitted(first);
		boolean isBandSplitSecond = isBandSplitted(second);
		//Delta for moving the band to the correct gene in the heatmap
		float fBD = isBandSplitFirst ?  0 : findLeftestOverlapIndex(first);
		float sBD = isBandSplitSecond ?  0 : findLeftestOverlapIndex(second);
		if (fLoc.x() < sLoc.x()) {
			// second right
			if (fLoc.x() + fSize.x() < sLoc.x()) {
				// second far at right
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y()+fBD*firRecScaFac, fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * (hOS+fBD))));
					highlightPoints.add(pair(sLoc.x(), sLoc.y()+sBD*secRecScaFac, sLoc.x(), (float) (sLoc.y() + secRecScaFac * (hOS+sBD))));
					bandPoints.add(pair(fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * (hOS+fBD)),
							fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * (os+fBD))));
					bandPoints.add(pair(sLoc.x(), (float) (sLoc.y() + secRecScaFac * (hOS+sBD)), sLoc.x(),
							(float) (sLoc.y() + secRecScaFac * (os+sBD))));

				} else {
					bandPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y()+fBD*firRecScaFac, fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * (os+fBD))));
					bandPoints.add(pair(sLoc.x(), sLoc.y()+sBD*secRecScaFac, sLoc.x(), (float) (sLoc.y() + secRecScaFac * (os+sBD))));
				}

			} else {
				// second in between
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x(), sLoc.y()+secRecScaFac*sBD, sLoc.x(), sLoc.y() + secRecScaFac * (os+sBD)));
					highlightPoints.add(pair(fLoc.x(), fLoc.y()+firRecScaFac*fBD, fLoc.x(), fLoc.y() + firRecScaFac * (hOS+fBD)));
					bandPoints.add(pair(sLoc.x(),sLoc.y() + secRecScaFac * (os+sBD), sLoc.x(),
							sLoc.y() + secRecScaFac * (os+sBD)));
					bandPoints.add(pair(fLoc.x(), fLoc.y() + firRecScaFac * (hOS+fBD), fLoc.x(),
							fLoc.y() + firRecScaFac * (hOS+fBD)));
				} else {
					bandPoints.add(pair(fLoc.x(), fLoc.y()+fBD*firRecScaFac, fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * (os+fBD))));
					bandPoints.add(pair(sLoc.x(), sLoc.y()+sBD*secRecScaFac, sLoc.x(),
							(float) (sLoc.y() + secRecScaFac * (os+sBD))));
				}
			}

		} else {
			// second left
			if (sLoc.x() + sSize.x() < fLoc.x()) {
				// second far at left
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y()+secRecScaFac*sBD, sLoc.x() + sSize.x(),
							sLoc.y() + secRecScaFac * (hOS+sBD)));
					highlightPoints.add(pair(fLoc.x(), fLoc.y()+firRecScaFac*fBD, fLoc.x(), fLoc.y() + firRecScaFac * (hOS+fBD)));
					bandPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y() + secRecScaFac * (hOS+sBD),
							sLoc.x() + sSize.x(), sLoc.y() + secRecScaFac * (os+sBD)));
					bandPoints.add(pair(fLoc.x(), fLoc.y() + firRecScaFac * (hOS+fBD), fLoc.x(),
							fLoc.y() + firRecScaFac * (os+fBD)));
				} else {
					bandPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y()+secRecScaFac*sBD, sLoc.x() + sSize.x(),
							sLoc.y() + secRecScaFac * (os+sBD)));
					bandPoints.add(pair(fLoc.x(), fLoc.y()+firRecScaFac*fBD, fLoc.x(), fLoc.y() + firRecScaFac * (os+fBD)));
				}
			} else {
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y()+firRecScaFac*fBD, fLoc.x(), fLoc.y() + firRecScaFac * (os+fBD)));
					highlightPoints.add(pair(sLoc.x(), sLoc.y()+secRecScaFac*sBD, sLoc.x(), sLoc.y() + secRecScaFac * (hOS+sBD)));
					bandPoints.add(pair(fLoc.x(), fLoc.y() + firRecScaFac * (os+fBD), fLoc.x(),
							fLoc.y() + firRecScaFac * (os+fBD)));
					bandPoints.add(pair(sLoc.x(), sLoc.y() + secRecScaFac * (hOS+sBD), sLoc.x(),
							sLoc.y() + secRecScaFac * (hOS+sBD)));
				} else {
					bandPoints.add(pair(fLoc.x(), fLoc.y()+firRecScaFac*fBD, fLoc.x(),
							fLoc.y() + firRecScaFac * (os+fBD)));
					bandPoints.add(pair(sLoc.x(), sLoc.y()+secRecScaFac*sBD, sLoc.x(),
							sLoc.y() + secRecScaFac * (os+sBD)));

				}
			}
		}
	}


	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();

	}

	protected boolean isBandSplitted(ClusterElement cluster) {
		return !cluster.isContinuousRecSequenze(overlap);
	}

	private int findLeftestOverlapIndex(ClusterElement element) {
		int smallest = 1000000;
		for (Integer i: overlap) {
			int cur =element.getRecIndexOf(i);
			if (cur < smallest)	 smallest =cur;
		}
		return smallest;
	}

}
