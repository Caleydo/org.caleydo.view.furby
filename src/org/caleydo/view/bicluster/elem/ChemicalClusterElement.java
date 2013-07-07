package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;
import org.caleydo.view.bicluster.util.ClusterRenameEvent;

public final class ChemicalClusterElement extends ClusterElement {

	private static float TEXT_SIZE = 10f;
	private VirtualArray elements;

	private String clusterName;
	private List<String> clusterList;
	private Map<Integer, String> elementToClusterMap;

	public ChemicalClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor,
			List<String> clusterList, Map<Integer, String> elementToClusterMap,
			GLRootElement biclusterRoot) {
		super(data, root, x, l, z, executor, biclusterRoot);
		this.clusterList = clusterList;
		this.elementToClusterMap = elementToClusterMap;
		List<Integer> elements = new ArrayList<>(elementToClusterMap.size());
		for (Integer i : elementToClusterMap.keySet()) {
			elements.add(i);
		}
		setHasContent(elements, null);
		content.setzDelta(0.5f);
		toolBar.remove(3);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		standardScaleFactor = 3;
		resetScaleFactor();
	}

	private ChemicalClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor, List<String> list,
			GLRootElement biclusterRoot) {
		super(data, root, x, l, z, executor, biclusterRoot);

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

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		// if (isHidden) return;
		IGLLayoutElement toolbar = children.get(0);
		IGLLayoutElement headerbar = children.get(1);
		if (isHovered) { // depending whether we are hovered or not, show hide
							// the toolbar's
			toolbar.setBounds(-18, 0, 18, 20);
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
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		float[] color = { 0, 0, 0, curOpacityFactor };
		Color highlightedColor = SelectionType.MOUSE_OVER.getColor();
		g.color(color);
		if (isHovered) {
			g.color(highlightedColor);
		}
		g.drawRect(-1, -1, w + 2, h + 3);

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
		y = 70f / scaleFactor;
		x = TEXT_SIZE * elements.size() / scaleFactor / 2;
		super.setClusterSize(x, y, maxClusterSize);
	}

	@Override
	protected void setHasContent(List<Integer> dimIndices,
			List<Integer> recIndices) {
		if (dimIndices.size() > 0) {
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
		return clusterName == null ? "Chemical clusters" : clusterName;
	}

	@ListenTo
	private void listenTo(ClusterRenameEvent e) {
		if (e.getSender() == this) {
			clusterName = e.getNewName();
		}
	}

	@Override
	protected void setLabel(String id) {
		// nothing to do here
	}

	@Override
	protected void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		this.elements = new VirtualArray(x.getDataDomain()
				.getDimensionGroupIDType(), dimIndices);
		((SpecialClusterContent) content).update();
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return elements;
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return new VirtualArray(x.getDataDomain().getRecordIDType());
	}

	@Override
	public int getNumberOfDimElements() {
		return elements.size();
	}

	@Override
	public int getNumberOfRecElements() {
		return 0;
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
	protected void upscale() {
		scaleFactor += 1.2;
	}

	private class SpecialClusterContent extends AnimatedGLElementContainer {

		List<String> chemicalClusterNames;

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			int i = chemicalClusterNames.size();
			float[] color = { 0, 0, 0, curOpacityFactor };
			g.textColor(color);
			g.save().gl.glRotatef(90, 0, 0, 1);

			float size = (i+1) * TEXT_SIZE;
			for (String s : chemicalClusterNames) {
				g.drawText(s, 1, i * TEXT_SIZE - 2 - size, h, TEXT_SIZE);
				i--;
			}
			g.restore();
			g.textColor(Color.BLACK);
			super.renderImpl(g, w, h);
		}

		void update() {
			chemicalClusterNames = clusterList;
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

	@Override
	public float getDimPosOf(int id) {
//		return clusterList.indexOf(elementToClusterMap.get(index)) * getSize().x()*(float)scaleFactor
//				/ getDimensionVirtualArray().size();
		return getDimIndexOf(id)*TEXT_SIZE;
	}
	
	@Override
	public List<List<Integer>> getListOfContinousDimSequences(
			List<Integer> overlap) {
		List<List<Integer>> sequences = new ArrayList<List<Integer>>();
		sequences.add(overlap);
		return sequences;
	}

	
	@Override
	public float getDimensionElementSize() {
		return getSize().x() / clusterList.size();
	}
	
	@Override
	public int getDimIndexOf(int value) {
		return clusterList.indexOf(elementToClusterMap.get(value));
	}
	
	@Override
	public int getNrOfElements(List<Integer> band){
		Set<String> usedElements = new HashSet<>();
		for (Integer i: band) {
			usedElements.add(elementToClusterMap.get(i));
		}
		return usedElements.size();
	}

	@Override
	protected GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers
				.fillImage("resources/icons/dialog_close.png"));
		hide.setTooltip("Unload cluster");
		hide.setSize(16, Float.NaN);
		hide.setCallback(new ISelectionCallback() {

			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				EventPublisher.trigger(new SpecialClusterRemoveEvent(cluster,
						false));
				cluster.isHidden = true;
				setVisibility();
				allClusters.remove(cluster);
				cluster.mouseOut();
			}

		});
		return hide;
	}

}