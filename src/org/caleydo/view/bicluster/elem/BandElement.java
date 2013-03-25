/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.awt.geom.Point2D;
import java.util.List;

import javax.media.opengl.GLContext;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.SelectionUpdateEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;

/**
 * @author user
 *
 */
public abstract class BandElement extends PickableGLElement implements IEventBasedSelectionManagerUser {

	protected static ConnectionBandRenderer bandRenderer = new ConnectionBandRenderer();
	protected static EventPublisher eventPublisher;


	{
		bandRenderer.init(GLContext.getCurrentGL().getGL2());
		eventPublisher = GeneralManager.get().getEventPublisher();
	}

	protected boolean highlight = false;
	protected ClusterElement first;
	protected ClusterElement second;
	protected Iterable<Integer> overlap;
	protected IDType idType;
	protected String dataDomainID;
	protected SelectionType selectionType;
	protected IDCategory idCategory;
	protected EventBasedSelectionManager selectionManager;

	protected List<Pair<Point2D, Point2D>> points;



	/**
	 *
	 */
	public BandElement(GLElement first, GLElement second) {
		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;
		dataDomainID = ((ClusterElement) first).getDataDomainID();

	}

	public void selectElements() {
		if (selectionManager == null)
			return;
		for (Integer id : overlap) {
			if (highlight)
				selectionManager.addToType(selectionType, idType, id);
			else
				selectionManager.removeFromType(selectionType, id);
		}
		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(this);
		SelectionDelta delta = selectionManager.getDelta();
		event.setSelectionDelta(delta);
		event.setEventSpace(dataDomainID);
		eventPublisher.triggerEvent(event);
	}

	public abstract void updatePosition();
}
