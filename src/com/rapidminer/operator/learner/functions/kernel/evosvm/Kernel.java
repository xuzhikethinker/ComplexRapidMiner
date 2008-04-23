/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.functions.kernel.evosvm;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.functions.kernel.SupportVector;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * Returns the distance of two examples. The method {@link #init(ExampleSet)}
 * must be invoked before the correct distances can be returned. Please note
 * that subclasses must provide an empty constructor to allow kernel creation
 * via reflection (for reading kernels from disk).
 * 
 * @author Ingo Mierswa
 * @version $Id: Kernel.java,v 1.4 2007/06/15 16:58:37 ingomierswa Exp $
 */
public abstract class Kernel implements Serializable {

	/** The kernels which can be used for the EvoSVM. */
	public static final String[] KERNEL_TYPES = { 
		"dot", "radial", "polynomial", "sigmoid", "anova", "epanechnikov", "gausian_combination", "multiquadric" 
	};

	/** Indicates a linear kernel. */
	public static final int KERNEL_DOT = 0;

	/** Indicates a rbf kernel. */
	public static final int KERNEL_RADIAL = 1;

	/** Indicates a polynomial kernel. */
	public static final int KERNEL_POLYNOMIAL = 2;

	/** Indicates a sigmoid kernel. */
	public static final int KERNEL_SIGMOID = 3;
	
	/** Indicates an anova kernel. */
	public static final int KERNEL_ANOVA = 4;

	/** Indicates a Epanechnikov kernel. */
	public static final int KERNEL_EPANECHNIKOV = 5;

	/** Indicates a Gaussian combination kernel. */
	public static final int KERNEL_GAUSSIAN_COMBINATION = 6;
	
	/** Indicates a multiquadric kernel. */
	public static final int KERNEL_MULTIQUADRIC = 7;
	
	/** The complete distance matrix for this kernel and a given example set. */
	private double[][] distances;

	/**
	 * Must return one out of KERNEL_DOT, KERNEL_RADIAL, KERNEL_POLYNOMIAL,
	 * KERNEL_SIGMOID, KERNEL_ANOVA, KERNEL_EPANECHNIKOV, KERNEL_GAUSSIAN_COMBINATION,
	 * or KERNEL_MULTIQUADRIC.
	 */
	public abstract int getType();

	/** Subclasses must implement this method. */
	public abstract double calculateDistance(double[] x1, double[] x2);

	/**
	 * Calculates all distances and store them in a matrix to speed up
	 * optimization.
	 */
	public void init(ExampleSet exampleSet) {
		int size = exampleSet.size();
		this.distances = new double[size][size];
		Iterator<Example> reader = exampleSet.iterator();
		int i = 0;
		while (reader.hasNext()) {
			Example example1 = reader.next();
			double[] x1 = new double[exampleSet.getAttributes().size()];
			int x = 0;
			for (Attribute attribute : exampleSet.getAttributes())
				x1[x++] = example1.getValue(attribute);
			Iterator<Example> innerReader = exampleSet.iterator();
			int j = 0;
			while (innerReader.hasNext()) {
				Example example2 = innerReader.next();
				double[] x2 = new double[exampleSet.getAttributes().size()];
				x = 0;
				for (Attribute attribute : exampleSet.getAttributes())
					x2[x++] = example2.getValue(attribute);
				double distance = calculateDistance(x1, x2);
				this.distances[i][j] = distance;
				this.distances[j][i] = distance;
				j++;
			}
			i++;
		}
	}

	/** Returns the distance between the examples with the given indices. */
	public double getDistance(int x1, int x2) {
		return distances[x1][x2];
	}

	/** Calculates the inner product of the given vectors. */
	public double innerProduct(double[] x1, double[] x2) {
		double result = 0.0d;
		for (int i = 0; i < x1.length; i++) {
			result += x1[i] * x2[i];
		}
		return result;
	}

	/** Calculates the L2-norm, i.e. ||x-y||^2. */
	public double norm2(double[] x1, double[] x2) {
		double result = 0;
		for (int i = 0; i < x1.length; i++) {
			double factor = x1[i] - x2[i];
			result += factor * factor;
		}
		return result;
	}

	/** Calculates w*x from the given support vectors using this kernel function. */
	public double getSum(Collection supportVectors, double[] currentX) {
		double sum = 0.0d;
		Iterator i = supportVectors.iterator();
		while (i.hasNext()) {
			SupportVector sv = (SupportVector) i.next();
			sum += sv.getY() * sv.getAlpha() * calculateDistance(sv.getX(), currentX);
		}
		return sum;
	}

	public static Kernel createKernel(int kernelType) {
		try {
			return createKernel(kernelType, null);
		} catch (UndefinedParameterError e) {
			return null;
		} // cannot happen
	}
	
	public static Kernel createKernel(int kernelType, EvoSVM operator) throws UndefinedParameterError {
		if (kernelType == KERNEL_DOT) {
			return new DotKernel();
		} else if (kernelType == KERNEL_RADIAL) {
			RBFKernel kernel = new RBFKernel();
			if (operator != null)
				kernel.setGamma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_GAMMA)); 
			return kernel;
		} else if (kernelType == KERNEL_POLYNOMIAL) {
			PolynomialKernel kernel = new PolynomialKernel();
			if (operator != null)
				kernel.setPolynomialParameters(
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE),  
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SHIFT)); 
			return kernel;
		} else if (kernelType == KERNEL_SIGMOID) {
			SigmoidKernel kernel = new SigmoidKernel();
			if (operator != null)
				kernel.setSigmoidParameters(
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_A),  
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_B)); 
			return kernel;
		} else if (kernelType == KERNEL_ANOVA) {
			AnovaKernel kernel = new AnovaKernel();
			if (operator != null) {
				kernel.setGamma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_GAMMA)); 
				kernel.setDegree(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_EPANECHNIKOV) {
			EpanechnikovKernel kernel = new EpanechnikovKernel();
			if (operator != null) {
				kernel.setSigma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setDegree(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_GAUSSIAN_COMBINATION) {
			GaussianCombinationKernel kernel = new GaussianCombinationKernel();
			if (operator != null) {
				kernel.setSigma1(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setSigma2(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA2)); 
				kernel.setSigma3(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA3)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_MULTIQUADRIC) {
			MultiquadricKernel kernel = new MultiquadricKernel();
			if (operator != null) {
				kernel.setSigma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setShift(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SHIFT)); 
			}
			return kernel;
		} else {
			return null;
		}
	}
}
