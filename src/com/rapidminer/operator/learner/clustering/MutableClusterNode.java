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

/**
 * Represents a mutable cluster node.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MutableClusterNode.java,v 1.1 2007/05/27 21:58:38 ingomierswa Exp $
 */
public interface MutableClusterNode extends ClusterNode, MutableCluster {

	/**
	 * Add a subcluster to this cluster.
	 * 
	 * @param clusterNode
	 *            the node to add
	 */
	public void addSubNode(ClusterNode clusterNode);

	/**
	 * Insert a cluster node at the specified position.
	 * 
	 * @param clusterNode
	 *            the cluster node
	 * @param index
	 *            the target position at whicht the node is stored
	 */
	public void insertSubNodeAt(ClusterNode clusterNode, int index);

	/**
	 * Set the weight of the node.
	 * 
	 * @param weight
	 *            a weight
	 */
	public void setWeight(double weight);

	/**
	 * Remove a sub node.
	 * 
	 * @param node
	 *            the node to remove
	 */
	public void removeSubNode(ClusterNode node);

	/**
	 * Remove a sub node at a given position
	 * 
	 * @param index
	 *            the position of the node
	 */
	public void removeSubNodeAt(int index);

	/**
	 * Get the number of subnodes.
	 * 
	 * @return the number of subnodes.
	 */
	public int getNumberOfSubNodes();
}
