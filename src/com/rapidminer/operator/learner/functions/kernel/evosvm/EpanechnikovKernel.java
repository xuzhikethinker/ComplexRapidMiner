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
package com.rapidminer.operator.learner.functions.kernel.evosvm;

/**
 * Returns the value of the Epanechnikov kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: EpanechnikovKernel.java,v 1.1 2007/05/27 22:02:26 ingomierswa Exp $
 */
public class EpanechnikovKernel extends Kernel {

	private static final long serialVersionUID = -4683350345234451645L;

	/** The parameter sigma of the Epanechnikov kernel. */
	private double sigma = 1.0d;

	/** The parameter degree of the Epanechnikov kernel. */
	private double degree = 1;
	
	public int getType() {
		return KERNEL_EPANECHNIKOV;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double getSigma() {
		return sigma;
	}

	public double getDegree() {
		return degree;
	}
	
	public void setDegree(double degree) {
		this.degree = degree;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculateDistance(double[] x1, double[] x2) {
		double expression = norm2(x1, x2) / sigma;
		if (expression > 1) 
			return 0.0d;
		else {
			double minus = 1.0d - expression;
			return Math.pow(minus, degree);
		}
	}
}
