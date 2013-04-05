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

import java.awt.Color;

import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class GlobalToolBarElement extends GLElementContainer {

	public GlobalToolBarElement() {
		setLayout(new GLFlowLayout(false, 5, new GLPadding(10)));

		// a simple button
		GLButton adaptive = new GLButton(EButtonMode.CHECKBOX);
		adaptive.setRenderer(GLRenderers.drawText("Global", VAlign.LEFT, new GLPadding(2)));
		adaptive.setSelectedRenderer(GLRenderers.drawText("Adaptive", VAlign.LEFT, new GLPadding(2)));
		adaptive.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				// TODO Auto-generated method stub
			}
		});
		adaptive.setSize(Float.NaN, 16);
		this.add(adaptive);

		setRenderer(GLRenderers.fillRect(new Color(0.8f, 0.8f, 0.8f, .8f)));
	}

}
