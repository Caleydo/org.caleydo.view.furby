/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.toolbar;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.isShowDimBands;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.isShowRecBands;
import gleem.linalg.Vec2f;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TableDoubleLists;
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
import org.caleydo.core.view.opengl.layout2.basic.GLSpinner;
import org.caleydo.core.view.opengl.layout2.basic.GLSpinner.IChangeCallback;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.elem.BiClustering;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.ui.MyUnboundSpinner;
import org.caleydo.view.bicluster.elem.ui.ThresholdSlider;
import org.caleydo.view.bicluster.event.ChangeMaxDistanceEvent;
import org.caleydo.view.bicluster.event.HideClusterEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.ResetSettingsEvent;
import org.caleydo.view.bicluster.event.ShowHideBandsEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SwitchVisualizationEvent;
import org.caleydo.view.bicluster.event.ZoomEvent;
import org.caleydo.view.bicluster.internal.BiClusterRenderStyle;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy.IComposeAbleSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.BandSortingStrategy;
import org.caleydo.view.bicluster.sorting.CenterProbabilitySortingStrategy;
import org.caleydo.view.bicluster.sorting.ComposedSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.DefaultSortingStrategy;
import org.caleydo.view.bicluster.sorting.ISortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.ProbabilitySortingStrategy;

import com.google.common.collect.Iterables;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class ParameterToolBarElement extends AToolBarElement implements MyUnboundSpinner.IChangeCallback,
		ThresholdSlider.ISelectionCallback, GLComboBox.ISelectionCallback<ISortingStrategyFactory> {

	private static final ISortingStrategyFactory DEFAULT_PRIMARY_SORTING_MODE = ProbabilitySortingStrategy.FACTORY_INC;
	private static final ISortingStrategyFactory DEFAULT_SECONDARY_SORTING_MODE = DefaultSortingStrategy.FACTORY;

	private final GLComboBox<ISortingStrategyFactory> sorterPrimary;
	private final GLComboBox<ISortingStrategyFactory> sorterSecondary;
	private List<ISortingStrategyFactory> sortingModelPrimary;
	private List<ISortingStrategyFactory> sortingModelSecondary;

	private final GLButton dimBandVisibilityButton;
	private final GLButton recBandVisibilityButton;

	private GLElement recLabel;
	private MyUnboundSpinner recNumberThresholdSpinner;
	private ThresholdSlider recThresholdSlider;
	private GLElement dimLabel;
	private MyUnboundSpinner dimNumberThresholdSpinner;
	private ThresholdSlider dimThresholdSlider;

	private final GLComboBox<GLElementSupplier> visualizationSwitcher;
	private final List<GLElementSupplier> visualizationSwitcherModel = new ArrayList<>();

	private final GLSpinner<Integer> maxDistance;
	private final HiddenClusters hidden;

	public ParameterToolBarElement() {
		this.add(createGroupLabelLine("Visual Settings"));
		this.sortingModelPrimary = createSortingModel(true);
		this.sorterPrimary = new GLComboBox<ISortingStrategyFactory>(sortingModelPrimary, GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		this.sorterPrimary.setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE);
		this.sorterPrimary.setCallback(this);
		this.sorterPrimary.setSize(Float.NaN, BUTTON_WIDTH);
		this.sorterPrimary.setzDeltaList(0.5f);
		this.add(sorterPrimary);

		this.sortingModelSecondary = new ArrayList<>();
		this.sorterSecondary = new GLComboBox<ISortingStrategyFactory>(sortingModelSecondary, GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		this.sorterSecondary.setCallback(this);
		this.sorterSecondary.setSize(Float.NaN, BUTTON_WIDTH);
		this.sorterSecondary.setzDeltaList(0.5f);
		this.add(sorterSecondary);

		updateSecondary(DEFAULT_PRIMARY_SORTING_MODE);

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

		this.add(createGroupLabelLine("Zoom"));
		this.add(createZoomControls());

		createThresholdSlider();

		this.add(createGroupLabelLine("Hidden Clusters"));

		this.hidden = new HiddenClusters();
		this.add(ScrollingDecorator.wrap(hidden, 8));
		this.add(createHorizontalLine());

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

	/**
	 * @return
	 */
	private GLElement createZoomControls() {
		GLElementContainer c = new GLElementContainer();
		c.add(createZoomButton("Zoom Out", BiClusterRenderStyle.ICON_ZOOM_OUT, -1, null));
		c.add(createZoomButton("Zoom Reset", BiClusterRenderStyle.ICON_ZOOM_RESET, 0, null));
		c.add(createZoomButton("Zoom In", BiClusterRenderStyle.ICON_ZOOM_IN, +1, null));
		c.add(createZoomButton("Zoom Out X", BiClusterRenderStyle.ICON_ZOOM_OUT, -1, EDimension.DIMENSION));
		c.add(createZoomButton("Zoom Out Y", BiClusterRenderStyle.ICON_ZOOM_OUT, -1, EDimension.RECORD));
		c.add(createZoomButton("Zoom In X", BiClusterRenderStyle.ICON_ZOOM_IN, +1, EDimension.DIMENSION));
		c.add(createZoomButton("Zoom In Y", BiClusterRenderStyle.ICON_ZOOM_IN, +1, EDimension.RECORD));
		c.add(new GLElement(GLRenderers.drawText("X:", VAlign.RIGHT)));
		c.add(new GLElement(GLRenderers.drawText("Y:", VAlign.RIGHT)));
		c.setSize(Float.NaN, BUTTON_WIDTH);
		c.setLayout(new IGLLayout2() {
			@Override
			public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h,
					IGLLayoutElement parent, int deltaTimeMs) {
				final float w_i = BUTTON_WIDTH + 1;
				float xi = (w - w_i * 10) * 0.5f;

				for (int i = 0; i < 3; ++i)
					children.get(i).setBounds(xi + i * w_i, 0, BUTTON_WIDTH, h);
				xi += w_i * 3;
				for (int i = 0; i < 2; ++i) {
					children.get(3 + i).setBounds(xi + (i * 3.5f + 1.5f) * w_i, 0, BUTTON_WIDTH, h);
					children.get(5 + i).setBounds(xi + (i * 3.5f + 2.5f) * w_i, 0, BUTTON_WIDTH, h);
					children.get(7 + i).setBounds(xi + i * 3.5f * w_i, 0, BUTTON_WIDTH * 1.5f, h);
				}
				return false;
			}
		});
		return c;
	}

	private GLElement createZoomButton(String label, URL icon, final int dir, final EDimension dim) {
		GLButton b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(icon));
		b.setTooltip(label);
		b.setCallback(new GLButton.ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				EventPublisher.trigger(new ZoomEvent(dir, dim));
			}
		});
		return b;
	}

	/**
	 * @param all
	 * @return
	 */
	private List<ISortingStrategyFactory> createSortingModel(boolean all) {
		List<ISortingStrategyFactory> r = new ArrayList<>();
		r.add(ProbabilitySortingStrategy.FACTORY_INC);
		r.add(ProbabilitySortingStrategy.FACTORY_INC_ABS);
		r.add(ProbabilitySortingStrategy.FACTORY_DEC);
		r.add(ProbabilitySortingStrategy.FACTORY_DEC_ABS);
		r.add(DefaultSortingStrategy.FACTORY);
		r.add(BandSortingStrategy.FACTORY);
		r.add(CenterProbabilitySortingStrategy.FACTORY);
		if (!all)
			for (Iterator<ISortingStrategyFactory> it = r.iterator(); it.hasNext();)
				if (!(it.next() instanceof AComposeAbleSortingStrategy))
					it.remove();
		return r;
	}

	@Override
	public void reset() {
		this.dimBandVisibilityButton.setSelected(isShowDimBands());
		this.recBandVisibilityButton.setSelected(isShowRecBands());
		this.sorterPrimary.setCallback(null).setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE).setCallback(this);
		updateSecondary(DEFAULT_PRIMARY_SORTING_MODE);

		this.maxDistance.setValue(MyPreferences.getMaxDistance());
		this.visualizationSwitcher.setSelected(0);

		this.dimNumberThresholdSpinner.setValue(getDimTopNElements());
		this.dimThresholdSlider.setCallback(null).setValue(getDimThreshold()).setCallback(this);
		this.recNumberThresholdSpinner.setValue(getRecTopNElements());
		this.recThresholdSlider.setValue(getRecThreshold());

		this.hidden.showAll();
	}

	private void updateSecondary(ISortingStrategyFactory primary) {
		this.sorterSecondary.setCallback(null);
		this.sortingModelSecondary.clear();
		if (!(primary instanceof IComposeAbleSortingStrategyFactory)) {
			this.sorterSecondary.setVisibility(EVisibility.VISIBLE);
			this.sorterSecondary.setSelected(-1);
		} else {
			this.sortingModelSecondary.clear();
			this.sortingModelSecondary.addAll(sortingModelPrimary);
			for (Iterator<ISortingStrategyFactory> it = this.sortingModelSecondary.iterator(); it.hasNext();) {
				ISortingStrategyFactory act = it.next();
				if (act == primary || !(act instanceof IComposeAbleSortingStrategyFactory))
					it.remove();
			}
			this.sorterSecondary
					.setSelectedItem(primary == DEFAULT_SECONDARY_SORTING_MODE ? DEFAULT_PRIMARY_SORTING_MODE
							: DEFAULT_SECONDARY_SORTING_MODE);
			this.sorterSecondary.setVisibility(EVisibility.PICKABLE);
		}
		this.sorterSecondary.setCallback(this);
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
	}

	@Override
	public void onSelectionChanged(ThresholdSlider slider, float value) {
		updateGeneSampleThresholds(EDimension.get(slider == dimThresholdSlider));
	}

	@Override
	public void onSelectionChanged(GLComboBox<? extends ISortingStrategyFactory> widget, ISortingStrategyFactory item) {
		if (widget == sorterPrimary && item != null) {
			updateSecondary(item);
		}
		ISortingStrategyFactory primary = sorterPrimary.getSelectedItem();
		ISortingStrategyFactory secondary = sorterSecondary.getSelectedItem();
		if (primary == null)
			return;
		ISortingStrategyFactory r;
		if (primary instanceof IComposeAbleSortingStrategyFactory && secondary instanceof IComposeAbleSortingStrategyFactory) {
			r = new ComposedSortingStrategyFactory((IComposeAbleSortingStrategyFactory) primary,
					(IComposeAbleSortingStrategyFactory) secondary);
		} else
			r = primary;
		EventPublisher.trigger(new SortingChangeEvent(r));
	}

	@Override
	public void onValueChanged(MyUnboundSpinner spinner, int value) {
		updateGeneSampleThresholds(EDimension.get(spinner == dimNumberThresholdSpinner));
	}

	/**
	 * @param eDimension
	 */
	private void updateGeneSampleThresholds(EDimension dim) {
		if (dim.isDimension())
			EventPublisher.trigger(new LZThresholdChangeEvent(dim, dimThresholdSlider.getValue(),
					dimNumberThresholdSpinner.getValue()));
		else
			EventPublisher.trigger(new LZThresholdChangeEvent(dim, recThresholdSlider.getValue(),
					recNumberThresholdSpinner.getValue()));
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if (button == dimBandVisibilityButton) {
			EventPublisher.trigger(new ShowHideBandsEvent(selected, recBandVisibilityButton.isSelected()));
		} else if (button == recBandVisibilityButton) {
			EventPublisher.trigger(new ShowHideBandsEvent(dimBandVisibilityButton.isSelected(), selected));
		}
	}

	@ListenTo
	private void listenTo(MaxThresholdChangeEvent e) {
		this.recThresholdSlider.setMinMax(0, (float) e.getRecThreshold());
		this.dimThresholdSlider.setMinMax(0, (float) e.getDimThreshold());
	}

	/**
	 *
	 */
	private void createThresholdSlider() {
		this.add(createGroupLabelLine("Thresholds"));

		this.dimLabel = new GLElement();
		setText(dimLabel, "Dimension Threshold");
		dimLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(dimLabel);

		this.dimNumberThresholdSpinner = new MyUnboundSpinner(getDimTopNElements());
		this.dimNumberThresholdSpinner.setCallback(this);
		this.dimNumberThresholdSpinner.setSize(Float.NaN, SLIDER_WIDH);
		this.add(wrapSpinner(this.dimNumberThresholdSpinner));

		this.dimThresholdSlider = new ThresholdSlider(0, 5f, getDimThreshold());
		dimThresholdSlider.setCallback(this);
		dimThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.add(dimThresholdSlider);

		this.recLabel = new GLElement();
		setText(dimLabel, "Record Threshold");
		recLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(recLabel);

		this.recNumberThresholdSpinner = new MyUnboundSpinner(getRecTopNElements());
		this.recNumberThresholdSpinner.setCallback(this);
		this.recNumberThresholdSpinner.setSize(Float.NaN, SLIDER_WIDH);
		this.add(wrapSpinner(this.recNumberThresholdSpinner));

		this.recThresholdSlider = new ThresholdSlider(0.02f, 0.2f, getRecThreshold());
		recThresholdSlider.setCallback(this);
		recThresholdSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.add(recThresholdSlider);

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
	public void init(final BiClustering clustering) {
		ATableBasedDataDomain dataDomain = clustering.getXDataDomain();
		final String dimensionIDCategory = dataDomain.getDimensionIDCategory().getCategoryName();
		final String recordIDCategory = dataDomain.getRecordIDCategory().getCategoryName();

		setText(dimLabel, dimensionIDCategory + " Threshold");
		setText(recLabel, recordIDCategory + " Threshold");

		recBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(recordIDCategory + " Bands"));
		dimBandVisibilityButton.setRenderer(GLButton.createCheckRenderer(dimensionIDCategory + " Bands"));

		this.visualizationSwitcherModel
				.addAll(createSupplier(clustering.getXDataDomain().getDefaultTablePerspective()));
		this.visualizationSwitcher.setSelected(0);

		dimThresholdSlider.setStats(TableDoubleLists.asRawList(clustering.getLZ(EDimension.DIMENSION)));
		recThresholdSlider.setStats(TableDoubleLists.asRawList(clustering.getLZ(EDimension.RECORD)));

	}

	/**
	 * @return
	 */
	@Override
	public Rect getPreferredBounds() {
		return new Rect(-205, 0, 200, 480 + 20);
	}

	private static class MyTextRender implements IGLRenderer {
		private final String text;
		private final float shift;

		public MyTextRender(String text, float shift) {
			this.text = text;
			this.shift = shift;
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			g.drawText(text, shift, 1, w - shift, 13);
		}
	}

	/**
	 * @param factory
	 */
	public void addSortingMode(ISortingStrategyFactory factory) {
		this.sortingModelPrimary.add(factory);
		if (this.sorterPrimary.getSelectedItem() instanceof IComposeAbleSortingStrategyFactory
				&& factory instanceof IComposeAbleSortingStrategyFactory)
			this.sortingModelSecondary.add(factory);
		this.sorterPrimary.repaint();
	}

	/**
	 * @param data
	 */
	public void removeSortingMode(ISortingStrategyFactory data) {
		if (sorterPrimary.getSelectedItem() == data)
			sorterPrimary.setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE);
		if (this.sortingModelPrimary.remove(data))
			this.sorterPrimary.repaint();
	}

	private static class HiddenClusters extends GLElementContainer implements IHasMinSize, GLButton.ISelectionCallback {
		public HiddenClusters() {
			super(GLLayouts.flowVertical(2));
		}

		public void add(ClusterElement elem) {
			GLButton b = new GLButton();
			b.setRenderer(GLRenderers.drawText(elem.getLabel(), VAlign.LEFT, new GLPadding(4, 1)));
			b.setSize(-1, 12);
			b.setLayoutData(elem);
			b.setCallback(this);
			this.add(b);
			relayoutParent();
		}

		public void showAll() {
			for (GLButton b : Iterables.filter(this, GLButton.class)) {
				b.setSelected(true);
			}
		}

		@ListenTo
		private void onHideClusterEvent(HideClusterEvent event) {
			add(event.getElem());
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			ClusterElement r = button.getLayoutDataAs(ClusterElement.class, null);
			r.show();
			remove(button);
			relayoutParent();
		}

		@Override
		public Vec2f getMinSize() {
			return new Vec2f(100, size() * 14);
		}
	}

	/**
	 * @param dimension
	 * @param thresh
	 */
	public void setThreshold(EDimension dimension, float thresh) {
		dimension.select(this.dimThresholdSlider, this.recThresholdSlider).setValue(thresh);
	}
}
