/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;

/**
 * @author Samuel Gratzl
 *
 */
public interface IBiClusterLayout extends IGLLayout {
	void addDelta(int deltaTimeMs);
}
