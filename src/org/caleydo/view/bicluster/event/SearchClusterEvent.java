/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * event during search of a cluster
 *
 * @author Samuel Gratzl
 *
 */
public class SearchClusterEvent extends AEvent {
	private String text;

	/**
	 *
	 */
	public SearchClusterEvent(String text) {
		this.text = text;
	}

	/**
	 * @return the text, see {@link #text}
	 */
	public String getText() {
		return text;
	}

	public boolean isClear() {
		return text == null;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
