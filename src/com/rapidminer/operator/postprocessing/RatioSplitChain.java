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
package com.rapidminer.operator.postprocessing;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * <p>An operator chain that split an {@link ExampleSet} into two disjunct parts
 * and applies the first child operator on the first part and applies the second
 * child on the second part and the result of the first child. The total result
 * is the result of the second operator.</p>
 * 
 * <p>The input example set will be splitted based on a defined ratio between 
 * 0 and 1.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: RatioSplitChain.java,v 1.2 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class RatioSplitChain extends AbstractSplitChain {


	/** The parameter name for &quot;Relative size of the training set.&quot; */
	public static final String PARAMETER_SPLIT_RATIO = "split_ratio";

	/** The parameter name for &quot;Defines the sampling type of this operator.&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	public RatioSplitChain(OperatorDescription description) {
		super(description);
	}

	protected SplittedExampleSet createSplittedExampleSet(ExampleSet inputSet) throws OperatorException {
		return new SplittedExampleSet(inputSet, 
				                      getParameterAsDouble(PARAMETER_SPLIT_RATIO), 
				                      getParameterAsInt(PARAMETER_SAMPLING_TYPE), 
				                      getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_SPLIT_RATIO, "Relative size of the training set.", 0.0d, 1.0d, 0.7d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of this operator.", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.SHUFFLED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
