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
package com.rapidminer.tools.math.optimization.ec.es;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Checks for each value if it should mutated. Sets a non-min value
 * to min and a zero value to max.
 * 
 * @author Ingo Mierswa
 * @version $Id: SwitchingMutation.java,v 1.4 2006/04/05 08:57:26 ingomierswa
 *          Exp $
 */
public class SwitchingMutation implements PopulationOperator {

	private double prob;

	private double[] min, max;

    private int[] valueTypes;
    
    private Random random;
    
    
	public SwitchingMutation(double prob, double[] min, double[] max, int[] valueTypes, Random random) {
		this.prob = prob;
		this.min = min;
        this.max = max;
        this.valueTypes = valueTypes;
        this.random = random;
	}

	public void operate(Population population) {
		List<Individual> newIndividuals = new LinkedList<Individual>();
		for (int i = 0; i < population.getNumberOfIndividuals(); i++) {
			Individual clone = (Individual) population.get(i).clone();
			double[] values = clone.getValues();
			boolean changed = false;
			for (int j = 0; j < values.length; j++) {
				if (random.nextDouble() < prob) {
					changed = true;
					if (values[j] != min[j])
						values[j] = min[j];
					else
						values[j] = max[j];
                    if (valueTypes[j] == ESOptimization.VALUE_TYPE_INT)
                        values[j] = (int)Math.round(values[j]);
				}
			}
			if (changed) {
				clone.setValues(values);
				newIndividuals.add(clone);
			}
		}
		population.addAll(newIndividuals);
	}
}
