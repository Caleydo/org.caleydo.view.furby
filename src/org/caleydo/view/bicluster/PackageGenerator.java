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
package org.caleydo.view.bicluster;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

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

		final String prefix = System.getProperty("prefix", "D:/Downloads/ROS1/fabia_0000789456_2_1_Run");
		final String name = prefix.substring(prefix.lastIndexOf("/") + 1);
		final String output = prefix + ".cal";

		GeneralManager.get().setDryMode(true);

		ProjectDescription project = new ProjectDescription();

		final IDSpecification geneNames = new IDSpecification("GENE", "GENE_NAME");
		geneNames.setIDTypeGene(true);
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
		Collection<ATableBasedDataDomain> dataDomains = new ArrayList<>();

		// Iterate over data type sets and trigger processing
		for (DataSetDescription dataSetDescription : project.getDataSetDescriptionCollection()) {
			ATableBasedDataDomain dataDomain = DataLoader.loadData(dataSetDescription);
			if (dataDomain != null)
				dataDomains.add(dataDomain);
		}

		try {
			ProjectManager.save(output, true, dataDomains).run(new NullProgressMonitor());
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
