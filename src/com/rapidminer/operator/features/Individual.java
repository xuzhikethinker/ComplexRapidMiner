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
package com.rapidminer.operator.features;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.performance.PerformanceVector;


/**
 * Individuals contain all necessary informations about example sets for
 * population based search heuristics, including the performance. Each
 * individiual can also handle a crowding distance for multi-objecitve
 * optimization approaches.
 * 
 * @author Ingo Mierswa
 * @version $Id: Individual.java,v 1.1 2007/05/27 21:59:44 ingomierswa Exp $
 */
public class Individual {

	/** The example set. */
	private AttributeWeightedExampleSet exampleSet;

	/**
	 * The performance this example set has achieved during evaluation. Null if
	 * no evaluation has been performed so far.
	 */
	private PerformanceVector performanceVector = null;

	/** The crowding distance can used for multiobjective optimization schemes. */
	private double crowdingDistance = Double.NaN;

	/**
	 * Some search schemes use attribute weights to guide the search point
	 * operations.
	 */
	private AttributeWeights attributeWeights = null;

	/** Creates a new individual. */
	public Individual(AttributeWeightedExampleSet exampleSet) {
		this.exampleSet = exampleSet;
	}

	public AttributeWeightedExampleSet getExampleSet() {
		return this.exampleSet;
	}

	public PerformanceVector getPerformance() {
		return performanceVector;
	}

	public void setPerformance(PerformanceVector performanceVector) {
		this.performanceVector = performanceVector;
	}

	public double getCrowdingDistance() {
		return this.crowdingDistance;
	}

	public void setCrowdingDistance(double crowdingDistance) {
		this.crowdingDistance = crowdingDistance;
	}

	public AttributeWeights getAttributeWeights() {
		return this.attributeWeights;
	}

	public void setAttributeWeights(AttributeWeights weights) {
		this.attributeWeights = weights;
	}

	public String toString() {
		return "#" + exampleSet.getNumberOfUsedAttributes();
	}
}
