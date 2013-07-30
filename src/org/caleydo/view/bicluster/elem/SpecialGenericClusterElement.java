/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

/**
 * a special cluster element based on an external table perspective e.g. a chemical cluster
 *
 * @author Samuel Gratzl
 *
 */
public final class SpecialGenericClusterElement extends ClusterElement {
	private final VirtualArray recordVA;
	private final VirtualArray dimVA;

	public SpecialGenericClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
 TablePerspective z, ExecutorService executor, GLRootElement biclusterRoot) {
		super(data, root, x, l, z, executor, biclusterRoot);
		content.setzDelta(0.5f);
		toolBar.remove(3);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		toolBar.remove(1);
		standardScaleFactor = 3;
		resetScaleFactor();
		setLabel(data.getDataDomain().getLabel() + " " + data.getLabel());

		this.recordVA = createVA(x.getRecordPerspective().getIdType(), data);
		this.dimVA = createVA(x.getDimensionPerspective().getIdType(), data);
		setHasContent(dimVA.getIDs(), recordVA.getIDs());
	}

	/**
	 * @param idType
	 * @param data
	 * @return
	 */
	private VirtualArray createVA(IDType idType, TablePerspective data) {
		// check if dimension can be converted
		IDType r = data.getRecordPerspective().getIdType();
		IDType c = data.getDimensionPerspective().getIdType();
		if (r.resolvesTo(idType)) {
			Perspective convertForeignPerspective = x.getDataDomain().convertForeignPerspective(
					data.getRecordPerspective());
			return convertForeignPerspective.getVirtualArray();
		} else if (c.resolvesTo(idType)) {
			Perspective convertForeignPerspective = x.getDataDomain().convertForeignPerspective(
					data.getDimensionPerspective());
			return convertForeignPerspective.getVirtualArray();
		} else
			return new VirtualArray(); // otherwise return a dummy
	}

	@Override
	protected void initContent() {
		toolBar = new ToolBar();
		headerBar = new HeaderBar();
		this.add(toolBar); // add a element toolbar
		this.add(headerBar);
		content = createContent();
		setZValuesAccordingToState();
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
	protected VirtualArray getDimensionVirtualArray() {
		return dimVA;
	}

	@Override
	public int getNumberOfDimElements() {
		return Math.max(1, dimVA.size());
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return recordVA;
	}

	@Override
	public int getNumberOfRecElements() {
		return Math.max(1, recordVA.size());
	}

	@Override
	public void setData(List<Integer> dimIndices, List<Integer> recIndices,
			boolean setXElements, String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		setVisibility();
	}

	@Override
	protected void setHasContent(List<Integer> dimIndices,
			List<Integer> recIndices) {
		hasContent = dimIndices.size() > 0 || recIndices.size() > 0;
		setVisibility();
	}

	@Override
	public void setVisibility() {
		if (isHidden || !hasContent || getRecordOverlapSize() == 0 && getDimensionOverlapSize() == 0)
			setVisibility(EVisibility.NONE);
		else
			setVisibility(EVisibility.PICKABLE);

	}

	@Override
	protected void sort(SortingType type) {
		// Nothing to do here
	}

	@Override
	protected void rebuildMyData(boolean isGlobal) {
		//
		setVisibility();
	}

	@Override
	protected GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_CLOSE));
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
