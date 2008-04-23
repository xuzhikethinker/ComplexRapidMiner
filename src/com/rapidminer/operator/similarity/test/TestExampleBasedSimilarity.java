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
package com.rapidminer.operator.similarity.test;

import com.rapidminer.operator.similarity.attributebased.EuclideanDistance;

import junit.framework.TestCase;

/**
 * A test for the example based similarities.
 * 
 * @author Michael Wurst
 * @version $Id: TestExampleBasedSimilarity.java,v 1.1 2007/05/27 22:04:00 ingomierswa Exp $
 */
public class TestExampleBasedSimilarity extends TestCase {

	public void testEuclideanDistance() {
		EuclideanDistance e = new EuclideanDistance();
		assertEquals(0.0, e.similarity(new double[] {
				0.1, 0.2
		}, new double[] {
				0.1, 0.2
		}));
	}
}
