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

	float att, rep, board;

	public float getAttractionForce() {
		return att;
	}

	public float getRepulsionForce() {
		return rep;
	}

	public float getBoarderForce() {
		return board;
	}

	public ForceChangeEvent(float att, float rep, float board) {
		this.att = att;
		this.board = board;
		this.rep = rep;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
