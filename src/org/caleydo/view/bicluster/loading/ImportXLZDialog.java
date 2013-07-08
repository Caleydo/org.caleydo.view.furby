/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.loading;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.DataSetDescription.ECreateDefaultProperties;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.datadomain.genetic.TCGADefinitions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Samuel Gratzl
 *
 */
public class ImportXLZDialog extends Dialog {

	private String xFile;
	private String lFile;
	private String zFile;
	private boolean genes = true;

	protected ImportXLZDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Import XLZ Data");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);
		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("X File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			final Text xFileUI = new Text(group, SWT.BORDER);
			xFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.widthHint = 200;
			xFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose X");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					xFile = onOpenFile(xFileUI);
					checkAllThere();
				}
			});
		}
		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("L File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			final Text xFileUI = new Text(group, SWT.BORDER);
			xFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.widthHint = 200;
			xFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose L");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					lFile = onOpenFile(xFileUI);
					checkAllThere();
				}
			});
		}

		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("Z File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			final Text xFileUI = new Text(group, SWT.BORDER);
			xFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.widthHint = 200;
			xFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose Z");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					zFile = onOpenFile(xFileUI);
					checkAllThere();
				}
			});
		}
		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("Options");
			group.setLayout(new GridLayout(1, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			final Button genesUI = new Button(group, SWT.CHECK);
			genesUI.setSelection(true);
			genesUI.setText("Row Names are valid Gene Symbols");
			genesUI.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					genes = genesUI.getSelection();
				}
			});
		}

		applyDialogFont(composite);
		return composite;
	}

	/**
	 *
	 */
	protected void checkAllThere() {
		boolean ok = xFile != null && lFile != null && zFile != null;
		getButton(IDialogConstants.OK_ID).setEnabled(ok);
	}

	protected String onOpenFile(Text xFileUI) {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setText("Open");
		// fileDialog.setFilterPath(filePath);
		String[] filterExt = { "*.csv" };
		fileDialog.setFilterExtensions(filterExt);

		String inputFileName = fileDialog.open();
		if (inputFileName == null)
			return null;
		xFileUI.setText(inputFileName);
		return inputFileName.replace('\\', '/');
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Import", true).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * @return
	 */
	public List<DataSetDescription> getDataSetDescriptions() {
		final IDSpecification geneNames = genes ? TCGADefinitions.createGeneIDSpecificiation() : new IDSpecification(
				"GENE_SAMPLES",
				"GENE_SAMPLES");
		final IDSpecification sample = new IDSpecification("SAMPLE", "SAMPLE");
		final IDSpecification bicluster = new IDSpecification("BICLUSTER", "BICLUSTER");

		List<DataSetDescription> r = new ArrayList<DataSetDescription>();

		String name = xFile.substring(xFile.lastIndexOf("/") + 1, xFile.lastIndexOf("."));
		{
			DataSetDescription x = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			x.setDataSourcePath(xFile);
			x.setDataSetName(name + "_X");
			x.setDelimiter("\t");
			x.setRowIDSpecification(geneNames);
			x.setColumnIDSpecification(sample);
			x.addParsingRule(createParsingRule());
			r.add(x);
		}
		{
			DataSetDescription l = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			l.setDataSourcePath(lFile);
			l.setDataSetName(name + "_L");
			l.setDelimiter("\t");
			l.setRowIDSpecification(geneNames);
			l.setColumnIDSpecification(bicluster);
			l.addParsingRule(createParsingRule());
			r.add(l);
		}
		{
			DataSetDescription z = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			z.setDataSourcePath(zFile);
			z.setDataSetName(name + "_Z");
			z.setDelimiter("\t");
			z.setRowIDSpecification(sample);
			z.setColumnIDSpecification(bicluster);
			z.addParsingRule(createParsingRule());
			r.add(z);
		}
		return r;
	}

	protected static ParsingRule createParsingRule() {
		ParsingRule r = new ParsingRule();
		r.setFromColumn(1);
		r.setParseUntilEnd(true);
		r.setColumnDescripton(new ColumnDescription());
		return r;
	}

}
