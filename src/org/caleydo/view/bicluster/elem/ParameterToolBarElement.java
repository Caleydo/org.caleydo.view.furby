/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec4f;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class ParameterToolBarElement extends AToolBarElement {
	float maxRecordValue = 0.2f;
	float maxDimensionValue = 5f;

	private GLButton fixedClusterButton;
	private GLButton dimBandVisibilityButton;
	private GLButton recBandVisibilityButton;
	private GLButton bandSortingModeButton;
	private GLButton probabilitySortingModeButton;
	private GLSlider recordThresholdSlider;
	private GLSlider dimensionThresholdSlider;
	private GLSlider clusterMinSizeThresholdSlider;

	private GLElement dimensionLabel;
	private GLElement recordLabel;
	private GLElement clusterMinSizeLabel;
	private GLButton clearHiddenClusterButton;

	private List<String> clearHiddenButtonTooltipList = new ArrayList<>();
	private TablePerspective x;

	public ParameterToolBarElement() {
		this.bandSortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		bandSortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Bands"));
		bandSortingModeButton.setSelected(false);
		bandSortingModeButton.setCallback(this);
		bandSortingModeButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(bandSortingModeButton);

		this.probabilitySortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		probabilitySortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Probability"));
		probabilitySortingModeButton.setSelected(true);
		probabilitySortingModeButton.setCallback(this);
		probabilitySortingModeButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(probabilitySortingModeButton);

		clearHiddenClusterButton = new GLButton(EButtonMode.BUTTON);
		setClearHiddenButtonRenderer();
		clearHiddenClusterButton.setCallback(this);
		clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
		this.add(clearHiddenClusterButton);

		this.dimBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Dimension Bands"));
		dimBandVisibilityButton.setSelected(true);
		dimBandVisibilityButton.setCallback(this);
		dimBandVisibilityButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(dimBandVisibilityButton);

		this.recBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Record Bands"));
		recBandVisibilityButton.setSelected(true);
		recBandVisibilityButton.setCallback(this);
		recBandVisibilityButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(recBandVisibilityButton);

		this.fixedClusterButton = new GLButton(EButtonMode.CHECKBOX);
		fixedClusterButton.setRenderer(GLButton.createCheckRenderer("Show only 15 Elements"));
		fixedClusterButton.setSelected(false);
		fixedClusterButton.setCallback(this);
		fixedClusterButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(fixedClusterButton);

		initSliders();
	}

	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1)));
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		if (slider == dimensionThresholdSlider || slider == recordThresholdSlider)
			updateGeneSampleThresholds();
		if (slider == clusterMinSizeThresholdSlider)
			EventPublisher.trigger(new MinClusterSizeThresholdChangeEvent(slider.getValue() / 100f));
	}

	private void updateGeneSampleThresholds() {
		float samplTh = dimensionThresholdSlider.getValue();
		float geneTh = recordThresholdSlider.getValue();
		EventPublisher.trigger(new LZThresholdChangeEvent(geneTh, samplTh, fixedClusterButton.isSelected(), true));
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if (button == fixedClusterButton) {
			updateGeneSampleThresholds();
		} else if (button == dimBandVisibilityButton) {
			EventPublisher.trigger(new RecalculateOverlapEvent(this, false, selected, recBandVisibilityButton
					.isSelected()));
		} else if (button == recBandVisibilityButton) {
			EventPublisher.trigger(new RecalculateOverlapEvent(this, false, dimBandVisibilityButton.isSelected(),
					selected));
		} else if (button == bandSortingModeButton) {
			bandSortingModeButton.setSelected(selected);
			probabilitySortingModeButton.setSelected(!selected);
		} else if (button == probabilitySortingModeButton) {
			probabilitySortingModeButton.setSelected(selected);
			bandSortingModeButton.setSelected(!selected);
		} else if (button == clearHiddenClusterButton) {
			clearHiddenButtonTooltipList = new ArrayList<>();
			clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
			setClearHiddenButtonRenderer();
			EventPublisher.trigger(new UnhidingClustersEvent());
		}
		boolean isBandSorting = bandSortingModeButton.isSelected();
		EventPublisher.trigger(new SortingChangeEvent(isBandSorting ? SortingType.bandSorting
				: SortingType.probabilitySorting, this));
	}

	@ListenTo
	private void listenTo(MaxThresholdChangeEvent e) {
		maxRecordValue = (float) e.getRecThreshold();
		maxDimensionValue = (float) e.getDimThreshold();
		initSliders();
	}

	private void setClearHiddenButtonRenderer() {
		if (clearHiddenButtonTooltipList.size() == 0) {
			clearHiddenClusterButton.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					g.drawText("Show all clusters", 18, 4, w, 13);
				}
			});
		} else
			clearHiddenClusterButton.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					g.drawText("Show all clusters (+" + clearHiddenButtonTooltipList.size() + ")", 18, 4, w, 13);
				}
			});
	}

	@ListenTo
	public void listenTo(ClusterGetsHiddenEvent event) {
		clearHiddenButtonTooltipList.add(event.getClusterID());
		StringBuilder tooltip = new StringBuilder("HiddenClusters: ");
		for (String s : clearHiddenButtonTooltipList) {
			tooltip.append("\n   ");
			tooltip.append(s);
		}
		clearHiddenClusterButton.setTooltip(tooltip.toString());
		setClearHiddenButtonRenderer();
	}

	/**
	 *
	 */
	private void initSliders() {
		createThresholdSlider();
		createMinimumClusterSizeSlider();
	}

	/**
	 *
	 */
	private void createMinimumClusterSizeSlider() {
		this.remove(clusterMinSizeLabel);
		this.clusterMinSizeLabel = new GLElement();
		clusterMinSizeLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(clusterMinSizeLabel);

		this.remove(clusterMinSizeThresholdSlider);
		this.clusterMinSizeThresholdSlider = new GLSlider(0, 100, 0f);
		clusterMinSizeThresholdSlider.setCallback(this);
		clusterMinSizeThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		clusterMinSizeThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterMinSizeThresholdSlider);
		setText(clusterMinSizeLabel, "Minumum Cluster Size (%)");
	}

	/**
	 *
	 */
	private void createThresholdSlider() {
		this.remove(dimensionLabel);
		this.dimensionLabel = new GLElement();
		dimensionLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(dimensionLabel);

		this.remove(dimensionThresholdSlider);
		this.dimensionThresholdSlider = new GLSlider(0.05f, maxDimensionValue, 4.5f);
		dimensionThresholdSlider.setCallback(this);
		dimensionThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		dimensionThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(dimensionThresholdSlider);

		this.remove(recordLabel);
		this.recordLabel = new GLElement();
		recordLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(recordLabel);

		this.remove(recordThresholdSlider);
		this.recordThresholdSlider = new GLSlider(0.02f, maxRecordValue, 0.08f);
		recordThresholdSlider.setCallback(this);
		recordThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		recordThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(recordThresholdSlider);

		if (x == null) {
			setText(dimensionLabel, "Dimension Threshold");
			setText(recordLabel, "Record Threshold");
		} else {
			setText(dimensionLabel, x.getDataDomain().getDimensionIDCategory().toString() + " Threshold");
			setText(recordLabel, x.getDataDomain().getRecordIDCategory().toString() + " Threshold");

		}

	}

	public void setXTablePerspective(final TablePerspective x) {
		if (x == null)
			return;
		else
			this.x = x;
		initSliders();
		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(x.getDataDomain().getRecordIDCategory()
				.toString()
				+ " Bands"));
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(x.getDataDomain().getDimensionIDCategory()
				.toString()));
	}

	/**
	 * @return
	 */
	@Override
	public Vec4f getPreferredBounds() {
		return new Vec4f(Float.NaN, 0, 200, 260);
	}

}
