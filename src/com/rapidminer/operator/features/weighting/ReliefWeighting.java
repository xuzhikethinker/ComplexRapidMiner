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
package com.rapidminer.operator.features.weighting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Tools;


/**
 * <p>Relief measures the relevance of features by sampling examples 
 * and comparing the value of the current feature for the nearest 
 * example of the same and of a different class. This version also
 * works for multiple classes and regression data sets. The resulting
 * weights are normalized into the interval between 0 and 1.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: ReliefWeighting.java,v 1.2 2007/07/10 18:02:02 ingomierswa Exp $
 */
public class ReliefWeighting extends Operator {


	/** The parameter name for &quot;Number of nearest neigbors for relevance calculation.&quot; */
	public static final String PARAMETER_NUMBER_OF_NEIGHBORS = "number_of_neighbors";

	/** The parameter name for &quot;Number of examples used for determining the weights.&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	/** Helper class holding the index of an example and the distance to current example. */
	private static class IndexDistance implements Comparable<IndexDistance> {
		
		private int exampleIndex;
		private double distance;
		
		public IndexDistance(int index, double distance) {
			this.exampleIndex = index;
			this.distance = distance;
		}
		
		public int getIndex() {
			return exampleIndex;
		}
		
		public double getDistance() {
			return distance;
		}

