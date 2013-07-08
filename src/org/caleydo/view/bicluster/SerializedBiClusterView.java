/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.serialize.ASerializedMultiTablePerspectiveBasedView;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;

/**
 * Serialized <INSERT VIEW NAME> view.
 *
 * @author <INSERT_YOUR_NAME>
 */
@XmlRootElement
@XmlType
public class SerializedBiClusterView extends ASerializedMultiTablePerspectiveBasedView {

	/**
	 * Default constructor with default initialization
	 */
	public SerializedBiClusterView() {
	}

	public SerializedBiClusterView(IMultiTablePerspectiveBasedView view) {
		super(view);
	}

	@Override
	public String getViewType() {
		return GLBiCluster.VIEW_TYPE;
	}

	@Override
	public String getViewClassType() {
		return GLBiCluster.class.getName();
	}
}
