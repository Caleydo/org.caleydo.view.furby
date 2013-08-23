/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.heatmap.v2.EShowLabels;

import com.google.common.base.Predicate;

/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public abstract class AMultiClusterElement extends ClusterElement {
	protected final ClusterContentElement content;


	public AMultiClusterElement(TablePerspective data, BiClustering clustering, Predicate<? super String> predicate) {
		super(data, clustering);

		content = createContent(predicate);
		this.add(content);
	}

	/**
	 * @param asElement
	 * @return
	 */
	protected static boolean doesShowLabels(GLElement asElement) {
		return (asElement instanceof ClusterContentElement && ((ClusterContentElement) asElement).doesShowLabels());
	}

	@Override
	public final String getID() {
		return data.getLabel();
	}

	/**
	 * @return
	 */
	protected final ClusterContentElement createContent(Predicate<? super String> filter) {
		Builder builder = GLElementFactoryContext.builder();
		builder.withData(data);
		builder.put(EDetailLevel.class, EDetailLevel.MEDIUM);
		ClusterContentElement c = new ClusterContentElement(builder, filter);

		// trigger a scale event on vis change
		c.onActiveChanged(new GLElementFactorySwitcher.IActiveChangedCallback() {
			@Override
			public void onActiveChanged(int active) {
				EventPublisher.trigger(new ClusterScaleEvent(AMultiClusterElement.this));
			}
		});
		return c;
	}


	@Override
	protected void handleFocus(boolean isFocused) {
		super.handleFocus(isFocused);
		if (isFocused) {
			content.showLabels(EShowLabels.RIGHT);
		} else {
			content.hideLabels();
		}
	}

	@Override
	public final float getDimPosOf(int index) {
		if (isFocused()) {
			int ind = getDimensionVirtualArray().indexOf(index);
			return content.getDimensionPos(ind);
		} else {
			return getDimIndexOf(index) * getSize().x() / getDimensionVirtualArray().size();
		}
	}

	@Override
	public final float getRecPosOf(int index) {
		if (isFocused()) {
			int ind = getRecordVirtualArray().indexOf(index);
			return content.getRecordPos(ind);
		} else {
			return getRecIndexOf(index) * getSize().y() / getRecordVirtualArray().size();
		}
	}

	@Override
	public final Vec2f getPreferredSize(float scaleX, float scaleY) {
		if (!(content.isShowingHeatMap())) {
			ClusterContentElement c = (content);
			return c.getMinSize();
		}
		return new Vec2f(getNumberOfDimElements() * scaleX, getNumberOfRecElements() * scaleY);
	}
}