/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
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
		if (!(name.length() > 0)) {
			setErrorMessage("New name must at least contain one character");
			return false;
		}
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
