/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;

/**
 * @author Samuel Gratzl
 *
 */
public interface ISortingStrategyFactory extends ILabeled {

	ISortingStrategy create(NormalClusterElement cluster, EDimension dim);
}
