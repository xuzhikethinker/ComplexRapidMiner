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
package com.rapidminer.operator.generator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/** A target function for regression labels, i.e. continous numercial labels. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: RegressionFunction.java,v 1.1 2007/05/27 21:58:45 ingomierswa Exp $
 */
public abstract class RegressionFunction implements TargetFunction {

	private double lower = -10.0d;

	private double upper = 10.0d;

	/** Does nothing. */
	public void init(RandomGenerator random) {}

	public void setLowerArgumentBound(double lower) {
		this.lower = lower;
	}

	public void setUpperArgumentBound(double upper) {
		this.upper = upper;
	}

	/** Does nothing. */
	public void setTotalNumberOfExamples(int number) {}

	/** Does nothing. */
	public void setTotalNumberOfAttributes(int number) {}

	public Attribute getLabel() {
		return AttributeFactory.createAttribute("label", Ontology.REAL);
	}

	public double[] createArguments(int dimension, RandomGenerator random) {
		double[] args = new double[dimension];
		for (int i = 0; i < args.length; i++)
			args[i] = random.nextDoubleInRange(lower, upper);
		return args;
	}
}
