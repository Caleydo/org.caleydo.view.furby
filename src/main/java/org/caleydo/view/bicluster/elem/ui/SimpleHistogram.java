/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.ui;

import java.util.Iterator;

import org.caleydo.core.util.function.IDoubleSizedIterator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

import com.google.common.primitives.Ints;

/**
 * @author Samuel Gratzl
 *
 */
public class SimpleHistogram implements Iterable<Integer> {
	/**
	 * the hist
	 */
	private final int[] bins;
	/**
	 * number of nans
	 */
	private int nans = 0;
	/**
	 * the largest bin value
	 */
	private int largestValue;
	/**
	 * the total number of items
	 */
	private int count;

	public SimpleHistogram(int bins) {
		this.bins = new int[bins];
		largestValue = 0;
	}

	/**
	 * @return the number of bins
	 */
	public int size() {
		return bins.length;
	}

	/**
	 * returns the bin of the given value or -1 if it is a NaN
	 *
	 * @param value
	 * @return
	 */
	public int getBinOf(double value) {
		if (Double.isNaN(value))
			return -1;
		return (int) Math.round(value * (bins.length - 1));
	}

	/**
	 * add the given normalized value to this histogram
	 *
	 * @param value
	 */
	public void add(double value) {
		if (Double.isNaN(value)) {
			nans++;
			return;
		}
		int bin = (int) Math.round(value * (bins.length - 1));
		if (bin < 0)
			bin = 0;
		if (bin >= bins.length)
			bin = bins.length - 1;
		bins[bin]++;
		count++;
		if (bins[bin] > largestValue)
			largestValue = bins[bin];
	}

	/**
	 * @return the count, see {@link #count}
	 */
	public int getCount(boolean includeNaN) {
		return count + (includeNaN ? nans : 0);
	}

	/**
	 * returns the number of NaN entries
	 *
	 * @return
	 */
	public int getNaN() {
		return nans;
	}

	/**
	 * returns the largest value of this histogram
	 *
	 * @param includeNaN
	 *            should also be NaN values be considered
	 * @return
	 */
	public int getLargestValue(boolean includeNaN) {
		if (includeNaN && nans > largestValue)
			return nans;
		return largestValue;
	}

	public int get(int bin) {
		return bins[bin];
	}

	@Override
	public Iterator<Integer> iterator() {
		return Ints.asList(bins).iterator();
	}

	public static int binsForWidth(int dataSize) {
		return Math.round((float) Math.sqrt(dataSize));
	}

	/**
	 * @param iterator
	 * @return
	 */
	public static SimpleHistogram of(IDoubleSizedIterator it, boolean useAbs) {
		final int bins = binsForWidth(it.size());
		SimpleHistogram h = new SimpleHistogram(bins);
		if (useAbs) {
			while (it.hasNext()) {
				double v = Math.abs(it.nextPrimitive());
				if (v == 0) // skip 0
					continue;
				h.add(v);
			}
		} else {
			while (it.hasNext()) {
				h.add(it.nextPrimitive());
			}
		}
		return h;
	}

	/**
	 * @param g
	 * @param w
	 * @param h
	 */
	public void render(GLGraphics g, float w, float h) {
		w -= 2;
		float factor = (h - 2) / getLargestValue(false);
		final int size = size();
		float delta = w / size;
		g.save();

		final float lineWidth = Math.min(delta - 1, 25);
		final float lineWidthHalf = lineWidth * 0.5f;
		float x = 1 + delta / 2;
		g.move(0, h - 1);
		for (int i = 0; i < size; ++i) {
			float v = -get(i) * factor;

			if (v <= -1) {
				g.fillRect(x - lineWidthHalf, 0, lineWidth, v);
			}
			x += delta;
		}
		g.restore();
	}
}
