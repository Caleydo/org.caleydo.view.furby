/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecTopNElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleSizedIterable;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.elem.annotation.ALZHeatmapElement;
import org.caleydo.view.bicluster.elem.annotation.ProbabilityLZHeatmapElement;
import org.caleydo.view.bicluster.elem.ui.ThresholdSlider;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.sorting.FuzzyClustering;
import org.caleydo.view.bicluster.sorting.ISortingStrategy;
import org.caleydo.view.bicluster.sorting.IntFloat;
import org.caleydo.view.bicluster.sorting.ProbabilitySortingStrategy;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

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

	private final FuzzyClustering dimClustering;
	private final FuzzyClustering recClustering;

	protected float recThreshold = getRecThreshold();
	protected int recNumberThreshold = getRecTopNElements();
	protected float dimThreshold = getDimThreshold();
	protected int dimNumberThreshold = getDimTopNElements();

	/**
	 * elements for showing the probability heatmaps
	 */
	private Collection<ALZHeatmapElement> annotations = new ArrayList<>(4);

	protected boolean showThreshold;

	protected ISortingStrategy dimSorter = ProbabilitySortingStrategy.FACTORY_INC.create(this, EDimension.DIMENSION);
	protected ISortingStrategy recSorter = ProbabilitySortingStrategy.FACTORY_INC.create(this, EDimension.RECORD);

	public NormalClusterElement(int bcNr, TablePerspective data, BiClustering clustering) {
		super(bcNr, data, clustering, Predicates.alwaysTrue());
		this.dimClustering = clustering.getDimClustering(bcNr);
		this.recClustering = clustering.getRecClustering(bcNr);

		this.add(createTopToolBar());
		toolBar = new ToolBar(content.createButtonBarBuilder().layoutAs(EButtonBarLayout.SLIDE_LEFT).size(16).build()
				.setSize(16, 16));
		this.add(toolBar); // add a element toolbar
		dimThreshBar = new ThresholdBar(true);
		recThreshBar = new ThresholdBar(false);
		this.add(dimThreshBar);
		this.add(recThreshBar);

		dimThreshBar.updateSliders(clustering.getMaxAbsProbability(EDimension.DIMENSION),
				clustering.getClustering(EDimension.DIMENSION, bcNr));
		recThreshBar.updateSliders(clustering.getMaxAbsProbability(EDimension.RECORD),
				clustering.getClustering(EDimension.DIMENSION, bcNr));

		this.add(new HeatMapLabelElement(true, data.getDimensionPerspective(), content));
		this.add(new HeatMapLabelElement(false, data.getRecordPerspective(), content));

		ProbabilityLZHeatmapElement p = new ProbabilityLZHeatmapElement(EDimension.DIMENSION,
				clustering.getMaxAbsProbability(EDimension.DIMENSION));
		this.annotations.add(p);
		this.add(p);
		p = new ProbabilityLZHeatmapElement(EDimension.RECORD, clustering.getMaxAbsProbability(EDimension.RECORD));
		this.annotations.add(p);
		this.add(p);

	}

	public float getProbability(EDimension dim, int index) {
		return clustering.getProbability(dim, bcNr, getVirtualArray(dim).get(index));
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);

		resort();
		updateVisibility();
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
		IGLLayoutElement dimLabel = children.get(6);
		IGLLayoutElement recLabel = children.get(7);

		// shift for probability heat maps
		final int firstAnnotation = 8;
		final int dimAnnotations = count(children.subList(firstAnnotation, children.size()), EDimension.DIMENSION);
		final int recAnnotations = count(children.subList(firstAnnotation, children.size()), EDimension.RECORD);

		final int annotationWidth = isFocused() ? 20 : 6;
		final float shift_x = annotationWidth * recAnnotations;
		final float shift_y = annotationWidth * dimAnnotations;

		if (isHovered || isShowAlwaysToolBar()) { // depending whether we are hovered or not, show hide the toolbar's
			corner.setBounds(-18 - shift_x, -18 - shift_y, 18, 18 * 2);
			if (showThreshold) {
				top.setBounds(0, -shift_y, w < 50 ? 50 : w, 0);
				left.setBounds(-18 - shift_x, 18, 0, left.getSetHeight());
				dimthreshbar.setBounds(Math.max(0, w * 0.5f - 90), -18 - shift_y, 180, 17);
				recthreshbar.setBounds(-18 - shift_x, Math.max(18, h * 0.5f - 90), 17, 180);
			} else {
				top.setBounds(0, -18 - shift_y, w < 50 ? 50 : w, 17);
				left.setBounds(-18 - shift_x, 18, 18, left.getSetHeight());
				dimthreshbar.hide();
				recthreshbar.hide();
			}
		} else {
			// hide by setting the width to 0
			corner.setBounds(-18 - shift_x, -shift_y, 0, 18 * 2);
			top.setBounds(0, -18 - shift_y, w < 50 ? 50 : w, 17);
			left.setBounds(-18 - shift_x, 20, 0, left.getSetHeight());

			dimthreshbar.hide();
			recthreshbar.hide();
		}

		content.setBounds(0, 0, w, h);

		if (isFocused()) {
			dimLabel.setBounds(0, h, w, 80);
			recLabel.setBounds(w, 0, 80, h);
		} else {
			dimLabel.setBounds(0, h, w, 0);
			recLabel.setBounds(w, 0, 0, h);
		}

		int actDimAnnotation = 1;
		int actRecAnnotation = 1;
		for (IGLLayoutElement elem : children.subList(firstAnnotation, children.size())) {
			EDimension dim = elem.getLayoutDataAs(EDimension.class, null);
			if (dim == null)
				continue;
			if (dim.isHorizontal()) {
				elem.setBounds(-1, -annotationWidth * actDimAnnotation, w + 2, annotationWidth);
				actDimAnnotation++;
			} else {
				elem.setBounds(-annotationWidth * actRecAnnotation, -1, annotationWidth, h + 2);
				actRecAnnotation++;
			}
		}

	}

	/**
	 * @param subList
	 * @param dimension
	 * @return
	 */
	private static int count(List<? extends IGLLayoutElement> l, EDimension dimension) {
		int c = 0;
		for (IGLLayoutElement elem : l)
			if (dimension == elem.getLayoutDataAs(EDimension.class, null))
				c++;
		return c;
	}

	@ListenTo
	private void listenTo(SortingChangeEvent e) {
		this.dimSorter = e.getFactory().create(this, EDimension.DIMENSION);
		this.recSorter = e.getFactory().create(this, EDimension.RECORD);
		resort();
	}

	@Override
	protected VirtualArray getDimVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	@Override
	protected VirtualArray getRecVirtualArray() {
		return data.getRecordPerspective().getVirtualArray();
	}

	@Override
	public int getDimSize() {
		return getDimVirtualArray().size();
	}

	@Override
	public int getRecSize() {
		return getRecVirtualArray().size();
	}

	@Override
	public void setFocus(boolean isFocused) {
		super.setFocus(isFocused);
		this.toolBar.setFocus(isFocused);
		if (isFocused) {
			for (ALZHeatmapElement annotation : annotations)
				annotation.nonUniformLayout(content);
		} else {
			for (ALZHeatmapElement annotation : annotations)
				annotation.uniformLayout();
		}
	}

	@Override
	public final boolean shouldBeVisible() {
		final int dim = getDimSize();
		final int rec = getRecSize();
		if (isHidden || dim == 0 || rec == 0)
			return false;
		GLRootElement root = findRootElement();
		if (root == null)
			return true;
		// float clusterSizeThreshold = root.getClusterSizeThreshold();
		// float biggest = root.getBiggestDimSize();
		// if ((dim / biggest) < clusterSizeThreshold && (rec / biggest) < clusterSizeThreshold)
		// return false;
		return true;
	}

	@Override
	public void onEdgeUpdateDone() {
		super.onEdgeUpdateDone();
		if (getVisibility() == EVisibility.PICKABLE
				&& (dimSorter.needsResortAfterBandsUpdate() || recSorter.needsResortAfterBandsUpdate()))
			resort();
	}


	public void setThresholds(float dimThreshold, int dimNumberThreshold, float recThreshold, int recNumberThreshold) {
		this.dimThreshold = dimThreshold;
		this.dimNumberThreshold = dimNumberThreshold;
		this.recThreshold = recThreshold;
		this.recNumberThreshold = recNumberThreshold;

		this.recThreshBar.setValue(recThreshold);
		this.dimThreshBar.setValue(dimThreshold);

		refilter(false);
	}

	/**
	 * @param dimension
	 * @param t
	 */
	public void setThreshold(EDimension dimension, float t) {
		setThresholds(dimension.select(t, dimThreshold), dimNumberThreshold, dimension.select(recThreshold, t),
				recNumberThreshold);
	}

	/**
	 * difference to {@link #setThreshold(EDimension, float)} is that in addition the stuff will be updated
	 *
	 * @param dimension
	 * @param t
	 */
	public void setLocalThreshold(EDimension dimension, float t) {
		setThreshold(dimension, t);
		updateMyEdges(dimension.isHorizontal(), dimension.isVertical());
		relayoutParent();
	}

	@Override
	protected void setLabel(String id) {
		data.setLabel(id);
	}

	private void resort() {
		Pair<List<IntFloat>, List<IntFloat>> p = filterData();

		List<IntFloat> dim = dimSorter.apply(p.getFirst());
		List<IntFloat> rec = recSorter.apply(p.getSecond());

		updateTablePerspective(dim, rec);
		fireTablePerspectiveChanged();
	}

	/**
	 * triggers a refiltering
	 *
	 * @param isGlobal
	 *            whether this is a local change or a global one
	 */
	private void refilter(boolean isGlobal) {
		if (isLocked && isGlobal)
			return;

		Pair<List<IntFloat>, List<IntFloat>> p = filterData();

		updateTablePerspective(p.getFirst(), p.getSecond());
		fireTablePerspectiveChanged();

		updateVisibility();
	}

	/**
	 * filter the data according to the current thresholds
	 *
	 * @return
	 */
	private Pair<List<IntFloat>, List<IntFloat>> filterData() {
		List<IntFloat> dims = dimClustering.filter(dimThreshold, dimNumberThreshold);
		List<IntFloat> recs = recClustering.filter(recThreshold, recNumberThreshold);

		Pair<List<IntFloat>, List<IntFloat>> p = Pair.make(dims, recs);
		return p;
	}

	/**
	 * applies the filtered data to the {@link TablePerspective}s
	 *
	 * @param dims
	 * @param recs
	 */
	private void updateTablePerspective(List<IntFloat> dims, List<IntFloat> recs) {
		fill(getDimVirtualArray(), dims);
		fill(getRecVirtualArray(), recs);

		this.data.invalidateContainerStatistics();

		for (ALZHeatmapElement annotation : annotations)
			if (annotation.getDim().isHorizontal())
				annotation.update(dims);
			else
				annotation.update(recs);
	}

	private static void fill(VirtualArray va, List<IntFloat> values) {
		va.clear();
		va.addAll(Lists.transform(values, IntFloat.TO_INDEX));
	}

	public void addAnnotation(ALZHeatmapElement annotation) {
		this.add(annotation);
		this.annotations.add(annotation);
		if (isFocused()) {
			annotation.nonUniformLayout(content);
		} else {
			annotation.uniformLayout();
		}
		resort();
	}

	/**
	 * @return the annotations, see {@link #annotations}
	 */
	public Collection<ALZHeatmapElement> getAnnotations() {
		return Collections.unmodifiableCollection(annotations);
	}

	public void removeAnnotation(ALZHeatmapElement annotation) {
		if (this.annotations.remove(annotation))
			this.remove(annotation);
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

	public void toggleLocked() {
		isLocked = !isLocked;
	}

	protected class ThresholdBar extends GLElementDecorator implements ThresholdSlider.ISelectionCallback {

		private final boolean isHorizontal;
		private final ThresholdSlider slider;
		// float globalMaxThreshold;
		private float localMaxSliderValue;

		protected ThresholdBar(boolean layout) {
			isHorizontal = layout;
			// move to the top
			setzDelta(DEFAULT_Z_DELTA);

			// create buttons
			float max = 0;
			this.slider = new ThresholdSlider(0, max, max / 2);
			slider.setCallback(this);
			slider.setHorizontal(isHorizontal);
			setContent(slider);
			setVisibility(EVisibility.PICKABLE); // for parent
		}


		@Override
		public void onSelectionChanged(ThresholdSlider slider, float value) {
			if (value >= localMaxSliderValue)
				return;
			setLocalThreshold(EDimension.get(isHorizontal), value);
		}

		/**
		 * @param dimProbabilities
		 */
		public void updateSliders(double max, IDoubleSizedIterable values) {
			localMaxSliderValue = (float) max;
			this.slider.setMinMax(0, localMaxSliderValue);
			this.slider.setStats(values);
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

	protected class ToolBar extends GLElementContainer implements ISelectionCallback {
		private GLButton enlarge, smaller, focus, lock;

		public ToolBar(GLElement switcher) {
			super(GLLayouts.flowVertical(3));
			setzDelta(-0.1f);
			this.add(switcher);
			createButtons();
			setSize(Float.NaN, this.size() * (16 + 3));
			setVisibility(EVisibility.PICKABLE);
		}

		protected void createButtons() {
			focus = new GLButton(GLButton.EButtonMode.CHECKBOX);
			focus.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_FOCUS));
			focus.setSelectedRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_FOCUS_OUT));
			focus.setSize(16, 16);
			focus.setTooltip("Focus this Cluster");
			focus.setCallback(this);
			this.add(focus);
			lock = new GLButton(GLButton.EButtonMode.CHECKBOX);
			lock.setTooltip("Lock this cluster. It will not recieve threshold updates.");
			lock.setSize(16, 16);
			lock.setCallback(this);
			lock.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_LOCK));
			lock.setSelectedRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_UNLOCK));
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

		public void setFocus(boolean focus) {
			this.focus.setCallback(null).setSelected(focus).setCallback(this);
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			if (button == enlarge) {
				zoomIn();
			} else if (button == smaller) {
				zoomOut();
			} else if (button == focus) {
				findAllClustersElement().setFocus(selected ? NormalClusterElement.this : null);
			} else if (button == lock) {
				toggleLocked();
				lock.setTooltip(isLocked ? "UnLock this cluster. It will again recieve threshold updates."
						: "Lock this cluster. It will not recieve threshold updates.");
			}
		}

	}

}
