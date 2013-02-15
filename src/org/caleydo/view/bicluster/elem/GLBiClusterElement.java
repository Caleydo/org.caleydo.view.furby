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

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class GLBiClusterElement extends GLElementContainer implements IGLLayout {

	private final AGLView view;


	public GLBiClusterElement(AGLView view) {
		this.view = view;

		setLayout(GLLayouts.flowHorizontal(5));

	}

	@Override
	protected void init(IGLElementContext context) {
		// TODO Auto-generated method stub
		super.init(context);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {

	}

	public void setData(List<TablePerspective> list) {
		this.clear();
		if (list != null) {
			System.out.println(list.size());
			for (TablePerspective p : list) {
				this.add(new ClusterElement(view, p));
			}
		}
	}
}
