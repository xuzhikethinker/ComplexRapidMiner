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
package com.rapidminer.tools.math;

/**
 * This class provides mathematical functions not already provided by
 * <tt>java.lang.Math</tt>:
 * <ul>
 * <li><tt>tanh()</tt> : tangens hyperbolicus, <i>y = tanh(x) = (e^x - e^-x) /
 * (e^x + e^-x)</i></li>
 * </ul>
 * 
 * @author Ralf Klinkenberg
 * @version $Id: MathFunctions.java,v 1.1 2007/05/27 21:59:33 ingomierswa Exp $
 */

public class MathFunctions {
	/**
	 *    coefficients for polynomials used in normalInverse() to estimate
	 *    normal distribution;
	 */
	// coefficients for approximation of intervall 0,138... < probability < 0,861...
	protected static final double DIVISOR_COEFFICIENTS_0[] = {
		-5.99633501014107895267E1,
		9.80010754185999661536E1,
		-5.66762857469070293439E1,
		1.39312609387279679503E1,
		-1.23916583867381258016E0,
	};
	protected static final double DIVIDER_COEFFICIENTS_0[] = {
		1.00000000000000000000E0,
		1.95448858338141759834E0,
		4.67627912898881538453E0,
		8.63602421390890590575E1,
		-2.25462687854119370527E2,
		2.00260212380060660359E2,
		-8.20372256168333339912E1,
		1.59056225126211695515E1,
		-1.18331621121330003142E0,
	};
	// coefficients for approximation of intervall exp(-32) < probability < 0,138... 
	//                                   or 0,861 < probability < 1 - exp(-32)
	protected static final double DIVISOR_COEFFICIENTS_1[] = {
		4.05544892305962419923E0,
		3.15251094599893866154E1,
		5.71628192246421288162E1,
		4.40805073893200834700E1,
		1.46849561928858024014E1,
		2.18663306850790267539E0,
		-1.40256079171354495875E-1,
		-3.50424626827848203418E-2,
		-8.57456785154685413611E-4,
	};
	protected static final double DIVIDER_COEFFICIENTS_1[] = {
		1.00000000000000000000E0,
		1.57799883256466749731E1,
		4.53907635128879210584E1,
		4.13172038254672030440E1,
		1.50425385692907503408E1,
		2.50464946208309415979E0,
		-1.42182922854787788574E-1,
		-3.80806407691578277194E-2,
		-9.33259480895457427372E-4,
	};
	// coefficients for approximation of intervall 0 < probability < exp(-32) 
	//                                or 1 - exp(-32) < probability < 1
	protected static final double  DIVISOR_COEFFICIENTS_3[] = {
		3.23774891776946035970E0,
		6.91522889068984211695E0,
		3.93881025292474443415E0,
		1.33303460815807542389E0,
		2.01485389549179081538E-1,
		1.23716634817820021358E-2,
		3.01581553508235416007E-4,
		2.65806974686737550832E-6,
		6.23974539184983293730E-9,
	};
	protected static final double  DIVIDER_COEFFICIENTS_3[] = {
		1.00000000000000000000E0,
		6.02427039364742014255E0,
		3.67983563856160859403E0,
		1.37702099489081330271E0,
		2.16236993594496635890E-1,
		1.34204006088543189037E-2,
		3.28014464682127739104E-4,
		2.89247864745380683936E-6,
		6.79019408009981274425E-9,
	};
	/**
	 * returns tangens hyperbolicus of <tt>x</tt>, i.e. <i>y = tanh(x) = (e^x -
	 * e^-x) / (e^x + e^-x)</i>.
	 */
	public static double tanh(double x) {
		return ((java.lang.Math.exp(x) - java.lang.Math.exp(-x)) / (java.lang.Math.exp(x) + java.lang.Math.exp(-x)));
	}
	 /**
	   * Returns the value x for which the area under the
	   * normal probability density function (integrated from
	   * minus infinity to this value x) is equal to the given probability.
	   * The normal distribution has mean of zero and variance of one.
	   *
	   * @param probability the area under the normal pdf
	   * @return x
	   */
	public static double normalInverse(double probability) { 

		final double smallArgumentEnd = Math.exp(-2);
		final double rootedPi = Math.sqrt(2.0*Math.PI);

		if( probability <= 0.0 ) throw new IllegalArgumentException();
		if( probability >= 1.0 ) throw new IllegalArgumentException();

		boolean wrappedArround = false;
		if( probability > (1.0 - smallArgumentEnd) ) {
			probability = 1.0 - probability;
			wrappedArround = true;
		}

		if( probability > smallArgumentEnd ) {
			//approximation for intervall 0,138... < probability < 0,861...
			probability = probability - 0.5;
			double squaredProbability = probability * probability;
			double x = probability;
			x += probability * (squaredProbability * solvePolynomial(squaredProbability, DIVISOR_COEFFICIENTS_0) /solvePolynomial( squaredProbability, DIVIDER_COEFFICIENTS_0));
			x = x * rootedPi; 
			return(x);
		} else {
			double x = Math.sqrt( -2.0 * Math.log(probability) );
			double inversedX = 1.0 / x;
			if( x < 8.0 ) { // equal to probability > exp(-32)
				// approximation for intervall exp(-32) < probability < 0,138... or 0,861 < probability < 1 - exp(-32)
				x = (x - Math.log(x)/x) - inversedX * solvePolynomial(inversedX, DIVISOR_COEFFICIENTS_1)/solvePolynomial(inversedX, DIVIDER_COEFFICIENTS_1);
			} else {
				// approximation for intervall 0 < probability < exp(-32) or 1 - exp(-32) < probability < 1
				x = (x - Math.log(x)/x) - inversedX * solvePolynomial(inversedX, DIVISOR_COEFFICIENTS_3)/solvePolynomial(inversedX, DIVIDER_COEFFICIENTS_3);
			}
			if(!wrappedArround) {
				x = -x;
			}
			return(x);
		}
	}
	/**
	 * Solves a given polynomial at x. The polynomial is given by the coefficients.
	 * The coefficients are stored in natural order: coefficients[i] : c_i*x^i
	 */
	public static double solvePolynomial(double x, double[] coefficients) {
		double value = coefficients[0];
		for (int i = 1; i < coefficients.length; i++) {
			value += coefficients[i]*Math.pow(x, i);
		}
		return value;
	}
	/**
	 * @param v
	 *            a vector values
	 * @param a
	 *            a threeshold, only values greater equal this value are used in the calculation
	 * @return the variance
	 */
	public static double variance(double v[], double a) {
		// calc mean
		double sum = 0.0;
		int counter = 0;
	
		for (int i = 0; i < v.length; i++) {
			if (v[i] >= a) {
				sum = sum + v[i];
				counter++;
			}
		}
		double mean = sum / counter;
	
		sum = 0.0;
		counter = 0;
		for (int i = 0; i < v.length; i++) {
			if (v[i] >= a) {
				sum = sum + (v[i] - mean) * (v[i] - mean);
				counter++;
			}
		}
		double variance = sum / counter;
		return variance;
	}
	public static double correlation(double[] x1, double[] x2) {
		// Calculate the mean and stddev
		int counter = 0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		double sumS1 = 0.0;
		double sumS2 = 0.0;
	
		for (int i = 0; i < x1.length; i++) {
			sum1 = sum1 + x1[i];
			sum2 = sum2 + x2[i];
			counter++;
		}
	
		double mean1 = sum1 / counter;
		double mean2 = sum2 / counter;
		
		double sum = 0.0;
		counter = 0;
		
		for (int i = 0; i < x1.length; i++) {
			sum = sum + (x1[i] - mean1) * (x2[i] - mean2);
			sumS1 = sumS1 + (x1[i] - mean1) * (x1[i] - mean1);
			sumS2 = sumS2 + (x2[i] - mean2) * (x2[i] - mean2);
			counter++;
		}
	
		return sum / Math.sqrt(sumS1 * sumS2);
	}
	public static double max(double[] x) {
		double best = x[0];
		for (int i = 1; i < x.length; i++)
			if (x[i] > best)
				best = x[i];
		return best;
	}
	public static double min(double[] x) {
		double best = x[0];
		for (int i = 1; i < x.length; i++)
			if (x[i] < best)
				best = x[i];
		return best;
	}
}
