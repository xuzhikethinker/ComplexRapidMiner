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
package com.rapidminer.operator.learner.tree;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * Calculates frequencies and weights.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FrequencyCalculator.java,v 1.3 2007/06/19 22:23:12 ingomierswa Exp $
 */
public class FrequencyCalculator {

    public FrequencyCalculator() {}
    
    /** Returns an array of the size of the partitions. 
     *  Each entry contains the sum of all weights of the 
     *  corresponding partition. */
    public double[] getPartitionWeights(SplittedExampleSet splitted) {
        Attribute weightAttribute = splitted.getAttributes().getWeight();
        double[] weights = new double[splitted.getNumberOfSubsets()];
        for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
            splitted.selectSingleSubset(i);
            for (Example e : splitted) {
                double weight = 1.0d;
                if (weightAttribute != null) {
                    weight = e.getValue(weightAttribute);
                }
                weights[i] += weight;
            }
        }
        return weights;
    }
    
    /** Returns an array of size of the number of different label
     *  values. Each entry corresponds to the weight sum of all
     *  examples with the current label. */
    public double[] getLabelWeights(ExampleSet exampleSet) {
        Attribute label = exampleSet.getAttributes().getLabel();
        Attribute weightAttribute = exampleSet.getAttributes().getWeight();
        double[] weights = new double[label.getMapping().size()];
        for (Example e : exampleSet) {
            int labelIndex = (int)e.getValue(label);
            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = e.getValue(weightAttribute);
            }
            weights[labelIndex] += weight;
        }
        return weights;
    }
    
    /** Returns the sum of the given weights. */
    public double getTotalWeight(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }
}
