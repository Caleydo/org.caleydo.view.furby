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
package org.caleydo.view.bicluster.elem.band;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

/**
 * @author Michael Gillhofer
 * 
 */
public class RecordBandElement extends BandElement {

	private static float[] recBandColor = { 0.862f, 0.862f, 1f, 1f };
	protected float topBandClusterPos, bottomBandClusterPos;
	protected RecBandMergeArea secondMergeArea, firstMergeArea;

	/**
	 * @param savedData
	 */
	public RecordBandElement(GLElement first, GLElement second,
			AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getRecOverlap(second),
				root.getSelectionMixin().getRecordSelectionManager(), root,
				recBandColor);
	}

	@Override
	public void update() {
		overlap = first.getRecOverlap(second);
		int overlapSize = overlap.size();
		if (overlapSize > 0 && first.isVisible() && second.isVisible()) {
			setVisibility(EVisibility.PICKABLE);

			// add Points TODO

			band = TesselatedPolygons.band(bandPoints)
					.setDrawBandBordersOnFill(false);
			if (highlightPoints.size() > 0)
				highlightBand = TesselatedPolygons.band(highlightPoints)
						.setDrawBandBordersOnFill(false);
		} else
			setVisibility(EVisibility.NONE);
		repaintAll();
	}

	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();

	}

	@Override
	public String toString() {
		return "Record Band: " + (first == null ? "null" : first.getID()) + " "
				+ (second == null ? "null" : second.getID());
	}

	@Override
	protected void initBand() {
		List<List<Integer>> firstSubBandIndices = first
				.getListOfContinousRecSequenzes(overlap);
		if (firstSubBandIndices.size() > 1) {
			firstMergeArea = new RecBandMergeArea(first, second);
			for (List<Integer> list : firstSubBandIndices) {
				firstSubBands.add(new RecordSubBandElement(first, this,
						root, list, firstMergeArea));
			}
		}
		List<List<Integer>> secondSubBandIndices = second
				.getListOfContinousRecSequenzes(overlap);
		if (secondSubBandIndices.size() > 1) {
			secondMergeArea = new RecBandMergeArea(second, first);
			for (List<Integer> list : secondSubBandIndices) {
				secondSubBands.add(new RecordSubBandElement(second, this,
						root, list, secondMergeArea));
			}
		}

	}
}
