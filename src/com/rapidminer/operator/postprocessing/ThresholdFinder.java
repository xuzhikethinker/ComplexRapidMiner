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
package com.rapidminer.operator.postprocessing;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This operator finds the best threshold for crisp classifying based on user
 * defined costs.
 * 
 * @author Martin Scholz, Ingo Mierswa
 * @version $Id: ThresholdFinder.java,v 1.2 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class ThresholdFinder extends Operator {

	// The parameters of this operator:
	private static final String PARAMETER_MISCLASSIFICATION_COSTS_FIRST = "misclassification_costs_first";

	private static final String PARAMETER_MISCLASSIFICATION_COSTS_SECOND = "misclassification_costs_second";

	private static final String PARAMETER_SHOW_ROC_PLOT = "show_roc_plot";

	public ThresholdFinder(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// sanity checks
		ExampleSet exampleSet = this.getInput(ExampleSet.class);
		Attribute label = exampleSet.getAttributes().getLabel();
		exampleSet.recalculateAttributeStatistics(label);
		if (label == null)
			throw new UserError(this, 105);
		if (!label.isNominal())
			throw new UserError(this, 101, label, "threshold finding");
		if (label.getMapping().size() != 2)
			throw new UserError(this, 118, new Object[] { label, Integer.valueOf(label.getMapping().getValues().size()), Integer.valueOf(2) });

		// create ROC data
		ROCDataGenerator rocDataGenerator = new ROCDataGenerator(getParameterAsDouble(PARAMETER_MISCLASSIFICATION_COSTS_SECOND), getParameterAsDouble(PARAMETER_MISCLASSIFICATION_COSTS_FIRST));
		List<double[]> rocData = rocDataGenerator.createROCDataList(exampleSet);

		// create plotter
		if (getParameterAsBoolean(PARAMETER_SHOW_ROC_PLOT))
			rocDataGenerator.createROCPlot(rocData, true, true);

		// create and return output
		return new IOObject[] { exampleSet, new Threshold(rocDataGenerator.getBestThreshold(), label.getMapping().getNegativeString(), label.getMapping().getPositiveString()) };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Threshold.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		list.add(new ParameterTypeDouble(PARAMETER_MISCLASSIFICATION_COSTS_FIRST, "The costs assigned when an example of the first class is classified as one of the second.", 0, Double.POSITIVE_INFINITY, 1)); 
		list.add(new ParameterTypeDouble(PARAMETER_MISCLASSIFICATION_COSTS_SECOND, "The costs assigned when an example of the second class is classified as one of the first.", 0, Double.POSITIVE_INFINITY, 1)); 
		list.add(new ParameterTypeBoolean(PARAMETER_SHOW_ROC_PLOT, "Display a plot of the ROC curve.", false)); 
		return list;
	}
}
