/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.toolbar;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.elem.BiClustering;
import org.caleydo.view.bicluster.event.ForceChangeEvent;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class LayoutToolBarElement extends AToolBarElement {
	private static final float MIN_REPULSION = 0;
	private static final float MIN_ATTRACTION = 0;
	private static final float MAX_REPULSION = 150000f;
	private static final float MAX_ATTRACTION = 150f;
	private static final float MIN_BORDER_FORCE = 0;
	private static final float MAX_BORDER_FORCE = 300f;
	private static final float DEFAULT_REPULSION = 100000f;
	private static final float DEFAULT_ATTRACTION = 100f;
	private static final float DEFAULT_BORDERFACTOR = 200f;

	private GLSlider repulsionSlider, attractionSlider, borderForceSlider;

	public LayoutToolBarElement() {
		createForceSliders();

		reset();
	}

	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1)));
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		if (slider == borderForceSlider || slider == repulsionSlider || slider == attractionSlider) {
			float att = attractionSlider.getValue();
			float rep = repulsionSlider.getValue();
			float bord = borderForceSlider.getValue();
			EventPublisher.trigger(new ForceChangeEvent(att, rep, bord));
		}
	}

	@Override
	public void reset() {
		this.repulsionSlider.setCallback(null).setValue(DEFAULT_REPULSION).setCallback(this);
		this.attractionSlider.setCallback(null).setValue(DEFAULT_ATTRACTION).setCallback(this);
		this.borderForceSlider.setCallback(null).setValue(DEFAULT_BORDERFACTOR).setCallback(this);
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

	@Override
	public void init(final BiClustering biClustering) {
		ATableBasedDataDomain dataDomain = biClustering.getXDataDomain();
	}

	/**
	 * @return
	 */
	@Override
	public Rect getPreferredBounds() {
		return new Rect(-205, 480 + 20 + 20, 200, 216);
	}

}
