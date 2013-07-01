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

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.bicluster.elem.ClusterElement;

/**
 * @author Michael Gillhofer
 * 
 */
public class DimensionBandElement extends BandElement {

	private static float[] dimBandColor = Color.NEUTRAL_GREY.getRGBA();

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
		if (!isVisible())
			return;
		overlap = first.getDimOverlap(second);
		if (overlap.size() > 0)
			setVisibility(EVisibility.PICKABLE);
		else
			setVisibility(EVisibility.NONE);
		// if (first.getID().contains("bicluster19")
		// && second.getID().contains("bicluster3"))
		// System.out.println("halt -  DimensionBandELement - updateStructure");
		firstSubIndices = first.getListOfContinousDimSequences(overlap);
		secondSubIndices = second.getListOfContinousDimSequences(overlap);
		if (firstSubIndices.size() == 0)
			return;
		bandFactory = new DimensionBandFactory(first, second, firstSubIndices,
				secondSubIndices, overlap);
		bands = bandFactory.getBands();
	}

	@Override
	public void updatePosition() {
		updateStructure();
	}

	@Override
	public void updateSelection() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (!isVisible())
			return;
//		g.incZ();
//		List<Vec2f> points = ((DimensionBandFactory)bandFactory).getTestPointsFirst();
//		g.color(Color.BLACK);
//		g.drawLine(points.get(0).x(), points.get(0).y(), points.get(1).x(),
//				points.get(1).y());
//		g.drawLine(points.get(2).x(), points.get(2).y(), points.get(3).x(),
//				points.get(3).y());
//		g.drawLine(points.get(4).x(), points.get(4).y(), points.get(5).x(),
//				points.get(5).y());
//
//		points = ((DimensionBandFactory)bandFactory).getTestPointsSecond();
//		g.color(Color.RED);
//		g.drawLine(points.get(0).x(), points.get(0).y(), points.get(1).x(),
//				points.get(1).y());
//		g.drawLine(points.get(2).x(), points.get(2).y(), points.get(3).x(),
//				points.get(3).y());
//		g.drawLine(points.get(4).x(), points.get(4).y(), points.get(5).x(),
//				points.get(5).y());
////		points = ((DimensionBandFactory)bandFactory).getTestPointsSecond();
////		g.drawLine(points.get(0).x(), points.get(0).y(), points.get(1).x(),
////				points.get(1).y());
//		g.decZ();
		super.renderImpl(g, w, h);
	}

}
