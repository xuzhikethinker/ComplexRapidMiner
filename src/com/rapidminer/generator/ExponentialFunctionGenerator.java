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
package com.rapidminer.generator;

import com.rapidminer.tools.LogService;

/**
 * This class has one numerical input attribute and one output attribute.
 * Depending on the mode specified in the constructor the result will be the
 * exponential or the logarithm function of the input attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExponentialFunctionGenerator.java,v 2.6 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class ExponentialFunctionGenerator extends SingularNumericalGenerator {

	public static final int EXP = 0;

	public static final int LOG = 1;

	private static final String[] FUNCTION_NAMES = { "exp", "log" };

	private int mode;

	public ExponentialFunctionGenerator(int mode) {
		this.mode = mode;
	}

	public ExponentialFunctionGenerator() {}

	public FeatureGenerator newInstance() {
		return new ExponentialFunctionGenerator(mode);
	}

	public double calculateValue(double value) {
		double r = 0;
		switch (mode) {
			case EXP:
				r = Math.exp(value);
				break;
			case LOG:
				r = Math.log(value);
				break;
		}
		return r;
	}

	public void setFunction(String name) {
		for (int i = 0; i < FUNCTION_NAMES.length; i++) {
			if (FUNCTION_NAMES[i].equals(name)) {
				this.mode = i;
				return;
			}
		}
		LogService.getGlobal().log("Illegal function name '" + name + "' for " + getClass().getName() + ".", LogService.ERROR);
	}

	public String getFunction() {
		return FUNCTION_NAMES[mode];
	}

	public boolean equals(Object o) {
		return (super.equals(o) && (this.mode == ((ExponentialFunctionGenerator) o).mode));
	}
	
	public int hashCode() {
		return super.hashCode() ^ Integer.valueOf(mode).hashCode();
	}
}
