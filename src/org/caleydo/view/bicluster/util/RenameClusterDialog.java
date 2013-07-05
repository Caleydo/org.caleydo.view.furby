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
package org.caleydo.view.bicluster.util;

import java.util.Collection;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Samuel Gratzl
 *
 */
public class RenameClusterDialog extends TitleAreaDialog {

	private Text input;
	private ClusterElement cluster;

	private Collection<Integer> result;

	public RenameClusterDialog(Shell shell, ClusterElement clusterElement) {
		super(shell);
		this.cluster = clusterElement;
	}


	@Override
	public void create() {
		super.create();
		setTitle("Renaming cluster " + cluster.getID());
		setMessage("New Clustername:");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		
		Label l = new Label(parent, SWT.NONE);
		l.setText("Input: ");
		l.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		input = new Text(parent, SWT.BORDER | SWT.MULTI);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.heightHint = convertHeightInCharsToPixels(4);
		gd.horizontalSpan = 1;
		input.setLayoutData(gd);

		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.RETRY_ID, "Validate", false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.RETRY_ID) {
			validate();
		}
		super.buttonPressed(buttonId);
	}

	private boolean validate() {
		String name = input.getText();
		String[] lines = name.split("\n");
		if (lines.length > 1) {
			setErrorMessage("New name must only be one line long");
			return false;
		}
		name = lines[0];
		if (!(name.length() < 20)) {
			setErrorMessage("New name must only be 20 characters long");
			return false;
		}
		setMessage("Valid");
		return true;
	}

	@Override
	protected void okPressed() {
		if (!validate())
			return;
		EventPublisher.trigger(new ClusterRenameEvent(cluster, input));
		super.okPressed();
	}

	public static void open(final ClusterElement clusterElement) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				new RenameClusterDialog(new Shell(), clusterElement).open();
			}
		});
	}
}
