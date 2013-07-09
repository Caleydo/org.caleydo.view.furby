/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxClusterSizeChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.util.ImportChemicalClustersDialog;
import org.caleydo.view.bicluster.util.ImportExternalDialog;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class GlobalToolBarElement extends GLElementContainer implements GLButton.ISelectionCallback,
		GLSlider.ISelectionCallback {


	private static final float MIN_DIMENSION_SIZE = 50;
	private static final float MIN_RECORD_SIZE = 50;
	private static final float MAX_DIMENSION_SIZE = 250;
	private static final float MAX_RECORD_SIZE = 250;

	float MAX_RECORD_VALUE = 0.2f;
	float MAX_DIMENSION_VALUE = 5f;

	private GLButton fixedClusterButton;
	private GLButton dimBandVisibilityButton;
	private GLButton recBandVisibilityButton;
	private GLButton bandSortingModeButton;
	private GLButton probabilitySortingModeButton;
	private GLSlider recordThresholdSlider;
	private GLSlider dimensionThresholdSlider;
	private GLSlider clusterMinSizeThresholdSlider;
	private GLSlider clusterDimSizeSlider, clusterRecSizeSlider;
	private GLElement clusterDimSizeLabel, clusterRecSizeLabel;
	private GLElement dimensionLabel;
	private GLElement recordLabel;
	private GLElement clusterMinSizeLabel;
	private GLButton clearHiddenClusterButton;
	private GLButton specialRecordButton;
	private GLButton specialDimensionButton;

	private List<String> clearHiddenButtonTooltipList = new ArrayList<>();
	private TablePerspective x;

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
		setClearHiddenButtonRenderer();
		clearHiddenClusterButton.setCallback(this);
		clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
		this.add(clearHiddenClusterButton);
		specialRecordButton = new GLButton(EButtonMode.BUTTON);
		specialRecordButton.setRenderer(new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.drawText("Add dim Element", 18, 4, w, 13);
			}
		});

		specialRecordButton.setCallback(this);
		specialRecordButton.setTooltip("Add special record Elements");
		this.add(specialRecordButton);

		specialDimensionButton = new GLButton(EButtonMode.BUTTON);
		specialDimensionButton.setRenderer(new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.drawText("Assign Chemical clusters", 18, 4, w, 13);
			}
		});
		specialDimensionButton.setCallback(this);
		specialDimensionButton.setTooltip("Add chemical Clusters");
		this.add(specialDimensionButton);

		this.dimBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Dimension Bands"));
		dimBandVisibilityButton.setSelected(true);
		dimBandVisibilityButton.setCallback(this);
		dimBandVisibilityButton.setSize(Float.NaN, 16);
		this.add(dimBandVisibilityButton);

		this.recBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Record Bands"));
		recBandVisibilityButton.setSelected(true);
		recBandVisibilityButton.setCallback(this);
		recBandVisibilityButton.setSize(Float.NaN, 16);
		this.add(recBandVisibilityButton);

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
		if (slider == dimensionThresholdSlider || slider == recordThresholdSlider)
			updateGeneSampleThresholds();
		if (slider == clusterMinSizeThresholdSlider)
			EventPublisher.trigger(new MinClusterSizeThresholdChangeEvent(slider.getValue() / 100f));
		if (slider == clusterDimSizeSlider || slider == clusterRecSizeSlider) {
			EventPublisher.trigger(new MaxClusterSizeChangeEvent(clusterDimSizeSlider.getValue(), clusterRecSizeSlider
					.getValue()));
		}
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
		} else if (button == specialRecordButton) {
			addSpecialRecords();
		} else if (button == specialDimensionButton) {
			addChemicalClusters();
		}
		boolean isBandSorting = bandSortingModeButton.isSelected();
		EventPublisher.trigger(new SortingChangeEvent(isBandSorting ? SortingType.bandSorting
				: SortingType.probabilitySorting, this));
	}

	private void addChemicalClusters() {
		ImportChemicalClustersDialog.open(this.x.getDimensionPerspective().getIdType());

	}

	private void addSpecialRecords() {
		ImportExternalDialog.open(this.x.getRecordPerspective().getIdType());
	}

	@ListenTo
	private void listenTo(MaxThresholdChangeEvent e) {
		MAX_RECORD_VALUE = (float) e.getRecThreshold();
		MAX_DIMENSION_VALUE = (float) e.getDimThreshold();
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
		this.remove(dimensionLabel);
		this.dimensionLabel = new GLElement();
		dimensionLabel.setSize(Float.NaN, 16);
		this.add(dimensionLabel);

		this.remove(dimensionThresholdSlider);
		this.dimensionThresholdSlider = new GLSlider(0.05f, MAX_DIMENSION_VALUE, 4.5f);
		dimensionThresholdSlider.setCallback(this);
		dimensionThresholdSlider.setSize(Float.NaN, 18);
		dimensionThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(dimensionThresholdSlider);

		this.remove(recordLabel);
		this.recordLabel = new GLElement();
		recordLabel.setSize(Float.NaN, 16);
		this.add(recordLabel);

		this.remove(recordThresholdSlider);
		this.recordThresholdSlider = new GLSlider(0.02f, MAX_RECORD_VALUE, 0.08f);
		recordThresholdSlider.setCallback(this);
		recordThresholdSlider.setSize(Float.NaN, 18);
		recordThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(recordThresholdSlider);

		this.remove(clusterMinSizeLabel);
		this.clusterMinSizeLabel = new GLElement();
		clusterMinSizeLabel.setSize(Float.NaN, 16);
		this.add(clusterMinSizeLabel);

		this.remove(clusterMinSizeThresholdSlider);
		this.clusterMinSizeThresholdSlider = new GLSlider(0, 100, 0f);
		clusterMinSizeThresholdSlider.setCallback(this);
		clusterMinSizeThresholdSlider.setSize(Float.NaN, 18);
		clusterMinSizeThresholdSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterMinSizeThresholdSlider);
		if (x == null) {
			setText(dimensionLabel, "Dimension Threshold");
			setText(recordLabel, "Record Threshold");
		} else {
			setText(dimensionLabel, x.getDataDomain().getDimensionIDCategory().toString() + " Threshold");
			setText(recordLabel, x.getDataDomain().getRecordIDCategory().toString() + " Threshold");

		}
		setText(clusterMinSizeLabel, "Minumum Cluster Size (%)");

		this.remove(clusterDimSizeLabel);
		this.clusterDimSizeLabel = new GLElement();
		this.clusterDimSizeLabel.setSize(Float.NaN, 16);
		this.add(clusterDimSizeLabel);

		this.remove(clusterDimSizeSlider);
		this.clusterDimSizeSlider = new GLSlider(MIN_DIMENSION_SIZE, MAX_DIMENSION_SIZE, 110f);
		this.clusterDimSizeSlider.setCallback(this);
		this.clusterDimSizeSlider.setSize(Float.NaN, 18);
		this.clusterDimSizeSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterDimSizeSlider);

		this.remove(clusterRecSizeLabel);
		this.clusterRecSizeLabel = new GLElement();
		this.clusterRecSizeLabel.setSize(Float.NaN, 16);
		this.add(clusterRecSizeLabel);

		this.remove(clusterRecSizeSlider);
		this.clusterRecSizeSlider = new GLSlider(MIN_RECORD_SIZE, MAX_RECORD_SIZE, 110f);
		this.clusterRecSizeSlider.setValue(150);
		this.clusterRecSizeSlider.setCallback(this);
		this.clusterRecSizeSlider.setSize(Float.NaN, 18);
		this.clusterRecSizeSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterRecSizeSlider);

		if (x == null) {
			setText(this.clusterDimSizeLabel, "Max. Dimension Size (pxl)");
			setText(this.clusterRecSizeLabel, "Max. Record Size (pxl)");
		} else {
			setText(this.clusterDimSizeLabel, "Max. " + x.getDataDomain().getDimensionIDCategory().toString()
					+ " Size (pxl)");
			setText(this.clusterRecSizeLabel, "Max. " + x.getDataDomain().getRecordIDCategory().toString()
					+ " Size (pxl)");

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
				.toString()
				+ " Bands"));
		specialRecordButton.setRenderer(new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.drawText("Add " + x.getDataDomain().getRecordIDCategory().toString() + " Elements", 18, 4, w, 13);
			}
		});

	}

}
