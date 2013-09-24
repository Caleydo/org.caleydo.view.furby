/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.Pick;

import com.google.common.base.Objects;

/**
 * a simple basic widget for a spinner, e.g integer spinner
 *
 * @author Samuel Gratzl
 *
 */
public class MyUnboundSpinner extends PickableGLElement {
	private static final float BUTTON_SIZE = 12;

	public static final int UNBOUND = 0;
	/**
	 * callback for value changes
	 */
	private IChangeCallback callback = DUMMY_CALLBACK;

	/**
	 * the internal value
	 */
	private int value;

	private boolean hovered;

	private int armed = -1;

	/**
	 *
	 */
	public MyUnboundSpinner(int initialValue) {
		this.value = initialValue <= 0 ? -1 : initialValue;
		setRenderer(GLRenderers.fillRect(Color.WHITE));
	}

	private void inc() {
		if (isUnBound())
			return;
		this.value++;
		repaint();
		fireCallback();
	}

	private void dec() {
		if (value <= 1)
			return;
		int next = Math.max(1, this.value--);
		if (next == this.value)
			return;
		this.value = next;
		repaint();
		fireCallback();
	}

	protected final void fireCallback() {
		callback.onValueChanged(this, isUnBound() ? UNBOUND : value);
	}

	/**
	 * @param callback
	 *            setter, see {@link callback}
	 * @return
	 */
	public MyUnboundSpinner setCallback(IChangeCallback callback) {
		this.callback = Objects.firstNonNull(callback, DUMMY_CALLBACK);
		return this;
	}

	@Override
	protected void onMouseOver(Pick pick) {
		this.hovered = true;
		repaint();
	}

	@Override
	protected void onMouseOut(Pick pick) {
		this.hovered = false;
		repaint();
	}


	/**
	 * allow value changes via mouse wheel
	 *
	 * @param pick
	 */
	@Override
	protected void onMouseWheel(Pick pick) {
		int r = ((IMouseEvent) pick).getWheelRotation();
		if (r > 0)
			inc();
		else if (r < 0)
			dec();
	}

	@Override
	protected void onClicked(Pick pick) {
		Vec2f p = toRelative(pick.getPickedPoint());
		Vec2f size = getSize();
		if (p.x() < size.x() - BUTTON_SIZE) {
			value = -value;
			fireCallback();
			repaint();
			return;
		}
		if (isUnBound())
			return;
		armed = p.y() <= size.y() * 0.5f ? 0 : 1;
		repaint();
	}

	private boolean isUnBound() {
		return value <= 0;
	}

	@Override
	protected void onDragged(Pick pick) {
		if (isUnBound())
			return;
		Vec2f p = toRelative(pick.getPickedPoint());
		Vec2f size = getSize();
		if (p.x() < size.x() - BUTTON_SIZE) {
			if (armed >= 0)
				repaint();
			armed = -1;
			return;
		}
		int newArmed = p.y() <= size.y() * 0.5f ? 0 : 1;
		if (newArmed != armed) {
			armed = newArmed;
			repaint();
		}
	}

	@Override
	protected void onMouseReleased(Pick pick) {
		if (armed < 0)
			return;
		if (armed == 0)
			inc();
		else
			dec();
		armed = -1;
		repaint();
	}

	/**
	 * @return the value, see {@link #value}
	 */
	public int getValue() {
		return isUnBound() ? UNBOUND : value;
	}


	/**
	 * @param value
	 *            setter, see {@link value}
	 */
	public MyUnboundSpinner setValue(int value) {
		if (this.value == value)
			return this;
		this.value = value <= 0 ? -1 : value;
		repaint();
		return this;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		if (!hovered || isUnBound()) {
			renderValue(g, w, h);
			return;
		}
		renderValue(g, w - BUTTON_SIZE, h);

		float hhalf = h * 0.5f;
		float offset = w - BUTTON_SIZE;
		// armed effects
		g.color(armed != 0 ? Color.LIGHT_GRAY : Color.GRAY).fillRect(offset, 0, BUTTON_SIZE, hhalf);
		g.color(armed != 1 ? Color.LIGHT_GRAY : Color.GRAY).fillRect(offset, hhalf, BUTTON_SIZE, hhalf);

		// borders and triangles
		g.color(Color.DARK_GRAY);
		g.drawRect(0, 0, w, h);
		g.move(offset, 0);
		g.fillPolygon(new Vec2f(1, hhalf - 2), new Vec2f(BUTTON_SIZE - 1, hhalf - 2), new Vec2f(BUTTON_SIZE * 0.5f, 2));
		g.fillPolygon(new Vec2f(1, hhalf + 2), new Vec2f(BUTTON_SIZE - 1, hhalf + 2), new Vec2f(BUTTON_SIZE * 0.5f,
				h - 2));

		g.drawLine(0, 0, 0, h);
		g.drawLine(0, hhalf, BUTTON_SIZE, hhalf);
		g.move(-offset, 0);

	}

	private void renderValue(GLGraphics g, float w, float h) {
		final boolean unbound = isUnBound();
		g.fillImage(EButtonIcon.CHECKBOX.get(!unbound), 1, 1, h - 2, h - 2);

		g.textColor(unbound ? Color.LIGHT_GRAY : Color.BLACK);
		g.drawText(String.valueOf(Math.abs(value)), h, 1, w - 2 - h, h - 3);
		g.textColor(Color.BLACK);
	}

	/**
	 * callback interface for value changes of a spinner
	 *
	 * @author Samuel Gratzl
	 *
	 */
	public interface IChangeCallback {
		void onValueChanged(MyUnboundSpinner spinner, int value);
	}

	private static final IChangeCallback DUMMY_CALLBACK = new IChangeCallback() {
		@Override
		public void onValueChanged(MyUnboundSpinner spinner, int value) {

		}
	};
}
