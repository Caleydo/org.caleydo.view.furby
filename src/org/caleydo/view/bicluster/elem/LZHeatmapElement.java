/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.FloatFunctions;
import org.caleydo.core.util.function.IFloatFunction;
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
	private final Table lOrz;
	private final boolean horizontal;
	private Texture texture;
	/**
	 * spacer for the focus case with non uniform cells
	 */
	private ClusterContentElement spaceProvider;

	public LZHeatmapElement(Table lOrz, boolean horizontal) {
		this.lOrz = lOrz;
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
		gl.glTranslatef(0, 0, g.z());
		gl.glScalef(w, h, 1.f);
		final int size = texture.getWidth();
		final float factor = 1.f / size;

		gl.glBegin(GL2GL3.GL_QUADS);
		if (spaceProvider == null || factor >= 1)
			rect(0, 1, 0, 1, gl);
		else {
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
			}
			rect(x * factor, (x + acc) * factor, sx * sfactor, (sx + sacc) * sfactor, gl);
		}
		gl.glEnd();
		g.checkError();
		texture.disable(gl);
		g.checkError();
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

	public void update(float threshold, int biClusterIndex, VirtualArray array) {
		if (texture == null)
			return;

		int width = array.size();
		if (width <= 0) {
			setVisibility(EVisibility.HIDDEN);
			return;
		} else
			setVisibility(EVisibility.VISIBLE);

		FloatBuffer buffer = FloatBuffer.allocate(width); // w*1*float

		// find max
		float max = Float.NEGATIVE_INFINITY;
		for (Integer index : array) {
			float v = lOrz.getRaw(biClusterIndex, index);
			v = Math.abs(v);
			max = Math.max(v, max);
		}

		IFloatFunction normalize = FloatFunctions.normalize(threshold, max);
		IFloatFunction clamp = FloatFunctions.CLAMP01;
		for (Integer index : array) {
			float v = lOrz.getRaw(biClusterIndex, index);
			v = Math.abs(v);
			v = normalize.apply(v);
			buffer.put(clamp.apply(v));
		}

		buffer.rewind();
		TextureData texData = new TextureData(GLProfile.getDefault(), GL2.GL_INTENSITY /* internalFormat */, width, 1,
				0 /* border */, GL.GL_LUMINANCE /* pixelFormat */, GL.GL_FLOAT /* pixelType */,
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
			spaceProvider.addRepaintOnRepaint(this);
		this.spaceProvider = null;
		repaint();
	}
}
