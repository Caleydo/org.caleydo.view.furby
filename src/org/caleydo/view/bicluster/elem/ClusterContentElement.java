/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.view.heatmap.v2.CellSpace;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class ClusterContentElement extends GLElementContainer implements IGLLayout {
	private final HeatMapElement heatmap;
	private final Collection<GLElement> repaintOnRepaint = new ArrayList<>(2);

	/**
	 * @param builder
	 */
	public ClusterContentElement(Builder builder) {
		setLayout(GLLayouts.flowVertical(2));
		builder.set("histogram.showColorMapper", false);
		GLElementFactoryContext context = builder.build();
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(context, "bicluster",
				Predicates.alwaysTrue());
		GLElementFactorySwitcher content = new GLElementFactorySwitcher(extensions, ELazyiness.NONE);
		heatmap = (HeatMapElement) content.get("heatmap");
		assert heatmap != null;
		this.add(content);
	}

	public GLElementContainer createVerticalButtonBar() {
		GLElementContainer bar = ((GLElementFactorySwitcher) get(0)).createButtonBar();
		bar.setLayout(GLLayouts.flowVertical(2));
		bar.setSize(16, Float.NaN);
		return bar;
	}

	@Override
	public void repaint() {
		super.repaint();
		super.repaintChildren();
		for (GLElement r : repaintOnRepaint)
			r.repaint();
	}

	@Override
	public void repaintPick() {
		super.repaintPick();
		super.repaintPickChildren();
	}

	public void addRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.add(elem);
	}

	public void removeRepaintOnRepaint(GLElement elem) {
		this.repaintOnRepaint.remove(elem);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		// TODO
	}
	/**
	 * @param right
	 */
	public void showLabels(EShowLabels right) {
		heatmap.setDimensionLabels(EShowLabels.RIGHT);
		heatmap.setRecordLabels(EShowLabels.RIGHT);
		heatmap.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
		heatmap.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
	}

	/**
	 *
	 */
	public void hideLabels() {
		heatmap.setDimensionLabels(EShowLabels.NONE);
		heatmap.setRecordLabels(EShowLabels.NONE);
		heatmap.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
		heatmap.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
	}

	/**
	 * @param ind
	 * @return
	 */
	public float getDimensionPos(int index) {
		return heatmap.getDimensionCellSpace(index).getPosition();
	}

	public CellSpace getDimensionCell(int index) {
		return heatmap.getDimensionCellSpace(index);
	}

	/**
	 * @param ind
	 * @return
	 */
	public float getRecordPos(int index) {
		return heatmap.getRecordCellSpace(index).getPosition();
	}

	public CellSpace getRecordCell(int index) {
		return heatmap.getRecordCellSpace(index);
	}
}
