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
package com.rapidminer.operator.features.aggregation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.tools.RandomGenerator;


/**
 * Performs an aggregation mutation on integer arrays. Each feature value is
 * mutated with probability 1/n. Mutation is done by randomly selecting a new
 * value between -1 and max(values).
 * 
 * @author Ingo Mierswa
 * @version $Id: AggregationMutation.java,v 1.4 2006/04/05 08:57:23 ingomierswa
 *          Exp $
 */
public class AggregationMutation {

    private RandomGenerator random;
    
    public AggregationMutation(RandomGenerator random) {
        this.random = random;
    }
    
	/** Checks if at least one feature was selected. */
	private boolean isValid(int[] individual) {
		for (int i = 0; i < individual.length; i++) {
			if (individual[i] >= 0)
				return true;
		}
		return false;
	}

	/**
	 * Invokes the method mutate(int[]) for each individual. The parents are
	 * kept.
	 */
	public void mutate(List<AggregationIndividual> population) {
		List<AggregationIndividual> children = new ArrayList<AggregationIndividual>();
		Iterator<AggregationIndividual> i = population.iterator();
		while (i.hasNext()) {
			AggregationIndividual individual = i.next();
			int[] parent = individual.getIndividual();
			int[] child = new int[parent.length];
			for (int j = 0; j < child.length; j++)
				child[j] = parent[j];
			mutate(child);
			if (isValid(child)) {
				children.add(new AggregationIndividual(child));
			}
		}
		population.addAll(children);
	}

	/**
	 * Changes the individual (each gene with probability 1 / n). Make clone if
	 * original individual should be kept.
	 */
	private void mutate(int[] individual) {
		double prob = 1.0d / individual.length;
		int max = -1;
		for (int i = 0; i < individual.length; i++) {
			if (individual[i] > max)
				max = individual[i];
		}
		for (int i = 0; i < individual.length; i++) {
			if (random.nextDouble() < prob) {
				individual[i] = random.nextIntInRange(-1, max + 1);
			}
		}
	}
}
