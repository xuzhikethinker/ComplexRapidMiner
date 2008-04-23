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
package com.rapidminer.operator.learner.bayes;

import com.rapidminer.operator.learner.Distribution;
import com.rapidminer.tools.Tools;


/**
 * NormalDistribution is a distribution, calculating the probaility 
 * for a given value from an gaussian normal distribution.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NormalDistribution.java,v 1.2 2007/06/22 22:39:09 ingomierswa Exp $
 */
public class NormalDistribution implements Distribution {

	private static final long serialVersionUID = -1819042904676198636L;

	private double mean;

	private double variance;

	private double scaleFactor;

	public NormalDistribution(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
		this.scaleFactor = 1 / (variance * Math.sqrt(2 * Math.PI));
	}

	public double getProbability(double x) {
		double distr = Math.exp(-0.5 * (Math.pow((x - mean) / variance, 2)));
		/*
		if (distr * scaleFactor > 1) {
			System.out.println("ds");
		}
		*/
		return scaleFactor * distr;
	}

	public String toString() {
		return ("Numerical --> mean: " + Tools.formatNumber(mean) + ", standard deviation: " + Tools.formatNumber(variance));
	}
}
