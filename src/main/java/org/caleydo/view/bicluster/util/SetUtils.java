/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.util;

import java.util.Collection;

/**
 * @author Samuel Gratzl
 *
 */
public class SetUtils {
	/**
	 * @param elements
	 * @return
	 */
	public static boolean containsAny(Collection<Integer> set, Iterable<Integer> elements) {
		if (set == null)
			return false;
		for (Integer elem : elements)
			if (set.contains(elem))
				return true;
		return false;
	}

}
