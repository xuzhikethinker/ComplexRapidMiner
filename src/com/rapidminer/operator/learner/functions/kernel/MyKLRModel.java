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
package com.rapidminer.operator.learner.functions.kernel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.svm.SVMInterface;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;


/**
 * The model for the MyKLR learner by Stefan Rueping.
 * 
 * @author Ingo Mierswa
 * @version $Id: MyKLRModel.java,v 1.6 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class MyKLRModel extends AbstractMySVMModel {

	private static final long serialVersionUID = 8033254475867697195L;

	public MyKLRModel(ExampleSet exampleSet, com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples model, Kernel kernel, int kernelType) {
		super(exampleSet, model, kernel, kernelType);
	}
    
    public String getModelInfo() {
        return 
           "KLR Model (" + getNumberOfSupportVectors() + " support vectors)";
    }

	public void apply(ExampleSet exampleSet) throws OperatorException {
		super.apply(exampleSet);
	}

	public SVMInterface createSVM() {
		return new KLR();
	}

	public void setPrediction(Example example, double _prediction) {
        double prediction = _prediction - 0.5d;
		Attribute predLabel = example.getAttributes().getPredictedLabel();
		int index = prediction > 0.0 ? predLabel.getMapping().getPositiveIndex() : predLabel.getMapping().getNegativeIndex();
		example.setValue(predLabel, index);
		example.setConfidence(predLabel.getMapping().getPositiveString(), 1.0d / (1.0d + java.lang.Math.exp(-prediction)));
		example.setConfidence(predLabel.getMapping().getNegativeString(), 1.0d / (1.0d + java.lang.Math.exp(prediction)));
	}
}
