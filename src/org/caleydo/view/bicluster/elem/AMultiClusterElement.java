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
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;

import com.google.common.base.Predicate;

/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public abstract class AMultiClusterElement extends ClusterElement {
	protected final ClusterContentElement content;


	public AMultiClusterElement(int bcNr, TablePerspective data, BiClustering clustering,
			Predicate<? super String> predicate) {
		super(bcNr, data, clustering);

		content = createContent(predicate);
		this.add(content);
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
		c.setMinSizeProvider(new IHasMinSize() {
			@Override
			public Vec2f getMinSize() {
				return getLayoutDataAs(Vec2f.class, getPreferredSize(1, 1));
			}
		});
		return c;
	}


	@Override
	public void setFocus(boolean isFocused) {
		super.setFocus(isFocused);
		content.changeFocus(isFocused);
	}

	@Override
	public final float getDimPosOf(int id) {
		if (isFocused()) {
			int ind = getDimVirtualArray().indexOf(id);
			return content.getDimensionPos(ind);
		} else {
			return getDimIndexOf(id) * getSize().x() / getDimVirtualArray().size();
		}
	}

	@Override
	public final float getRecPosOf(int id) {
		if (isFocused()) {
			int ind = getRecVirtualArray().indexOf(id);
			return content.getRecordPos(ind);
		} else {
			return getRecIndexOf(id) * getSize().y() / getRecVirtualArray().size();
		}
	}

	@Override
	public final Vec2f getPreferredSize(float scaleX, float scaleY) {
		if (content.isShowingHeatMap()) {
			return new Vec2f(getDimSize() * scaleX, getRecSize() * scaleY);
		}
		if (content.isShowingLinearPlot()) {
			return new Vec2f(getDimSize() * scaleX, getRecSize() * 2 * scaleY);
		}
		ClusterContentElement c = (content);
		return c.getMinSize();
	}
}
