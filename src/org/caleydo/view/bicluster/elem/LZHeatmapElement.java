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
import org.caleydo.core.util.function.ExpressionFunctions;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;

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
		g.color(Color.GRAY).drawRect(0, 0, w, h);
		GL2 gl = g.gl;
		texture.enable(gl);
		texture.bind(gl);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		g.color(Color.WHITE);
		g.save();
		gl.glTranslatef(0, 0, g.z());
		gl.glScalef(w, h, 1.f);
		final int size = texture.getWidth();
		final float factor = 1.f / size;

		float centerPos = 0;
		gl.glBegin(GL2GL3.GL_QUADS);
		if (spaceProvider == null || factor >= 1) {
			rect(0, 1, 0, 1, gl);
			centerPos = center > 0 ? center * factor : 0;
		} else {
			int x = 0;
			int acc = 0;
			float slast = Float.NaN;
			float sfactor = 1.f / (horizontal ? w : h);
			float sx = 0;
			float sacc = 0;
			for (int i = 0; i < size; ++i) {
				float si = (horizontal ? spaceProvider.getDimensionCell(i) : spaceProvider.getRecordCell(i)).getSize();
				if (Float.isNaN(slast) || Math.abs(si - slast) < 0.3) {
					// uniform continue
				} else { // flush intermediate rect
					rect(x * factor, (x + acc) * factor, sx * sfactor, (sx + sacc) * sfactor, gl);
					x += acc;
					sx += sacc;
					acc = 0;
					sacc = 0;
				}
				slast = si;
				acc++;
				sacc += si;
				if (i == center) {
					centerPos = (sx + sacc) * sfactor;
				}
			}
			rect(x * factor, (x + acc) * factor, sx * sfactor, (sx + sacc) * sfactor, gl);
		}
		gl.glEnd();
		g.checkError();
		texture.disable(gl);
		g.checkError();

		// System.out.println(center + " " + centerPos);
		if (center != -2) {
			g.lineWidth(3).color(Color.BLUE);
			if (horizontal)
				g.drawLine(centerPos, 0, centerPos, 1);
			else
				g.drawLine(0, centerPos, 1, centerPos);
			g.lineWidth(1);
		}

		g.restore();
	}

	private void rect(float s1, float s2, float x, float x2, GL2 gl) {
		if (horizontal) {
			gl.glTexCoord2f(s1, 1f);
			gl.glVertex2f(x, 0);
			gl.glTexCoord2f(s2, 1f);
			gl.glVertex2f(x2, 0);
			gl.glTexCoord2f(s2, 0f);
			gl.glVertex2f(x2, 1);
			gl.glTexCoord2f(s1, 0f);
			gl.glVertex2f(x, 1);
		} else {
			gl.glTexCoord2f(s1, 0);
			gl.glVertex2f(0, x);
			gl.glTexCoord2f(s1, 1);
			gl.glVertex2f(1, x);
			gl.glTexCoord2f(s2, 1);
			gl.glVertex2f(1, x2);
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
				center = i;
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

		IDoubleFunction transform = ExpressionFunctions.compose(DoubleFunctions.CLAMP01,
				DoubleFunctions.normalize(min, max));

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
