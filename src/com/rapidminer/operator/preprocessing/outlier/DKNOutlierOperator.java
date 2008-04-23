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
package com.rapidminer.operator.preprocessing.outlier;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * <p>This operator performs a D^k_n Outlier Search according to the outlier detection 
 * approach recommended by Ramaswamy, Rastogi and Shim in "Efficient Algorithms for 
 * Mining Outliers from Large Data Sets". It is primarily a statistical outlier search
 * based on a distance measure similar to the DB(p,D)-Outlier Search from Knorr and Ng. 
 * But it utilizes a distance search through the k-th nearest neighbourhood, so it 
 * implements some sort of locality as well.</p>
 * 
 * <p>The method states, that those objects with the largest distance to their k-th nearest 
 * neighbours are likely to be outliers respective to the data set, because it can be assumed, 
 * that those objects have a more sparse neighbourhood than the average objects. As this 
 * effectively provides a simple ranking over all the objects in the data set according to 
 * the distance to their k-th nearest neighbours, the user can specify a number of n objects 
 * to be the top-n outliers in the data set.</p>
 * 
 * <p>The operator supports cosine, sine or squared distances in addition to the euclidian 
 * distance which can be specified by a distance parameter. The Operator takes an example set
 * and passes it on with an boolean top-n D^k outlier status in a new boolean-valued
 * special outlier attribute indicating true (outlier) and false (no outlier).</p>
 * 
 * @author Stephan Deutsch, Ingo Mierswa
 * @version $Id: DKNOutlierOperator.java,v 1.2 2007/06/15 16:58:39 ingomierswa Exp $
 */
public class DKNOutlierOperator extends Operator {


	/** The parameter name for &quot;Specifies the k value for the k-th nearest neighbours to be the analyzed.&quot; */
	public static final String PARAMETER_NUMBER_OF_NEIGHBORS = "number_of_neighbors";

	/** The parameter name for &quot;The number of top-n Outliers to be looked for.&quot; */
	public static final String PARAMETER_NUMBER_OF_OUTLIERS = "number_of_outliers";

	/** The parameter name for &quot;choose which distance function will be used for calculating &quot; */
	public static final String PARAMETER_DISTANCE_FUNCTION = "distance_function";
	private static final String[] distanceFunctionList = {
		"euclidian distance", 
		"squared distance", 
		"cosine distance", 
		"inverted cosine distance", 
		"angle" 
	};


	public DKNOutlierOperator(OperatorDescription description) {
		super(description);
	}

	/**
	 * This method implements the main functionality of the Operator but can be considered 
	 * as a sort of wrapper to pass the RapidMiner operator chain data deeper into the search space
	 * class, so do not expect a lot of things happening here.
	 */
	public IOObject[] apply() throws OperatorException {
		// declaration and initializing the necessary fields from input
		int k = this.getParameterAsInt(PARAMETER_NUMBER_OF_NEIGHBORS);
		int n = this.getParameterAsInt(PARAMETER_NUMBER_OF_OUTLIERS);
		n = n - 2; // this has to do with the internal indexing in the SearchSpace's methods
		int kindOfDistance = this.getParameterAsInt(PARAMETER_DISTANCE_FUNCTION);

		// create a new SearchSpace for the DKN(p,D)-Outlier search
		ExampleSet eSet = getInput(ExampleSet.class);
		Iterator<Example> reader = eSet.iterator();
		int searchSpaceDimension = eSet.getAttributes().size();
		SearchSpace sr = new SearchSpace(searchSpaceDimension, k, k);

		// now read through the Examples of the ExampleSet
		int counter = 0;
		while (reader.hasNext()) {
			Example example = reader.next(); // read the next example & create a search object
			SearchObject so = new SearchObject(searchSpaceDimension, "object" + counter, k, k + 1); // for now, give so an id like label
			counter++;
			int i = 0;
			for (Attribute attribute : eSet.getAttributes()) {
				so.setVektor(i++, example.getValue(attribute)); // get the attributes for the so from example and pass it on
			}
			sr.addObject(so); // finally add the search object to the search room
		}

		log("Searching d=" + sr.getDimensions() + " dimensions with k=" + k + " and n=" + n);

        // set all Outlier Status to ZERO to be sure
		sr.resetOutlierStatus(); 

        // find all Containers for the DKN first
		sr.findAllKdContainers(kindOfDistance); 
		
		// perform the outlier search 
		sr.computeDKN(k, n);

		// create a new special attribute for the exampleSet
		Attribute outlierAttribute = AttributeFactory.createAttribute("Outlier", Ontology.BINOMINAL);
		outlierAttribute.getMapping().mapString("false");
		outlierAttribute.getMapping().mapString("true");
		eSet.getExampleTable().addAttribute(outlierAttribute);
		eSet.getAttributes().setOutlier(outlierAttribute);

		counter = 0; // reset counter to zero
		Iterator<Example> reader2 = eSet.iterator();
		while (reader2.hasNext()) {
			Example example = reader2.next(); 
			if (sr.getSearchObjectOutlierStatus(counter) == true) {
				example.setValue(outlierAttribute, outlierAttribute.getMapping().mapString("true"));
			} else {
				example.setValue(outlierAttribute, outlierAttribute.getMapping().mapString("false"));
			}
			counter++;
		}

		return new IOObject[] { eSet };
	}

	/**
	 * This method override specifies the DKNOutlierOperator to take an ExampleSet as input.
	 */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/**
	 * This method override specifies the DKNOutlierOperator to probide an ExampleSet as output. 
	 * (please note, that the output ExampleSets will be a modified version of the input 
	 * ExampleSet, e.g. a label will be added representing the Outlier Status
	 * (in a true/false nature).
	 * 
	 */
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_NUMBER_OF_NEIGHBORS, "Specifies the k value for the k-th nearest neighbours to be the analyzed." + "(default value is 10, minimum 1 and max is set to 1 million)", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeInt(PARAMETER_NUMBER_OF_OUTLIERS, "The number of top-n Outliers to be looked for." + "(default value is 10, minimum 2 (internal reasons) and max is set to 1 million)", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeCategory(PARAMETER_DISTANCE_FUNCTION, "choose which distance function will be used for calculating " + "the distance between two objects", distanceFunctionList, 0));
		return types;
	}
}
