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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.view.bicluster.event.ChemicalClusterAddedEvent;
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
public class ImportChemicalClustersDialog extends TitleAreaDialog {
	protected IDType target;

	private Text input;
	private ComboViewer idType;

	private List<String> clusterList;
	private Map<Integer, String> elementToClusterMap;

	public ImportChemicalClustersDialog(Shell shell, IDType target) {
		super(shell);
		assert target.getDataType() == EDataType.INTEGER;
		this.target = target;
		clusterList = new ArrayList<>();
		elementToClusterMap = new HashMap<Integer, String>();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Adding chemical Clusters.");
		setMessage("Every cluster in a single line starting with its name followed by ':' and its elements, seperated by ',' ';' or ' '");
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
		idType.setSelection(new StructuredSelection(target.getIDCategory()
				.getHumanReadableIDType()));
		idType.getControl().setLayoutData(
				new GridData(SWT.LEFT, SWT.TOP, false, false));

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
		setErrorMessage(null);
		IDType source = (IDType) ((StructuredSelection) idType.getSelection())
				.getFirstElement();
		if (source == null)
			return false;
		String[] clusters = input.getText().split("\n");
		if (clusters.length == 0)
			return false;
		clusterList = new ArrayList<>();
		elementToClusterMap = new HashMap<>();
		for (String cluster : clusters) {
			StringBuilder errors = new StringBuilder();
			Collection<Integer> result;
			int index = cluster.indexOf(':');
			if (index < 0) {
				setErrorMessage("You have to specifiy the name for the chemical cluster followed by ':' and its elements");
				return false;
			} else if (index > 15) {
				setErrorMessage("Name " + cluster.substring(0,index) + " to long. Please use only 25 characters");
				return false;
			}
			String clusterName = cluster.substring(0, index);
			cluster = cluster.substring(index+1);
			String[] values = cluster.split("[,;\\t]");
			if (values.length < 1) {
				setErrorMessage("Every cluster needs at least one element");
				return false;
			} else {
				clusterList.add(clusterName); 
			}
			for (int i =0; i < values.length; i++) {
				values[i] = values[i].replaceAll("\\s","");  // remove whitespace
			}
			IIDTypeMapper<Object, Integer> mapper = IDMappingManagerRegistry
					.get().getIDMappingManager(target)
					.getIDTypeMapper(source, target);
			List<Object> input = new ArrayList<>();
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
				setErrorMessage("can't parse: "
						+ errors.substring(0, errors.length() - 2) + " to "
						+ source.getDataType());
				return false;
			}
			result = mapper.apply(input);
			if (result.isEmpty()) {
				setErrorMessage("can't map anything from " + clusterName);
				return false;
			}
			if (result == null || result.size() < input.size()) {
				setErrorMessage("can only map " + result.size() + " out of "
						+ input.size() + " from " + clusterName);
				return false;
			}
			for (Integer i : result) {
				elementToClusterMap.put(i, clusterName);
			}
		}
		setMessage("Valid");
		return true;
	}

	@Override
	protected void okPressed() {
		if (!validate())
			return;
		EventPublisher.trigger(new ChemicalClusterAddedEvent(clusterList, elementToClusterMap));
		super.okPressed();
	}

	public static void open(final IDType target) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				new ImportChemicalClustersDialog(new Shell(), target).open();
			}
		});
	}
}
