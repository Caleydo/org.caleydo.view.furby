/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.heatmap.v2.CellSpace;

/**
 * element for rendering the label of a heatmap or linear plot
 *
 * @author Samuel Gratzl
 *
 */
public class HeatMapLabelElement extends GLElement {
	private static final float MAX_TEXT_HEIGHT = 12;
	private static final int TEXT_OFFSET = 5;
	private static final int MAX_TEXT_WIDTH = 200;
	private final Perspective perspective;
	private final ClusterContentElement spaceProvider;
	private final boolean horizontal;

	public HeatMapLabelElement(boolean horizontal, Perspective perspective, ClusterContentElement content) {
		this.horizontal = horizontal;
		this.perspective = perspective;
		this.spaceProvider = content;
		this.spaceProvider.addRepaintOnRepaint(this);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		final ATableBasedDataDomain dataDomain = (ATableBasedDataDomain) perspective.getDataDomain();
		VirtualArray va = perspective.getVirtualArray();

		Rect clipingArea = spaceProvider.getClippingArea();
		if (horizontal) {
			g.save();
			g.gl.glRotatef(-90, 0, 0, 1);
			float offset = -clipingArea.x();
			for (int i = 0; i < va.size(); ++i) {
				Integer id = va.get(i);
				String text = dataDomain.getDimensionLabel(id);
				CellSpace cell = spaceProvider.getDimensionCell(i);
				float x = cell.getPosition();
				float fieldWidth = cell.getSize();
				if (fieldWidth < 5)
					continue;
				if (x < clipingArea.x() || (x + fieldWidth) > clipingArea.x2())
					continue;

				float textHeight = Math.min(fieldWidth, MAX_TEXT_HEIGHT);
				g.drawText(text, -MAX_TEXT_WIDTH, offset + x + (fieldWidth - textHeight) * 0.5f, MAX_TEXT_WIDTH
						- TEXT_OFFSET, textHeight, VAlign.RIGHT);
			}
			g.restore();
		} else {
			float offset = -clipingArea.y();
			for (int i = 0; i < va.size(); ++i) {
				Integer id = va.get(i);
				String text = dataDomain.getRecordLabel(id);
				CellSpace cell = spaceProvider.getRecordCell(i);
				float y = cell.getPosition();
				float fieldHeight = cell.getSize();
				if (fieldHeight < 5)
					continue;
				if (y < clipingArea.y() || (y + fieldHeight) > clipingArea.y2())
					continue;
				float textHeight = Math.min(fieldHeight, MAX_TEXT_HEIGHT);
				g.drawText(text, TEXT_OFFSET, offset + y + (fieldHeight - textHeight) * 0.5f, MAX_TEXT_WIDTH,
						textHeight,
						VAlign.LEFT);
			}
		}
	}
}
