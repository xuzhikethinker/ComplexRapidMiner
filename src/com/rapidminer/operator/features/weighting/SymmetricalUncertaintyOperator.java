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
 * <p>
 * This operator calculates the relevance of an attribute by measuring 
 * the symmetrical uncertainty with respect to the class. 
 * The formulaization for this is:
 * </p>
 * 
 * <code>relevance = 2 * (P(Class) - P(Class | Attribute)) / P(Class) + P(Attribute)</code>
 * 
 * @author Ingo Mierswa
 * @version $Id: SymmetricalUncertaintyOperator.java,v 1.4 2007/06/15 16:58:37 ingomierswa Exp $
 */
public class SymmetricalUncertaintyOperator extends Operator {

	public SymmetricalUncertaintyOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (!label.isNominal()) {
			throw new UserError(this, 101, "symmetrical uncertainty", label.getName());
		}

		// discretize numerical data
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

		// create and deliver weights
		AttributeWeights weights = new AttributeWeights(exampleSet);
		for (Attribute attribute : exampleSet.getAttributes()) {
			double[][] counters = new double[attribute.getMapping().size()][label.getMapping().size()];
			for (Example example : exampleSet) {
				counters[(int)example.getValue(attribute)][(int)example.getLabel()]++;
			}
			double weight = ContingencyTableTools.symmetricalUncertainty(counters);
			weights.setWeight(attribute.getName(), weight);
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
