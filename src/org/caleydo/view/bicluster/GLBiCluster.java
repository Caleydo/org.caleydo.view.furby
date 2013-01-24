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
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
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


	public static String VIEW_TYPE = "org.caleydo.view.bicluster";

	public static String VIEW_NAME = "BiCluster Visualization";

	private BiClusterRenderStyle renderStyle;

	private List<TablePerspective> perspectives = new ArrayList<>();

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
		renderStyle = new BiClusterRenderStyle(viewFrustum);

		super.renderStyle = renderStyle;
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

	public ATableBasedDataDomain getDataDomainByName(String name) {
		for (ATableBasedDataDomain d : DataDomainManager.get().getDataDomainsByType(ATableBasedDataDomain.class)) {
			if (d.getLabel().equals(name))
				return d;
		}
		return null;
	}

	@Override
	public void display(GL2 gl) {

		ATableBasedDataDomain table_x = getDataDomainByName("ws1_X");
		Table X = table_x.getTable();

		ATableBasedDataDomain table_l = getDataDomainByName("ws1_L");
		Table L = table_l.getTable();

		ATableBasedDataDomain table_z = getDataDomainByName("ws1_Z");
		Table Z = table_z.getTable();

		Float value = Z.getRaw(0, 0);

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

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

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
	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		this.perspectives.addAll(newTablePerspectives);
	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return perspectives;
	}

	@Override
	public void removeTablePerspective(int tablePerspectiveID) {
		// TODO Auto-generated method stub

	}

}
