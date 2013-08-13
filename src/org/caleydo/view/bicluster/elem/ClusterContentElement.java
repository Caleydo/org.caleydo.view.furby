/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collection;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.IActiveChangedCallback;
import org.caleydo.view.heatmap.v2.CellSpace;
import org.caleydo.view.heatmap.v2.EShowLabels;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class ClusterContentElement extends GLElementDecorator implements IActiveChangedCallback {
	private final Collection<GLElement> repaintOnRepaint = new ArrayList<>(2);
	private final TablePerspective data;

	/**
	 * @param builder
	 * @param filter
	 */
	public ClusterContentElement(Builder builder, Predicate<? super String> filter) {
		builder.set("histogram.showColorMapper", false);
		GLElementFactoryContext context = builder.build();
		this.data = context.getData();
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(context, "bicluster",
 filter);
		GLElementFactorySwitcher content = new GLElementFactorySwitcher(extensions, ELazyiness.NONE);
		content.onActiveChanged(this);
		setContent(content);
	}

	public GLElementContainer createVerticalButtonBar() {
		GLElementContainer bar = getSwitcher().createButtonBar();
		bar.setLayout(GLLayouts.flowVertical(2));
		bar.setSize(16, Float.NaN);
		return bar;
	}

	@Override
	public void onActiveChanged(int active) {
		relayoutParent();
	}

	@Override
	public void repaint() {
		super.repaint();
		GLElementAccessor.repaintDown(getContent());
		for (GLElement r : repaintOnRepaint)
			r.repaint();
	}

	@Override
	public void repaintPick() {
		super.repaintPick();
		GLElementAccessor.repaintPickDown(getContent());
	}

	public void addRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.add(elem);
	}

	public void removeRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.remove(elem);
	}

	/**
	 * @param right
	 */
	public boolean showLabels(EShowLabels right) {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null) {
			heatmap.setDimensionLabels(EShowLabels.RIGHT);
			heatmap.setRecordLabels(EShowLabels.RIGHT);
			heatmap.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
			heatmap.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
			return true;
		} else
			return false;
	}

	boolean doesShowLabels() {
		return isShowingHeatMap() && getHeatMap().getRecordLabels() == EShowLabels.RIGHT;
	}

	private GLElementFactorySwitcher getSwitcher() {
		return (GLElementFactorySwitcher) getContent();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		boolean labels = doesShowLabels();
		g.color(Color.WHITE).fillRect(0, 0, w - (labels ? 79 : 0), h - (labels ? 79 : 0));
		super.renderImpl(g, w, h);

		// render blend out overlay
		ClusterElement cluster = findParent(ClusterElement.class);
		float a = 1.0f;
		if (!cluster.isFocused) {
			a *= cluster.curOpacityFactor;
		}
		if (a < 1) {
			g.incZ(5);
			g.color(1, 1, 1, 1 - a).fillRect(0, 0, w, h);
			g.incZ(-5);
		}
	}

	boolean isShowingHeatMap() {
		HeatMapElement heatMap = getHeatMap();
		if (heatMap == null)
			return false;
		GLElementFactorySwitcher switcher = getSwitcher();
		return "heatmap".equals(switcher.getActiveId());
	}

	public Vec2f getMinSize() {
		IHasMinSize minSize = getContent().getLayoutDataAs(IHasMinSize.class, null);
		if (minSize != null)
			return minSize.getMinSize();
		return getContent().getLayoutDataAs(Vec2f.class, new Vec2f(data.getNrDimensions(), data.getNrRecords()));
	}
	/**
	 * @return
	 */
	private HeatMapElement getHeatMap() {
		return (HeatMapElement) getSwitcher().get("heatmap");
	}

	/**
	 *
	 */
	public boolean hideLabels() {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null) {
			heatmap.setDimensionLabels(EShowLabels.NONE);
			heatmap.setRecordLabels(EShowLabels.NONE);
			heatmap.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
			heatmap.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			return true;
		} else
			return false;
	}

	/**
	 * @param ind
	 * @return
	 */
	public float getDimensionPos(int index) {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getDimensionCellSpace(index).getPosition();
		return getDimensionCell(index).getPosition();
	}

	public CellSpace getDimensionCell(int index) {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getDimensionCellSpace(index);
		float w = getSize().x();
		float wi = w / data.getDimensionPerspective().getVirtualArray().size();
		return new CellSpace(index * wi, wi);
	}

	/**
	 * @param ind
	 * @return
	 */
	public float getRecordPos(int index) {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getRecordCellSpace(index).getPosition();
		return getRecordCell(index).getPosition();
	}

	public CellSpace getRecordCell(int index) {
		HeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getRecordCellSpace(index);
		float h = getSize().y();
		float hi = h / data.getRecordPerspective().getVirtualArray().size();
		return new CellSpace(index * hi, hi);
	}

	/**
	 * @param iActiveChangedCallback
	 */
	public void onActiveChanged(IActiveChangedCallback callback) {
		getSwitcher().onActiveChanged(callback);
	}
}
