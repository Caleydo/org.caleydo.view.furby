/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.serialize.ASerializedMultiTablePerspectiveBasedView;

/**
 * Serialized BiCluster view.
 *
 * @author Michael Gillhofer
 */
@XmlRootElement
@XmlType
public class SerializedBiClusterView extends ASerializedMultiTablePerspectiveBasedView {
	/**
	 * Default constructor with default initialization
	 */
	public SerializedBiClusterView() {
	}

	public SerializedBiClusterView(GLBiCluster view) {
		super(view);
	}

	@Override
	public String getViewType() {
		return GLBiCluster.VIEW_TYPE;
	}
}
