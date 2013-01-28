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
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.event.EventListenerManagers;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.listener.AddTablePerspectivesEvent;
import org.caleydo.core.view.listener.AddTablePerspectivesListener;
import org.caleydo.core.view.listener.RemoveTablePerspectiveEvent;
import org.caleydo.core.view.listener.RemoveTablePerspectiveListener;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.view.bicluster.renderstyle.BiClusterRenderStyle;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Sample GL2 view.
 * </p>
 * <p>
 * This Template is derived from {@link ATableBasedView}, but if the view does
 * not use a table, changing that to {@link AGLView} is necessary.
 * </p>
 *
 * @author Marc Streit
 */

public class GLBiCluster extends AGLView implements IMultiTablePerspectiveBasedView {
	public static final String VIEW_TYPE = "org.caleydo.view.bicluster";
	public static final String VIEW_NAME = "BiCluster Visualization";

	private final EventListenerManager listeners = EventListenerManagers.wrap(this);

	private List<TablePerspective> perspectives = new ArrayList<>();
	private TablePerspective X; // gene x sample
	private TablePerspective L; // gene x bicluster
	private TablePerspective Z; // sample x bicluster
	private BiClusterRenderStyle renderStyle;

	/**
	 * Constructor.
	 *
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLBiCluster(IGLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);
	}

	@Override
	public void init(GL2 gl) {
		displayListIndex = gl.glGenLists(1);
		this.renderStyle = new BiClusterRenderStyle(viewFrustum);
		detailLevel = EDetailLevel.HIGH;
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		// Register keyboard listener to GL2 canvas
		glParentView.getParentComposite().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				glParentView.getParentComposite().addKeyListener(glKeyListener);
			}
		});

		this.glMouseListener = glMouseListener;

		init(gl);
	}

	@Override
	public void displayLocal(GL2 gl) {
		pickingManager.handlePicking(this, gl);
		display(gl);
		if (busyState != EBusyState.OFF) {
			renderBusyMode(gl);
		}

	}

	@Override
	public void displayRemote(GL2 gl) {
		display(gl);
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	/**
	 * determines which of the given {@link TablePerspective} is L and Z, given the known X table
	 *
	 * @param a
	 * @param b
	 */
	private void findLZ(TablePerspective a, TablePerspective b) {
		// row: gene, row: gene
		if (a.getDataDomain().getRecordIDCategory().equals(this.X.getDataDomain().getRecordIDCategory())) {
			this.L = a;
			this.Z = b;
		} else {
			this.L = b;
			this.Z = a;
		}
	}

	/**
	 * infers the X,L,Z tables from the dimension and record ID categories, as L and Z has the same Dimension ID
	 * Category, the remaining one will be X
	 */
	private void findXLZ() {
		TablePerspective a = perspectives.get(0);
		TablePerspective b = perspectives.get(1);
		TablePerspective c = perspectives.get(2);

		IDCategory a_d = a.getDataDomain().getDimensionIDCategory();
		IDCategory b_d = b.getDataDomain().getDimensionIDCategory();
		IDCategory c_d = c.getDataDomain().getDimensionIDCategory();

		if (a_d.equals(b_d)) {
			this.X = c;
			findLZ(a, b);
		} else if (a_d.equals(c_d)) {
			this.X = b;
			findLZ(a, c);
		} else {
			this.X = a;
			findLZ(b, c);
		}
	}

	@Override
	public void display(GL2 gl) {
		if (Z != null) {
			Float value = Z.getDataDomain().getTable().getRaw(0, 0);
		}

		//samples
		// Perspective sample = new Perspective(table_x, table_x.getDimensionIDType());
		// PerspectiveInitializationData init = new PerspectiveInitializationData();
		// init.setData(indices);
		// sample.init(init);
		// table_x.getTable().registerDimensionPerspective(dimensionPerspective)
		// table_x.getTablePerspective(recordPerspectiveID, dimensionPerspectiveID);


		// TODO: IMPLEMENT GL2 STUFF

		gl.glBegin(GL2.GL_QUADS);
		gl.glColor3f(0, 1, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 1, 0);
		gl.glVertex3f(1, 1, 0);
		gl.glVertex3f(1, 0, 0);
		gl.glEnd();

		checkForHits(gl);
	}

	@Override
	protected void handlePickingEvents(PickingType pickingType, PickingMode pickingMode,
			int externalID, Pick pick) {

		// TODO: Implement picking processing here!
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedBiClusterView serializedForm = new SerializedBiClusterView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "BiCluster";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		listeners.register(this);
		listeners.register(AddTablePerspectivesEvent.class, new AddTablePerspectivesListener().setHandler(this));
		listeners.register(RemoveTablePerspectiveEvent.class, new RemoveTablePerspectiveListener().setHandler(this));
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
		listeners.unregisterAll();
	}

	@Override
	protected void destroyViewSpecificContent(GL2 gl) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.tableBased;
	}

	@Override
	public void addTablePerspective(TablePerspective newTablePerspective) {
		this.perspectives.add(newTablePerspective);
		if (perspectives.size() == 3)
			findXLZ();
	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		this.perspectives.addAll(newTablePerspectives);
		if (perspectives.size() == 3)
			findXLZ();
	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return perspectives;
	}

	@Override
	public void removeTablePerspective(int tablePerspectiveID) {
		for (Iterator<TablePerspective> it = perspectives.iterator(); it.hasNext();) {
			if (it.next().getID() == tablePerspectiveID)
				it.remove();
		}
		if (this.perspectives.size() < 3) {
			this.X = this.L = this.Z = null;
		}
	}

}
