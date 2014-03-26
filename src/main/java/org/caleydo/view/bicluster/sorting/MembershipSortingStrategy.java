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
public class MembershipSortingStrategy extends AComposeAbleSortingStrategy {
	public static final ISortingStrategyFactory FACTORY_INC = new ConstantSortingStrategyFactory(
			new MembershipSortingStrategy(true, false), "Membership");
	public static final ISortingStrategyFactory FACTORY_DEC = new ConstantSortingStrategyFactory(
			new MembershipSortingStrategy(false, false), "Membership (Decreasing)");
	public static final ISortingStrategyFactory FACTORY_INC_ABS = new ConstantSortingStrategyFactory(
			new MembershipSortingStrategy(true, true), "Abs. Membership");
	public static final ISortingStrategyFactory FACTORY_DEC_ABS = new ConstantSortingStrategyFactory(
			new MembershipSortingStrategy(false, true), "Abs. Membership (Decreasing)");

	private final boolean increasing;
	private final boolean absolute;

	private MembershipSortingStrategy(boolean increasing, boolean absolute) {
		this.increasing = increasing;
		this.absolute = absolute;
	}

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		final float a = absolute ? Math.abs(o1.getMembership()) : o1.getMembership();
		final float b = absolute ? Math.abs(o2.getMembership()) : o2.getMembership();
		return increasing ? Float.compare(a, b) : Float.compare(b, a);
	}

}
