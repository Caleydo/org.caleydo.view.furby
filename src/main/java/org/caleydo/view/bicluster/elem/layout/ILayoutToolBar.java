/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

/**
 * @author Samuel Gratzl
 *
 */
public interface ILayoutToolBar {

	/**
	 * @param name
	 *            name of this parameter
	 * @param label
	 * @param value
	 * @param min
	 * @param max
	 */
	void addSlider(String name, String label, float value, float min, float max);

}
