/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import static org.caleydo.view.bicluster.elem.ZoomLogic.initialOverviewScaleFactor;
import static org.caleydo.view.bicluster.elem.ZoomLogic.nextZoomDelta;
import static org.caleydo.view.bicluster.elem.ZoomLogic.toDirection;
import gleem.linalg.Vec2f;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.AdvancedDoubleStatistics;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.elem.annotation.ALZHeatmapElement;
import org.caleydo.view.bicluster.elem.annotation.CategoricalLZHeatmapElement;
import org.caleydo.view.bicluster.elem.annotation.GoLZHeatmapElement;
import org.caleydo.view.bicluster.elem.band.AllBandsElement;
import org.caleydo.view.bicluster.elem.band.BandElement;
import org.caleydo.view.bicluster.elem.toolbar.AToolBarElement;
import org.caleydo.view.bicluster.elem.toolbar.LayoutToolBarElement;
import org.caleydo.view.bicluster.elem.toolbar.ParameterToolBarElement;
import org.caleydo.view.bicluster.elem.ui.MyUnboundSpinner;
import org.caleydo.view.bicluster.event.AlwaysShowToolBarEvent;
import org.caleydo.view.bicluster.event.ChangeMaxDistanceEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.ShowHideBandsEvent;
import org.caleydo.view.bicluster.event.ShowToolBarEvent;
import org.caleydo.view.bicluster.event.ZoomEvent;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.sorting.CategoricalSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.EThresholdMode;
import org.caleydo.view.bicluster.sorting.IntFloat;
import org.caleydo.view.bicluster.util.SetUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer {
	private static final Logger log = Logger.create(GLRootElement.class);

	private final ParameterToolBarElement toolbarParam = new ParameterToolBarElement();
	private final LayoutToolBarElement layoutParam = new LayoutToolBarElement();
	private final List<AToolBarElement> toolbars = Arrays.asList(toolbarParam, layoutParam);

	private boolean dimBands = MyPreferences.isShowDimBands();
	private boolean recBands = MyPreferences.isShowRecBands();

	private int maxDistance = MyPreferences.getMaxDistance();

	private boolean isShowAlwaysToolBar = false;

	private BiClustering clustering;
	private final AllClustersElement clusters = new AllClustersElement(this);
	private AllBandsElement bands;
	private TablePerspective x;

	/**
	 * a special layer just for watching for zooming events
	 */
	private final GLElement zoomLayer = new GLElement().setVisibility(EVisibility.PICKABLE)
.setPicker(
			GLRenderers.fillRect(Color.CYAN));

	private IIDTypeMapper<Integer, String> dim2label;
	private IIDTypeMapper<Integer, String> rec2label;


	public GLRootElement() {
		setLayout(GLLayouts.LAYERS);
		zoomLayer.onPick(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onZoomLayerPick(pick);
			}
		});
		this.add(zoomLayer);

		this.layoutParam.fill(clusters.getLayout());
	}


	/**
	 * @param pick
	 */
	protected void onZoomLayerPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			zoom((IMouseEvent) pick);
			break;
		default:
			break;
		}
	}

	/**
	 * @param f
	 */
	protected void zoom(IMouseEvent event) {
		final int dim = toDirection(event, EDimension.DIMENSION);
		final int rec = toDirection(event, EDimension.RECORD);
		if (dim == 0 && rec == 0)
			return;
		setZoom(dim, rec);
	}

	/**
	 * @param dim
	 * @param rec
	 */
	private void setZoom(int dim, int rec) {
		final Iterable<ClusterElement> clusters = this.clusters.allClusters();
		final int size = this.clusters.size();
		double[] dimValues = new double[size];
		double[] recValues = new double[size];
		int i = 0;
		for (ClusterElement elem : clusters) {
			if (elem.isFocused())
				continue;
			float d = nextZoomDelta(dim, elem.getZoom(EDimension.DIMENSION), elem.getDimSize());
			float r = nextZoomDelta(rec, elem.getZoom(EDimension.RECORD), elem.getRecSize());
			dimValues[i] = elem.getZoom(EDimension.DIMENSION) + d;
			recValues[i] = elem.getZoom(EDimension.RECORD) + r;
			i++;
		}
		final AdvancedDoubleStatistics dimStats = AdvancedDoubleStatistics.of(dimValues);
		final AdvancedDoubleStatistics recStats = AdvancedDoubleStatistics.of(recValues);
		final float dimNext = (float) dimStats.getMedian();
		final float recNext = (float) recStats.getMedian();

		for (ClusterElement elem : clusters) {
			if (elem.isFocused()) // no global zoom for focussed elements
				continue;
			elem.setZoom(dimNext, recNext);
		}
	}


	@ListenTo
	private void onZoomEvent(ZoomEvent event) {
		final EDimension dim = event.getDim();
		switch (event.getDirection()) {
		case -1:
			setZoom(-1, dim);
			return;
		case 0:
			zoomReset();
			return;
		case 1:
			setZoom(1, dim);
			return;
		}
	}

	/**
	 * @param dim
	 * @param dim
	 */
	private void setZoom(int dir, EDimension dim) {
		setZoom(dim == null || dim.isHorizontal() ? dir : 0, dim == null || dim.isVertical() ? dir : 0);
	}

	private void zoomReset() {
		for (ClusterElement elem : clusters.allClusters())
			elem.zoomReset();
	}

	@Override
	public void layout(int deltaTimeMs) {
		for (AToolBarElement toolbar : toolbars)
			if (toolbar.hasMoved()) {
				clusters.relayout();
				bands.relayout();
				break;
			}
		super.layout(deltaTimeMs);
	}

	/**
	 * @return the isShowAlwaysToolBar, see {@link #isShowAlwaysToolBar}
	 */
	public boolean isShowAlwaysToolBar() {
		return isShowAlwaysToolBar;
	}

	@ListenTo
	private void onAlwaysShowToolBarEvent(AlwaysShowToolBarEvent event) {
		this.isShowAlwaysToolBar = !isShowAlwaysToolBar;
		for (GLElement elem : clusters)
			GLElementAccessor.relayoutDown(elem);
	}

	/**
	 * @return the toolbars, see {@link #toolbars}
	 */
	public List<AToolBarElement> getToolbars() {
		return toolbars;
	}

	public boolean isRecBandsEnabled() {
		return recBands;
	}

	public boolean isDimBandsEnabled() {
		return dimBands;
	}

	public boolean isBandsEnabled(EDimension dim) {
		return dim.select(dimBands, recBands);
	}

	/**
	 * @return the maxDistance, see {@link #maxDistance}
	 */
	public int getMaxDistance() {
		return maxDistance;
	}

	@ListenTo
	private void onChangeMaxDistanceEvent(ChangeMaxDistanceEvent event) {
		this.maxDistance = event.getMaxDistance();
		this.clusters.onChangeMaxDistance();
		this.clusters.relayout();
		this.bands.relayout();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		// show the global toolbar as a popup
		toolbarParam.toggle(context);
	}

	@ListenTo
	private void onShowToolBar(ShowToolBarEvent event) {
		if (context == null)
			return;
		boolean parameters = event.isShowParameter();
		if (parameters)
			toolbars.get(0).toggle(context);
		else
			toolbars.get(1).toggle(context);
		clusters.relayout();
	}

	/**
	 * @param biClustering
	 * @param size
	 * @param maxDimThreshold
	 * @param maxRecThreshold
	 */
	public void init(BiClustering biClustering, TablePerspective x, Vec2f size, double maxDimThreshold,
			double maxRecThreshold) {
		this.x = x;
		this.clustering = biClustering;

		this.dim2label = x2Label(this.x.getDimensionPerspective().getIdType());
		this.rec2label = x2Label(this.x.getRecordPerspective().getIdType());

		for (AToolBarElement toolbar : getToolbars())
			toolbar.init(biClustering);

		bands = new AllBandsElement(x);
		this.clear();
		this.add(zoomLayer);
		this.add(bands);
		this.add(clusters);

		final int count = biClustering.getBiClusterCount();
		log.info(count + " bi clusters loaded.");
		List<Dimension> dimensions = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			final ClusterElement el = new NormalClusterElement(i, clustering.getData(i), clustering, maxDimThreshold,
					maxRecThreshold);
			clusters.add(el);
			dimensions.add(el.getSizes());
		}

		final Map<EDimension, Float> scales = initialOverviewScaleFactor(dimensions, size.x(), size.y());
		final float dimScale = scales.get(EDimension.DIMENSION);
		final float recScale = scales.get(EDimension.RECORD);

		log.info("creating bands");
		final List<GLElement> l = clusters.asList();
		for (int i = 0; i < l.size(); ++i) {
			ClusterElement start = (ClusterElement) l.get(i);
			start.setZoom(dimScale, recScale);

			for (int j = i + 1; j < l.size(); ++j) {
				ClusterElement end = (ClusterElement) l.get(j);
				Edge edge = new Edge(start, end);
				start.addEdge(end, edge);
				end.addEdge(start, edge);
				bands.add(new BandElement(edge, EDimension.RECORD, bands.getRecordSelectionManager(), rec2label));
				bands.add(new BandElement(edge, EDimension.DIMENSION, bands.getDimensionSelectionManager(), dim2label));
			}
		}
		bands.updateSelection();
		bands.updateStructure();
	}

	public void initializeScaleFactor(Vec2f size) {
		if (clusters.isAnyFocussed())
			return;
		List<Dimension> dimensions = new ArrayList<>();
		final List<GLElement> l = clusters.asList();
		for (int i = 0; i < l.size(); ++i) {
			ClusterElement elem = (ClusterElement) l.get(i);
			dimensions.add(elem.getSizes());
		}

		final Map<EDimension, Float> scales = initialOverviewScaleFactor(dimensions, size.x(), size.y());
		final float dimScale = scales.get(EDimension.DIMENSION);
		final float recScale = scales.get(EDimension.RECORD);

		for (int i = 0; i < l.size(); ++i) {
			ClusterElement start = (ClusterElement) l.get(i);
			start.setZoom(dimScale, recScale);
		}
		bands.updateStructure();
	}

	/**
	 * @param idType
	 * @return
	 */
	private static IIDTypeMapper<Integer, String> x2Label(IDType idType) {
		return IDMappingManagerRegistry.get().getIDMappingManager(idType)
				.getIDTypeMapper(idType, idType.getIDCategory().getHumanReadableIDType());
	}

	/**
	 *
	 */
	public void reset() {
		this.x = null;
		this.clustering = null;

		this.clear();
		clusters.clear();
		this.bands = null;
	}

	@ListenTo
	private void listenTo(LZThresholdChangeEvent event) {
		final EDimension dim = event.getDim();
		final int numberThreshold = event.getNumberThreshold();
		final float threshold = event.getThreshold();
		final EThresholdMode mode = event.getMode();

		// 1. update thresholds
		Iterable<NormalClusterElement> allNormalClusters = allNormalClusters();
		DoubleStatistics.Builder b = DoubleStatistics.builder();
		final Vec2f total = getSize();
		for (NormalClusterElement cluster : allNormalClusters) {
			Dimension old = cluster.getSizes();
			cluster.setThreshold(dim, threshold, numberThreshold, mode);
			Dimension new_ = cluster.getSizes();
			float s = ZoomLogic.adaptScaleFactorToSize(dim, old, new_, cluster.getZoom(EDimension.DIMENSION),
					cluster.getZoom(EDimension.RECORD), total.x(), total.y());
			// System.out.println(s);
			b.add(s);
		}
		float s = (float) b.build().getMin();
		if (s != 1.0f) {
			for (ClusterElement elem : clusters.allClusters()) {
				elem.scaleZoom(dim.select(s, 1), dim.select(1, s));
			}
		}

		// 2. update overlaps
		updateAllEdges();
		clusters.relayout();
	}

	/**
	 * @return
	 */
	private Dimension getSizes() {
		return new Dimension(x.getNrDimensions(), x.getNrRecords());
	}

	private Iterable<NormalClusterElement> allNormalClusters() {
		return Iterables.filter(clusters, NormalClusterElement.class);
	}

	@ListenTo
	private void onShowHideBandsEvent(ShowHideBandsEvent event) {
		this.dimBands = event.isShowDimBand();
		this.recBands = event.isShowRecBand();
		this.bands.relayout();
		this.clusters.onChangeMaxDistance();
	}

	/**
	 *
	 * @param isDimensionThresholds
	 * @param thresholds
	 *            bicluster id x threshold
	 */
	public void setThresholds(EDimension dimension, Map<Integer, Float> thresholds) {
		float thresh = Float.NEGATIVE_INFINITY;
		for (NormalClusterElement elem : allNormalClusters()) {
			int number = elem.getBiClusterNumber();
			if (thresholds.containsKey(number)) {
				float t = thresholds.get(number);
				if (Float.isInfinite(thresh))
					thresh = t;
				if (t != thresh)
					thresh = Float.NaN;
				elem.setThreshold(dimension, t, MyUnboundSpinner.UNBOUND, EThresholdMode.ABS);
			}
		}
		if (!Float.isNaN(thresh) && !Float.isInfinite(thresh)) { // all the same set that in the parameter toolbar
			this.toolbarParam.setThreshold(dimension, thresh);
		}
		updateAllEdges();
	}

	private void updateAllEdges() {
		// update all edges
		for (ClusterElement elem : clusters.allClusters()) {
			elem.updateOutgoingEdges(true, true);
		}
		for (ClusterElement elem : clusters.allClusters()) {
			elem.onEdgeUpdateDone();
		}
		bands.relayout();
	}

	/**
	 * @param shared
	 * @param selection
	 * @param mouseOver
	 * @return
	 */
	public boolean isAnyRecSelected(Collection<Integer> ids, SelectionType... toCheck) {
		return isAnySelected(ids, bands.getRecordSelectionManager(), toCheck);
	}

	/**
	 * checks whether any of the given ids is an active element within the given SelectionTypes
	 *
	 * @param ids
	 * @param manager
	 * @param toCheck
	 * @return
	 */
	private boolean isAnySelected(Collection<Integer> ids, SelectionManager manager, SelectionType[] toCheck) {
		if (toCheck.length == 0)
			return false;
		for (SelectionType type : toCheck) {
			if (SetUtils.containsAny(manager.getElements(type), ids))
				return true;
		}
		return false;
	}

	public boolean isAnyDimSelected(Collection<Integer> ids, SelectionType... toCheck) {
		return isAnySelected(ids, bands.getRecordSelectionManager(), toCheck);
	}

	/**
	 *
	 */
	public void focusPrevious() {
		if (clusters != null)
			clusters.focusPrevious();
	}

	public void focusNext() {
		if (clusters != null)
			clusters.focusNext();
	}

	/**
	 * @param record
	 * @param t
	 */
	public void addCategoricalAnnotation(EDimension dimension, TablePerspective t) {
		final Integer oppositeID = dimension.select(t.getRecordPerspective(), t.getDimensionPerspective())
				.getVirtualArray().get(0);
		final Table table = t.getDataDomain().getTable();
		final IDType target = dimension.select(t.getDataDomain().getDimensionIDType(), t.getDataDomain()
				.getRecordIDType());
		ATableBasedDataDomain source = clustering.getXDataDomain();
		if (source.getDimensionIDCategory().isOfCategory(target)) {
			final IIDTypeMapper<Integer, Integer> mapper = source.getDimensionIDMappingManager().getIDTypeMapper(
					source.getDimensionIDType(), target);
			final CategoricalSortingStrategyFactory factory = new CategoricalSortingStrategyFactory(
					EDimension.DIMENSION, oppositeID, table, mapper);
			toolbarParam.addSortingMode(factory, EDimension.DIMENSION);
			for (NormalClusterElement cluster : allNormalClusters())
				cluster.addAnnotation(new CategoricalLZHeatmapElement(EDimension.DIMENSION, factory));
		} else if (source.getRecordIDCategory().isOfCategory(target)) {
			final IIDTypeMapper<Integer, Integer> mapper = source.getRecordIDMappingManager().getIDTypeMapper(
					source.getRecordIDType(), target);

			final CategoricalSortingStrategyFactory factory = new CategoricalSortingStrategyFactory(EDimension.RECORD,
					oppositeID, table, mapper);
			toolbarParam.addSortingMode(factory, EDimension.RECORD);
			for (NormalClusterElement cluster : allNormalClusters())
				cluster.addAnnotation(new CategoricalLZHeatmapElement(EDimension.RECORD, factory));
		}
	}

	public void addMultiAnnotation(EDimension dim, TablePerspective t) {
		final IDType annotateTo = clustering.getIDType(dim);
		final IDMappingManagerRegistry registry = IDMappingManagerRegistry.get();
		// record of annotation to gene/samples

		final ATableBasedDataDomain d = t.getDataDomain();
		final Table table = d.getTable();

		final IDType go = t.getRecordPerspective().getIdType();
		final IDMappingManager m = registry.getIDMappingManager(annotateTo);
		IIDTypeMapper<Integer, Integer> rec2annotateTo = m.getIDTypeMapper(go,
				annotateTo);

		IDType goLabel = rec2annotateTo.getPath().get(0).getToIDType();
		IIDTypeMapper<Integer, String> rec2label = m.getIDTypeMapper(go, goLabel);

		for (NormalClusterElement cluster : allNormalClusters()) {
			Integer col = cluster.getBiClusterNumber();
			List<Integer> records = findBestColumns(col, t);
			if (records.isEmpty())
				continue;
			for (Integer record : records) {
				float pValue = ((Number) table.getRaw(col, record)).floatValue();
				Set<Integer> containedGenes = containedGenes(record, rec2annotateTo);
				if (containedGenes == null || containedGenes.isEmpty())
					continue;
				final Set<String> labels = rec2label.apply(record);
				String label = labels == null ? record.toString() : (labels.size() == 1 ? labels.iterator().next()
						.toString() : StringUtils.join(labels, ", "));
				cluster.addAnnotation(new GoLZHeatmapElement(dim, label, pValue, Predicates.in(containedGenes), d));
			}
		}
	}


	private Set<Integer> containedGenes(Integer record, IIDTypeMapper<Integer, Integer> rec2annotateTo) {
		return rec2annotateTo.apply(record);
	}

	public void removeMultiAnnotation(EDimension dimension, TablePerspective t) {
		ATableBasedDataDomain d = t.getDataDomain();

		for (NormalClusterElement cluster : allNormalClusters()) {
			for (ALZHeatmapElement a : ImmutableList.copyOf(cluster.getAnnotations())) {
				if (a instanceof GoLZHeatmapElement && ((GoLZHeatmapElement) a).getOrigin() == d)
					cluster.removeAnnotation(a);
			}
		}
	}


	private static List<Integer> findBestColumns(Integer col, TablePerspective t) {
		final VirtualArray va = t.getRecordPerspective().getVirtualArray();
		List<IntFloat> tmp = new ArrayList<>(va.size());
		final int max = MyPreferences.getMaxNumberofGOs();
		final float pMax = MyPreferences.getMaximalGOPValue();
		for (Integer rec : va) {
			final Table table = t.getDataDomain().getTable();
			float p = ((Number) table.getRaw(col, rec)).floatValue();
			if (p > pMax)
				continue;
			tmp.add(new IntFloat(rec, p));
		}
		Collections.sort(tmp, IntFloat.BY_MEMBERSHIP);
		List<Integer> top = Lists.transform(tmp, IntFloat.TO_INDEX);
		if (max < tmp.size())
			return top.subList(0, max);
		return top;
	}


	/**
	 * @param record
	 * @param t
	 */
	public void removeAnnotation(EDimension dimension, TablePerspective t) {
		final Integer oppositeID = dimension.select(t.getRecordPerspective(), t.getDimensionPerspective())
				.getVirtualArray().get(0);
		final IDType target = dimension.select(t.getDataDomain().getDimensionIDType(), t.getDataDomain()
				.getRecordIDType());
		ATableBasedDataDomain source = clustering.getXDataDomain();
		if (source.getDimensionIDCategory().isOfCategory(target)) {
			for (NormalClusterElement cluster : allNormalClusters()) {
				for (CategoricalLZHeatmapElement h : Iterables.filter(cluster.getAnnotations(),
						CategoricalLZHeatmapElement.class)) {
					if (h.is(oppositeID, target)) {
						cluster.removeAnnotation(h);
						toolbarParam.removeSortingMode(h.getData(), h.getDim());
						break;
					}
				}
			}
		} else if (source.getRecordIDCategory().isOfCategory(target)) {
			for (NormalClusterElement cluster : allNormalClusters()) {
				for (CategoricalLZHeatmapElement h : Iterables.filter(cluster.getAnnotations(),
						CategoricalLZHeatmapElement.class)) {
					if (h.is(oppositeID, target)) {
						cluster.removeAnnotation(h);
						toolbarParam.removeSortingMode(h.getData(), h.getDim());
						break;
					}
				}
			}
		}
	}
}
