/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.sorting.BandSorting;

import com.google.common.base.Predicates;

/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public class NormalClusterElement extends AMultiClusterElement {

	private ToolBar toolBar;
	private ThresholdBar dimThreshBar;
	private ThresholdBar recThreshBar;

	/**
	 * elements for showing the probability heatmaps
	 */
	private LZHeatmapElement propabilityHeatMapHor;
	private LZHeatmapElement propabilityHeatMapVer;
	protected boolean showThreshold;

	public NormalClusterElement(TablePerspective data, BiClustering clustering) {
		super(data, clustering, Predicates.alwaysTrue());

		this.add(createTopToolBar());
		toolBar = new ToolBar(content.createButtonBarBuilder().layoutAs(EButtonBarLayout.SLIDE_LEFT).size(16).build()
				.setSize(16, 16));
		this.add(toolBar); // add a element toolbar
		dimThreshBar = new ThresholdBar(true);
		recThreshBar = new ThresholdBar(false);
		this.add(dimThreshBar);
		this.add(recThreshBar);

		this.propabilityHeatMapHor = new LZHeatmapElement(clustering.getZ(), true);
		this.add(this.propabilityHeatMapHor);
		this.propabilityHeatMapVer = new LZHeatmapElement(clustering.getL(), false);
		this.add(this.propabilityHeatMapVer);



		setZValuesAccordingToState();
	}

	private GLElementContainer createTopToolBar() {
		GLElementContainer topToolBar = new GLElementContainer(GLLayouts.flowVertical(3));
		topToolBar.add(createHideClusterButton());
		GLButton thresholds = new GLButton(EButtonMode.CHECKBOX);
		thresholds.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				showThreshold = selected;
				relayout();
			}
		});
		thresholds.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_TOOLS));
		thresholds.setSelectedRenderer(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.fillImage(g.getTexture(BiClusterRenderStyle.ICON_TOOLS), 0, 0, w, h, new Color(1, 1, 1, 0.5f));
			}
		});
		thresholds.setTooltip("Set cluster specific thresholds");
		thresholds.setSize(16, 16);
		topToolBar.add(thresholds);
		return topToolBar;
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		// if (isHidden) return;
		IGLLayoutElement top = children.get(0);
		IGLLayoutElement content = children.get(1);
		IGLLayoutElement corner = children.get(2);
		IGLLayoutElement left = children.get(3);
		IGLLayoutElement dimthreshbar = children.get(4);
		IGLLayoutElement recthreshbar = children.get(5);

		// shift for propability heat maps
		float shift = 6;

		if (isHovered || isShowAlwaysToolBar()) { // depending whether we are hovered or not, show hide the toolbar's
			corner.setBounds(-18 - shift, -18 - shift, 18, 18 * 2);
			if (showThreshold) {
				top.setBounds(0, -shift, w < 50 ? 50 : w, 0);
				left.setBounds(-18 - shift, 18, 0, left.getSetHeight());
				dimthreshbar.setBounds(0, -18 - shift, 180, 17);
				recthreshbar.setBounds(-18 - shift, 18, 17, 180);
			} else {
				top.setBounds(0, -18 - shift, w < 50 ? 50 : w, 17);
				left.setBounds(-18 - shift, 18, 18, left.getSetHeight());
				dimthreshbar.hide();
				recthreshbar.hide();
			}
		} else {
			// hide by setting the width to 0
			corner.setBounds(-18 - shift, -shift, 0, 18 * 2);
			top.setBounds(0, -18 - shift, w < 50 ? 50 : w, 17);
			left.setBounds(-18 - shift, 20, 0, left.getSetHeight());

			dimthreshbar.hide();
			recthreshbar.hide();
		}

		children.get(6).setBounds(-1, -shift, w + 2, shift);
		children.get(7).setBounds(-shift, -1, shift, h + 2);
		if (isFocused() && doesShowLabels(content.asElement())) {
			content.setBounds(0, 0, w + 79, h + 79);
		} else {
			content.setBounds(0, 0, w, h);
		}
	}

	@ListenTo
	private void listenTo(SortingChangeEvent e) {
		if (e.getSender() == this) {
			// only local change
		} else {
			sort(e.getType());
		}
		toolBar.setSortingCaption(e.getType());
	}

	protected class ThresholdBar extends GLElementDecorator implements
			org.caleydo.core.view.opengl.layout2.basic.GLSlider.ISelectionCallback {

		private final boolean isHorizontal;
		private final GLSlider slider;
		// float globalMaxThreshold;
		private float localMaxSliderValue;
		private float localMinSliderValue;

		protected ThresholdBar(boolean layout) {
			isHorizontal = layout;
			// move to the top
			setzDelta(DEFAULT_Z_DELTA);

			// create buttons
			float max = localMaxSliderValue > localMinSliderValue ? localMaxSliderValue : localMinSliderValue;
			this.slider = new GLSlider(0, max, max / 2);
			slider.setCallback(this);
			slider.setHorizontal(isHorizontal);
			slider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
			setContent(slider);
			setVisibility(EVisibility.PICKABLE); // for parent
		}

		@Override
		public void onSelectionChanged(GLSlider slider, float value) {
			if (value <= localMinSliderValue || value >= localMaxSliderValue)
				return;
			setThresholdImpl(isHorizontal, value);
		}

		protected void updateSliders(double maxValue, double minValue) {
			localMaxSliderValue = (float) maxValue;
			localMinSliderValue = (float) minValue;
			float max = localMaxSliderValue > localMinSliderValue ? localMaxSliderValue : localMinSliderValue;
			this.slider.setMinMax(0, max);
		}

		// @ListenTo
		// public void listenTo(MaxThresholdChangeEvent event) {
		// globalMaxThreshold = (float) (isHorizontal ? event.getDimThreshold() : event.getRecThreshold());
		// createButtons();
		// }

		@ListenTo
		public void listenTo(LZThresholdChangeEvent event) {
			if (event.isGlobalEvent()) {
				setValue(isHorizontal ? event.getDimensionThreshold() : event.getRecordThreshold());
			}
		}

		/**
		 * @param value
		 */
		public void setValue(float value) {
			slider.setCallback(null); // to avoid that we will be callbacked
			slider.setValue(value);
			slider.setCallback(this);
		}
	}

	/**
	 * @param isDimension
	 * @param value
	 */
	final void setThresholdImpl(boolean isDimension, float value) {
		if ((isDimension && dimThreshold == value) || (!isDimension && recThreshold == value))
			return;
		if (isDimension)
			dimThreshold = value;
		else
			recThreshold = value;
		rebuildMyData(false);
	}

	public final void setThreshold(boolean isDimension, float value) {
		setThresholdImpl(isDimension, value);
		if (isDimension && dimThreshBar != null)
			dimThreshBar.setValue(value);
		else if (!isDimension && recThreshBar != null)
			recThreshBar.setValue(value);
	}

	@Override
	protected void recreateVirtualArrays(List<Integer> dimIndices, List<Integer> recIndices) {
		VirtualArray dimArray = getDimensionVirtualArray();
		VirtualArray recArray = getRecordVirtualArray();
		addAll(dimArray, dimIndices, dimNumberThreshold);
		addAll(recArray, recIndices, recNumberThreshold);

		this.data.invalidateContainerStatistics();

		propabilityHeatMapHor.update(dimThreshold, this.bcNr, dimArray);
		propabilityHeatMapVer.update(recThreshold, this.bcNr, recArray);
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return data.getRecordPerspective().getVirtualArray();
	}

	@Override
	public int getNumberOfDimElements() {
		return getDimensionVirtualArray().size();
	}

	@Override
	public int getNumberOfRecElements() {
		return getRecordVirtualArray().size();
	}

	@Override
	protected void handleFocus(boolean isFocused) {
		super.handleFocus(isFocused);
		if (isFocused) {
			propabilityHeatMapHor.nonUniformLayout((content));
			propabilityHeatMapVer.nonUniformLayout((content));
		} else {
			propabilityHeatMapHor.uniformLayout();
			propabilityHeatMapVer.uniformLayout();
		}
	}

	@Override
	public void updateVisibility() {
		if (isHidden || !hasContent)
			setVisibility(EVisibility.NONE);
		else if (getDimensionVirtualArray().size() / elementCountBiggestCluster > clusterSizeThreshold)
			setVisibility(EVisibility.PICKABLE);
		else if (getRecordVirtualArray().size() / elementCountBiggestCluster > clusterSizeThreshold)
			setVisibility(EVisibility.PICKABLE);
		else
			setVisibility(EVisibility.NONE);
	}

	@Override
	public void setData(List<Integer> dimIndices, List<Integer> recIndices, String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		setLabel(id);
		if (maxDim >= 0 && maxRec >= 0) {
			dimThreshBar.updateSliders(maxDim, minDim);
			recThreshBar.updateSliders(maxRec, minRec);
		}
		dimProbabilitySorting = new ArrayList<Integer>(dimIndices);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		setHasContent(dimIndices, recIndices);
		updateVisibility();
	}

	@Override
	protected void setLabel(String id) {
		data.setLabel(id);
	}

	@Override
	protected void setHasContent(List<Integer> dimIndices, List<Integer> recIndices) {
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			hasContent = true;
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			hasContent = false;
		}
	}

	@Override
	protected void sort(SortingType type) {
		switch (type) {
		case probabilitySorting:
			sortingType = SortingType.probabilitySorting;
			probabilitySorting();
			break;
		case bandSorting:
			sortingType = SortingType.bandSorting;
			bandSorting();
			break;
		default:
		}
	}

	private void bandSorting() {
		Set<Integer> finalDimSorting = new LinkedHashSet<Integer>();
		List<List<Integer>> nonEmptyDimBands = new ArrayList<>();
		for (List<Integer> dimBand : dimOverlap.values()) {
			if (dimBand.size() > 0)
				nonEmptyDimBands.add(dimBand);
		}
		BandSorting dimConflicts = new BandSorting(nonEmptyDimBands);
		for (Integer i : dimConflicts) {
			finalDimSorting.add(i);
		}
		finalDimSorting.addAll(dimProbabilitySorting);

		Set<Integer> finalRecSorting = new LinkedHashSet<Integer>();
		List<List<Integer>> nonEmptyRecBands = new ArrayList<>();
		for (List<Integer> recBand : recOverlap.values()) {
			if (recBand.size() > 0)
				nonEmptyRecBands.add(recBand);
		}
		BandSorting recConflicts = new BandSorting(nonEmptyRecBands);
		for (Integer i : recConflicts) {
			finalRecSorting.add(i);
		}
		finalRecSorting.addAll(recProbabilitySorting);
		recreateVirtualArrays(new ArrayList<Integer>(finalDimSorting), new ArrayList<Integer>(finalRecSorting));
		fireTablePerspectiveChanged();
	}

	private void probabilitySorting() {
		sortingType = SortingType.probabilitySorting;
		recreateVirtualArrays(dimProbabilitySorting, recProbabilitySorting);
		fireTablePerspectiveChanged();
	}

	@Override
	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		Pair<List<Integer>, List<Integer>> pair = clustering.scan(bcNr, dimThreshold, recThreshold);
		setData(pair.getFirst(), pair.getSecond(), getID(), bcNr, -1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal, dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));
	}

	private GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_CLOSE));
		hide.setTooltip("Close");
		hide.setSize(18, 18);
		hide.setCallback(new ISelectionCallback() {

			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				hideThisCluster();
			}

		});
		return hide;
	}

	protected class ToolBar extends GLElementContainer implements ISelectionCallback {

		GLButton sorting, enlarge, smaller, focus, lock;
		SortingType sortingButtonCaption = SortingType.probabilitySorting;

		public ToolBar(GLElement switcher) {
			super(GLLayouts.flowVertical(3));
			setzDelta(-0.1f);
			this.add(switcher);
			createButtons();
			setSize(Float.NaN, this.size() * (16 + 3));
			setVisibility(EVisibility.PICKABLE);
		}

		protected void createButtons() {
			sorting = new GLButton();
			sorting.setRenderer(GLRenderers.drawText(
					sortingButtonCaption == SortingType.probabilitySorting ? "P" : "B", VAlign.CENTER));
			sorting.setSize(16, 16);
			sorting.setTooltip("Change sorting");
			sorting.setCallback(this);
			this.add(sorting);
			focus = new GLButton(GLButton.EButtonMode.CHECKBOX);
			focus.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_FOCUS));
			focus.setSize(16, 16);
			focus.setTooltip("Focus this Cluster");
			focus.setCallback(this);
			this.add(focus);
			lock = new GLButton();
			lock.setTooltip("Lock this cluster. It will not recieve threshold updates.");
			lock.setSize(16, 16);
			lock.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					if (isLocked)
						g.fillImage(BiClusterRenderStyle.ICON_UNLOCK, 0, 0, w, h);
					else
						g.fillImage(BiClusterRenderStyle.ICON_LOCK, 0, 0, w, h);
				}

			});
			lock.setCallback(this);
			this.add(lock);
			enlarge = new GLButton();
			enlarge.setSize(16, 16);
			enlarge.setTooltip("Enlarge");
			enlarge.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_ZOOM_IN));
			enlarge.setCallback(this);
			this.add(enlarge);
			smaller = new GLButton();
			smaller.setTooltip("Reduce");
			smaller.setSize(16, 16);
			smaller.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_ZOOM_OUT));
			smaller.setCallback(this);
			this.add(smaller);

		}

		void setSortingCaption(SortingType caption) {
			sortingButtonCaption = caption;
			sorting.setRenderer(GLRenderers.drawText(
					sortingButtonCaption == SortingType.probabilitySorting ? "P" : "B", VAlign.CENTER));
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			if (button == sorting) {
				setSortingCaption(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
				sort(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
			} else if (button == enlarge) {
				upscale();
				resize();
			} else if (button == smaller) {
				reduceScaleFactor();
				resize();
			} else if (button == focus) {
				EventPublisher.trigger(new FocusChangeEvent(NormalClusterElement.this, selected));
			} else if (button == lock) {
				isLocked = !isLocked;
				lock.setTooltip(isLocked ? "UnLock this cluster. It will again recieve threshold updates."
						: "Lock this cluster. It will not recieve threshold updates.");
			}
		}

	}
}
