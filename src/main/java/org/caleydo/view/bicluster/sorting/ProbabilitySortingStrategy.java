/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;


/**
 * @author Samuel Gratzl
 *
 */
public class ProbabilitySortingStrategy extends AComposeAbleSortingStrategy {
	public static final ISortingStrategyFactory FACTORY_INC = new ConstantSortingStrategyFactory(
			new ProbabilitySortingStrategy(true, false), "Probability");
	public static final ISortingStrategyFactory FACTORY_DEC = new ConstantSortingStrategyFactory(
			new ProbabilitySortingStrategy(false, false), "Probability (Decreasing)");
	public static final ISortingStrategyFactory FACTORY_INC_ABS = new ConstantSortingStrategyFactory(
			new ProbabilitySortingStrategy(true, true), "Abs. Probability");
	public static final ISortingStrategyFactory FACTORY_DEC_ABS = new ConstantSortingStrategyFactory(
			new ProbabilitySortingStrategy(false, true), "Abs. Probability (Decreasing)");

	private final boolean increasing;
	private final boolean absolute;

	private ProbabilitySortingStrategy(boolean increasing, boolean absolute) {
		this.increasing = increasing;
		this.absolute = absolute;
	}

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		final float a = absolute ? Math.abs(o1.getProbability()) : o1.getProbability();
		final float b = absolute ? Math.abs(o2.getProbability()) : o2.getProbability();
		return increasing ? Float.compare(a, b) : Float.compare(b, a);
	}

}
