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
	private final float membership;

	public IntFloat(int index, float membership) {
		this.index = index;
		this.membership = membership;
	}

	/**
	 * @return the index, see {@link #index}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the probability, see {@link #membership}
	 */
	public float getMembership() {
		return membership;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntFloat(").append(index).append('=').append(membership).append(')');
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + Float.floatToIntBits(membership);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntFloat other = (IntFloat) obj;
		if (index != other.index)
			return false;
		if (Float.floatToIntBits(membership) != Float.floatToIntBits(other.membership))
			return false;
		return true;
	}

	public final static Comparator<IntFloat> BY_MEMBERSHIP = new Comparator<IntFloat>() {
		@Override
		public int compare(IntFloat o1, IntFloat o2) {
			int val = Float.compare(o1.membership, o2.membership);
			if (val == 0) {
				if (o1.index == Integer.MIN_VALUE || o2.index == Integer.MIN_VALUE)
					return 0;
				val = o1.index - o2.index;
			}
			return val;
		}
	};

	public static final Function<IntFloat, Integer> TO_INDEX = new Function<IntFloat, Integer>() {
		@Override
		public Integer apply(IntFloat input) {
			return input == null ? null : input.getIndex();
		}
	};
	public static final Function<IntFloat, Float> TO_MEMBERSHIP = new Function<IntFloat, Float>() {
		@Override
		public Float apply(IntFloat input) {
			return input == null ? null : input.getMembership();
		}
	};
}