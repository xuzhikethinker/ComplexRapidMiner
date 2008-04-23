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
 * A class for complex numbers which consists of a real and an imaginary part.
 * 
 * @author Ingo Mierswa
 * @version $Id: Complex.java,v 1.1 2007/05/27 21:59:32 ingomierswa Exp $
 */
public class Complex implements Comparable {

	private double real, imaginary;

	public Complex(double real, double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}

	public double getReal() {
		return real;
	}

	public double getImaginary() {
		return imaginary;
	}

	public double getLength() {
		return Math.sqrt(real * real + imaginary * imaginary);
	}

	/**
	 * Normalizes the amplitude to the correct value. <code>n</code> must be
	 * the total used size, i.e. <code>nyquist * 2</code>.
	 */
	public double getMagnitude(int n) {
		return (2.0d * Math.sqrt(real * real + imaginary * imaginary)) / n;
	}

	public int compareTo(Object o) {
		Complex c = (Complex) o;
		return Double.compare(this.real, c.real);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Complex)) {
			return false;
		} else {
			 Complex c = (Complex)o;
			 return (this.real == c.real) && (this.imaginary == c.imaginary); 
		}
	}
	
	public int hashCode() {
		return Double.valueOf(this.real).hashCode() ^  Double.valueOf(this.imaginary).hashCode();
	}
	
	public String toString() {
		return "y = " + real + " + " + imaginary + " * i";
	}
}
