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

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ALayoutContainer;
import org.caleydo.core.view.opengl.layout.ElementLayouts;
import org.caleydo.core.view.opengl.layout.RowLayout;

/**
 * @author Samuel Gratzl
 *
 */
public class GLBiClusterElement extends ALayoutContainer {

	private final AGLView view;
	private TablePerspective x;
	private TablePerspective l;
	private TablePerspective z;

	public GLBiClusterElement(AGLView view) {
		super(new RowLayout());
		this.view = view;

		init();
	}

	private void init() {

	}

	public void setData(TablePerspective x, TablePerspective l, TablePerspective z) {
		this.x = x;
		this.l = l;
		this.z = z;
		if (this.x == null) {
			this.clear();
		} else {
			this.clear();
			initData();
		}
	}

	private void initData() {
		// current three clusters in a row with x, l and z
		ClusterElement elem = new ClusterElement(view, x);
		this.add(elem);

		this.add(ElementLayouts.createXSpacer(20));

		elem = new ClusterElement(view, l);
		this.add(elem);

		this.add(ElementLayouts.createXSpacer(20));

		elem = new ClusterElement(view, z);
		this.add(elem);
	}

}

