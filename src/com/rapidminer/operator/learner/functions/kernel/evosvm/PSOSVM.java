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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;


/**
 * This is a SVM implementation using a particle swarm optimization (PSO)
 * approach to solve the dual optimization problem of a SVM. It turns out that
 * on many datasets this simple implementation is as fast and accurate as the
 * usual SVM implementations.
 * 
 * @rapidminer.index SVM
 * 
 * @author Ingo Mierswa
 * @version $Id: PSOSVM.java,v 1.2 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class PSOSVM extends AbstractLearner {


	/** The parameter name for &quot;Indicates if a dialog with a convergence plot should be drawn.&quot; */
	public static final String PARAMETER_SHOW_CONVERGENCE_PLOT = "show_convergence_plot";

	/** The parameter name for &quot;The SVM kernel type&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The SVM kernel parameter sigma (radial kernel).&quot; */
	public static final String PARAMETER_KERNEL_GAMMA = "kernel_gamma";

	/** The parameter name for &quot;The SVM kernel parameter degree (polynomial).&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The SVM kernel parameter shift (polynomial).&quot; */
	public static final String PARAMETER_KERNEL_SHIFT = "kernel_shift";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";

	/** The parameter name for &quot;The SVM complexity constant (0: calculates probably good value).&quot; */
	public static final String PARAMETER_C = "C";

	/** The parameter name for &quot;Stop after this many evaluations&quot; */
	public static final String PARAMETER_MAX_EVALUATIONS = "max_evaluations";

	/** The parameter name for &quot;Stop after this number of generations without improvement (-1: optimize until max_iterations).&quot; */
	public static final String PARAMETER_GENERATIONS_WITHOUT_IMPROVAL = "generations_without_improval";

	/** The parameter name for &quot;The population size (-1: number of examples)&quot; */
	public static final String PARAMETER_POPULATION_SIZE = "population_size";

	/** The parameter name for &quot;The (initial) weight for the old weighting.&quot; */
	public static final String PARAMETER_INERTIA_WEIGHT = "inertia_weight";

	/** The parameter name for &quot;The weight for the individual's best position during run.&quot; */
	public static final String PARAMETER_LOCAL_BEST_WEIGHT = "local_best_weight";

	/** The parameter name for &quot;The weight for the population's best position during run.&quot; */
	public static final String PARAMETER_GLOBAL_BEST_WEIGHT = "global_best_weight";

	/** The parameter name for &quot;If set to true the inertia weight is improved during run.&quot; */
	public static final String PARAMETER_DYNAMIC_INERTIA_WEIGHT = "dynamic_inertia_weight";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	/**
	 * Creates a new SVM which uses a particle swarm optimization approach for
	 * optimization.
	 */
	public PSOSVM(OperatorDescription description) {
		super(description);
	}

	/** Learns and returns a model. */
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Attribute label = exampleSet.getAttributes().getLabel();
        if (label.getMapping().size() != 2) {
            throw new UserError(this, 114, getName(), label.getName());
        }
		// kernel
		int kernelType = getParameterAsInt(PARAMETER_KERNEL_TYPE);
		Kernel kernel = Kernel.createKernel(kernelType);
		if (kernelType == Kernel.KERNEL_RADIAL) {
			double gamma = getParameterAsDouble(PARAMETER_KERNEL_GAMMA);
			// if (sigma == 0.0d)
			// sigma = 1.0d / (double)exampleSet.getNumberOfAttributes();
			((RBFKernel) kernel).setGamma(gamma);
		} else if (kernelType == Kernel.KERNEL_POLYNOMIAL)
			((PolynomialKernel) kernel).setPolynomialParameters(getParameterAsInt(PARAMETER_KERNEL_DEGREE), getParameterAsDouble(PARAMETER_KERNEL_SHIFT));
		else if (kernelType == Kernel.KERNEL_SIGMOID)
			((SigmoidKernel) kernel).setSigmoidParameters(getParameterAsDouble(PARAMETER_KERNEL_A), getParameterAsDouble(PARAMETER_KERNEL_B));

		// optimization
		PSOSVMOptimization optimization = 
            new PSOSVMOptimization(exampleSet, kernel, getParameterAsDouble(PARAMETER_C), 
                    getParameterAsInt(PARAMETER_MAX_EVALUATIONS), getParameterAsInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL), 
                    getParameterAsInt(PARAMETER_POPULATION_SIZE), getParameterAsDouble(PARAMETER_INERTIA_WEIGHT),
                    getParameterAsDouble(PARAMETER_LOCAL_BEST_WEIGHT), getParameterAsDouble(PARAMETER_GLOBAL_BEST_WEIGHT), 
                    getParameterAsBoolean(PARAMETER_DYNAMIC_INERTIA_WEIGHT), 
                    getParameterAsBoolean(PARAMETER_SHOW_CONVERGENCE_PLOT),
                    RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)));
		optimization.optimize();

		double[] bestAlphas = optimization.getBestValuesEver();
		return optimization.getModel(bestAlphas);
	}

	/**
	 * Returns true for numerical attributes, binominal classes, and numerical
	 * target attributes.
	 */
	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.BINOMINAL_CLASS)
			return true;
		return false;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_SHOW_CONVERGENCE_PLOT, "Indicates if a dialog with a convergence plot should be drawn.", false));
		ParameterType type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE, "The SVM kernel type", Kernel.KERNEL_TYPES, Kernel.KERNEL_DOT);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_GAMMA, "The SVM kernel parameter sigma (radial kernel).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_DEGREE, "The SVM kernel parameter degree (polynomial).", 0.0d, Double.POSITIVE_INFINITY, 3.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SHIFT, "The SVM kernel parameter shift (polynomial).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_A, "The SVM kernel parameter a (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_B, "The SVM kernel parameter b (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_C, "The SVM complexity constant (0: calculates probably good value).", 0.0d, Double.POSITIVE_INFINITY, 0.0d);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_MAX_EVALUATIONS, "Stop after this many evaluations", 1, Integer.MAX_VALUE, 500));
		types.add(new ParameterTypeInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL, "Stop after this number of generations without improvement (-1: optimize until max_iterations).", -1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeInt(PARAMETER_POPULATION_SIZE, "The population size (-1: number of examples)", -1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeDouble(PARAMETER_INERTIA_WEIGHT, "The (initial) weight for the old weighting.", 0.0d, Double.POSITIVE_INFINITY, 0.1d));
		types.add(new ParameterTypeDouble(PARAMETER_LOCAL_BEST_WEIGHT, "The weight for the individual's best position during run.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
		types.add(new ParameterTypeDouble(PARAMETER_GLOBAL_BEST_WEIGHT, "The weight for the population's best position during run.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
		types.add(new ParameterTypeBoolean(PARAMETER_DYNAMIC_INERTIA_WEIGHT, "If set to true the inertia weight is improved during run.", true));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
