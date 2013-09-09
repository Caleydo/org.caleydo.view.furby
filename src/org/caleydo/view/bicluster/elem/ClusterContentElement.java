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
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.IActiveChangedCallback;
import org.caleydo.view.bicluster.event.SwitchVisualizationEvent;
import org.caleydo.view.heatmap.v2.AHeatMapElement;
import org.caleydo.view.heatmap.v2.CellSpace;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.ISpacingStrategy;
import org.caleydo.view.heatmap.v2.LinearBarHeatMapElement;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class ClusterContentElement extends GLElementDecorator {
	/**
	 *
	 */
	private static final ISpacingStrategy FISH_EYE = SpacingStrategies.fishEye(18);
	private final Collection<GLElement> repaintOnRepaint = new ArrayList<>(2);
	private final TablePerspective data;
	private final GLElementFactorySwitcher switcher;
	private final ScrollingDecorator scroller;

	/**
	 * @param builder
	 * @param filter
	 */
	public ClusterContentElement(Builder builder, Predicate<? super String> filter) {
		builder.set("histogram.showColorMapper", false); // don't show the color mapper
		builder.set("heatmap.linearBar.scaleLocally"); // scale plot per table perspective
		GLElementFactoryContext context = builder.build();
		this.data = context.getData();
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(context, "bicluster",
 filter);
		this.switcher = new GLElementFactorySwitcher(extensions, ELazyiness.NONE);
		this.scroller = ScrollingDecorator.wrap(this.switcher, 10);
		this.scroller.setEnabled(false);
		setContent(this.scroller);
	}

	@Override
	public void repaint() {
		super.repaint();
		GLElementAccessor.repaintDown(this.switcher);
		for (GLElement r : repaintOnRepaint)
			if (r.getVisibility().doRender())
				r.repaint();
	}

	@Override
	public void repaintPick() {
		super.repaintPick();
		GLElementAccessor.repaintDown(this.switcher);
	}

	public void addRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.add(elem);
	}

	public void removeRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.remove(elem);
	}

	@ListenTo
	private void onSwitchVisualizationEvent(SwitchVisualizationEvent event) {
		String target = event.getId();
		for (GLElementSupplier s : switcher) {
			if (s.getId().equals(target)) {
				switcher.setActive(s);
				break;
			}
		}
	}
	/**
	 * @param right
	 */
	public boolean changeFocus(boolean focus) {
		boolean any = false;
		for (AHeatMapElement heatmap : Iterables.filter(switcher.getInstances(), AHeatMapElement.class)) {
			if (focus) {
				heatmap.setRecordSpacingStrategy(FISH_EYE);
				heatmap.setDimensionSpacingStrategy(FISH_EYE);
			} else {
				heatmap.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
				heatmap.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			}
			any = true;
		}
		scroller.setEnabled(focus);
		return any;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(Color.WHITE).fillRect(0, 0, w, h);
		super.renderImpl(g, w, h);

		// render blend out overlay
		ClusterElement cluster = findParent(ClusterElement.class);
		float a = 1.0f;
		if (!cluster.isFocused()) {
			a *= cluster.curOpacityFactor;
		}
		if (a < 1) {
			g.incZ(5);
			g.color(1, 1, 1, 1 - a).fillRect(0, 0, w, h);
			g.incZ(-5);
		}
	}

	boolean isShowingHeatMap() {
		return switcher.getActiveElement() instanceof HeatMapElement;
	}

	boolean isShowingLinearPlot() {
		return switcher.getActiveElement() instanceof LinearBarHeatMapElement;
	}

	public Vec2f getMinSize() {
		IHasMinSize minSize = switcher.getLayoutDataAs(IHasMinSize.class, null);
		if (minSize != null)
			return minSize.getMinSize();
		return getContent().getLayoutDataAs(Vec2f.class, new Vec2f(data.getNrDimensions(), data.getNrRecords()));
	}
	/**
	 * @return
	 */
	private AHeatMapElement getHeatMap() {
		GLElement activeElement = switcher.getActiveElement();
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
		switcher.onActiveChanged(callback);
	}

	/**
	 * @return
	 */
	public ButtonBarBuilder createButtonBarBuilder() {
		return switcher.createButtonBarBuilder();
	}

	/**
	 * @return
	 *
	 */
	public Rect getClippingArea() {
		return scroller.getClipingArea();
	}

	/**
	 * @param iHasMinSize
	 */
	public void setMinSizeProvider(IHasMinSize minSizeProvider) {
		scroller.setMinSizeProvider(minSizeProvider);
	}
}
