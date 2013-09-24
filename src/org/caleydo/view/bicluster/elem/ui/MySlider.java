/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.ui;

import java.util.Locale;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.gleem.ColoredVec2f;
import org.caleydo.core.view.opengl.util.text.ETextStyle;

/**
 * custom slider implementation with inversion support
 *
 * @author Samuel Gratzl
 *
 */
public class MySlider extends PickableGLElement {
	/**
	 * width of a gl slider
	 */
	private static final int BAR_WIDTH = 5;

	private ISelectionCallback callback = DUMMY_CALLBACK;

	/**
	 * left and minimal value
	 */
	private float min = 0;
	/**
	 * right and maximal value
	 */
	private float max = 1;
	/**
	 * current value
	 */
	private float value = 0.5f;

	/**
	 * if the user uses the mouse wheel to manipulate the value, how many DIPs are one mouse wheel rotation
	 */
	private float wheelInc = 1f;

	private boolean hovered = false;
	private boolean dragged = false;

	/**
	 * horizontal or vertical rendering
	 */
	private boolean isHorizontal = true;

	/**
	 * the format string to use for rendering a value using {@link String#format(String, Object...)}
	 */
	private String valueFormat = "%.2f";

	private final boolean invert;

	public MySlider(boolean invert) {
		this.invert = invert;
	}

	public MySlider(boolean invert, float min, float max, float value) {
		this.invert = invert;
		this.min = min;
		this.max = max;
		this.value = clamp(value);
	}

	/**
	 * @param wheelInc
	 *            setter, see {@link wheelInc}
	 */
	public MySlider setWheelInc(float wheelInc) {
		this.wheelInc = wheelInc;
		return this;
	}

	/**
	 * @return the isHorizontal, see {@link #isHorizontal}
	 */
	public boolean isHorizontal() {
		return isHorizontal;
	}

