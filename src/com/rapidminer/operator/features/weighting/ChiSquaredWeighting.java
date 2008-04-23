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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.discretization.SimpleBinDiscretization;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.math.ContingencyTableTools;


/**
 * This operator calculates the relevance of a feature by computing 
 * for each attribute of the input example set the value of the 
 * chi-squared statistic with respect to the class attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: ChiSquaredWeighting.java,v 1.1 2007/06/16 03:28:12 ingomierswa Exp $
 */
public class ChiSquaredWeighting extends Operator {

	public ChiSquaredWeighting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		Attribute label = exampleSet.getAttributes().getLabel();
		if (!label.isNominal()) {
			throw new UserError(this, 101, "chi squared test", label.getName());
		}
		
		Operator discretization = null;
		try {
			discretization = OperatorService.createOperator(SimpleBinDiscretization.class);
		} catch (OperatorCreationException e) {
			throw new UserError(this, 904, "Discretization", e.getMessage());
		}
		
		int numberOfBins = getParameterAsInt(SimpleBinDiscretization.NUMBER_OF_BINS);
		discretization.setParameter(SimpleBinDiscretization.NUMBER_OF_BINS, numberOfBins + "");
		IOContainer ioContainer = new IOContainer(new IOObject[] { exampleSet });
		ioContainer = discretization.apply(ioContainer);
		exampleSet = ioContainer.get(ExampleSet.class);

	    // init
	    double[][][] counters = new double[exampleSet.getAttributes().size()][numberOfBins][label.getMapping().size()];
	    Attribute weightAttribute = exampleSet.getAttributes().getWeight();
	    
	    // count
	    int exampleCounter = 0;
	    double[] temporaryCounters = new double[label.getMapping().size()];
	    for (Example example : exampleSet) {
	    	double weight = 1.0d;
	    	if (weightAttribute != null) {
	    		weight = example.getValue(weightAttribute);
	    	}
	    	int labelIndex = (int)example.getLabel();
	    	temporaryCounters[labelIndex] += weight;
	    	exampleCounter++;
	    }
	    
	    for (int k = 0; k < counters.length; k++) {
	    	for (int i = 0; i < temporaryCounters.length; i++) {
	    		counters[k][0][i] = temporaryCounters[i];
	    	}
	    }

	    // attribute counts
	    for (Example example : exampleSet) {
	    	int labelIndex = (int)example.getLabel();
	    	double weight = 1.0d;
	    	if (weightAttribute != null) {
	    		weight = example.getValue(weightAttribute);
	    	}
	    	int attributeCounter = 0;
	    	for (Attribute attribute : exampleSet.getAttributes()) {
	    		int attributeIndex = (int)example.getValue(attribute);
	    		counters[attributeCounter][attributeIndex][labelIndex] += weight;
	    		counters[attributeCounter][0][labelIndex] -= weight;
	    		attributeCounter++;
	    	}
	    }
	    	    
	    // calculate the actual chi-squared values and assign them to weights
		AttributeWeights weights = new AttributeWeights(exampleSet);
		int attributeCounter = 0;
	    for (Attribute attribute : exampleSet.getAttributes()) {
	    	double weight = ContingencyTableTools.getChiSquaredStatistics(ContingencyTableTools.deleteEmpty(counters[attributeCounter]), false);
	    	weights.setWeight(attribute.getName(), weight);
	    	attributeCounter++;
	    }
	    
	    // normalization
	    weights.normalize();
	    
		return new IOObject[] { exampleSet, weights };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}
	
	public List<ParameterType> getParameterTypes() { 
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(SimpleBinDiscretization.NUMBER_OF_BINS, "The number of bins used for discretization of numerical attributes before the chi squared test can be performed.", 2, Integer.MAX_VALUE, 10));
		return types;
	}
}
