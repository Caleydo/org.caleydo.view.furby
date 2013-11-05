/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.internal.loading;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.DataSetDescription.ECreateDefaultProperties;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.core.io.gui.dataimport.widget.CategoricalDataPropertiesWidget;
import org.caleydo.core.startup.IStartupAddon;
import org.caleydo.core.startup.IStartupProcedure;
import org.caleydo.datadomain.genetic.TCGADefinitions;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kohsuke.args4j.Option;

/**
 * @author Samuel Gratzl
 *
 */
public class BiClusterStartupAddon implements IStartupAddon {
	@Option(name = "-bicluster:X", usage = "specify the bicluster x data file")
	private File xFile;
	@Option(name = "-bicluster:L", usage = "specify the bicluster L data file")
	private File lFile;
	@Option(name = "-bicluster:Z", usage = "specify the bicluster Z data file")
	private File zFile;
	@Option(name = "-bicluster:chemical", usage = "specify the bicluster chemical cluster file")
	private File chemicalFile;
	@Option(name = "-bicluster:thresholds", usage = "specify the bicluster thresholds fiel")
	private File thresholdsFile;
	@Option(name = "-bicluster:genes", usage = "whether the record names are valid gene symbols")
	private boolean genes = false;

	private Text lFileUI;
	private Text zFileUI;
	private Text chemicalFileUI;
	private Text thresholdsFileUI;
	private Button specifyCategoriesUI;
	private CategoricalClassDescription<String> chemicalProperties;

	@Override
	public boolean init() {
		if (validate())
			return true;
		if (isValid(xFile))
			inferFromX();
		if (validate())
			return true;
		return false;
	}

	private static boolean isValid(File f) {
		return f != null && f.isFile() && f.exists();
	}

