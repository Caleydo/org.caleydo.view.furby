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

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.event.ToolbarEvent;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class GlobalToolBarElement extends GLElementContainer implements GLButton.ISelectionCallback,
		GLSlider.ISelectionCallback {

	private final GLButton fixedClusterButton;
	private final GLSlider geneThrSpinner;
	private final GLSlider sampleThrSpinner;
	private final GLElement sampleLabel;
	private final GLElement geneLabel;

	public GlobalToolBarElement() {
		setLayout(new GLFlowLayout(false, 5, new GLPadding(10)));

		this.fixedClusterButton = new GLButton(EButtonMode.CHECKBOX);
		fixedClusterButton.setRenderer(GLButton.createCheckRenderer("Show only 15 Elements"));
		fixedClusterButton.setSelected(false);
		fixedClusterButton.setCallback(this);
		fixedClusterButton.setSize(Float.NaN, 16);
		this.add(fixedClusterButton);

		this.sampleLabel = new GLElement();
		sampleLabel.setSize(Float.NaN, 16);
		this.add(sampleLabel);

		this.sampleThrSpinner = new GLSlider(0, 5, 4.5f);
		sampleThrSpinner.setCallback(this);
		sampleThrSpinner.setSize(Float.NaN, 18);
		this.add(sampleThrSpinner);

		this.geneLabel = new GLElement();
		geneLabel.setSize(Float.NaN, 16);
		this.add(geneLabel);

		this.geneThrSpinner = new GLSlider(0, 0.3f, 0.08f);
		geneThrSpinner.setCallback(this);
		geneThrSpinner.setSize(Float.NaN, 18);
		this.add(geneThrSpinner);

		setText(sampleLabel, "Sample Threshold");
		setText(geneLabel, "Gene Threshold");

		setRenderer(GLRenderers.fillRect(new Color(0.8f, 0.8f, 0.8f, .8f)));
	}


	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1)));

	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		update();
	}

	private void update() {
		float samplTh = sampleThrSpinner.getValue();
		float geneTh = geneThrSpinner.getValue();
		EventPublisher.trigger(new ToolbarEvent(geneTh, samplTh, fixedClusterButton.isSelected()));
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		update();
	}

}
