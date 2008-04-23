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
package com.rapidminer.operator.learner.meta;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Tools;


/**
 * <p>This operator trains an ensemble of classifiers for boolean target
 * attributes. In each iteration the training set is reweighted, so that
 * previously discovered patterns and other kinds of prior knowledge are
 * &quot;sampled out&quot; {@rapidminer.cite Scholz/2005b}. An inner classifier,
 * typically a rule or decision tree induction algorithm, is sequentially
 * applied several times, and the models are combined to a single global model.
 * The number of models to be trained maximally are specified by the parameter
 * <code>iterations</code>.</p>
 * 
 * <p>If the parameter <code>rescale_label_priors</code> is set, then the example
 * set is reweighted, so that all classes are equally probable (or frequent).
 * For two-class problems this turns the problem of fitting models to maximize
 * weighted relative accuracy into the more common task of classifier induction
 * {@rapidminer.cite Scholz/2005a}. Applying a rule induction algorithm as an inner
 * learner allows to do subgroup discovery. This option is also recommended for
 * data sets with class skew, if a &quot;very weak learner&quot; like a decision
 * stump is used. If <code>rescale_label_priors</code> is not set, then the
 * operator performs boosting based on probability estimates.</p>
 * 
 * <p>The estimates used by this operator may either be computed using the same set
 * as for training, or in each iteration the training set may be split randomly,
 * so that a model is fitted based on the first subset, and the probabilities
 * are estimated based on the second. The first solution may be advantageous in
 * situations where data is rare. Set the parameter
 * <code>ratio_internal_bootstrap</code> to 1 to use the same set for training
 * as for estimation. Set this parameter to a value of lower than 1 to use the
 * specified subset of data for training, and the remaining examples for
 * probability estimation.</p>
 * 
 * <p>If the parameter <code>allow_marginal_skews</code> is <em>not</em> set,
 * then the support of each subset defined in terms of common base model
 * predictions does not change from one iteration to the next. Analogously the
 * class priors do not change. This is the procedure originally described in
 * {@rapidminer.cite Scholz/2005b} in the context of subgroup discovery.</p>
 * 
 * <p>Setting the <code>allow_marginal_skews</code> option to <code>true</code>
 * leads to a procedure that changes the marginal weights/probabilities of
 * subsets, if this is beneficial in a boosting context, and stratifies the two
 * classes to be equally likely. As for AdaBoost, the total weight upper-bounds
 * the training error in this case. This bound is reduced more quickly by the
 * BayesianBoosting operator, however.</p>
 * 
 * <p>In sum, to reproduce the sequential sampling, or knowledge-based sampling, 
 * from {@rapidminer.cite Scholz/2005b} for subgroup discovery, two of the 
 * default parameter settings of this operator have to be changed: 
 * <code>rescale_label_priors</code> must 
 * be set to <code>true</code>, and <code>allow_marginal_skews</code> must 
 * be set to <code>false</code>. In addition, a boolean (binomial) label 
 * has to be used.</p>
 *  
 * <p>The operator requires an example set as its input. To sample out prior
 * knowledge of a different form it is possible to provide another model as an
 * optional additional input. The predictions of this model are used to weight
 * produce an initial weighting of the training set. The ouput of the operator
 * is a classification model applicable for estimating conditional class
 * probabilities or for plain crisp classification. It contains up to the
 * specified number of inner base models. In the case of an optional initial
 * model, this model will also be stored in the output model, in order to
 * produce the same initial weighting during model application.</p>
 * 
 * @author Martin Scholz
 * @version $Id: BayesianBoosting.java,v 1.56 2006/04/14 15:14:32 ingomierswa
 *          Exp $
 */
public class BayesianBoosting extends AbstractMetaLearner {

	/**
	 * Name of the variable specifying the maximal number of iterations of the
	 * learner.
	 */
	public static final String PARAMETER_ITERATIONS = "iterations";

	/** Name of the flag indicating internal bootstrapping. */
	public static final String PARAMETER_USE_SUBSET_FOR_TRAINING = "use_subset_for_training";

	/**
	 * Boolean parameter to specify whether the label priors should be equally
	 * likely after first iteration.
	 */
	public static final String PARAMETER_RESCALE_LABEL_PRIORS = "rescale_label_priors";

