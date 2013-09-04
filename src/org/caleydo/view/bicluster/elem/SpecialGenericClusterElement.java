/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

import com.google.common.base.Predicates;

/**
 * a special cluster element based on an external table perspective e.g. a chemical cluster
 *
 * @author Samuel Gratzl
 *
 */
public final class SpecialGenericClusterElement extends AMultiClusterElement {
	private final VirtualArray recordVA;
	private final VirtualArray dimVA;

	public SpecialGenericClusterElement(TablePerspective data, BiClustering clustering) {
		super(-1, data, clustering, Predicates
				.not(Predicates.in(Arrays.asList("distribution.pie", "distribution.hist"))));
		content.setzDelta(0.5f);
		setLabel(data.getDataDomain().getLabel() + " " + data.getLabel());

		this.recordVA = createVA(clustering.getXDataDomain().getRecordIDType(), data);
		this.dimVA = createVA(clustering.getXDataDomain().getDimensionIDType(), data);
		updateVisibility();
		this.add(createHideClusterButton());
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
		final ATableBasedDataDomain x = clustering.getXDataDomain();
		if (r.resolvesTo(idType)) {
			Perspective convertForeignPerspective = x.convertForeignPerspective(
					data.getRecordPerspective());
			return convertForeignPerspective.getVirtualArray();
		} else if (c.resolvesTo(idType)) {
			Perspective convertForeignPerspective = x.convertForeignPerspective(
					data.getDimensionPerspective());
			return convertForeignPerspective.getVirtualArray();
		} else
			return new VirtualArray(); // otherwise return a dummy
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		// if (isHidden) return;
		IGLLayoutElement headerbar = children.get(0);
		IGLLayoutElement igllContent = children.get(1);
		IGLLayoutElement close = children.get(2);
		if (isHovered || isShowAlwaysToolBar()) { // depending whether we are hovered or not, show hide
							// the toolbar's
			close.setBounds(-18, 0, 18, 18);
			headerbar.setBounds(0, -19, w < 55 ? 57 : w + 2, 20);
		} else {
			close.setBounds(0, 0, 0, 0); // hide by setting the width to 0
			headerbar.setBounds(0, -18, w < 50 ? 50 : w, 17);
		}
		if (isFocused()) {
			igllContent.setBounds(0, 0, w + 79, h + 79);
		} else {
			igllContent.setBounds(0, 0, w, h);
		}
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
	public boolean shouldBeVisible() {
		if (isHidden || (getRecordOverlapSize() == 0 || recordVA.size() == 0 || !anyShown(recOverlap))
				&& (getDimensionOverlapSize() == 0 || dimVA.size() == 0 || !anyShown(dimOverlap))) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param recOverlap
	 * @return
	 */
	private static boolean anyShown(Map<GLElement, List<Integer>> elems) {
		for (Map.Entry<GLElement, List<Integer>> entry : elems.entrySet())
			if (entry.getKey().getVisibility().doRender() && !entry.getValue().isEmpty())
				return true;
		return false;
	}

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
		updateVisibility();
		findParent(AllClustersElement.class).remove(this);
		this.mouseOut();
	}

	@Override
	protected void setLabel(String id) {
		data.setLabel(id);
	}
}
