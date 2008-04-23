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
package com.rapidminer.operator.learner.clustering;

import com.rapidminer.example.Example;

/**
 * A cluster model that contains a centroid for each cluster.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: CentroidBasedClusterModel.java,v 1.1 2007/06/30 23:24:35 ingomierswa Exp $
 */
public interface CentroidBasedClusterModel extends FlatClusterModel {

	/**
	 * Get the distance from a given example to the centroid with a given index.
	 * 
	 * @param index
	 * @param e
	 * @return distance
	 */
	double getDistanceFromCentroid(int index, Example e);

	/**
	 * Get the distance between two centroids with given indices.
	 * 
	 * @param index1
	 * @param index2
	 * @return the distance
	 */
	double getCentroidDistance(int index1, int index2);
	
}
