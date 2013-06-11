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

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;

/**
 * @author Michael Gillhofer
 * 
 */
public class DimensionBandElement extends BandElement {

	private static float[] dimBandColor = Colors.NEUTRAL_GREY.getRGBA();

	public DimensionBandElement(GLElement first, GLElement second,
			AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getDimOverlap(second),
				root.getSelectionMixin().getDimensionSelectionManager(), root,
				dimBandColor);
	}

	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireDimensionSelectionDelta();
	}

	@Override
	protected void initBand() {
		updateStructure();
	}

	@Override
	public void updateStructure() {
		overlap = first.getDimOverlap(second);
		subBands = new HashMap<>();
//		if (first.getID().contains("bicluster19")
//				&& second.getID().contains("bicluster3"))
//			System.out.println("halt -  DimensionBandELement - updateStructure");
		firstSubIndices = first.getListOfContinousDimSequences(overlap);
		firstMergeArea = new DimBandMergeArea(first, second, firstSubIndices);
		for (List<Integer> subBand : firstSubIndices) {
			subBands.put(subBand, firstMergeArea.getBand(subBand));
		}
		secondSubIndices = first.getListOfContinousDimSequences(overlap);
		secondMergeArea = new DimBandMergeArea(second, first, secondSubIndices);
		for (List<Integer> subBand : secondSubIndices) {
			subBands.put(subBand, secondMergeArea.getBand(subBand));
		}
		createBand();
	}

	private void createBand() {
		List<Pair<Vec3f, Vec3f>> bandPoints = new ArrayList<>();
		List<Pair<Vec3f, Vec3f>> firstAnchor = firstMergeArea
				.getConnectionFromBand();
		List<Pair<Vec3f, Vec3f>> secondAnchor = secondMergeArea
				.getConnectionFromBand();
		bandPoints.add(firstAnchor.get(0));
		bandPoints.add(firstAnchor.get(1));
		bandPoints.add(secondAnchor.get(1));
		bandPoints.add(secondAnchor.get(0));
		this.band = TesselatedPolygons.band(bandPoints);
	}

	@Override
	public void updatePosition() {
		updateStructure();
	}

	@Override
	public void updateSelection() {
		// TODO Auto-generated method stub

	}

}
