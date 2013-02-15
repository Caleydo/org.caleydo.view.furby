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
package org.caleydo.view.bicluster.view;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.view.bicluster.event.ToolbarEvent;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

/**
 * @author Michael Gillhofer
 *
 */
public class Toolbar extends ControlContribution {

	public Toolbar() {
		super("Trend Highlight Mode");
	}

	@Override
	protected Control createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		RowLayout layout = new RowLayout();
		composite.setLayout(layout);

		final Label sampleLabel = new Label(composite, SWT.HORIZONTAL);
		sampleLabel.setLayoutData(new RowData(200, 20));

		final Slider sampleThrSpinner = new Slider(composite, SWT.HORIZONTAL);
		sampleThrSpinner.setValues(200, 0, 300, 2, 5, 1);
		sampleThrSpinner.setLayoutData(new RowData(150, 20));
		sampleThrSpinner.setEnabled(true);

		final Label geneLabel = new Label(composite, SWT.HORIZONTAL);
		geneLabel.setLayoutData(new RowData(205, 20));

		final Slider geneThrSpinner = new Slider(composite, SWT.HORIZONTAL);
		geneThrSpinner.setValues(10, -100, 100, 2, 1, 1);
		geneThrSpinner.setLayoutData(new RowData(200, 20));
		geneThrSpinner.setEnabled(true);

		sampleLabel.setText("Sample Threshold: 2");
		geneLabel.setText("       Gene Threshold: 0.1");

		Listener thresholdUpdateListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				float samplTh = sampleThrSpinner.getSelection();
				float geneTh = geneThrSpinner.getSelection();
				sampleLabel.setText("Sample Threshold: " + samplTh / 100);
				geneLabel.setText("      Gene Threshold: " + geneTh / 100);
				// update.setText("Update thresholds: S:" + samplTh / 100 + " G:" + geneTh / 100);
				GeneralManager.get().getEventPublisher().triggerEvent(new ToolbarEvent(samplTh / 100, geneTh / 100));
			}

		};
		sampleThrSpinner.addListener(SWT.Selection, thresholdUpdateListener);
		geneThrSpinner.addListener(SWT.Selection, thresholdUpdateListener);

		return composite;
	}

}
