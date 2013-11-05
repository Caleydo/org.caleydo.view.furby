/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import gleem.linalg.Vec2f;

import java.util.List;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.ForceChangeEvent;
import org.caleydo.view.bicluster.event.UpdateBandsEvent;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AForceBasedLayout implements IGLLayout2 {
	protected final AllClustersElement parent;

	public AForceBasedLayout(AllClustersElement parent) {
		this.parent = parent;
	}

	public abstract void fillLayoutToolBar(ILayoutToolBar elem);

	@ListenTo
	private void listenTo(ForceChangeEvent e) {
		setParameter(e.getName(), e.getValue());
		parent.relayout();
	}

	protected abstract void setParameter(String name, float value);

	@Override
	public final boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		setSizes(children, w, h);
		boolean r = forceBasedLayout(children, w, h, deltaTimeMs);
		EventPublisher.trigger(new UpdateBandsEvent());
		return r;
	}


	private void setSizes(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement child : children) {
			GLElement g = child.asElement();
			if (!(g instanceof ClusterElement)) {
				child.hide();
				continue;
			}

			ClusterElement elem = (ClusterElement) g;
			if (!elem.isVisible()) {
				child.hide();
				continue;
			}

			float scaleX = elem.getZoom(EDimension.DIMENSION);
			float scaleY = elem.getZoom(EDimension.RECORD);
			if (elem.needsUniformScaling())
				scaleX = scaleY = (scaleX + scaleY) * 0.5f; // mean

			Vec2f minSize = elem.getMinSize();
			float w_i = minSize.x() * scaleX;
			float h_i = minSize.y() * scaleY;
			if (elem.isFocused()) {
				Vec2f old = elem.getLayoutDataAs(Vec2f.class, null);
				Vec2f new_ = new Vec2f(w_i - 6, h_i - 6);
				if (!Objects.equals(old, new_)) {
					elem.setLayoutData(new_);
					elem.relayoutContent();
				}
				w_i = Math.min(w_i, w * 0.7f);
				h_i = Math.min(h_i, h * 0.85f);
			}
			child.setSize(w_i, h_i);
		}
	}

	protected abstract boolean forceBasedLayout(List<? extends IGLLayoutElement> children, float w, float h,
			int deltaTimeMs);

	protected static int computeNumberOfIterations(int deltaTimeMs) {
		final float iterationFactor = 1000;

		int iterations = (int) ((float) 1 / deltaTimeMs * iterationFactor) + 1;
		return iterations;
	}
}
