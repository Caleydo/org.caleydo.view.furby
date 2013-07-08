/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataLoader;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.DataSetDescription.ECreateDefaultProperties;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.core.io.ProjectDescription;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ProjectManager;
import org.caleydo.core.serialize.ProjectMetaData;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

/**
 * a simple eclipse application for creating packages based on the exported csv files from the bio informatics
 *
 * @author Samuel Gratzl
 *
 */
public class PackageGenerator implements IApplication {
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// String[] programArguments = (String[]) context.getArguments().get("application.args");

		final String prefix = System.getProperty("prefix", "D:/Downloads/ROS1/fabia_0000163454_0_1_Run");
		final String name = prefix.substring(prefix.lastIndexOf("/") + 1);
		final String output = prefix + ".cal";

		GeneralManager.get().setDryMode(true);

		ProjectDescription project = new ProjectDescription();

		final IDSpecification geneNames = new IDSpecification("GENE_SAMPLE", "GENE_SAMPLE");
		final IDSpecification sample = new IDSpecification("SAMPLE", "SAMPLE");
		final IDSpecification bicluster = new IDSpecification("BICLUSTER", "BICLUSTER");

		{
			DataSetDescription x = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			x.setDataSourcePath(prefix + "_X.csv");
			x.setDataSetName(name + "_X");
			x.setDelimiter("\t");
			x.setRowIDSpecification(geneNames);
			x.setColumnIDSpecification(sample);
			x.addParsingRule(createParsingRule());
			project.add(x);
		}
		{
			DataSetDescription l = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			l.setDataSourcePath(prefix + "_L.csv");
			l.setDataSetName(name + "_L");
			l.setDelimiter("\t");
			l.setRowIDSpecification(geneNames);
			l.setColumnIDSpecification(bicluster);
			l.addParsingRule(createParsingRule());
			project.add(l);
		}
		{
			DataSetDescription z = new DataSetDescription(ECreateDefaultProperties.NUMERICAL);
			z.setDataSourcePath(prefix + "_Z.csv");
			z.setDataSetName(name + "_Z");
			z.setDelimiter("\t");
			z.setRowIDSpecification(sample);
			z.setColumnIDSpecification(bicluster);
			z.addParsingRule(createParsingRule());
			project.add(z);
		}

		dump(project, output);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {

	}

	protected static ParsingRule createParsingRule() {
		ParsingRule r = new ParsingRule();
		r.setFromColumn(1);
		r.setParseUntilEnd(true);
		r.setColumnDescripton(new ColumnDescription());
		return r;
	}

	protected static void dump(ProjectDescription project, String output) {
		List<ATableBasedDataDomain> dataDomains = new ArrayList<>();

		// Iterate over data type sets and trigger processing
		for (DataSetDescription dataSetDescription : project.getDataSetDescriptionCollection()) {
			ATableBasedDataDomain dataDomain = DataLoader.loadData(dataSetDescription, new NullProgressMonitor());
			if (dataDomain == null)
				continue;
			dataDomains.add(dataDomain);
			dataDomain.getDefaultTablePerspective();
		}


		try {
			ProjectManager.save(output + ".tmp", true, dataDomains, ProjectMetaData.createDefault()).run(
					new NullProgressMonitor());
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// patch in the workbench.xmi
		String template = "";
		try {
			template = CharStreams.toString(new InputStreamReader(PackageGenerator.class
					.getResourceAsStream("/resources/workbench.in.xmi")));

			for (int i = 0; i < dataDomains.size(); ++i) {
				template = template.replace("@DATADOMAIN" + (i + 1) + "@", dataDomains.get(i).getDataDomainID());
				template = template.replace("@PERSPECTIVE" + (i + 1) + "@", dataDomains.get(i)
						.getDefaultTablePerspective()
						.getTablePerspectiveKey());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		try (ZipFile in = new ZipFile(output + ".tmp");
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output))) {

			// first, copy contents from existing war
			Enumeration<? extends ZipEntry> entries = in.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				out.putNextEntry(entry);
				InputStream iin = in.getInputStream(entry);
	            if (!entry.isDirectory()) {
					ByteStreams.copy(iin, out);
	            }
				iin.close();
	            out.closeEntry();
	        }

			ZipEntry entry = new ZipEntry("workbench.xmi");
			out.putNextEntry(entry);
			ByteStreams.copy(new ByteArrayInputStream(template.getBytes()), out);
			out.closeEntry();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File(output + ".tmp").delete();
	}

}
