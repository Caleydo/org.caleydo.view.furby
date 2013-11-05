/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.toolbar;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.elem.BiClustering;
import org.caleydo.view.bicluster.elem.layout.AForceBasedLayout;
import org.caleydo.view.bicluster.elem.layout.ILayoutToolBar;
import org.caleydo.view.bicluster.event.ForceChangeEvent;

import com.google.common.collect.Iterables;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class LayoutToolBarElement extends AToolBarElement implements ILayoutToolBar {

	public LayoutToolBarElement() {
		reset();
	}

	private void setText(GLElement elem, String text) {
		elem.setRenderer(GLRenderers.drawText(text, VAlign.LEFT, new GLPadding(1)));
	}

	@Override
	public void addSlider(String name, String label, float value, float min, float max) {
		GLElement l = new GLElement();
		l.setSize(Float.NaN, LABEL_WIDTH);
		setText(l, label);
		this.add(l);

		GLSlider s = new GLSlider(min, max, value);
		s.setCallback(this);
		s.setSize(Float.NaN, SLIDER_WIDH);
		s.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
		s.setLayoutData(new ParameterData(name, value));
		this.add(s);
	}

	/**
	 * @param layout
	 */
	public void fill(AForceBasedLayout layout) {
		layout.fillLayoutToolBar(this);
	}

	private static class ParameterData {
		private final String name;
		private final float default_;

		public ParameterData(String name, float default_) {
			this.name = name;
			this.default_ = default_;
		}

		/**
		 * @return the name, see {@link #name}
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the default_, see {@link #default_}
		 */
		public float getDefault_() {
			return default_;
		}
	}

	@Override
	public void onSelectionChanged(GLSlider slider, float value) {
		ParameterData p = slider.getLayoutDataAs(ParameterData.class, null);
		if (p == null)
			return;
		EventPublisher.trigger(new ForceChangeEvent(p.getName(), value));
	}

	@Override
	public void reset() {
		for (GLSlider s : Iterables.filter(this, GLSlider.class)) {
			ParameterData p = s.getLayoutDataAs(ParameterData.class, null);
			if (p == null)
				continue;
			s.setValue(p.getDefault_());
		}
	}

	@Override
	public void init(final BiClustering biClustering) {

	}

	/**
	 * @return
	 */
	@Override
	public Rect getPreferredBounds() {
		return new Rect(-205, 520 + 20 + 20, 200, size() / 2 * 44);
	}

}
