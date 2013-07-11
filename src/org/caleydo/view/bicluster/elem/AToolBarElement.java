/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec4f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IPopupLayer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;

/**
 *
 * @author Samuel Gratzl
 *
 */
public abstract class AToolBarElement extends GLElementContainer implements GLButton.ISelectionCallback,
		GLSlider.ISelectionCallback {

	protected static final float LABEL_WIDTH = 14;
	protected static final float SLIDER_WIDH = 16;
	protected static final float BUTTON_WIDTH = 16;

	public AToolBarElement() {
		setLayout(new GLFlowLayout(false, 5, new GLPadding(10)));
		setRenderer(GLRenderers.fillRect(new Color(0.8f, 0.8f, 0.8f, .8f)));
	}

	public final boolean isVisible() {
		return getParent() != null;
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {

	}
	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {

	}

	public void toggle(IGLElementContext context) {
		if (context == null)
			return;
		boolean visible = isVisible();
		if (visible) {
			context.getPopupLayer().hide(this);
		} else
			context.getPopupLayer().show(
					this,
					getPreferredBounds(),
					IPopupLayer.FLAG_BORDER | IPopupLayer.FLAG_MOVEABLE | IPopupLayer.FLAG_COLLAPSABLE
							| IPopupLayer.FLAG_CLOSEABLE);
	}

	public abstract Vec4f getPreferredBounds();

}