	/**
	 * @param isHorizontal
	 *            setter, see {@link isHorizontal}
	 */
	public MySlider setHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
		return this;
	}

	/**
	 * @param valueFormat
	 *            setter, see {@link valueFormat}
	 */
	public MySlider setValueFormat(String valueFormat) {
		this.valueFormat = valueFormat;
		return this;
	}

	/**
	 * @return the value, see {@link #value}
	 */
	public float getValue() {
		return value;
	}

	/**
	 * @param value
	 *            setter, see {@link value}
	 */
	public MySlider setValue(float value) {
		value = clamp(value);
		if (this.value == value)
			return this;
		this.value = value;
		repaintAll();
		fireCallback(value);
		return this;
	}

	public MySlider setMinMax(float min, float max) {
		if (this.min == min && this.max == max)
			return this;
		this.min = min;
		this.max = max;
		this.value = clamp(value);
		repaintAll();
		return this;
	}

	/**
	 * @return the min, see {@link #min}
	 */
	public float getMin() {
		return min;
	}

	/**
	 * @return the max, see {@link #max}
	 */
	public float getMax() {
		return max;
	}


	protected final void fireCallback(float value) {
		callback.onSelectionChanged(this, value);
	}

	/**
	 * @param callback
	 *            setter, see {@link callback}
	 */
	public final MySlider setCallback(ISelectionCallback callback) {
		if (callback == null)
			callback = DUMMY_CALLBACK;
		if (this.callback == callback)
			return this;
		this.callback = callback;
		return this;
	}



	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		final boolean showText = true;
		final boolean showMinMaxText = hovered;

		{
			Color a = invert ? Color.BLACK : Color.WHITE;
			Color b = !invert ? Color.BLACK : Color.WHITE;
			if (isHorizontal)
				g.fillPolygon(new ColoredVec2f(0, 0, a), new ColoredVec2f(w, 0, b), new ColoredVec2f(w, h, b),
						new ColoredVec2f(0, h, a));
			else
				g.fillPolygon(new ColoredVec2f(0, 0, a), new ColoredVec2f(w, 0, a), new ColoredVec2f(w, h, b),
						new ColoredVec2f(0, h, b));
		}

		if (hovered || dragged)
			g.color(Color.BLUE);
		else
			g.color(Color.LIGHT_BLUE);

		if (isHorizontal) {
			float x = mapValue(w) + 1;
			g.fillRect(x, 0, Math.min(BAR_WIDTH, w - x), h);
			if (showMinMaxText) {
				g.textColor(invert ? Color.WHITE : Color.BLACK);
				g.drawText(format(invert ? max : min), 2, 3, w - 4, h - 9, VAlign.LEFT);
				g.textColor(!invert ? Color.WHITE : Color.BLACK);
				g.drawText(format(invert ? min : max), 2, 3, w - 5, h - 9, VAlign.RIGHT);
			}
			if (showText)
				g.textColor(Color.BLUE).drawText(format(value), 2, 2, w - 3, h - 6, VAlign.CENTER, ETextStyle.BOLD);
		} else {
			float y = mapValue(h) + 1;
			g.fillRect(0, y, w, Math.min(BAR_WIDTH, h - y));
			if (showText)
				g.save().gl.glRotatef(90, 0, 0, 1);
			if (showMinMaxText) {
				g.textColor(invert ? Color.WHITE : Color.BLACK);
				g.drawText(format(invert ? max : min), 2, 3 - w, h - 4, w - 9, VAlign.LEFT);
				g.textColor(!invert ? Color.WHITE : Color.BLACK);
				g.drawText(format(invert ? min : max), 2, 3 - w, h - 5, w - 9, VAlign.RIGHT);
			}
			if (showText)
				g.textColor(Color.BLUE)
						.drawText(format(value), 2, 2 - w, h - 3, w - 6, VAlign.CENTER, ETextStyle.BOLD);
			if (showText)
				g.restore();
		}
		g.textColor(Color.BLACK);
		g.color(Color.BLACK).drawRect(0, 0, w, h);
	}

	protected String format(float v) {
		return String.format(Locale.ENGLISH, valueFormat, v);
	}

	private float mapValue(float total) {
		total -= BAR_WIDTH + 2;
		float range = max - min;
		float factor = total / range;
		float r = (value - min) * factor;
		if (invert)
			r = total - r;
		return r;
	}

	private float unmapValue(float v) {
		float total = isHorizontal ? getSize().x() : getSize().y();
		total -= BAR_WIDTH + 2;
		if (invert)
			v = total - v;
		float range = max - min;
		float factor = total / range;
		return clamp(v / factor + min);
	}

	private float clamp(float v) {
		return Math.max(min, Math.min(max, v));
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (isHorizontal) {
			float x = mapValue(w);
			g.fillRect(x, 0, Math.min(BAR_WIDTH, w - x), h);
		} else {
			float y = mapValue(h);
			g.fillRect(0, y, w, Math.min(BAR_WIDTH, h - y));
		}
	}

	@Override
	protected void onMouseOver(Pick pick) {
		if (pick.isAnyDragging())
			return;
		hovered = true;
		repaint();
	}

	@Override
	protected void onMouseOut(Pick pick) {
		if (!hovered)
			return;
		dragged = false;
		hovered = false;
		repaint();
	}

	@Override
	protected void onMouseWheel(Pick pick) {
		setValue(unmapValue(mapValue(getSize().x()) + ((IMouseEvent) (pick)).getWheelRotation() * wheelInc));
		repaintAll();
	}

	@Override
	protected void onClicked(Pick pick) {
		if (pick.isAnyDragging())
			return;
		pick.setDoDragging(true);
		this.dragged = true;
		repaint();
	}

	@Override
	protected void onDragged(Pick pick) {
		if (!pick.isDoDragging())
			return;
		float v;
		if (isHorizontal) {
			v = mapValue(getSize().x()) + pick.getDx();
		} else {
			v = mapValue(getSize().y()) + pick.getDy();
		}
		setValue(unmapValue(v));
		repaintAll();
	}

	@Override
	protected void onMouseReleased(Pick pick) {
		this.dragged = false;
		repaint();
	}

	/**
	 * callback interface for selection changes
	 *
	 * @author Samuel Gratzl
	 *
	 */
	public interface ISelectionCallback {
		void onSelectionChanged(MySlider slider, float value);
	}

	private static final ISelectionCallback DUMMY_CALLBACK = new ISelectionCallback() {
		@Override
		public void onSelectionChanged(MySlider slider, float value) {

		}
	};
}
