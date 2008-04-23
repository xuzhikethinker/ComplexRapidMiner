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
package com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel;

/**
 * Polynomial Kernel
 * 
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: KernelPolynomial.java,v 1.1 2007/06/15 18:44:37 ingomierswa Exp $
 */
public class KernelPolynomial extends Kernel {

	private static final long serialVersionUID = 7385441798122306059L;
	
	private double degree = 2;

	/**
	 * Class constructor
	 */
	public KernelPolynomial() {};

	/**
	 * Output as String
	 */
	public String toString() {
		return ("poly(" + degree + ")");
	}

	public void setDegree(double degree) {
		this.degree = degree;
	}

	/**
	 * Calculates kernel value of vectors x and y
	 */
	public double calculate_K(int[] x_index, double[] x_att, int[] y_index, double[] y_att) {
		double prod = innerproduct(x_index, x_att, y_index, y_att);
		double result = prod;
		for (int i = 1; i < degree; i++) {
			result *= prod;
		};
		return result;
	}
}
