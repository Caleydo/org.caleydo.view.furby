/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

public final class SpecialRecordClusterElement extends ClusterElement {

	private VirtualArray elements;
	private float width = 10f;

	private String clusterName;

	public SpecialRecordClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor, List<Integer> elements, GLRootElement biclusterRoot) {
		super(data, root, x, l, z, executor, biclusterRoot);
		setHasContent(null, elements);
		content.setzDelta(0.5f);
		toolBar.remove(3);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		minScaleFactor = 3;
		setScaleFactor(3);
	}

	private SpecialRecordClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor, GLRootElement biclusterRoot) {
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
 String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		setLabel(id);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		setHasContent(dimIndices, recIndices);
		setVisibility();
	}

	@Override
	public void setClusterSize(double x, double y, double maxClusterSize) {
		x = 100f/scaleFactor;
		y = width*elements.size()/scaleFactor;
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
		return clusterName == null ? "Special " + x.getDataDomain().getRecordIDCategory().getDenominationPlural().toString() : clusterName;
	}

	@Override
	protected void setLabel(String id) {
		this.clusterName = id;
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
	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		setData(elements.getIDs(), elements.getIDs(), getID(), bcNr, -1, -1, -1, -1);
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

		List<String> recordNames;


		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			int i = 0;
			float[] color = { 0, 0, 0, curOpacityFactor };
			g.textColor(color);
			for (String s: recordNames) {
				g.drawText(s, 1, i*width-2, w, width);
				i++;
			}
			g.textColor(Color.BLACK);
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

	@Override
	protected GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers
.fillImage(BiClusterRenderStyle.ICON_CLOSE));
		hide.setTooltip("Unload cluster");
		hide.setSize(16, Float.NaN);
		hide.setCallback(new ISelectionCallback(){

			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				remove();
			}

		});
		return hide;
	}

	public void remove() {
		EventPublisher.trigger(new SpecialClusterRemoveEvent(this, false));
		this.isHidden = true;
		setVisibility();
		allClusters.remove(this);
		this.mouseOut();
	}

}
