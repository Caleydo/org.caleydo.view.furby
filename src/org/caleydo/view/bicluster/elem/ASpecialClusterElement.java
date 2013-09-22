/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

public abstract class ASpecialClusterElement extends ClusterElement {

	private String clusterName;

	public ASpecialClusterElement(TablePerspective data,
 BiClustering clustering) {
		super(-1, data, clustering);
		minScaleFactor = 3;
		setScaleFactor(3);

		this.add(createHideClusterButton());
	}

	@Override
	public final void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		IGLLayoutElement headerbar = children.get(0);
		IGLLayoutElement close = children.get(1);
		if (isHovered || isShowAlwaysToolBar()) {
			close.setBounds(-18, 0, 18, 20);
			headerbar.setBounds(0, -19, w < 55 ? 57 : w + 2, 20);
		} else {
			close.setBounds(0, 0, 0, 0); // hide by setting the width to 0
			headerbar.setBounds(0, -18, w < 50 ? 50 : w, 17);
		}
		children.get(2).setBounds(0, 0, w, h);
	}

	@Override
	protected final void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		float[] color = { 0, 0, 0, actOpacityFactor };
		Color highlightedColor = SelectionType.MOUSE_OVER.getColor();
		g.color(color);
		if (isHovered) {
			g.color(highlightedColor);
		}
		g.drawRect(-1, -1, w + 2, h + 3);

	}


	@Override
	protected final void setLabel(String id) {
		this.clusterName = id;
	}

	@Override
	public final String getID() {
		return clusterName;
	}
	@Override
	public final String getLabel() {
		return clusterName;
	}

	@Override
	protected final void upscale() {
		scaleFactor += 1.2;
	}


	private GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_CLOSE));
		hide.setTooltip("Unload cluster");
		hide.setSize(16, Float.NaN);
		hide.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				remove();
			}

		});
		return hide;
	}

	void remove() {
		EventPublisher.trigger(new SpecialClusterRemoveEvent(this, false));
		this.isHidden = true;
		updateVisibility();
		findParent(AllClustersElement.class).remove(this);
		this.mouseOut();
	}
}
