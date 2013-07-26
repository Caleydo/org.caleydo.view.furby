/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.view.heatmap.v2.BasicBlockColorer;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.IBlockColorer;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;

/**
 * @author Samuel Gratzl
 *
 */
public class ClusterContentElement extends GLElementDecorator {
	private final HeatMapElement heatmap;

	public ClusterContentElement(TablePerspective data, ClassToInstanceMap<Object> params) {
		heatmap = new HeatMapElement(data, getDefault(params, IBlockColorer.class, BasicBlockColorer.INSTANCE),
				getDefault(params, EDetailLevel.HIGH));
		setContent(heatmap);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getDefault(ClassToInstanceMap<Object> params, T default_) {
		Preconditions.checkNotNull(default_);
		if (params.containsKey(default_.getClass()))
			return (T) params.get(default_.getClass());
		return default_;
	}

	private static <T> T getDefault(ClassToInstanceMap<Object> params, Class<T> clazz, T default_) {
		if (params.containsKey(clazz))
			return params.getInstance(clazz);
		return default_;
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

	/**
	 * @param ind
	 * @return
	 */
	public float getRecordPos(int index) {
		return heatmap.getRecordCellSpace(index).getPosition();
	}
}
