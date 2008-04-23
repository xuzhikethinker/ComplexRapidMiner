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
package com.rapidminer.operator.features.selection;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.IndividualOperator;


/**
 * This PopulationOperator realises forward selection, i.e. creates a list of
 * clones of each individual and switches on one attribute in each of the
 * clones. In contrast to the normal forward selection this one actually selects
 * attributes instead of completely adding them. Although this might use more
 * memory for simple selection tasks this might be necessary in case of
 * FeatureOperators which generate new attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: SwitchingForwardSelection.java,v 1.1 2006/04/14 11:42:27
 *          ingomierswa Exp $
 */
public class SwitchingForwardSelection extends IndividualOperator {

	public List<Individual> operate(Individual individual) {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		List<Individual> l = new LinkedList<Individual>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (!exampleSet.isAttributeUsed(attribute)) {
				AttributeWeightedExampleSet nes = (AttributeWeightedExampleSet) exampleSet.clone();
				nes.setAttributeUsed(attribute, true);
				l.add(new Individual(nes));
			}
		}
		return l;
	}
}
