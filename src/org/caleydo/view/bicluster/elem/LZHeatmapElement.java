/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.heatmap.v2.CellSpace;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

/**
 * element for showing a heatmap of probabilities
 *
 * @author Samuel Gratzl
 *
 */
public class LZHeatmapElement extends GLElement {
	private final boolean horizontal;
	private Texture texture;
	/**
	 * spacer for the focus case with non uniform cells
	 */
	private ClusterContentElement spaceProvider;
	private int center;

	public LZHeatmapElement(boolean horizontal) {
		this.horizontal = horizontal;
		setSize(horizontal ? Float.NaN : 4, horizontal ? 4 : Float.NaN);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		texture = new Texture(GL.GL_TEXTURE_2D);

	}

	@Override
	protected void takeDown() {
		texture.destroy(GLContext.getCurrentGL());
		super.takeDown();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		GL2 gl = g.gl;
		texture.enable(gl);
		texture.bind(gl);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		g.color(Color.WHITE);
		g.save();
		gl.glTranslatef(1, 1, g.z());

		final float s_factor = 1.f / texture.getWidth();
		final float size = horizontal ? w : h;
		final float op_size = -2 + (horizontal ? h : w);

		float centerPos = 0;
		gl.glBegin(GL2GL3.GL_QUADS);
		if (spaceProvider == null) {
			rect(0, 1, 0, size, op_size, gl);
			centerPos = center > 0 ? center * size * s_factor : 0;
		} else {
			final Rect clippingArea = spaceProvider.getClippingArea();
			final float clippingStart = horizontal ? clippingArea.x() : clippingArea.y();
			final float clippingEnd = horizontal ? clippingArea.x2() : clippingArea.y2();

			int s = 0;
			int s_acc = 0;
			float p_last = Float.NaN;
			float p_x = 0;
			float p_acc = 0;
			for (int i = 0; i < texture.getWidth(); ++i) {
				final CellSpace cell = horizontal ? spaceProvider.getDimensionCell(i) : spaceProvider.getRecordCell(i);
				float p_i = cell.getSize();
				if (cell.getPosition() + p_i < clippingStart) {
					s++; // move texel
					continue;
				}
				if (cell.getPosition() > clippingEnd)
					break;
				if (cell.getPosition() < clippingStart) // reduce size on corner cases
					p_i -= clippingStart - cell.getPosition();
				if (cell.getPosition() + p_i > clippingEnd)
					p_i = clippingEnd - cell.getPosition();

				if (Float.isNaN(p_last) || Math.abs(p_i - p_last) < 0.3) {
					// uniform continue
				} else { // flush intermediate rect
					rect(s * s_factor, (s + s_acc) * s_factor, p_x, (p_x + p_acc), op_size, gl);
					s += s_acc;
					p_x += p_acc;
					s_acc = 0;
					p_acc = 0;
				}
				p_last = p_i;
				s_acc++;
				p_acc += p_i;
				if (i == center) {
					centerPos = (p_x + p_acc);
				}
			}
			rect(s * s_factor, (s + s_acc) * s_factor, p_x, (p_x + p_acc), op_size, gl);
		}
		gl.glEnd();
		g.checkError();
		texture.disable(gl);
		g.checkError();
		g.restore();

		// System.out.println(center + " " + centerPos);
		if (center != -2) {
			g.lineWidth(3).color(Color.BLUE);
			if (horizontal)
				g.drawLine(centerPos, 0, centerPos, h);
			else
				g.drawLine(0, centerPos, w, centerPos);
			g.lineWidth(1);
		}

		g.color(Color.GRAY).drawRect(1, 1, w - 2, h - 2);
	}

	/**
	 * s ... texel x ... vertex
	 *
	 * @param s1
	 * @param s2
	 * @param x
	 * @param x2
	 * @param gl
	 */
	private void rect(float s1, float s2, float x, float x2, float y, GL2 gl) {
		if (horizontal) {
			gl.glTexCoord2f(s1, 1f);
			gl.glVertex2f(x, 0);
			gl.glTexCoord2f(s2, 1f);
			gl.glVertex2f(x2, 0);
			gl.glTexCoord2f(s2, 0f);
			gl.glVertex2f(x2, y);
			gl.glTexCoord2f(s1, 0f);
			gl.glVertex2f(x, y);
		} else {
			gl.glTexCoord2f(s1, 0);
			gl.glVertex2f(0, x);
			gl.glTexCoord2f(s1, 1);
			gl.glVertex2f(y, x);
			gl.glTexCoord2f(s2, 1);
			gl.glVertex2f(y, x2);
			gl.glTexCoord2f(s2, 0);
			gl.glVertex2f(0, x2);
		}
	}

	public void update(List<Float> data) {
		if (texture == null)
			return;

		final int width = data.size();
		if (width <= 0) {
			setVisibility(EVisibility.HIDDEN);
			return;
		}

		float max;
		float min;
		float last;
		last = data.get(0);
		min = max = Math.abs(last);

		int center = -1;
		boolean multiCenter = false;
		for (int i = 1; i < width; ++i) {
			float v = data.get(i);
			float v_a = Math.abs(v);
			if (v_a > max)
				max = v_a;
			if (v_a < min)
				min = v_a;
			if (last < 0 && v > 0 && !multiCenter) {
				multiCenter = center >= 0; // already set a center -> multi center
				center = i - 1; // before me
			}
			last = v;
		}
		if (multiCenter)
			this.center = -2;
		else if (center != -1)
			this.center = center;//in the middle
		else if (last > 0) // all positive
			this.center = -1; //first
		else
			// all negative
			this.center = width-1; //last

		IDoubleFunction transform = DoubleFunctions.CLAMP01;
		// IDoubleFunction transform = ExpressionFunctions.compose(DoubleFunctions.CLAMP01,
		// DoubleFunctions.normalize(min, max));

		update(width, transform, data);
	}

	private void update(int width, IDoubleFunction transform, Iterable<Float> data) {
		if (width <= 0) {
			setVisibility(EVisibility.HIDDEN);
			return;
		} else
			setVisibility(EVisibility.VISIBLE);

		FloatBuffer buffer = FloatBuffer.allocate(width * 3); // w*rgb*float
		for (Float f : data) {
			float v = Math.abs(f.floatValue());
			v = (float) transform.apply(v);
			v = 1 - v; // invert color mapping
			buffer.put(v);
			buffer.put(v);
			buffer.put(v);
		}

		buffer.rewind();
		TextureData texData = new TextureData(GLProfile.getDefault(), GL.GL_RGB /* internalFormat */, width, 1,
				0 /* border */, GL.GL_RGB /* pixelFormat */, GL.GL_FLOAT /* pixelType */,
				false /* mipmap */,
				false /* dataIsCompressed */, false /* mustFlipVertically */, buffer, null);
		texture.updateImage(GLContext.getCurrentGL(), texData);
		texData.destroy();

		repaint();
	}

	/**
	 * @param clusterContentElement
	 */
	public void nonUniformLayout(ClusterContentElement clusterContentElement) {
		this.spaceProvider = clusterContentElement;
		spaceProvider.addRepaintOnRepaint(this);
		repaint();
	}

	/**
	 *
	 */
	public void uniformLayout() {
		if (spaceProvider != null)
			spaceProvider.removeRepaintOnRepaint(this);
		this.spaceProvider = null;
		repaint();
	}
}