	@Override
	public Composite create(Composite parent, final WizardPage page, final Listener changeListener) {
		// create composite
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(1, true));
		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("X File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

			final Text xFileUI = new Text(group, SWT.BORDER);
			xFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 200;
			xFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose X");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					xFile = onOpenFile(xFileUI);
					checkAllThere(page, changeListener);
					// infer the other
					if (isValid(xFile))
						inferFromX();
					if (isValid(lFile))
						lFileUI.setText(lFile.getAbsolutePath());
					if (isValid(zFile))
						zFileUI.setText(zFile.getAbsolutePath());
					if (isValid(chemicalFile)) {
						chemicalFileUI.setText(chemicalFile.getAbsolutePath());
						specifyCategoriesUI.setEnabled(true);
					}
					if (isValid(thresholdsFile))
						thresholdsFileUI.setText(thresholdsFile.getAbsolutePath());
					checkAllThere(page, changeListener);
				}
			});
		}
		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("L File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

			this.lFileUI = new Text(group, SWT.BORDER);
			lFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 200;
			lFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose L");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					lFile = onOpenFile(lFileUI);
					checkAllThere(page, changeListener);
				}
			});
		}

		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("Z File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

			this.zFileUI = new Text(group, SWT.BORDER);
			zFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 200;
			zFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose Z");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					zFile = onOpenFile(zFileUI);
					checkAllThere(page, changeListener);
				}
			});
		}

		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("Chemical Clustering File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

			this.chemicalFileUI = new Text(group, SWT.BORDER);
			chemicalFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 200;
			chemicalFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					chemicalFile = onOpenFile(chemicalFileUI);
					if (chemicalFile != null)
						specifyCategoriesUI.setEnabled(true);
					checkAllThere(page, changeListener);
				}
			});

			specifyCategoriesUI = new Button(group, SWT.PUSH);
			specifyCategoriesUI.setText("Set Properties");
			gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.horizontalSpan = 2;
			specifyCategoriesUI.setLayoutData(gridData);
			specifyCategoriesUI.setEnabled(false);
			specifyCategoriesUI.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onSpecifyChemicalCategories();
				}
			});
		}

		{
			Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
			group.setText("Thresholds File");
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

			this.thresholdsFileUI = new Text(group, SWT.BORDER);
			thresholdsFileUI.setEditable(false);
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 200;
			thresholdsFileUI.setLayoutData(gridData);

			Button openFileButton = new Button(group, SWT.PUSH);
			openFileButton.setText("Choose");
			openFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					thresholdsFile = onOpenFile(thresholdsFileUI);
					checkAllThere(page, changeListener);
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
		{
			Composite rest = new Composite(parent, SWT.None);
			rest.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		return parent;
	}

	/**
	 *
	 */
	protected void onSpecifyChemicalCategories() {
		if (this.chemicalFile == null)
			return;
		ChemicalProperties p = new ChemicalProperties(null, chemicalFile, this.chemicalProperties);
		p.setBlockOnOpen(true);
		if (p.open() == Window.OK) {
			this.chemicalProperties = p.result;
		}
	}


	/**
	 * @param changeListener
	 * 
	 */
	protected void checkAllThere(WizardPage page, Listener changeListener) {
		boolean ok = validate();
		page.setPageComplete(ok);
		changeListener.handleEvent(null); // fake it
	}

	protected File onOpenFile(Text xFileUI) {
		FileDialog fileDialog = new FileDialog(xFileUI.getShell());
		fileDialog.setText("Open");
		// fileDialog.setFilterPath(filePath);
		String[] filterExt = { "*.csv" };
		fileDialog.setFilterExtensions(filterExt);
		fileDialog.setFileName(xFileUI.getText());

		String inputFileName = fileDialog.open();
		if (inputFileName == null)
			return StringUtils.isBlank(xFileUI.getText()) ? null : new File(xFileUI.getText());
		xFileUI.setText(StringUtils.defaultString(inputFileName));
		return new File(inputFileName);
	}

	/**
	 * @return
	 */
	public List<DataSetDescription> toDataSetDescriptions() {
		final IDSpecification geneNames = genes ? TCGADefinitions.createGeneIDSpecificiation() : new IDSpecification(
				"GENE_SAMPLES", "GENE_SAMPLES");
		final IDSpecification sample = new IDSpecification("SAMPLE", "SAMPLE");
		final IDSpecification bicluster = new IDSpecification("BICLUSTER", "BICLUSTER");

		List<DataSetDescription> r = new ArrayList<DataSetDescription>();

		String name = getProjectName();
		{
			DataSetDescription x = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			x.setDataSourcePath(xFile.getAbsolutePath());
			x.setDataSetName(name + "_X");
			x.setDelimiter("\t");
			x.setRowIDSpecification(geneNames);
			x.setColumnIDSpecification(sample);
			x.addParsingRule(createParsingRule());
			r.add(x);
		}
		{
			DataSetDescription l = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			l.setDataSourcePath(lFile.getAbsolutePath());
			l.setDataSetName(name + "_L");
			l.setDelimiter("\t");
			l.setRowIDSpecification(geneNames);
			l.setColumnIDSpecification(bicluster);
			l.addParsingRule(createParsingRule());
			r.add(l);
		}
		{
			DataSetDescription z = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			z.setDataSourcePath(zFile.getAbsolutePath());
			z.setDataSetName(name + "_Z");
			z.setDelimiter("\t");
			z.setRowIDSpecification(sample);
			z.setColumnIDSpecification(bicluster);
			z.addParsingRule(createParsingRule());
			r.add(z);
		}

		if (isValid(chemicalFile)) {
			DataSetDescription c = new DataSetDescription();
			c.setDataSourcePath(chemicalFile.getAbsolutePath());
			c.setDataSetName(name + "_ChemicalClusters");
			c.setDelimiter("\t");
			c.setRowIDSpecification(sample);
			c.setColumnIDSpecification(createDummy(c.getDataSetName()));
			ParsingRule p = new ParsingRule();
			p.setFromColumn(1);
			p.setToColumn(2);
			final ColumnDescription desc = new ColumnDescription(new DataDescription(EDataClass.CATEGORICAL, EDataType.STRING));
			desc.getDataDescription().setCategoricalClassDescription(chemicalProperties);
			p.setColumnDescripton(desc);
			c.addParsingRule(p);
			r.add(c);
		}

		if (isValid(thresholdsFile)) {
			DataSetDescription t = new DataSetDescription();
			t.setDataSourcePath(thresholdsFile.getAbsolutePath());
			t.setDataSetName(name + "_Thresholds");
			t.setDelimiter("\t");
			t.setRowIDSpecification(bicluster);
			t.setColumnIDSpecification(createDummy(t.getDataSetName()));
			for (int i = 1; i < 3; ++i) {
				ParsingRule p = new ParsingRule();
				p.setFromColumn(i);
				p.setToColumn(i + 1);
				p.setColumnDescripton(new ColumnDescription(
						new DataDescription(EDataClass.REAL_NUMBER, EDataType.FLOAT)));
				t.addParsingRule(p);
			}
			r.add(t);
		}
		return r;
	}

	private String getProjectName() {
		String xFileName = xFile.getName();
		String name = xFileName.substring(0, xFileName.lastIndexOf("."));
		name = StringUtils.removeEnd(name, "_X");
		return name;
	}

	/**
	 * @param dataSetName
	 * @return
	 */
	private IDSpecification createDummy(String dataSetName) {
		IDSpecification columnIDSpecification = new IDSpecification();
		IDCategory idCategory = IDCategory.registerCategoryIfAbsent(dataSetName + "_column");
		idCategory.setInternalCategory(true);
		IDType idType = IDType.registerType(dataSetName + "_column", idCategory, EDataType.STRING);
		columnIDSpecification.setIDSpecification(idCategory.getCategoryName(), idType.getTypeName());
		return columnIDSpecification;
	}

	protected static ParsingRule createParsingRule() {
		ParsingRule r = new ParsingRule();
		r.setFromColumn(1);
		r.setParseUntilEnd(true);
		r.setColumnDescripton(new ColumnDescription());
		return r;
	}

	@Override
	public boolean validate() {
		return isValid(xFile) && isValid(lFile) && isValid(zFile);
	}

	/**
	 */
	private void inferFromX() {
		final File p = xFile.getParentFile();
		final String base = StringUtils.removeEnd(xFile.getName(), "_X.csv");
		if (!isValid(xFile))
			lFile = new File(p, base + "_X.csv");
		if (!isValid(lFile))
			lFile = new File(p, base + "_L.csv");
		if (!isValid(zFile))
			zFile = new File(p, base + "_Z.csv");
		if (!isValid(chemicalFile))
			chemicalFile = new File(p, base + "_chemicalClusters.csv");
		if (!isValid(thresholdsFile))
			thresholdsFile = new File(p, base + "_thresholds.csv");
	}

	@Override
	public IStartupProcedure create() {
		return new LoadBiClusterStartupProcedure(getProjectName(), toDataSetDescriptions());
	}

	private class ChemicalProperties extends Dialog {
		private CategoricalDataPropertiesWidget widget;
		private CategoricalClassDescription<String> result;
		private final File inputFile;
		private CategoricalClassDescription<String> old;

		/**
		 * @param parentShell
		 * @param old
		 */
		protected ChemicalProperties(Shell parentShell, File inputFile, CategoricalClassDescription<String> old) {
			super(parentShell);
			this.inputFile = inputFile;
			this.old = old;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			List<List<String>> data = parseFile(inputFile);
			widget = new CategoricalDataPropertiesWidget(parent);
			if (old != null)
				widget.updateCategories(data, 1, old);
			else
				widget.updateCategories(data, 1);

			return parent;
		}

		@Override
		protected void okPressed() {
			this.result = widget.getCategoricalClassDescription();
			super.okPressed();
		}

		/**
		 * @param inputFile2
		 * @return
		 */
		private List<List<String>> parseFile(File f) {
			if (!f.exists() || !f.isFile())
				return Collections.emptyList();
			List<List<String>> r = new ArrayList<>();
			try {
				List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));
				for (String line : lines.subList(1, lines.size())) {
					r.add(Arrays.asList(line.split("\t")));
				}
			} catch (IOException e) {
				ErrorDialog.openError(getParentShell(), "Can't load file", "Can't load file: " + f, new Status(
						IStatus.ERROR, "Caleydo", "Can't load file: " + f, e));

			}
			return r;
		}

	}

}
