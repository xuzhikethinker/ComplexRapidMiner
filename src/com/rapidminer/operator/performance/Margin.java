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
package com.rapidminer.operator.performance;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;


/**
 * The margin of a classifier, defined as the minimal confidence for the correct label.
 *  
 * @author Martin Scholz
 * @version $Id: Margin.java,v 1.2 2007/07/13 22:52:13 ingomierswa Exp $
 */
public class Margin extends MeasuredPerformance {

	private static final long serialVersionUID = -2987795640706342168L;

	/** The value of the criterion. */
	private double margin = Double.NaN;

	/** A counter for average building. */
	private int counter = 1;

	/** Clone constructor. */
	public Margin() {}

	public Margin(Margin m) {
		super(m);
		this.margin = m.margin;
		this.counter = m.counter;
	}

	/** Calculates the margin. */
	public void startCounting(ExampleSet exampleSet) throws OperatorException {
		// compute margin
		Iterator<Example> reader = exampleSet.iterator();
		this.margin = 1.0d;
		Attribute labelAttr = exampleSet.getAttributes().getLabel();
		while (reader.hasNext()) {
			Example example = reader.next();
			String trueLabel = example.getNominalValue(labelAttr);
			double confidence = example.getConfidence(trueLabel);
			this.margin = Math.min(margin, confidence);
		}
	}

	/** Does nothing. Everything is done in {@link #startCounting(ExampleSet)}. */
	public void countExample(Example example) {}

	public int getExampleCount() {
		return 1;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public double getMikroAverage() {
		return margin / counter;
	}

	/** Returns the fitness. */
	public double getFitness() {
		return getAverage();
	}

	public String getName() {
		return "margin";
	}

	public String getDescription() {
		return "The margin of a classifier, defined as the minimal confidence for the correct label.";
	}

	public void buildSingleAverage(Averagable performance) {
		Margin other = (Margin) performance;
		this.counter += other.counter;
		this.margin += other.margin;
	}

	/** Returns the super class implementation of toString(). */
	public String toString() {
		return super.toString();
	}
}
