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

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.view.bicluster.elem.ClusterElement;

/**
 * @author Michael Gillhofer
 * 
 */
public class RecordBandElement extends BandElement {

	private static float[] recBandColor = { 0.862f, 0.862f, 1f, 1f };
	protected float topBandClusterPos, bottomBandClusterPos;
	protected RecordBandFactory secondMergeArea, firstMergeArea;

	/**
	 * @param savedData
	 */
	public RecordBandElement(GLElement first, GLElement second,
			AllBandsElement root, IGLElementContext context) {
		super(first, second, ((ClusterElement) first).getRecOverlap(second),
				root.getSelectionMixin().getRecordSelectionManager(), root,
				recBandColor, context);
	}



	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();
	}



	@Override
	protected void initBand() {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void updateStructure() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void updatePosition() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void updateSelection() {
		// TODO Auto-generated method stub
		
	}


}
