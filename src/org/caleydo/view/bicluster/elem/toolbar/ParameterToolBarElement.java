/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.toolbar;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.UNBOUND_NUMBER;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.isShowDimBands;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.isShowRecBands;

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
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.basic.GLSpinner;
import org.caleydo.core.view.opengl.layout2.basic.GLSpinner.IChangeCallback;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.elem.ui.MySlider;
import org.caleydo.view.bicluster.event.ChangeMaxDistanceEvent;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.ResetSettingsEvent;
import org.caleydo.view.bicluster.event.ShowHideBandsEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.SwitchVisualizationEvent;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class ParameterToolBarElement extends AToolBarElement implements GLSpinner.IChangeCallback<Integer>,
		MySlider.ISelectionCallback {

	private static final SortingType DEFAULT_SORTING_MODE = SortingType.BY_PROBABILITY;

	private final GLButton bandSortingModeButton;
	private final GLButton probabilitySortingModeButton;

	private final GLButton dimBandVisibilityButton;
	private final GLButton recBandVisibilityButton;

	private final GLButton clearHiddenClusterButton;
	private final List<String> clearHiddenButtonTooltipList = new ArrayList<>();

	private GLElement recordLabel;
	private GLSpinner<Integer> recordNumberThresholdSpinner;
	private MySlider recordThresholdSlider;
	private GLElement dimensionLabel;
	private GLSpinner<Integer> dimensionNumberThresholdSpinner;
	private MySlider dimensionThresholdSlider;
	private GLElement clusterMinSizeLabel;
	private GLSlider clusterMinSizeThresholdSlider;

	private final GLComboBox<GLElementSupplier> visualizationSwitcher;
	private final List<GLElementSupplier> visualizationSwitcherModel = new ArrayList<>();

	private final GLSpinner<Integer> maxDistance;

	public ParameterToolBarElement() {
		this.bandSortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		bandSortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Bands"));
		bandSortingModeButton.setSelected(DEFAULT_SORTING_MODE == SortingType.BY_BAND);
		bandSortingModeButton.setCallback(this);
		bandSortingModeButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(bandSortingModeButton);

		this.probabilitySortingModeButton = new GLButton(EButtonMode.CHECKBOX);
		probabilitySortingModeButton.setRenderer(GLButton.createRadioRenderer("Sort by Probability"));
		probabilitySortingModeButton.setSelected(DEFAULT_SORTING_MODE == SortingType.BY_PROBABILITY);
		probabilitySortingModeButton.setCallback(this);
		probabilitySortingModeButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(probabilitySortingModeButton);
		this.add(createHorizontalLine());

		{
			clearHiddenClusterButton = new GLButton(EButtonMode.BUTTON);
			setClearHiddenButtonRenderer();
			clearHiddenClusterButton.setCallback(this);
			clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
			clearHiddenClusterButton.setSize(Float.NaN, BUTTON_WIDTH);
			this.add(clearHiddenClusterButton);
		}
		this.add(createHorizontalLine());

		this.dimBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Dimension Bands"));
		dimBandVisibilityButton.setSelected(isShowDimBands());
		dimBandVisibilityButton.setCallback(this);
		dimBandVisibilityButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(dimBandVisibilityButton);

		this.recBandVisibilityButton = new GLButton(EButtonMode.CHECKBOX);
		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer("Record Bands"));
		recBandVisibilityButton.setSelected(isShowRecBands());
		recBandVisibilityButton.setCallback(this);
		recBandVisibilityButton.setSize(Float.NaN, BUTTON_WIDTH);
		this.add(recBandVisibilityButton);

		visualizationSwitcher = new GLComboBox<>(visualizationSwitcherModel, new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				float wi = h - 2;
				GLElementSupplier elem = parent.getLayoutDataAs(GLElementSupplier.class, null);
				g.fillImage(elem.getIcon(), 1, 1, wi, wi);
				g.drawText(elem.getLabel(), wi + 3, 1, w - wi - 5, h - 2);
			}
		}, GLRenderers.fillRect(Color.WHITE));
		visualizationSwitcher.setCallback(new ISelectionCallback<GLElementSupplier>() {
			@Override
			public void onSelectionChanged(GLComboBox<? extends GLElementSupplier> widget, GLElementSupplier item) {
				EventPublisher.trigger(new SwitchVisualizationEvent(item.getId()));
			}
		});
		visualizationSwitcher.setSize(Float.NaN, BUTTON_WIDTH);
		visualizationSwitcher.setzDelta(0.2f);
		this.add(visualizationSwitcher);

		createThresholdSlider();
		createMinimumClusterSizeSlider();

		GLElementContainer c = new GLElementContainer(GLLayouts.flowHorizontal(2));
		this.maxDistance = GLSpinner.createIntegerSpinner(1, 0, 4, 1);
		maxDistance.setCallback(new IChangeCallback<Integer>() {
			@Override
			public void onValueChanged(GLSpinner<? extends Integer> spinner, Integer value) {
				EventPublisher.trigger(new ChangeMaxDistanceEvent(value.intValue()));
			}
		});
		maxDistance.setTooltip("specifies the maximal distance for automatic hiding of connected clusters");
		maxDistance.setSize(-1, -1);
		c.add(new GLElement(GLRenderers.drawText("Max Distance: ")).setSize(100, -1));
		c.add(maxDistance);
		this.add(c.setSize(-1, LABEL_WIDTH));

		GLButton reset = new GLButton();
		reset.setRenderer(GLRenderers.drawText("Reset", VAlign.CENTER, new GLPadding(0, 0, 0, 2)));
		reset.setCallback(new GLButton.ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				EventPublisher.trigger(new ResetSettingsEvent());
			}
		});
		reset.setTooltip("Reset the layout parameters to their default value");
		reset.setSize(Float.NaN, LABEL_WIDTH);
		this.add(reset);
	}

	@Override
	public void reset() {
		this.dimBandVisibilityButton.setSelected(isShowDimBands());
		this.recBandVisibilityButton.setSelected(isShowRecBands());
		this.bandSortingModeButton.setCallback(null).setSelected(DEFAULT_SORTING_MODE == SortingType.BY_BAND).setCallback(this);
		this.probabilitySortingModeButton.setSelected(DEFAULT_SORTING_MODE == SortingType.BY_PROBABILITY);

		this.maxDistance.setValue(MyPreferences.getMaxDistance());
		this.visualizationSwitcher.setSelected(0);

		this.clusterMinSizeThresholdSlider.setValue(0);

		this.dimensionNumberThresholdSpinner.setCallback(null).setValue(getDimTopNElements()).setCallback(this);
		this.dimensionThresholdSlider.setCallback(null).setValue(getDimThreshold()).setCallback(this);
		this.recordNumberThresholdSpinner.setCallback(null).setValue(getRecTopNElements()).setCallback(this);
		this.recordThresholdSlider.setValue(getRecThreshold());

		setClearHiddenButtonRenderer();
		EventPublisher.trigger(new UnhidingClustersEvent());
	}

	/**
	 * @return
	 */
	private static List<GLElementSupplier> createSupplier(TablePerspective data) {
		GLElementFactoryContext.Builder builder = GLElementFactoryContext.builder();
		builder.withData(data);
		return GLElementFactories.getExtensions(builder.build(), "bicluster", null);
	}

	private GLElement createHorizontalLine() {
		return new GLElement(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.color(Color.DARK_GRAY).drawLine(2, h / 2, w - 4, h / 2);
			}
		}).setSize(Float.NaN, 5);
	}

	private GLElement createGroupLabelLine(final String name) {
		return new GLElement(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.color(Color.DARK_GRAY).drawLine(2, h - 1, w - 4, h - 1);
				g.drawText(name, 1, 1, w - 2, h - 3);
			}
		}).setSize(Float.NaN, LABEL_WIDTH + 4);
	}

	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1, 0, 1, 2)));
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		if (slider == clusterMinSizeThresholdSlider)
			EventPublisher.trigger(new MinClusterSizeThresholdChangeEvent(slider.getValue() / 100f));
	}

	@Override
	public void onSelectionChanged(MySlider slider, float value) {
		if (slider == dimensionThresholdSlider || slider == recordThresholdSlider)
			updateGeneSampleThresholds();
	}

	private void updateGeneSampleThresholds() {
		float samplTh = dimensionThresholdSlider.getValue();
		float geneTh = recordThresholdSlider.getValue();
		int sampleNumberTh = dimensionNumberThresholdSpinner.getValue();
		int geneNumberTh = recordNumberThresholdSpinner.getValue();
		EventPublisher.trigger(new LZThresholdChangeEvent(geneTh, samplTh, geneNumberTh, sampleNumberTh, true));
	}

	@Override
	public void onValueChanged(GLSpinner<? extends Integer> spinner, Integer value) {
		updateGeneSampleThresholds();
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if (button == dimBandVisibilityButton) {
			EventPublisher.trigger(new ShowHideBandsEvent(selected, recBandVisibilityButton.isSelected()));
		} else if (button == recBandVisibilityButton) {
			EventPublisher.trigger(new ShowHideBandsEvent(dimBandVisibilityButton.isSelected(), selected));
		} else if (button == bandSortingModeButton) {
			bandSortingModeButton.setSelected(selected);
			probabilitySortingModeButton.setSelected(!selected);
		} else if (button == probabilitySortingModeButton) {
			probabilitySortingModeButton.setSelected(selected);
			bandSortingModeButton.setSelected(!selected);
		} else if (button == clearHiddenClusterButton) {
			clearHiddenButtonTooltipList.clear();
			clearHiddenClusterButton.setTooltip("Currently no Clusters are hidden");
			setClearHiddenButtonRenderer();
			EventPublisher.trigger(new UnhidingClustersEvent());
		}
		boolean isBandSorting = bandSortingModeButton.isSelected();
		EventPublisher.trigger(new SortingChangeEvent(isBandSorting ? SortingType.BY_BAND
 : SortingType.BY_PROBABILITY,
				this));
	}

	@ListenTo
	private void listenTo(MaxThresholdChangeEvent e) {
		this.recordThresholdSlider.setMinMax(0.02f, (float) e.getRecThreshold());
		this.dimensionThresholdSlider.setMinMax(0, (float) e.getDimThreshold());
	}

	private void setClearHiddenButtonRenderer() {
		String text;
		if (clearHiddenButtonTooltipList.size() == 0) {
			text = "Show all clusters";
		} else
			text = "Show all clusters (+" + clearHiddenButtonTooltipList.size() + ")";
		clearHiddenClusterButton.setRenderer(new MyTextRender(text));
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
	private void createMinimumClusterSizeSlider() {
		this.clusterMinSizeLabel = new GLElement();
		clusterMinSizeLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(clusterMinSizeLabel);

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
		this.add(createGroupLabelLine("Thresholds"));

		this.dimensionLabel = new GLElement();
		setText(dimensionLabel, "Dimension Threshold");
		dimensionLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(dimensionLabel);

		this.dimensionNumberThresholdSpinner = GLSpinner.createIntegerSpinner(getDimTopNElements(), UNBOUND_NUMBER,
				Integer.MAX_VALUE, 1,
				SPINNER_UNBOUND);
		this.dimensionNumberThresholdSpinner.setCallback(this);
		this.dimensionNumberThresholdSpinner.setSize(Float.NaN, SLIDER_WIDH);
		this.add(wrapSpinner(this.dimensionNumberThresholdSpinner));

		this.dimensionThresholdSlider = new MySlider(true, 0, 5f, getDimThreshold());
		dimensionThresholdSlider.setCallback(this);
		dimensionThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.add(dimensionThresholdSlider);

		this.recordLabel = new GLElement();
		setText(recordLabel, "Record Threshold");
		recordLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(recordLabel);

		this.recordNumberThresholdSpinner = GLSpinner.createIntegerSpinner(getRecTopNElements(), UNBOUND_NUMBER,
				Integer.MAX_VALUE, 1, SPINNER_UNBOUND);
		this.recordNumberThresholdSpinner.setCallback(this);
		this.recordNumberThresholdSpinner.setSize(Float.NaN, SLIDER_WIDH);
		this.add(wrapSpinner(this.recordNumberThresholdSpinner));

		this.recordThresholdSlider = new MySlider(true, 0.02f, 0.2f, getRecThreshold());
		recordThresholdSlider.setCallback(this);
		recordThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.add(recordThresholdSlider);

	}

	/**
	 * @param dimensionNumberThresholdSpinner2
	 * @return
	 */
	private static GLElement wrapSpinner(GLElement elem) {
		GLElementContainer c = new GLElementContainer(GLLayouts.flowHorizontal(2));
		GLElement label = new GLElement(new MyTextRender("Max # elements: ", 2));
		c.add(label.setSize(90, Float.NaN));
		c.add(elem);
		c.setSize(Float.NaN, elem.getSize().y());
		return c;
	}

	@Override
	public void init(final TablePerspective x) {
		final String dimensionIDCategory = x.getDataDomain().getDimensionIDCategory().getCategoryName();
		final String recordIDCategory = x.getDataDomain().getRecordIDCategory().getCategoryName();

		setText(dimensionLabel, dimensionIDCategory + " Threshold");
		setText(recordLabel, recordIDCategory + " Threshold");

		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(recordIDCategory + " Bands"));
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(dimensionIDCategory + " Bands"));

		this.visualizationSwitcherModel.addAll(createSupplier(x));
		this.visualizationSwitcher.setSelected(0);
	}

	/**
	 * @return
	 */
	@Override
	public Rect getPreferredBounds() {
		return new Rect(-205, 0, 200, 365 + 20);
	}

	private static class MyTextRender implements IGLRenderer {
		private final String text;
		private final float shift;

		public MyTextRender(String text) {
			this(text, 18);
		}

		public MyTextRender(String text, float shift) {
			this.text = text;
			this.shift = shift;
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			g.drawText(text, shift, 1, w - shift, 13);
		}
	}

	public static final IGLRenderer SPINNER_UNBOUND = new IGLRenderer() {
		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			Integer r = parent.getLayoutDataAs(Integer.class, Integer.valueOf(UNBOUND_NUMBER));
			String text;
			if (r == UNBOUND_NUMBER) {
				text = "Unbound";
			} else
				text = r.toString();
			g.drawText(text, 2, 1, w - 2, h - 3);
		}

	};

}
