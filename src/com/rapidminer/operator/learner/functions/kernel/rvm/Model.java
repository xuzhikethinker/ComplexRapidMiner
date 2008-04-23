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
package com.rapidminer.operator.learner.functions.kernel.rvm;

import java.io.Serializable;

import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelBasisFunction;
import com.rapidminer.tools.RandomGenerator;


/**
 * The lerned model.
 * 
 * @author Piotr Kasprzak, Ingo Mierswa
 * @version $Id: Model.java,v 1.1 2007/05/29 00:13:01 ingomierswa Exp $
 *
 */
public class Model implements Serializable {

	private static final long serialVersionUID = -8223343885533884477L;
	
	private KernelBasisFunction[] kernels;			// The unpruned kernels
	private double[] weights;						// Associated weights
	
	private boolean bias;							// Bias
	
	private boolean regression;						// Regression or classification model
	
	/** Constructors */
	
    // for reflection creation
    private Model() {}
    
	public Model(double[] weights, KernelBasisFunction[] kernels, boolean bias, boolean regression) {	
		this.kernels		= kernels;
		this.weights		= weights;
		this.bias			= bias;
		this.regression		= regression;
	}
    
    public int getNumberOfRelevanceVectors() { 
        return weights.length;
    }
    
    public double getWeight(int index) {
        return weights[index];
    }
    
	/** Model application. Returns the function value, not a crisp prediction. */
    public double applyToVector(double[] vector) {
    	int j;
    	double prediction;
	    if (bias) {
	    	j 			= 1;
	    	prediction	= weights[0];
	    } else {
	    	j			= 0;
	    	prediction	= 0;
	    }
	    for (; j < weights.length; j++) {
	    	prediction += weights[j] * kernels[j].eval(vector);
	    }
        
	    return prediction;
    }
    
    public double[] apply(double[][] inputVectors) {   	
    	double[] prediction = new double[inputVectors.length];
    	for (int i = 0; i < inputVectors.length; i++) {
    	    prediction[i] = applyToVector(inputVectors[i]);
            if (!regression) {
                if (prediction[i] > 0.0)
                    prediction[i] = 1.0;
                else 
                    prediction[i] = 0.0;
            }
    	}
        
    	return prediction;
    }
    
	public double norm_l2(double[] vector) {
		double result = 0;
		for (int i = 0; i < vector.length; i++) {
			result += vector[i] * vector[i];
		}
		return Math.sqrt(result);
	}
	
	public double calculate(double[] att) {
		double noise 	= 0.1;
		double norm		= norm_l2(att);	
		return (Math.sin(norm) / norm + noise * RandomGenerator.getRandomGenerator(0).nextDouble() - 0.5 * noise);  
	}
}
