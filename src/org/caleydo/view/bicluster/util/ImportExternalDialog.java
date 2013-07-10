/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.view.bicluster.event.SpecialClusterAddedEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
public class ImportExternalDialog extends TitleAreaDialog {
	protected IDType target;

	private Text input;
	private ComboViewer idType;

	private Collection<Integer> result;

	public ImportExternalDialog(Shell shell, IDType target) {
		super(shell);
		assert target.getDataType() == EDataType.INTEGER;
		this.target = target;
	}


	@Override
	public void create() {
		super.create();
		setTitle("Adding special " + target.getIDCategory()
				+ " elements: one of COMMA(,),TAB or SEMICOLON(;) as a seperator");
		setMessage("Enter data");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label l = new Label(parent, SWT.NONE);
		l.setText("Input Type: ");
		l.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		idType = new ComboViewer(parent, SWT.READ_ONLY);
		idType.setContentProvider(ArrayContentProvider.getInstance());
		idType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IDType) element).getTypeName();
			}
		});
		idType.setInput(target.getIDCategory().getPublicIdTypes());
		idType.setSelection(new StructuredSelection(target.getIDCategory().getHumanReadableIDType()));
		idType.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		l = new Label(parent, SWT.NONE);
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

	protected boolean validate() {
		IDType source = (IDType) ((StructuredSelection) idType.getSelection()).getFirstElement();
		if (source == null)
			return false;
		String[] values = input.getText().split("[,;\\t]");
		if (values.length == 0)
			return false;
		IIDTypeMapper<Object, Integer> mapper = IDMappingManagerRegistry.get().getIDMappingManager(target)
				.getIDTypeMapper(source, target);
		List<Object> input = new ArrayList<>();
		StringBuilder errors = new StringBuilder();
		switch (source.getDataType()) {
		case FLOAT:
			for (String v : values) {
				try {
					input.add(Float.parseFloat(v));
				} catch (NumberFormatException e) {
					errors.append(v).append(", ");
				}
			}
			break;
		case INTEGER:
			for (String v : values) {
				try {
					input.add(Integer.parseInt(v));
				} catch (NumberFormatException e) {
					errors.append(v).append(", ");
				}
			}
			break;
		case STRING:
			input.addAll(Arrays.asList(values));
			break;
		}
		if (errors.length() > 0) {
			setErrorMessage("can't parse: " + errors.substring(0, errors.length() - 2) + " to " + source.getDataType());
			return false;
		}
		this.result = mapper.apply(input);
		if (this.result.isEmpty()) {
			setErrorMessage("can't map anything");
			return false;
		}
		if (this.result == null || this.result.size() < input.size()) {
			setErrorMessage("can only map " + result.size() + " out of " + input.size());
			return false;
		}
		setMessage("Valid");
		return true;
	}

	@Override
	protected void okPressed() {
		if (!validate())
			return;
		EventPublisher.trigger(new SpecialClusterAddedEvent(new ArrayList<>(result), false));
		super.okPressed();
	}


	public static void open(final IDType target) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				new ImportExternalDialog(new Shell(), target).open();
			}
		});
	}
}
