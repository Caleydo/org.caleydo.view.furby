/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.Comparator;

import com.google.common.base.Function;

public final class IntFloat {
	private final int index;
	private final float probability;

	public IntFloat(int index, float probability) {
		this.index = index;
		this.probability = probability;
	}

	/**
	 * @return the index, see {@link #index}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the probability, see {@link #probability}
	 */
	public float getProbability() {
		return probability;
	}

	public final static Comparator<IntFloat> BY_ABS_PROBABILITY = new Comparator<IntFloat>() {
		@Override
		public int compare(IntFloat o1, IntFloat o2) {
			// the large t
			int val = Float.compare(Math.abs(o1.probability), Math.abs(o2.probability));
			if (val == 0)
				val = o1.index - o2.index;
			return val;
		}
	};

	public final static Comparator<IntFloat> BY_PROBABILITY = new Comparator<IntFloat>() {
		@Override
		public int compare(IntFloat o1, IntFloat o2) {
			int val = Float.compare(o1.probability, o2.probability);
			if (val == 0)
				val = o1.index - o2.index;
			return val;
		}
	};

	public static final Function<IntFloat, Integer> TO_INDEX = new Function<IntFloat, Integer>() {
		@Override
		public Integer apply(IntFloat input) {
			return input == null ? null : input.getIndex();
		}
	};
	public static final Function<IntFloat, Float> TO_PROBABILITY = new Function<IntFloat, Float>() {
		@Override
		public Float apply(IntFloat input) {
			return input == null ? null : input.getProbability();
		}
	};
}