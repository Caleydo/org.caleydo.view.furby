package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.ClusterElement.HeaderBar;
import org.caleydo.view.bicluster.elem.ClusterElement.ThresholdBar;
import org.caleydo.view.bicluster.elem.ClusterElement.ToolBar;
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
		toolBar = new ToolBar();
		headerBar = new HeaderBar();
		this.add(toolBar); // add a element toolbar
		this.add(headerBar);
		content = new SpecialClusterContent();
		content.setzDelta(0.5f);
		this.add(content);
	}

	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		// if (isHidden) return;
		IGLLayoutElement toolbar = children.get(0);
		IGLLayoutElement headerbar = children.get(1);
		if (isHovered) { // depending whether we are hovered or not, show hide
							// the toolbar's
			toolbar.setBounds(-18, 0, 18, 70);
			headerbar.setBounds(0, -19, w < 55 ? 57 : w + 2, 20);
		} else {
			toolbar.setBounds(0, 0, 0, 0); // hide by setting the width to 0
			headerbar.setBounds(0, -18, w < 50 ? 50 : w, 17);
		}
		IGLLayoutElement igllContent = children.get(2);
		if (isFocused) {
			igllContent.setBounds(0, 0, w + 79, h + 79);
		} else {
			igllContent.setBounds(0, 0, w, h);
		}
	}
	
	
	@Override
	public void setData(List<Integer> dimIndices, List<Integer> recIndices,
			boolean setXElements, String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		setLabel(id);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		this.setOnlyShowXElements = setXElements;
		setHasContent(dimIndices, recIndices);
		setVisibility();
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
		((SpecialClusterContent)content).update();
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

		List<String> recordNames;
		int width = 10;
		
		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			int last = recordNames.size();
			int i = last;
			for (String s: recordNames) {
				g.drawText(s, 1, (last-i)*width, w, width);
				i--;
			}
			super.renderImpl(g, w, h);
		}

		void update() {
			recordNames = new ArrayList<String>();
			for (Integer i: elements) {
				recordNames.add(x.getDataDomain().getRecordLabel(i));
			}
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
