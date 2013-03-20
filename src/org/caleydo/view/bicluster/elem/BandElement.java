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

import java.awt.geom.Point2D;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

/**
 * @author user
 *
 */
public class BandElement extends GLElementAdapter {

	/**
	 * @param view
	 */
	public BandElement(AGLView view, GLBiClusterElement root, boolean dimBand) {
		super(view);
		this.view = view;
		this.root = root;
		this.dimBand = dimBand;
	}

	private GLBiClusterElement root;
	private final AGLView view;

	private ClusterElement first;
	private ClusterElement second;
	private boolean dimBand;

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
		// TODO Auto-generated method stub
		super.renderImpl(g, w, h);
	}

}
