/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.similarity.bregmandivergences;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * The &quot;Squared Euclidean distance &quot;.
 * 
 * @author Regina Fritsch
 * @version $Id: SquaredEuclideanDistance.java,v 1.4 2008/09/12 10:30:44 tobiasmalbrecht Exp $
 */
public class SquaredEuclideanDistance extends AbstractBregmanDivergence{
	
	public SquaredEuclideanDistance(ExampleSet es) throws InstantiationException {
		super(es);
	}

	public double distance(Example x, double[] y) {
		double result = 0;
		double[] vector = vectorSubtraction(x, y);
		for (int i = 0; i < vector.length ; i++) {
			result += (vector[i] * vector[i]);
		}
		return result;
	}

	public boolean isApplicable(Example x) {
		return true;
	}

}
