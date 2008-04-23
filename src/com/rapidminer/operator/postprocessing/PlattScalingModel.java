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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**
 * A model that contains a boolean classifier and a scaling operation that turns
 * confidence scores into probability estimates. It is the result of a
 * <code>PlattScaling</code> operator.
 * 
 * @author Martin Scholz
 * @version $Id: PlattScalingModel.java,v 1.2 2006/04/14 20:53:38 martin_scholz
 *          Exp $
 */
public class PlattScalingModel extends PredictionModel {

	private static final long serialVersionUID = 6281707312532843604L;

	private PlattParameters parameters;

	private Model model;

	public PlattScalingModel(ExampleSet exampleSet, Model model, PlattParameters parameters) {
		super(exampleSet);
		this.model = model;
		this.parameters = parameters;
	}

	public void performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		Attribute label = this.getLabel();
		final int posLabel = label.getMapping().getPositiveIndex();
		final int negLabel = label.getMapping().getNegativeIndex();
		final String posLabelS = label.getMapping().mapIndex(posLabel);
		final String negLabelS = label.getMapping().mapIndex(negLabel);

		model.apply(exampleSet);
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double predicted = PlattScaling.getLogOddsPosConfidence(example.getConfidence(posLabelS));
			double scaledPos = 1.0d / (1.0d + Math.exp(predicted * parameters.getA() + parameters.getB()));
			double scaledNeg = 1.0d - scaledPos;

			example.setValue(predictedLabel, (scaledPos >= scaledNeg) ? posLabel : negLabel);
			example.setConfidence(posLabelS, scaledPos);
			example.setConfidence(negLabelS, scaledNeg);
		}
	}

	/** @return a <code>String</code> representation of this scaling model. */
	public String toString() {
		String result = super.toString() + " (" + this.parameters.toString() + ") " + Tools.getLineSeparator() + "Model: " + model.toResultString();
		return result;
	}
}
