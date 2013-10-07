/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.internal.prefs;

import java.util.ArrayList;
import java.util.Collection;

import org.caleydo.core.gui.util.FontUtil;
import org.caleydo.core.gui.util.ScaleFieldEditor2;
import org.caleydo.core.gui.util.SpinnerFieldEditor;
import org.caleydo.view.bicluster.internal.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
/**
 * @author Samuel Gratzl
 *
 */
public class MyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Collection<Label> labels = new ArrayList<>();

	public MyPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		addField(new ScaleFieldEditor2("view.bicluster.aspectratio", "Initial Aspect Ratio W/H", parent, 25, 400, 5,
				100, ScaleFieldEditor2.PERCENT_FORMATTER));
		addField(new IntegerFieldEditor("view.bicluster.maxDistance", "Max Distance", parent));
		addFields(parent, "rec", "Record");
		addFields(parent, "dim", "Dimension");
	}

	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		int cols = ((GridLayout) (getFieldEditorParent().getLayout())).numColumns;
		for (Label label : labels)
			((GridData) label.getLayoutData()).horizontalSpan = cols;
	}

	private void addFields(final Composite parent, String suffix, String label) {
		Label l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labels.add(l);

		l = new Label(parent, SWT.NONE);
		l.setText(label);
		FontUtil.makeBold(l);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labels.add(l);

		addField(new ScaleFieldEditor2("view.bicluster.threshold." + suffix, "Threshold", parent, 0, 5000, 10, 100,
				ScaleFieldEditor2.PERCENT_FORMATTER));
		addField(new SpinnerFieldEditor("view.bicluster.top." + suffix, "Max # elements", parent, 0, Integer.MAX_VALUE,
				1, 10));
		addField(new BooleanFieldEditor("view.bicluster.showbands." + suffix, "Show Bands", parent));

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Default BiCluster View settings");
	}
}