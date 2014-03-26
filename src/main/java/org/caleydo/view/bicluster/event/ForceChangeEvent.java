/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author Michael Gillhofer
 *
 */
public class ForceChangeEvent extends AEvent {

	private final String name;
	private final float value;

	public ForceChangeEvent(String name, float value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name, see {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value, see {@link #value}
	 */
	public float getValue() {
		return value;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
