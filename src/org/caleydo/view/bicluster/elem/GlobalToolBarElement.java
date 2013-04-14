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

import org.caleydo.core.event.EventListenerManager.ListenTo;
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
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.ToolbarThresholdEvent;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class GlobalToolBarElement extends GLElementContainer implements GLButton.ISelectionCallback,
		GLSlider.ISelectionCallback {

	float maxRecordValue = 0.2f;
	float maxDimensionValue = 5f;

	private GLButton fixedClusterButton;
	private GLButton overlapSortingModeButton;
	private GLButton probabilitySortingModeButton;
	private GLSlider recordThresholdSlider;
	private GLSlider dimensionThresholdSlider;
	private GLElement dimensionLabel;
	private GLElement recordLabel;

	public GlobalToolBarElement() {
		setLayout(new GLFlowLayout(false, 5, new GLPadding(10)));
		this.overlapSortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		overlapSortingModeButton.setRenderer(GLButton.createCheckRenderer("Sort by Bands"));
		overlapSortingModeButton.setSelected(false);
		overlapSortingModeButton.setCallback(this);
		overlapSortingModeButton.setSize(Float.NaN, 16);
		this.add(overlapSortingModeButton);

		this.probabilitySortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		probabilitySortingModeButton.setRenderer(GLButton.createCheckRenderer("Sort by Probability"));
		probabilitySortingModeButton.setSelected(true);
		probabilitySortingModeButton.setCallback(this);
		probabilitySortingModeButton.setSize(Float.NaN, 16);
		this.add(probabilitySortingModeButton);

		this.add(new GLButton(EButtonMode.BUTTON)); // Spacer
		this.fixedClusterButton = new GLButton(EButtonMode.CHECKBOX);
		fixedClusterButton.setRenderer(GLButton.createCheckRenderer("Show only 15 Elements"));
		fixedClusterButton.setSelected(false);
		fixedClusterButton.setCallback(this);
		fixedClusterButton.setSize(Float.NaN, 16);
		this.add(fixedClusterButton);

		initSliders();
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
		float samplTh = dimensionThresholdSlider.getValue();
		float geneTh = recordThresholdSlider.getValue();
		EventPublisher.trigger(new ToolbarThresholdEvent(geneTh, samplTh, fixedClusterButton.isSelected()));
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if (button == fixedClusterButton)
			update();
		if (button == overlapSortingModeButton) {
			overlapSortingModeButton.setSelected(selected);
			probabilitySortingModeButton.setSelected(!selected);
		}
		if (button == probabilitySortingModeButton) {
			probabilitySortingModeButton.setSelected(selected);
			overlapSortingModeButton.setSelected(!selected);
		}
	}

	@ListenTo
	public void listenTo(MaxThresholdChangeEvent e) {
		maxRecordValue = (float) e.getRecThreshold();
		maxDimensionValue = (float) e.getDimThreshold();
		initSliders();
	}

	/**
	 *
	 */
	private void initSliders() {
		this.remove(dimensionLabel);
		this.dimensionLabel = new GLElement();
		dimensionLabel.setSize(Float.NaN, 16);
		this.add(dimensionLabel);

		this.remove(dimensionThresholdSlider);
		this.dimensionThresholdSlider = new GLSlider(0, maxDimensionValue, 4.5f);
		dimensionThresholdSlider.setCallback(this);
		dimensionThresholdSlider.setSize(Float.NaN, 18);
		this.add(dimensionThresholdSlider);

		this.remove(recordLabel);
		this.recordLabel = new GLElement();
		recordLabel.setSize(Float.NaN, 16);
		this.add(recordLabel);

		this.remove(recordThresholdSlider);
		this.recordThresholdSlider = new GLSlider(0, maxRecordValue, 0.08f);
		recordThresholdSlider.setCallback(this);
		recordThresholdSlider.setSize(Float.NaN, 18);
		this.add(recordThresholdSlider);

		setText(dimensionLabel, "Sample Threshold");
		setText(recordLabel, "Gene Threshold");

	}

}