		public int hashCode() {
			return Double.valueOf(this.distance).hashCode();
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof IndexDistance))
				return false;
			IndexDistance o = (IndexDistance)other;
			return this.distance == o.distance;
		}
		
		public int compareTo(IndexDistance o) {
			return Double.compare(this.distance, o.distance);
		}
		
		public String toString() { 
			return exampleIndex + " (d: " + Tools.formatNumber(distance) + ")";
		}
	}
	
	
	double differentLabelWeight;
	double[] differentAttributesWeights;
	double[] differentLabelAndAttributesWeights;
	double[] classProbabilities;
	    
	       
	public ReliefWeighting(OperatorDescription description) {
		super(description);
	}
  
	public IOObject[] apply() throws OperatorException {
	    ExampleSet inputSet = getInput(ExampleSet.class);
	    inputSet.recalculateAllAttributeStatistics();
	    
	    // checks
	    Attribute label = inputSet.getAttributes().getLabel();
	    if (label == null)
	    	throw new UserError(this, 105);
	    
	    // init	weights
	    AttributeWeights weights = new AttributeWeights(inputSet);
	    for (Attribute attribute : inputSet.getAttributes()) {
	    	weights.setWeight(attribute.getName(), 0.0d);
	    }
	   
	    // calculate class probabilities for nominal labels and initialize 
	    // vectors for numerical labels
	    this.differentLabelWeight = 0;
	    this.differentAttributesWeights = new double[inputSet.getAttributes().size()];
	    this.differentLabelAndAttributesWeights = new double[inputSet.getAttributes().size()];
	    this.classProbabilities = null;
	    if (label.isNominal()) {
	    	classProbabilities = new double[label.getMapping().size()];
	    	int counter = 0;
	    	for (String value : label.getMapping().getValues()) {
	    		classProbabilities[counter++] = inputSet.getStatistics(label, Statistics.COUNT, value) / inputSet.size();
	    	}
	    }
	    
	    // number of neighbors
	    int numberOfNeighbors = getParameterAsInt(PARAMETER_NUMBER_OF_NEIGHBORS);
	    
	    double sampleRatio = getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
	    ExampleSet exampleSet = inputSet;
	    if (sampleRatio < 1.0d) {
	    	exampleSet = new SplittedExampleSet(inputSet, sampleRatio, SplittedExampleSet.STRATIFIED_SAMPLING, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
	    	((SplittedExampleSet)exampleSet).selectSingleSubset(0);
	    }
	    
	    int exampleCounter = 0;
	    for (Example example : exampleSet) {
	    	Map<String, SortedSet<IndexDistance>> neighborSets = 
	    		searchNeighbors(exampleSet, example, exampleCounter, label, numberOfNeighbors);
	    	if (label.isNominal()) {
	    		updateWeightsClassification(neighborSets, exampleSet, example, weights, label);
	    	} else {
	    		updateWeightsRegression(neighborSets, exampleSet, example, weights, label, numberOfNeighbors);
	    	}
	    	exampleCounter++;
	    }
	    
	    // calculate final weights for regression
	    if (!label.isNominal()) {
	    	int attributeCounter = 0;
	    	for (Attribute attribute : exampleSet.getAttributes()) {
	    		double weight = 
	    			differentLabelAndAttributesWeights[attributeCounter] / differentLabelWeight -
	    			((differentAttributesWeights[attributeCounter] - differentLabelAndAttributesWeights[attributeCounter]) /
	    			 (exampleSet.size() - differentLabelWeight));
	    		weights.setWeight(attribute.getName(), weight);
	    		attributeCounter++;
	    	}
	    }
	    
	    // normalize weights
	    weights.normalize();
	    
	    return new IOObject[] { exampleSet, weights };
	}

	private void updateWeightsRegression(Map<String, SortedSet<IndexDistance>> neighborSets,
			                             ExampleSet exampleSet,
			                             Example example, 
			                             AttributeWeights weights,
			                             Attribute label,
			                             int numberOfNeighbors) {
		Iterator<IndexDistance> i = neighborSets.get("regression").iterator();
		while (i.hasNext()) {
			IndexDistance indexDistance = i.next();
			Example neighbor = exampleSet.getExample(indexDistance.getIndex());
			double labelDiff = normedDifference(example, neighbor, exampleSet, label);
			// no weighting by distance --> same influence for all neighbors
			differentLabelWeight += labelDiff / numberOfNeighbors;
			
			int attributeCounter = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				double diff = normedDifference(example, neighbor, exampleSet, attribute);
				// no weighting by distance --> same influence for all neighbors
				differentAttributesWeights[attributeCounter] += diff / numberOfNeighbors;
				differentLabelAndAttributesWeights[attributeCounter] += labelDiff * diff / numberOfNeighbors;
				attributeCounter++;
			}
		}
	}
	
	private void updateWeightsClassification(Map<String, SortedSet<IndexDistance>> neighborSets,
							                 ExampleSet exampleSet,
							                 Example example, 
							                 AttributeWeights weights,
							                 Attribute label) {
		double classProbabilityNormalization = 1.0d - classProbabilities[(int)example.getValue(label)];
		int classCounter = 0;
		for (String classValue : label.getMapping().getValues()) {
			Iterator<IndexDistance> i = neighborSets.get(classValue).iterator();
			while (i.hasNext()) {
				IndexDistance indexDistance = i.next();
				Example neighbor = exampleSet.getExample(indexDistance.getIndex());	
				int attributeCounter = 0;
				for (Attribute attribute : exampleSet.getAttributes()) {
					double weight = weights.getWeight(attribute.getName());	
					double diff = normedDifference(example, neighbor, exampleSet, attribute);
					if (classValue.equals(example.getValueAsString(label))) {
						// hit
						weight -= diff / exampleSet.size();
					} else { 
						// miss
						weight += 
							classProbabilities[classCounter] / 
							classProbabilityNormalization * 
							diff / exampleSet.size();
					}
					weights.setWeight(attribute.getName(), weight);
					attributeCounter++;
				}
			}
			classCounter++;
		}
	}
	
	private double normedDifference(Example first, Example second, ExampleSet exampleSet, Attribute attribute) {
		double diff = Math.abs(first.getValue(attribute) - second.getValue(attribute));
		if (attribute.isNominal()) {
			if (diff == 0)
				return 0;
			else
				return 1;
		} else {
		    double min = exampleSet.getStatistics(attribute, Statistics.MINIMUM);
            double max = exampleSet.getStatistics(attribute, Statistics.MAXIMUM);
			return (diff - min) / (max - min);
		}
	}
	
	private Map<String, SortedSet<IndexDistance>> searchNeighbors(ExampleSet exampleSet, 
			                                                      Example example, 
			                                                      int exampleIndex, 
			                                                      Attribute label, 
			                                                      int numberOfNeighbors) {
		
		Map<String, SortedSet<IndexDistance>> neighborSets = new HashMap<String, SortedSet<IndexDistance>>();
		if (label.isNominal()) {
			for (String value : label.getMapping().getValues()) {
				neighborSets.put(value, new TreeSet<IndexDistance>());
			}
		} else {
			neighborSets.put("regression", new TreeSet<IndexDistance>());
		}
		
		int exampleCounter = 0;
		for (Example candidate : exampleSet) {
			if (exampleIndex != exampleCounter) {
				double distance = calculateDistance(example, candidate);
				SortedSet<IndexDistance> currentSet = null;
				if (label.isNominal()) {
					String classValue = candidate.getValueAsString(label);
					currentSet = neighborSets.get(classValue);
				} else {
					currentSet = neighborSets.get("regression");
				}
				currentSet.add(new IndexDistance(exampleCounter, distance));
				if (currentSet.size() > numberOfNeighbors)
					currentSet.remove(currentSet.last());
			}
			exampleCounter++;
		}
		
		return neighborSets;
	}

	/** Calculates the euclidean distance between both examples. */
	private double calculateDistance(Example first, Example second) {
	    double distance = 0;
	    for (Attribute attribute : first.getAttributes()) {
	    	double diff = first.getValue(attribute) - second.getValue(attribute);
	    	distance += diff * diff;
	    }
	    return Math.sqrt(distance);
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_NEIGHBORS, "Number of nearest neigbors for relevance calculation.", 1, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "Number of examples used for determining the weights.", 0.0d, 1.0d, 1.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
