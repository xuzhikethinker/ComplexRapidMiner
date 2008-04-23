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
package com.rapidminer.operator.features.transformation;

/**
 * This class holds information about one eigenvector and eigenvalue.
 *  
 * @author Ingo Mierswa
 * @version $Id: WeightVector.java,v 1.2 2007/06/08 15:07:48 ingomierswa Exp $
 */
public class WeightVector implements ComponentVector {
        
    private static final long serialVersionUID = -8280081065217261596L;
    
	private double eigenvalue;
    private double[] weights;

    public WeightVector(double[] weights, double eigenvalue) {
        this.weights = weights;
        this.eigenvalue = eigenvalue;
    }

    public double[] getWeights() { 
        return this.weights; 
    }
    
    public double getEigenvalue() { 
        return this.eigenvalue; 
    }

    public double[] getVector() {
        return getWeights();
    }

    public int compareTo(ComponentVector o) {
        return -1 * Double.compare(this.eigenvalue, o.getEigenvalue());
    }
    
    public boolean equals(Object o) {
    	if (!(o instanceof WeightVector)) {
    		return false;
    	} else {
    		WeightVector ev = (WeightVector)o;
    		return (this.eigenvalue == ev.eigenvalue) && (this.weights == ev.weights);
    	}
    }
    
    public int hashCode() {
    	return Double.valueOf(this.eigenvalue).hashCode() ^ this.weights.hashCode();
    }
}
