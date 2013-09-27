/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.List;

/**
 * @author Samuel Gratzl
 *
 */
public interface ISortingStrategy {

	/**
	 * @return
	 */
	boolean needsResortAfterBandsUpdate();

	/**
	 * @param first
	 * @return
	 */
	List<IntFloat> apply(List<IntFloat> list);

}