	/**
	 * Boolean parameter that switches between KBS (if set to false) and a
	 * boosting-like reweighting.
	 */
	public static final String PARAMETER_ALLOW_MARGINAL_SKEWS = "allow_marginal_skews";

	/** Discard models with an advantage of less than the specified value. */
	public static final double MIN_ADVANTAGE = 0.001;

	// A model to initialise the example weights.
	private Model startModel;

	// field for visualizing performance
	protected int currentIteration;

	// A performance measure to be visualized. Not yet implemented!
	private double performance = 0;

	// A backup of the original weights of the training set to restore them
	// after learning.
	private double[] oldWeights;

	/** Constructor. */
	public BayesianBoosting(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "The performance.") {

			public double getValue() {
				return performance;
			}
		});
		addValue(new Value("iteration", "The current iteration.") {

			public double getValue() {
				return currentIteration;
			}
		});
	}

	/**
	 * Overrides the method of the super class. Returns true for polynominal
	 * class.
	 */
	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.NUMERICAL_CLASS || lc == LearnerCapability.POLYNOMINAL_CLASS)
			return false;
		
        if (lc == LearnerCapability.WEIGHTED_EXAMPLES)
            return true;
        
		return super.supportsCapability(lc);
	}

	/**
	 * Constructs a <code>Model</code> repeatedly running a weak learner,
	 * reweighting the training example set accordingly, and combining the
	 * hypothesis using the available weighted performance values. If the input
	 * contains a model, then this model is used as a starting point for
	 * weighting the examples.
	 */
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		// Read start model if present.
		this.readOptionalParameters();
		
		// the resulting model of this operator
		Model model;

		double[] classPriors = this.prepareWeights(exampleSet);
		
		// check whether only one or no class is present		
		double maxPrior  = Double.NEGATIVE_INFINITY;
		double sumPriors = 0;
		for (int i=0; i<classPriors.length; i++) {
			if (classPriors[i] > maxPrior)
				maxPrior = classPriors[i];
			sumPriors += classPriors[i];
		}
		if (Tools.isEqual(sumPriors, maxPrior)) {
			// nothing to do, return an empty ensemble model
			model = new BayBoostModel(exampleSet, new Vector<BayBoostBaseModelInfo>(), classPriors);
		}
		else {
			// only in this case boosting makes sense
			model = this.trainBoostingModel(exampleSet, classPriors);
		}

		if (this.oldWeights != null) { // need to reset weights
			Iterator<Example> reader = exampleSet.iterator();
			int i = 0;
			while (reader.hasNext() && i < this.oldWeights.length) {
				reader.next().setWeight(this.oldWeights[i++]);
			}
		} else { // need to delete the weights attribute
			Attribute weight = exampleSet.getAttributes().getWeight();
			exampleSet.getAttributes().remove(weight);
			exampleSet.getExampleTable().removeAttribute(weight);
		}

		return model;
	}

	/**
	 * Creates a weight attribute if not yet done. It either backs up the old
	 * weoghts for restoring them later, or it fills the newly created attribute
	 * with the initial value of 1. If rescaling to equal class priors is
	 * activated then the weights are set accordingly.
	 * 
	 * @param exampleSet
	 *            the example set to be prepared
	 * @return a <code>double[]</code> array containing the class priors.
	 */
	protected double[] prepareWeights(ExampleSet exampleSet) {
		Attribute weightAttr = exampleSet.getAttributes().getWeight();
		if (weightAttr == null) {
			this.oldWeights = null;

			// example weights are initialized so that the total weight
			// is equal to the number of examples:
			this.performance = exampleSet.size();

			return this.createNewWeightAttribute(exampleSet);
		}
		else { 
			// Back up old weights and compute priors:
			this.oldWeights = new double[exampleSet.size()];
			double[] priors = new double[exampleSet.getAttributes().getLabel().getMapping().size()];
			double totalWeight = 0;
			Iterator<Example> reader = exampleSet.iterator();

			for (int i = 0; (reader.hasNext() && i < oldWeights.length); i++) {
				Example example = reader.next();
				if (example != null) {
					double weight = example.getWeight();
					this.oldWeights[i] = weight;
					int label = (int) example.getLabel();
					
					if (0 <= label && label < priors.length) {
						priors[label] += weight;
						totalWeight += weight;
					} 
					else example.setWeight(0); // Unrecognized label, try to ignore it!
				}
			}
			this.performance = totalWeight;

			// Normalize:
			for (int i = 0; i < priors.length; i++) {
				priors[i] /= totalWeight;
			}

			return priors;
		}
	}

	private double[] createNewWeightAttribute(ExampleSet exampleSet) {
		com.rapidminer.example.Tools.createWeightAttribute(exampleSet);

		Iterator<Example> exRead = exampleSet.iterator();
		int numClasses = exampleSet.getAttributes().getLabel().getMapping().getValues().size();
		double[] classPriors = new double[numClasses];

		int total = exampleSet.size();
		double invTotal = 1.0d / total;

		if (this.getParameterAsBoolean(PARAMETER_RESCALE_LABEL_PRIORS) == false) {
			while (exRead.hasNext()) {
				Example example = exRead.next();
				example.setWeight(1);
				classPriors[(int) (example.getLabel())] += invTotal;
			}
		} 
		else { 
			// first count the class frequencies
			while (exRead.hasNext()) {
				classPriors[(int) (exRead.next().getLabel())] += invTotal;
			}
			this.rescaleToEqualPriors(exampleSet, classPriors);
		}
		return classPriors;
	}

	private void rescaleToEqualPriors(ExampleSet exampleSet, double[] currentPriors) {
		// The weights of class i are calculated as
		// (1 / #classes) / (#rel_freq_class_i)
		double[] weights = new double[currentPriors.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1.0d / (weights.length * (currentPriors[i]));
		}

		Iterator<Example> exRead = exampleSet.iterator();
		while (exRead.hasNext()) {
			Example example = exRead.next();
			example.setWeight(weights[(int) (example.getLabel())]);
		}
	}

	/**
	 * Runs the &quot;embedded&quot; learner on the example set and retuns a
	 * model.
	 * 
	 * @param exampleSet
	 *            an <code>ExampleSet</code> to train a model for
	 * @return a <code>Model</code>
	 */
	protected Model trainBaseModel(ExampleSet exampleSet) throws OperatorException {
		Model model = applyInnerLearner(exampleSet);
		return model;
	}

	/** Helper method reading a start model from the input if present. */
	private void readOptionalParameters() {
		try {
			this.startModel = getInput(Model.class);
		} catch (MissingIOObjectException e) {
			log(getName() + ": No model found in input.");
		}
	}

	/**
	 * Helper method applying the start model and adding it to the modelInfo
	 * collection
	 */
	private void applyPriorModel(ExampleSet trainingSet, List<BayBoostBaseModelInfo> modelInfo) throws OperatorException {
		// If the input contains a model already, initialise the example weights.
		if (this.startModel != null) {

			this.startModel.apply(trainingSet);

			// Initial values and the input model are stored in the output model.
			WeightedPerformanceMeasures wp = new WeightedPerformanceMeasures(trainingSet);

			this.reweightExamples(wp, trainingSet);
			modelInfo.add(new BayBoostBaseModelInfo(this.startModel, wp.getContingencyMatrix()));
			PredictionModel.removePredictedLabel(trainingSet);
		}
	}

	/** Main method for training the ensemble classifier */
	private BayBoostModel trainBoostingModel(ExampleSet trainingSet, final double[] classPriors) throws OperatorException {
		// for models and their probability estimates
		Vector<BayBoostBaseModelInfo> modelInfo = new Vector<BayBoostBaseModelInfo>();

		// if present apply the start model first
		this.applyPriorModel(trainingSet, modelInfo);

		// check whether to use the complete training set for training
		final double splitRatio = this.getParameterAsDouble(PARAMETER_USE_SUBSET_FOR_TRAINING);
		final boolean bootstrap = ((splitRatio > 0) && (splitRatio < 1.0));
		log(bootstrap ? "Bootstrapping enabled." : "Bootstrapping disabled.");

		final boolean allowSkew = this.getParameterAsBoolean(PARAMETER_ALLOW_MARGINAL_SKEWS);
		
		SplittedExampleSet splittedSet = null;
		if (bootstrap == true) {
			splittedSet = new SplittedExampleSet(trainingSet, splitRatio, SplittedExampleSet.SHUFFLED_SAMPLING, -1);
		}
		
		// final int optimizeWeightsNum =
		// this.getParameterAsInt(ADJUST_WEIGHTS);
		// LogService.logMessage((optimizeWeightsNum > 0)
		// ? "Weights are optimized after each " + optimizeWeightsNum + "
		// iterations."
		// : "Weight optimization is off.", LogService.STATUS);
		//
		// boolean canOptimiseWeights = true;

		// maximum number of iterations
		final int iterations = this.getParameterAsInt(PARAMETER_ITERATIONS);
		L: for (int i = 0; i < iterations; i++) {
			this.currentIteration = i;

			Model model;
			WeightedPerformanceMeasures wp;
			if (bootstrap == true) {
				
				splittedSet.selectSingleSubset(0); // switch to learning subset 
				model = this.trainBaseModel(splittedSet);

				// apply model to all examples
				model.apply(trainingSet);
				
				// reweight learning subset
				wp = new WeightedPerformanceMeasures(splittedSet);
				WeightedPerformanceMeasures.reweightExamples(splittedSet, wp.getContingencyMatrix(), allowSkew);

				// handle test set: reweight it separately, use its estimates
				// for future predictions
				splittedSet.selectSingleSubset(1);
				wp = new WeightedPerformanceMeasures(splittedSet);
				this.performance = // performance should be estimated based on the hold-out set 
					WeightedPerformanceMeasures.reweightExamples(splittedSet, wp.getContingencyMatrix(), allowSkew);
			}
			else {
				// train one model per iteration
				model = this.trainBaseModel(trainingSet);
				model.apply(trainingSet);

				// get the weighted performance value of the example set with
				// respect to the model
				wp = new WeightedPerformanceMeasures(trainingSet);

				// Reweight the example set with respect to the weighted
				// performance values:
				this.performance = this.reweightExamples(wp, trainingSet);
			}
			PredictionModel.removePredictedLabel(trainingSet);

			if (classPriors.length == 2) {
				//this.debugMessage(wp);
			}

			// Stop if only one class is present/left.
			if (wp.getNumberOfNonEmptyClasses() < 2) {
				// Using the model here is just necessary to avoid a
				// NullPointerException if this is the first iteration.
				// One could use an empty model instead:
				modelInfo.add(new BayBoostBaseModelInfo(model, wp.getContingencyMatrix()));
					
				break L; // No more iterations!
			}

			final ContingencyMatrix cm = wp.getContingencyMatrix();

			// Add the new model and its weights to the collection of models:
			modelInfo.add(new BayBoostBaseModelInfo(model, cm));

			if (this.isModelUseful(cm) == false) {
				// If the model is not considered to be useful (low advantage)
				// then discard it and stop.
				log("Discard model because of low advantage on training data.");
				modelInfo.remove(modelInfo.size() - 1);
				break L;
			}

			// Stop if weight is null, because all examples have been explained
			// "deterministically".
			if (this.performance == 0) {
				break L;
			}

			// Optimize weights if turned on.
			// The iteration counter starts from 0, so (i + 1) mod optimizeWeightNum needs to be 0.
			// if ((optimizeWeightsNum > 0) && (canOptimiseWeights)
			//      && ((i + 1) % optimizeWeightsNum == 0)) {
			//    canOptimiseWeights = this.adjustBaseModelWeights(trainingSet, modelInfo, classPriors);
			// }
            
            inApplyLoop();
		}

		// Build a Model object. Last parameter is "crispPredictions", nowadays
		// always true.
		return new BayBoostModel(trainingSet, modelInfo, classPriors);
	}

    /*
	private void debugMessage(WeightedPerformanceMeasures wp) {
		String message = Tools.getLineSeparator() + "Model learned - training performance of base learner:" + Tools.getLineSeparator() + "TPR: " + wp.getProbability(0, 0) + " FPR: " + wp.getProbability(1, 0) + " | Positively predicted: " + (wp.getProbability(1, 0) + wp.getProbability(0, 0)) + Tools.getLineSeparator() + "FNR: " + wp.getProbability(0, 1) + " TNR: "
				+ wp.getProbability(1, 1) + " | Negatively predicted: " + (wp.getProbability(0, 1) + wp.getProbability(1, 1)) + Tools.getLineSeparator() + "Positively labelled: " + (wp.getProbability(0, 0) + wp.getProbability(0, 1)) + Tools.getLineSeparator() + "Negatively labelled: " + (wp.getProbability(1, 0) + wp.getProbability(1, 1));

		LogService.logMessage(message, LogService.STATUS);
	}
    */

	/**
	 * This method reweights the example set with respect to the
	 * <code>WeightedPerformanceMeasures</code> object. Please note that the
	 * weights will not be reset at any time, because they continuously change
	 * from one iteration to the next. This method does not change the priors of
	 * the classes.
	 * 
	 * @param wp
	 *            the WeightedPerformanceMeasures to use
	 * @param exampleSet
	 *            <code>ExampleSet</code> to be reweighted
	 * @return the total weight of examples as an error estimate
	 */
	protected double reweightExamples(WeightedPerformanceMeasures wp, ExampleSet exampleSet)
		throws OperatorException
	{
		boolean allowMarginalSkews = this.getParameterAsBoolean(PARAMETER_ALLOW_MARGINAL_SKEWS);
		double remainingWeight = WeightedPerformanceMeasures.
			reweightExamples(exampleSet, wp.getContingencyMatrix(), allowMarginalSkews);

		return remainingWeight;
	}

	
	/**
	 * This helper method takes as input the traing set plus class priors, and
	 * the set of models trained so far. It optimises the weights iteratively,
	 * which means that it changes the contents of the modelInfo container.
	 * Works with crisp base classifiers, only!
	 * 
	 * @param exampleSet
	 *            the training set to be used to tune the weights
	 * @param modelInfo
	 *            the modelInfos with old weights
	 * @param classPriors
	 *            the prior of all classes in the originally unweighted training
	 *            set
	 * @return <code>true</code> if optimisation was successful, and
	 *         <code>false</code> if a non-crisp base classifier was found.
	 */
	
	/*
	private boolean adjustBaseModelWeights(ExampleSet exampleSet, Vector<BayBoostBaseModelInfo> modelInfo, double[] classPriors) throws OperatorException {
		double totalWeight = 0;
		if (modelInfo.size() <= 1)
			return true; // nothing to do

		final double[] weightedPriors; // class priors after the optional
										// initial step of reweighting
		if (this.getParameterAsBoolean(BayesianBoosting.EQUALLY_PROB_LABELS) == true) {
			// If the parameter to rescale priors has been set, then all classes
			// are equally likely by now.
			weightedPriors = new double[classPriors.length];
			for (int i = 0; i < weightedPriors.length; i++) {
				// The odds of p = 0.5 are 0.5 / ( 1 - 0.5 ) = 1, so this makes
				// the term drop out in the
				// method
				// WeightedPerformanceMeasures.reweightExamples(ExampleSet,
				// double[][], double[]).
				weightedPriors[i] = 0.5;
			}
		} else { // Otherwise the initially computed priors remain valid.
			weightedPriors = classPriors;
		}

		// *********** Optimisation loop: ***********

		// for (int j=modelInfo.size()-2; j>=0; j--)
		L: for (int j = 0; j < modelInfo.size(); j++) {
			// p("** Retraining model: " + j + Tools.getLineSeparator());

			// initial weighting:
			this.setInitialWeights(exampleSet, classPriors);
			// p("Initial weights in adjustBaseModelWeights:" + Tools.getLineSeparator());
			// firstTenWeights(exampleSet);

			// apply all models but the considered one:
			if (applyAllButJthModel(exampleSet, modelInfo, j, weightedPriors) == false)
				return false; // found an unsupported soft classifier

			// p("** Applying considered model." + Tools.getLineSeparator());
			Model consideredModel = modelInfo.get(j).getModel();
			consideredModel.apply(exampleSet);

			WeightedPerformanceMeasures wp = new WeightedPerformanceMeasures(exampleSet);

			totalWeight = this.reweightExamples(wp, exampleSet);
			if (totalWeight <= 0) { // only re-estimate if remaining total
									// weight is > 0
				break L;
			}
			modelInfo.set(j, new BayBoostBaseModelInfo(consideredModel, wp.getContingencyMatrix()));

			PredictionModel.removePredictedLabel(exampleSet);
		}
		this.performance = totalWeight;
		return true;
	}
*/
	/**
	 * Helper method.
	 * 
	 * @param exampleSet
	 *            the example set to be reweighted by the models plus lift
	 *            estimates
	 * @param modelInfo
	 *            the set of models and corresponding lift estimates
	 * @param j
	 *            the number of model not to be applied classPriors the class
	 *            priors computed by <code>WeightedPerformanceMeasures</code>
	 * @return <code>true</code> iff the modelInfo contains no (invalid) soft
	 *         classifier
	 * @throws OperatorException
	 */
	/*
	private boolean applyAllButJthModel(ExampleSet exampleSet, Vector<BayBoostBaseModelInfo> modelInfo, int j, double[] classPriors) throws OperatorException {
		// apply all model but the j-th, then estimate parameters for j-th model
		for (int index = 0; index < modelInfo.size(); index++) {
			if (index != j) {
				BayBoostBaseModelInfo currentModelInfo = modelInfo.get(index);
				currentModelInfo.getModel().apply(exampleSet);
				BayesianBoosting.reweightExamples(exampleSet, currentModelInfo.getContingencyMatrix());
				PredictionModel.removePredictedLabel(exampleSet);
			}
		}
		return true;
	}
	*/
	
	/**
	 * Helper method.
	 * 
	 * @param exampleSet
	 *            the example set which needs to be prepared with the initial
	 *            weighting
	 * @param classPriors
	 *            Array of class priors. This parameter only has an effect if
	 *            the parameter <code>EQUALLY_PROB_LABELS</code> is set,
	 *            otherwise all examples are assigned a weight of 1.
	 */
	/*
	private void setInitialWeights(ExampleSet exampleSet, double[] classPriors) {
		double[] weights = new double[classPriors.length];
		if (this.getParameterAsBoolean(BayesianBoosting.EQUALLY_PROB_LABELS) == true) {
			for (int k = 0; k < weights.length; k++) {
				weights[k] = ((double) 1.0) / (((double) weights.length) * (classPriors[k]));
			}
		} else {
			for (int k = 0; k < weights.length; k++) {
				weights[k] = 1;
			}
		}

		Iterator<Example> exRead = exampleSet.getExampleReader();
		while (exRead.hasNext()) {
			Example example = exRead.next();
			example.setWeight(weights[(int) (example.getLabel())]);
		}
	}
	*/

	/**
	 * Helper method to decide whether a model improves the training error
	 * enough to be considered.
	 * 
	 * @param cm
	 *            the lift ratio matrix as returned by the getter of the
	 *            WeightedPerformance class
	 * @return <code>true</code> iff the advantage is high enough to consider
	 *         the model to be useful
	 */
	private boolean isModelUseful(ContingencyMatrix cm) {
		// should rather be decided offline by properly setting
		// the number of iterations
		return true; 
		/*
		for (int row=0; row<cm.getNumberOfClasses(); row++) {
			for (int col=0; col<cm.getNumberOfPredictions(); col++) {
				double current = cm.getLift(row, col);
				if (current > MIN_ADVANTAGE)
					return true;
			}
		}
		return false;
		*/ 
	}

    /**
     * Adds the parameters &quot;number of iterations&quot; and &quot;model
     * file&quot;.
     */
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeBoolean(PARAMETER_RESCALE_LABEL_PRIORS, "Specifies whether the proportion of labels should be equal by construction after first iteration .", false));
        types.add(new ParameterTypeDouble(PARAMETER_USE_SUBSET_FOR_TRAINING, "Fraction of examples used for training, remaining ones are used to estimate the confusion matrix. Set to 1 to turn off test set.", 0, 1, 1));
        types.add(new ParameterTypeInt(PARAMETER_ITERATIONS, "The maximum number of iterations.", 1, Integer.MAX_VALUE, 10));
        types.add(new ParameterTypeBoolean(PARAMETER_ALLOW_MARGINAL_SKEWS, "Allow to skew the marginal distribution (P(x)) during learning.", true));
        // types.add(new ParameterTypeInt(ADJUST_WEIGHTS, "If set to a value
        // greater than 0 then classifier weights are optimised every that
        // number of iterations has passed.", 0, Integer.MAX_VALUE, 0));
        return types;
    }
}
