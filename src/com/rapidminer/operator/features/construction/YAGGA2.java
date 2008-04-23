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
package com.rapidminer.operator.features.construction;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.generator.AbsoluteValueGenerator;
import com.rapidminer.generator.ExponentialFunctionGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.FloorCeilGenerator;
import com.rapidminer.generator.MinMaxGenerator;
import com.rapidminer.generator.PowerGenerator;
import com.rapidminer.generator.SquareRootGenerator;
import com.rapidminer.generator.TrigonometricFunctionGenerator;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.EquivalentAttributeRemoval;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.RemoveUselessAttributes;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>YAGGA is an acronym for Yet Another Generating Genetic Algorithm. Its
 * approach to generating new attributes differs from the original one. The
 * (generating) mutation can do one of the following things with different
 * probabilities:</p>
 * <ul>
 * <li>Probability {@rapidminer.math p/4}: Add a newly generated attribute to the
 * feature vector</li>
 * <li>Probability {@rapidminer.math p/4}: Add a randomly chosen original attribute
 * to the feature vector</li>
 * <li>Probability {@rapidminer.math p/2}: Remove a randomly chosen attribute from
 * the feature vector</li>
 * </ul>
 * <p>Thus it is guaranteed that the length of the feature vector can both grow and
 * shrink. On average it will keep its original length, unless longer or shorter
 * individuals prove to have a better fitness.</p>
 * 
 * <p>In addition to the usual YAGGA operator, this operator allows more feature
 * generators and provides several techniques for intron prevention. This leads to 
 * smaller example sets containing less redundant features.</p>
 * 
 * <p>Since this operator does not contain algorithms to extract features from
 * value series, it is restricted to example sets with only single attributes.
 * For (automatic) feature extraction from values series the value series plugin
 * for RapidMiner should be used.</p>
 *
 * <p>For more information please refer to</p>
 * <p>Mierswa, Ingo (2007): <em>RobustGP: Intron-Free Multi-Objective Feature Construction</em> (to appear)</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: YAGGA2.java,v 1.2 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class YAGGA2 extends YAGGA {


	/** The parameter name for &quot;Generate square root values.&quot; */
	public static final String PARAMETER_USE_SQUARE_ROOTS = "use_square_roots";

	/** The parameter name for &quot;Generate the power of one attribute and another.&quot; */
	public static final String PARAMETER_USE_POWER_FUNCTIONS = "use_power_functions";

	/** The parameter name for &quot;Generate sinus.&quot; */
	public static final String PARAMETER_USE_SIN = "use_sin";

	/** The parameter name for &quot;Generate cosinus.&quot; */
	public static final String PARAMETER_USE_COS = "use_cos";

	/** The parameter name for &quot;Generate tangens.&quot; */
	public static final String PARAMETER_USE_TAN = "use_tan";

	/** The parameter name for &quot;Generate arc tangens.&quot; */
	public static final String PARAMETER_USE_ATAN = "use_atan";

	/** The parameter name for &quot;Generate exponential functions.&quot; */
	public static final String PARAMETER_USE_EXP = "use_exp";

	/** The parameter name for &quot;Generate logarithmic functions.&quot; */
	public static final String PARAMETER_USE_LOG = "use_log";

	/** The parameter name for &quot;Generate absolute values.&quot; */
	public static final String PARAMETER_USE_ABSOLUTE_VALUES = "use_absolute_values";

	/** The parameter name for &quot;Generate minimum values.&quot; */
	public static final String PARAMETER_USE_MIN = "use_min";

	/** The parameter name for &quot;Generate maximum values.&quot; */
	public static final String PARAMETER_USE_MAX = "use_max";

	/** The parameter name for &quot;Generate floor, ceil, and rounded values.&quot; */
	public static final String PARAMETER_USE_FLOOR_CEIL_FUNCTIONS = "use_floor_ceil_functions";

	/** The parameter name for &quot;Use restrictive generator selection (faster).&quot; */
	public static final String PARAMETER_RESTRICTIVE_SELECTION = "restrictive_selection";

	/** The parameter name for &quot;Remove useless attributes.&quot; */
	public static final String PARAMETER_REMOVE_USELESS = "remove_useless";

	/** The parameter name for &quot;Remove equivalent attributes.&quot; */
	public static final String PARAMETER_REMOVE_EQUIVALENT = "remove_equivalent";

	/** The parameter name for &quot;Check this number of samples to prove equivalency.&quot; */
	public static final String PARAMETER_EQUIVALENCE_SAMPLES = "equivalence_samples";

	/** The parameter name for &quot;Consider two attributes equivalent if their difference is not bigger than epsilon.&quot; */
	public static final String PARAMETER_EQUIVALENCE_EPSILON = "equivalence_epsilon";

	/** The parameter name for &quot;Recalculates attribute statistics before equivalence check.&quot; */
	public static final String PARAMETER_EQUIVALENCE_USE_STATISTICS = "equivalence_use_statistics";

	/** The parameter name for &quot;The maximum depth for the argument attributes used for attribute construction (-1: allow all depths).&quot; */
	public static final String PARAMETER_MAX_CONSTRUCTION_DEPTH = "max_construction_depth";

	/** The parameter name for &quot;Space separated list of functions which are not allowed in arguments for attribute construction.&quot; */
	public static final String PARAMETER_UNUSED_FUNCTIONS = "unused_functions";

	/** The parameter name for &quot;Generate random constant attributes with this probability.&quot; */
	public static final String PARAMETER_CONSTANT_GENERATION_PROB = "constant_generation_prob";

	/** The parameter name for &quot;Post processing after crossover (only possible for runs with only one generator).&quot; */
	public static final String PARAMETER_ASSOCIATIVE_ATTRIBUTE_MERGING = "associative_attribute_merging";
	public YAGGA2(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		if (getParameterAsBoolean(PARAMETER_RESTRICTIVE_SELECTION))
			FeatureGenerator.setSelectionMode(FeatureGenerator.SELECTION_MODE_RESTRICTIVE);
		else
			FeatureGenerator.setSelectionMode(FeatureGenerator.SELECTION_MODE_ALL);
		return super.apply();
	}

	protected PopulationOperator getMutationPopulationOperator(ExampleSet exampleSet) throws OperatorException {
		GeneratingMutation mutation = (GeneratingMutation) super.getMutationPopulationOperator(exampleSet);
		mutation.setMaxConstructionDepth(getParameterAsInt(PARAMETER_MAX_CONSTRUCTION_DEPTH));
		String unused = getParameterAsString(PARAMETER_UNUSED_FUNCTIONS);
		if (unused != null)
			mutation.setUnusedFunctions(unused.split(" "));
		return mutation;
	}

	public List<FeatureGenerator> getGenerators() {
		List<FeatureGenerator> generators = super.getGenerators();
		if (getParameterAsBoolean(PARAMETER_USE_SQUARE_ROOTS)) {
			generators.add(new SquareRootGenerator());
		}
		if (getParameterAsBoolean(PARAMETER_USE_POWER_FUNCTIONS)) {
			generators.add(new PowerGenerator());
		}

		if (getParameterAsBoolean(PARAMETER_USE_SIN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.SINUS));
		if (getParameterAsBoolean(PARAMETER_USE_COS))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.COSINUS));
		if (getParameterAsBoolean(PARAMETER_USE_TAN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.TANGENS));
		if (getParameterAsBoolean(PARAMETER_USE_ATAN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.ARC_TANGENS));

		if (getParameterAsBoolean(PARAMETER_USE_EXP))
			generators.add(new ExponentialFunctionGenerator(ExponentialFunctionGenerator.EXP));
		if (getParameterAsBoolean(PARAMETER_USE_LOG))
			generators.add(new ExponentialFunctionGenerator(ExponentialFunctionGenerator.LOG));

		if (getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_VALUES))
			generators.add(new AbsoluteValueGenerator());
		if (getParameterAsBoolean(PARAMETER_USE_MIN))
			generators.add(new MinMaxGenerator(MinMaxGenerator.MIN));
		if (getParameterAsBoolean(PARAMETER_USE_MAX))
			generators.add(new MinMaxGenerator(MinMaxGenerator.MAX));

		if (getParameterAsBoolean(PARAMETER_USE_FLOOR_CEIL_FUNCTIONS)) {
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.FLOOR));
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.CEIL));
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.ROUND));
		}
		return generators;
	}

	protected List<PopulationOperator> getPreProcessingPopulationOperators(ExampleSet eSet) throws OperatorException {
		List<PopulationOperator> popOps = super.getPreProcessingPopulationOperators(eSet);
		double constantProb = getParameterAsDouble(PARAMETER_CONSTANT_GENERATION_PROB);
		if (constantProb > 0.0d)
			popOps.add(new ConstantGeneration(constantProb, getRandom()));
		if (getParameterAsBoolean(PARAMETER_REMOVE_USELESS))
			popOps.add(new RemoveUselessAttributes());
		if (getParameterAsBoolean(PARAMETER_REMOVE_EQUIVALENT))
			popOps.add(new EquivalentAttributeRemoval(getParameterAsInt(PARAMETER_EQUIVALENCE_SAMPLES), getParameterAsDouble(PARAMETER_EQUIVALENCE_EPSILON), getParameterAsBoolean(PARAMETER_EQUIVALENCE_USE_STATISTICS), getRandom()));
		return popOps;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_USE_SQUARE_ROOTS, "Generate square root values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_POWER_FUNCTIONS, "Generate the power of one attribute and another.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_SIN, "Generate sinus.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_COS, "Generate cosinus.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_TAN, "Generate tangens.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ATAN, "Generate arc tangens.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_EXP, "Generate exponential functions.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_LOG, "Generate logarithmic functions.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_VALUES, "Generate absolute values.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MIN, "Generate minimum values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MAX, "Generate maximum values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_FLOOR_CEIL_FUNCTIONS, "Generate floor, ceil, and rounded values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_RESTRICTIVE_SELECTION, "Use restrictive generator selection (faster).", true));
		types.add(new ParameterTypeBoolean(PARAMETER_REMOVE_USELESS, "Remove useless attributes.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_REMOVE_EQUIVALENT, "Remove equivalent attributes.", true));
		types.add(new ParameterTypeInt(PARAMETER_EQUIVALENCE_SAMPLES, "Check this number of samples to prove equivalency.", 1, Integer.MAX_VALUE, 5));
		types.add(new ParameterTypeDouble(PARAMETER_EQUIVALENCE_EPSILON, "Consider two attributes equivalent if their difference is not bigger than epsilon.", 0.0d, Double.POSITIVE_INFINITY, 0.05d));
		types.add(new ParameterTypeBoolean(PARAMETER_EQUIVALENCE_USE_STATISTICS, "Recalculates attribute statistics before equivalence check.", true));
		types.add(new ParameterTypeInt(PARAMETER_MAX_CONSTRUCTION_DEPTH, "The maximum depth for the argument attributes used for attribute construction (-1: allow all depths).", -1, Integer.MAX_VALUE, -1));
		types.add(new ParameterTypeString(PARAMETER_UNUSED_FUNCTIONS, "Space separated list of functions which are not allowed in arguments for attribute construction."));
		types.add(new ParameterTypeDouble(PARAMETER_CONSTANT_GENERATION_PROB, "Generate random constant attributes with this probability.", 0.0d, 1.0d, 0.02d));
		types.add(new ParameterTypeBoolean(PARAMETER_ASSOCIATIVE_ATTRIBUTE_MERGING, "Post processing after crossover (only possible for runs with only one generator).", false));
		return types;
	}
}
