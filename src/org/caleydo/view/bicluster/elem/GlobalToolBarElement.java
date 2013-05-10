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
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;

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
	private GLButton bandSortingModeButton;
	private GLButton probabilitySortingModeButton;
	private GLSlider recordThresholdSlider;
	private GLSlider dimensionThresholdSlider;
	private GLElement dimensionLabel;
	private GLElement recordLabel;
	private GLButton clearHiddenClusterButton;
	private List<String> clearHiddenButtonTooltipList = new ArrayList<>();

	public GlobalToolBarElement() {
		setLayout(new GLFlowLayout(false, 5, new GLPadding(10)));
		this.bandSortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		bandSortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Bands"));
		bandSortingModeButton.setSelected(false);
		bandSortingModeButton.setCallback(this);
		bandSortingModeButton.setSize(Float.NaN, 16);
		this.add(bandSortingModeButton);

		this.probabilitySortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		probabilitySortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Probability"));
		probabilitySortingModeButton.setSelected(true);
		probabilitySortingModeButton.setCallback(this);
		probabilitySortingModeButton.setSize(Float.NaN, 16);
		this.add(probabilitySortingModeButton);


		clearHiddenClusterButton = new GLButton(EButtonMode.BUTTON);
		clearHiddenClusterButton.setRenderer(new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.drawText("Show all Clusters", 18, 5, w, 14);

			}
		});
		clearHiddenClusterButton.setCallback(this);
		clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
		this.add(clearHiddenClusterButton);

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
		EventPublisher.trigger(new LZThresholdChangeEvent(geneTh, samplTh, fixedClusterButton.isSelected(), true));
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if (button == fixedClusterButton) {
			update();
			return;
		}
		if (button == bandSortingModeButton) {
			bandSortingModeButton.setSelected(selected);
			probabilitySortingModeButton.setSelected(!selected);
		}
		if (button == probabilitySortingModeButton) {
			probabilitySortingModeButton.setSelected(selected);
			bandSortingModeButton.setSelected(!selected);
		}
		if (button == clearHiddenClusterButton) {
			clearHiddenButtonTooltipList = new ArrayList<>();
			clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
			clearHiddenClusterButton.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					g.drawText("Show all clusters", 18, 5, w, 14);

				}
			});
			EventPublisher.trigger(new UnhidingClustersEvent());
		}
		boolean isBandSorting = bandSortingModeButton.isSelected();
		EventPublisher.trigger(new SortingChangeEvent(isBandSorting ? SortingType.bandSorting
				: SortingType.probabilitySorting, this));
	}

	@ListenTo
	public void listenTo(MaxThresholdChangeEvent e) {
		maxRecordValue = (float) e.getRecThreshold();
		maxDimensionValue = (float) e.getDimThreshold();
		initSliders();
	}


	@ListenTo
	public void listenTo(ClusterGetsHiddenEvent event){
		clearHiddenButtonTooltipList.add(event.getClusterID());
		StringBuilder tooltip = new StringBuilder("HiddenClusters: ");
		for (String s: clearHiddenButtonTooltipList) {
			tooltip.append("\n   ");
			tooltip.append(s);
		}
		clearHiddenClusterButton.setTooltip(tooltip.toString());
		clearHiddenClusterButton.setRenderer(new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.drawText("Show all clusters (+" + clearHiddenButtonTooltipList.size() + ")", 18, 5, w, 14);

			}
		});
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
