/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.event.ForceChangeEvent;
import org.caleydo.view.bicluster.event.MaxClusterSizeChangeEvent;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class LayoutToolBarElement extends AToolBarElement {
	private static final float MIN_DIMENSION_SIZE = 50;
	private static final float MIN_RECORD_SIZE = 50;
	private static final float MAX_DIMENSION_SIZE = 250;
	private static final float MAX_RECORD_SIZE = 250;

	private static final float MIN_REPULSION = 0;
	private static final float MIN_ATTRACTION = 0;
	private static final float MAX_REPULSION = 150000f;
	private static final float MAX_ATTRACTION = 150f;
	private static final float MIN_BORDER_FORCE = 0;
	private static final float MAX_BORDER_FORCE = 300f;
	private static final float DEFAULT_REPULSION = 100000f;
	private static final float DEFAULT_ATTRACTION = 100f;
	private static final float DEFAULT_BORDERFACTOR = 200f;

	private GLSlider clusterDimSizeSlider, clusterRecSizeSlider;
	private GLSlider repulsionSlider, attractionSlider, borderForceSlider;

	private TablePerspective x;

	public LayoutToolBarElement() {
		initSliders();
	}

	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1)));
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		if (slider == clusterDimSizeSlider || slider == clusterRecSizeSlider)
			EventPublisher.trigger(new MaxClusterSizeChangeEvent(clusterDimSizeSlider.getValue(), clusterRecSizeSlider
					.getValue()));
		if (slider == borderForceSlider || slider == repulsionSlider || slider == attractionSlider) {
			float att = attractionSlider.getValue();
			float rep = repulsionSlider.getValue();
			float bord = borderForceSlider.getValue();
			EventPublisher.trigger(new ForceChangeEvent(att, rep, bord));
		}
	}

	/**
	 *
	 */
	private void initSliders() {
		createClusterSizeSlider();
		createForceSliders();

		GLButton reset = new GLButton();
		reset.setRenderer(GLRenderers.drawText("Reset", VAlign.CENTER, new GLPadding(0, 0, 0, 2)));
		reset.setCallback(new GLButton.ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				reset();
			}
		});
		reset.setTooltip("Reset the layout parameters to their default value");
		reset.setSize(Float.NaN, LABEL_WIDTH);
		this.add(reset);

		reset();
	}

	/**
	 *
	 */
	public void reset() {
		this.repulsionSlider.setValue(DEFAULT_REPULSION);
		this.attractionSlider.setValue(DEFAULT_ATTRACTION);
		this.borderForceSlider.setValue(DEFAULT_BORDERFACTOR);
		this.clusterDimSizeSlider.setValue(110f);
		this.clusterRecSizeSlider.setValue(150f);
	}

	/**
	 *
	 */
	private void createForceSliders() {
		GLElement repulsionLabel = new GLElement();
		repulsionLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(repulsionLabel);

		this.repulsionSlider = new GLSlider(MIN_REPULSION, MAX_REPULSION, DEFAULT_REPULSION);
		this.repulsionSlider.setCallback(this);
		this.repulsionSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.repulsionSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(repulsionSlider);

		GLElement attractionLabel = new GLElement();
		attractionLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(attractionLabel);

		this.attractionSlider = new GLSlider(MIN_ATTRACTION, MAX_ATTRACTION, DEFAULT_ATTRACTION);
		this.attractionSlider.setCallback(this);
		this.attractionSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.attractionSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(attractionSlider);

		GLElement borderForceLabel = new GLElement();
		borderForceLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(borderForceLabel);

		this.borderForceSlider = new GLSlider(MIN_BORDER_FORCE, MAX_BORDER_FORCE, DEFAULT_BORDERFACTOR);
		this.borderForceSlider.setCallback(this);
		this.borderForceSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.borderForceSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(borderForceSlider);

		setText(repulsionLabel, "Repulsion between Clusters");
		setText(attractionLabel, "Attraction between Clusters");
		setText(borderForceLabel, "Force from the Windowborder");

	}

	/**
	 *
	 */
	private void createClusterSizeSlider() {
		GLElement clusterDimSizeLabel = new GLElement();
		clusterDimSizeLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(clusterDimSizeLabel);

		this.clusterDimSizeSlider = new GLSlider(MIN_DIMENSION_SIZE, MAX_DIMENSION_SIZE, 110f);
		this.clusterDimSizeSlider.setCallback(this);
		this.clusterDimSizeSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.clusterDimSizeSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterDimSizeSlider);

		GLElement clusterRecSizeLabel = new GLElement();
		clusterRecSizeLabel.setSize(Float.NaN, LABEL_WIDTH);
		this.add(clusterRecSizeLabel);

		this.clusterRecSizeSlider = new GLSlider(MIN_RECORD_SIZE, MAX_RECORD_SIZE, 150f);
		this.clusterRecSizeSlider.setCallback(this);
		this.clusterRecSizeSlider.setSize(Float.NaN, SLIDER_WIDH);
		this.clusterRecSizeSlider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		this.add(clusterRecSizeSlider);

		if (x == null) {
			setText(clusterDimSizeLabel, "Max. Dimension Size (pxl)");
			setText(clusterRecSizeLabel, "Max. Record Size (pxl)");
		} else {
			setText(clusterDimSizeLabel, "Max. " + x.getDataDomain().getDimensionIDCategory().toString()
					+ " Size (pxl)");
			setText(clusterRecSizeLabel, "Max. " + x.getDataDomain().getRecordIDCategory().toString()
					+ " Size (pxl)");

		}
	}

	public void setXTablePerspective(final TablePerspective x) {
		if (x == null)
			return;
		else
			this.x = x;
	}

	/**
	 * @return
	 */
	@Override
	public Rect getPreferredBounds() {
		return new Rect(-205, 340, 200, 236);
	}

}
