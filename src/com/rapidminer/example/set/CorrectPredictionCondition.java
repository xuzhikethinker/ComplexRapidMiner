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
package com.rapidminer.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * This subclass of {@link Condition} serves to accept all examples which are
 * correctly predicted.
 * 
 * @author Ingo Mierswa
 * @version $Id: CorrectPredictionCondition.java,v 2.2 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class CorrectPredictionCondition implements Condition {

	private static final long serialVersionUID = -2971139314612252926L;

	/** Creates a new condition. */
	public CorrectPredictionCondition() {}

	/**
	 * Throws an exception since this condition does not support parameter
	 * string.
	 */
	public CorrectPredictionCondition(ExampleSet exampleSet, String parameterString) {
		if ((parameterString != null) && (parameterString.trim().length() != 0))
			throw new IllegalArgumentException("CorrectPredictionCondition does not need any parameters!");
		if (exampleSet.getAttributes().getLabel() == null)
			throw new IllegalArgumentException("CorrectPredictionCondition needs an example set with label attribute!");
		if (exampleSet.getAttributes().getPredictedLabel() == null)
			throw new IllegalArgumentException("CorrectPredictionCondition needs an example set with predicted label attribute!");
	}

	/**
	 * Since the condition cannot be altered after creation we can just return
	 * the condition object itself.
	 */
	public Condition duplicate() {
		return this;
	}

	/** Returns true if the example is correctly predicted. */
	public boolean conditionOk(Example example) {
        return example.equalValue(example.getAttributes().getLabel(), example.getAttributes().getPredictedLabel());
	}
}
