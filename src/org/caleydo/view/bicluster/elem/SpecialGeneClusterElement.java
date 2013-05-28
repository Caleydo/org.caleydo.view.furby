package org.caleydo.view.bicluster.elem;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

public final class SpecialGeneClusterElement extends ClusterElement {

	private VirtualArray elements;

	public SpecialGeneClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor, List<Integer> elements) {
		super(data, root, x, l, z, executor);
		setHasContent(null, elements);
		content.setzDelta(0.5f);
		toolBar.remove(3);
		toolBar.remove(1);
	}

	private SpecialGeneClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor) {
		super(data, root, x, l, z, executor);

	}

	@Override
	protected void initContent() {
		content = new SpecialClusterContent();
		content.setzDelta(0.5f);
		this.add(content);
	}

	@Override
	public void setClusterSize(double x, double y, double maxClusterSize) {
		x = 30;
		super.setClusterSize(x, y, maxClusterSize);
	}

	@Override
	protected void setHasContent(List<Integer> dimIndices,
			List<Integer> recIndices) {
		if (recIndices.size() > 0) {
			hasContent = true;
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			hasContent = false;
		}
	}

	@Override
	public void setVisibility() {
		if (isHidden || !hasContent)
			setVisibility(EVisibility.NONE);
		else
			setVisibility(EVisibility.PICKABLE);

	}

	@Override
	public String getID() {
		return "Special Genes";
	}

	@Override
	protected void setLabel(String id) {
		// nothing to do here
	}

	@Override
	protected void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		this.elements = new VirtualArray(x.getDataDomain()
				.getRecordGroupIDType(), recIndices);
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return new VirtualArray(x.getDataDomain().getDimensionIDType());
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return elements;
	}

	@Override
	public int getNumberOfDimElements() {
		return 0;
	}

	@Override
	public int getNumberOfRecElements() {
		return elements.size();
	}

	@Override
	protected void focusThisCluster() {
		// TODO
		this.isFocused = !this.isFocused;
		HeatMapElement hm = (HeatMapElement) content;
		if (isFocused) {
			scaleFactor = scaleFactor >= 4 ? 4 : 3;
			hm.setDimensionLabels(EShowLabels.RIGHT);
			hm.setRecordLabels(EShowLabels.RIGHT);
			hm.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
			hm.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
			resize();
			EventPublisher.trigger(new FocusChangeEvent(this));
		} else {
			scaleFactor = 1;
			hm.setDimensionLabels(EShowLabels.NONE);
			hm.setRecordLabels(EShowLabels.NONE);
			hm.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
			hm.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			resize();
			EventPublisher.trigger(new FocusChangeEvent(null));
			mouseOut();
		}
		repaintAll();
	}

	@Override
	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		setData(elements.getIDs(), elements.getIDs(), setOnlyShowXElements,
				getID(), bcNr, -1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal,
				dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}

	@Override
	protected void resetScaleFactor() {
		scaleFactor = 3;
	}

	@Override
	protected void upscale() {
		scaleFactor += 1.2;
	}

	private class SpecialClusterContent extends AnimatedGLElementContainer {

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.fillRect(0, 0, w, h);
			super.renderImpl(g, w, h);
		}

		void update() {

		}

		@Override
		public String toString() {
			return "special clusterContent";
		}
	}

	@Override
	protected void sort(SortingType type) {
		// Nothing to do here
	}

}
