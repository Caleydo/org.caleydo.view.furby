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
import org.caleydo.view.heatmap.v2.AHeatMapElement;
import org.caleydo.view.heatmap.v2.BasicBlockColorer;
import org.caleydo.view.heatmap.v2.CellSpace;
import org.caleydo.view.heatmap.v2.EShowLabels;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.IBlockColorer;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
		builder.set("histogram.showColorMapper", false); // don't show the color mapper
		builder.set("heatmap.linearBar.scaleLocally"); // scale plot per table perspective
		builder.put(IBlockColorer.class, BasicBlockColorer.INSTANCE);
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
		boolean any = false;
		for (AHeatMapElement heatmap : Iterables.filter(getSwitcher().getInstances(), AHeatMapElement.class)) {
			heatmap.setDimensionLabels(EShowLabels.RIGHT);
			heatmap.setRecordLabels(EShowLabels.RIGHT);
			heatmap.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
			heatmap.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
			any = true;
		}
		return any;
	}

	/**
	 *
	 */
	public boolean hideLabels() {
		boolean any = false;
		for (AHeatMapElement elem : Iterables.filter(getSwitcher().getInstances(), AHeatMapElement.class)) {
			elem.setDimensionLabels(EShowLabels.NONE);
			elem.setRecordLabels(EShowLabels.NONE);
			elem.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
			elem.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			any = true;
		}
		return any;
	}

	boolean doesShowLabels() {
		AHeatMapElement h = getHeatMap();
		return h != null && h.getRecordLabels() == EShowLabels.RIGHT;
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
		return getSwitcher().getActiveElement() instanceof HeatMapElement;
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
	private AHeatMapElement getHeatMap() {
		GLElementFactorySwitcher s = getSwitcher();
		GLElement activeElement = s.getActiveElement();
		if (activeElement instanceof AHeatMapElement)
			return (AHeatMapElement) activeElement;
		return null;
	}

	/**
	 * @param ind
	 * @return
	 */
	public float getDimensionPos(int index) {
		AHeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getDimensionCellSpace(index).getPosition();
		return getDimensionCell(index).getPosition();
	}

	public CellSpace getDimensionCell(int index) {
		AHeatMapElement heatmap = getHeatMap();
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
		AHeatMapElement heatmap = getHeatMap();
		if (heatmap != null)
			return heatmap.getRecordCellSpace(index).getPosition();
		return getRecordCell(index).getPosition();
	}

	public CellSpace getRecordCell(int index) {
		AHeatMapElement heatmap = getHeatMap();
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
