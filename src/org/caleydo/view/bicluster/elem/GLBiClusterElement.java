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

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ALayoutContainer;
import org.caleydo.core.view.opengl.layout.ElementLayouts;
import org.caleydo.core.view.opengl.layout.RowLayout;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class GLBiClusterElement extends ALayoutContainer {

	private final AGLView view;
	private List<TablePerspective> perspectives;
	

	public GLBiClusterElement(AGLView view) {
		super(new RowLayout());
		this.view = view;

		init();
	}

	private void init() {

	}


	public void setData(List<TablePerspective> list) {
		if (list != null) {
			perspectives = new ArrayList<>();
			for (TablePerspective p : list) {
				if (p != null) 	perspectives.add(p);
			}
			this.clear();
			initData();
		}  else {
			this.clear();
			initData();
		}
	}

	private void initData() {
		
		for (TablePerspective p: perspectives) {
			this.add(new ClusterElement(view, p));
			this.add(ElementLayouts.createXSpacer(5));
		}
		

	}

}
