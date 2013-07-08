/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.serialize.ISerializationAddon;
import org.caleydo.core.serialize.SerializationData;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.view.bicluster.SerializedBiClusterView;

/**
 * addon to the project serialization / deserialization to persist data, if you want to persist view specific data, use
 * the {@link SerializedBiClusterView} object
 *
 * @author Samuel Gratzl
 *
 */
public class SerializationAddon implements ISerializationAddon {
	private static final String ADDON_KEY = "BiCluster";
	private final static Logger log = Logger.create(SerializationAddon.class);

	@Override
	public Collection<Class<?>> getJAXBContextClasses() {
		Collection<Class<?>> tmp = new ArrayList<>(1);
		tmp.add(BiClusterSerializationData.class);
		return tmp;
	}

	@Override
	public void deserialize(String dirName, Unmarshaller unmarshaller) {

	}

	@Override
	public void serialize(Collection<? extends IDataDomain> toSave, Marshaller marshaller, String dirName) {
		BiClusterSerializationData data = new BiClusterSerializationData();
		// TODO

		try {
			marshaller.marshal(data, new File(dirName, "bicluster.xml"));
		} catch (JAXBException e) {
			log.error("can't serialize bicluster data", e);
		}
	}

	@Override
	public void deserialize(String dirName, Unmarshaller unmarshaller, SerializationData data) {
		File f = new File(dirName, "bicluster.xml");
		if (!f.exists())
			return;
		BiClusterSerializationData bicluster;
		try {
			bicluster = (BiClusterSerializationData) unmarshaller.unmarshal(new File(dirName, "bicluster.xml"));
			data.setAddonData(ADDON_KEY, bicluster);
		} catch (JAXBException e) {
			log.error("can't deserialize bicluster data", e);
		}
	}

	@Override
	public void load(SerializationData data) {
		BiClusterSerializationData desc = (BiClusterSerializationData) data.getAddonData(ADDON_KEY);
		if (desc == null)
			return;
		// TODO
	}

}
