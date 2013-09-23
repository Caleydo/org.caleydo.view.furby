/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.util.List;

import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.ClusterElement;

/**
 * @author Samuel Gratzl
 *
 */
public class MDSLayout implements IGLLayout2 {
	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w_p, float h_p, IGLLayoutElement parent,
			int deltaTimeMs) {
		final int n = children.size();
		RealMatrix a = MatrixUtils.createRealMatrix(n, n);

		// Sind als Startwerte anstatt Distanzen Ähnlichkeitsmaße c_{ij} zwischen Objekten gegeben, so lassen sich diese
		// durch die Transformation
		//
		// d_{ij} = \sqrt{c_{ii}+c_{jj}-2c_{ij}}
		//
		// in Distanzen überführen.

		for (int i = 0; i < n; ++i) {
			ClusterElement ci = (ClusterElement) children.get(i).asElement();
			int c_ii = ci.getDimSize() + ci.getRecSize();

			for (int j = i + 1; j < n; ++j) {
				ClusterElement cj = (ClusterElement) children.get(j).asElement();
				int recOverlap = ci.getRecOverlap(cj);
				int dimOverlap = ci.getDimOverlap(cj);

				int c_jj = cj.getDimSize() + cj.getRecSize();
				int c_ij = recOverlap + dimOverlap;

				double d_ij = Math.sqrt(c_ii + c_jj - 2 * c_ij);

				a.setEntry(i, j, d_ij);
				a.setEntry(j, i, d_ij);
			}
		}

		//#1. negative squared dissimilarity matrix Q
		//q = as.matrix( -0.5 * d ** 2 )
		RealMatrix q = a.copy();
		for(int i = 0; i < n; ++i) {
			q.getRowVector(i).mapPowToSelf(2);
		}
		q = q.scalarMultiply(-0.5);

		//#2. centering matrix H
		//h = diag(n) - 1/n * 1
		RealMatrix h = MatrixUtils.createRealMatrix(n, n);
		for(int i = 0; i < n; ++i)
			h.setEntry(i,i, n - 1./n * 1);
		//#3. double-center matrix B
		//b = h %*% q %*% h
		RealMatrix b = h.copy().multiply(q).multiply(h);
		// #4. eigen decomposition of B
		// eig = eigen(b)
		EigenDecompositionImpl eig = new EigenDecompositionImpl(b, 0);
		// #5. use top k values/vectors to compute projected points
		// points = eig$vectors[,1:k] %*% diag(sqrt(eig$values[1:k]))
		for(int i = 0; i < n; ++i) {
			RealVector v = eig.getEigenvector(i).getSubVector(0, 2);
			double x = v.getEntry(0)*eig.getRealEigenvalue(0);
			double y = v.getEntry(1)*eig.getRealEigenvalue(1);
			IGLLayoutElement child = children.get(i);
			child.setLocation((float) x, (float) y);
		}

		return false;
	}
}
