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
 * Returns the value of the RBF kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: RBFKernel.java,v 1.1 2007/05/27 22:02:29 ingomierswa Exp $
 */
public class RBFKernel extends Kernel {

	private static final long serialVersionUID = 2928962529445448574L;
	
	/** The parameter gamma of the RBF kernel. */
	private double gamma = -1.0d;

	public int getType() {
		return KERNEL_RADIAL;
	}

	public void setGamma(double gamma) {
		this.gamma = -gamma;
	}

	public double getGamma() {
		return -gamma;
	}

	/** Calculates kernel value of vectors x and y. */
	public double calculateDistance(double[] x1, double[] x2) {
		return (Math.exp(this.gamma * norm2(x1, x2))); // gamma = -params.gamma
	}
}
