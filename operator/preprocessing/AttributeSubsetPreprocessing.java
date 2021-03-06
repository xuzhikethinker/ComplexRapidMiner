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
package com.rapidminer.operator.preprocessing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeSelectionExampleSet;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.NonSpecialAttributesExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.preprocessing.filter.attributes.AttributeFilter;
import com.rapidminer.operator.preprocessing.filter.attributes.AttributeFilterCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;

/**
 * <p>This operator can be used to select one attribute (or a subset) by defining a 
 * regular expression for the attribute name and applies its inner operators to
 * the resulting subset. Please note that this operator will also use special 
 * attributes which makes it necessary for all preprocessing steps which should
 * be performed on special attributes (and are normally not performed on special
 * attributes).</p>
 * 
 * <p>This operator is also able to deliver the additional results of the inner
 * operator if desired.</p> 
 * 
 * <p>Afterwards, the remaining original attributes are added
 * to the resulting example set if the parameter &quot;keep_subset_only&quot; is set to 
 * false (default).</p>
 * 
 * <p>Please note that this operator is very powerful and can be used to create
 * new preprocessing schemes by combinating it with other preprocessing operators.
 * Hoewever, there are two major restrictions (among some others): first, since the inner result
 * will be combined with the rest of the input example set, the number of 
 * examples (data points) is not allowed to be changed inside of the subset preprocessing. 
 * Second, attribute role changes will not be delivered to the outside since internally all special
 * attributes will be changed to regular for the inner operators and role changes can afterwards
 * not be delivered.</p> 
 * 
 * @author Ingo Mierswa, Shevek
 * @version $Id: AttributeSubsetPreprocessing.java,v 1.15 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class AttributeSubsetPreprocessing extends OperatorChain {

	/** The parameter name for &quot;A regular expression which matches against all attribute names (including special attributes).&quot; */
	public static final String PARAMETER_ATTRIBUTE_NAME_REGEX = "attribute_name_regex";

	/** The parameter name for &quot;Indicates if the attributes which did not match the regular expression should be removed by this operator.&quot; */
	public static final String PARAMETER_INVERT_SELECTION = "invert_selection";
	
	/** The parameter name for &quot;Indicates if special attributes like labels etc. should also be processed.&quot; */
	public static final String PARAMETER_PROCESS_SPECIAL_ATTRIBUTES = "process_special_attributes";

	/** The parameter name for &quot;Indicates if the additional results (other than example set) of the inner operator should also be returned.&quot; */
	public static final String PARAMETER_DELIVER_INNER_RESULTS = "deliver_inner_results";
	
	/** The parameter name for &quot;Indicates if the attributes which did not match the regular expression should be removed by this operator.&quot; */
	public static final String PARAMETER_KEEP_SUBSET_ONLY = "keep_subset_only";
	
    public AttributeSubsetPreprocessing(OperatorDescription description) {
        super(description);
    }
    
    public IOObject[] apply() throws OperatorException {
        ExampleSet inputSet = getInput(ExampleSet.class);        
        ExampleSet workingExampleSet = (ExampleSet)inputSet.clone();
        if (getParameterAsBoolean(PARAMETER_PROCESS_SPECIAL_ATTRIBUTES))
            workingExampleSet = new NonSpecialAttributesExampleSet(workingExampleSet);
        
        // this list will be filled in the method createSubSetView(...)
        List<Attribute> unusedAttributes = new LinkedList<Attribute>();
        workingExampleSet = createSubSetView(workingExampleSet, unusedAttributes);

        // perform inner operators
        IOContainer input = new IOContainer(new IOObject[] { workingExampleSet });
        for (int i = 0; i < getNumberOfOperators(); i++) {
        	input = getOperator(i).apply(input);
        }

        // retrieve transformed example set
        ExampleSet resultSet = input.get(ExampleSet.class);

        // transform special attributes back
        Iterator<AttributeRole> r = resultSet.getAttributes().allAttributeRoles();
        while (r.hasNext()) {
        	AttributeRole newRole = r.next();
        	AttributeRole oldRole = inputSet.getAttributes().getRole(newRole.getAttribute().getName());
        	if (oldRole != null) {
        		if (oldRole.isSpecial()) {
        			String specialName = oldRole.getSpecialName();
        			newRole.setSpecial(specialName);
        		}
        	}
        }

        // add old attributes if desired
        if (!getParameterAsBoolean(PARAMETER_KEEP_SUBSET_ONLY)) {
        	if (resultSet.size() != inputSet.size()) {
        		throw new UserError(this, 127, "changing the size of the example set is not allowed if the non-processed attributes should be kept.");
        	}

        	if (resultSet.getExampleTable().equals(inputSet.getExampleTable())) {
        		for (Attribute attribute : unusedAttributes) {
        			AttributeRole role = inputSet.getAttributes().getRole(attribute);
        			resultSet.getAttributes().add(role);
        		}
        	} else {
        		logWarning("Underlying example table has changed: data copy into new table is necessary in order to keep non-processed attributes.");
        		for (Attribute oldAttribute : unusedAttributes) {
        			AttributeRole oldRole = inputSet.getAttributes().getRole(oldAttribute);

        			// create and add copy of attribute 
        			Attribute newAttribute = (Attribute)oldAttribute.clone();
        			resultSet.getExampleTable().addAttribute(newAttribute);
        			AttributeRole newRole = new AttributeRole(newAttribute);
        			if (oldRole.isSpecial())
        				newRole.setSpecial(oldRole.getSpecialName());
        			resultSet.getAttributes().add(newRole);

        			// copy data for the new attribute
        			Iterator<Example> oldIterator = inputSet.iterator();
        			Iterator<Example> newIterator = resultSet.iterator();
        			while (oldIterator.hasNext()) {
        				Example oldExample = oldIterator.next();
        				Example newExample = newIterator.next();
        				newExample.setValue(newAttribute, oldExample.getValue(oldAttribute));
        			}
        		}
        	}
        } 

        // add all other results if desired
        List<IOObject> allResults = new LinkedList<IOObject>();
        allResults.add(resultSet);
        if (getParameterAsBoolean(PARAMETER_DELIVER_INNER_RESULTS)) {
        	for (IOObject current : input.getIOObjects()) {
        		if (!(current instanceof ExampleSet)) {
        			allResults.add(current);
        		}
        	}
        }
        
        // create and deliver final result
        IOObject[] finalResult = new IOObject[allResults.size()];
        allResults.toArray(finalResult);
        return finalResult;
    }

    private ExampleSet createSubSetView(ExampleSet exampleSet, List<Attribute> unusedAttributes) throws UserError {
    	Attributes attributes = exampleSet.getAttributes();
        boolean[] selectionMask = new boolean[exampleSet.getAttributes().size()];

		// init and removing attributes not needed to checked per example
		String conditionName = getParameterAsString(AttributeFilter.PARAMETER_CONDITION_NAME);
		String parameterString = null;
		if (AttributeFilter.CONDITION_NAMES[AttributeFilter.CONDITION_ATTRIBUTE_NAME_FILTER].equals(conditionName)) {
			if (isParameterSet(PARAMETER_ATTRIBUTE_NAME_REGEX))
				parameterString = getParameterAsString(PARAMETER_ATTRIBUTE_NAME_REGEX);
			else
				throw new UndefinedParameterError("Parameter '" + PARAMETER_ATTRIBUTE_NAME_REGEX + "' not defined.");
		} else {
			parameterString = getParameterAsString(AttributeFilter.PARAMETER_PARAMETER_STRING);
		}
		boolean invert = getParameterAsBoolean(PARAMETER_INVERT_SELECTION);
		
		try {
			AttributeFilterCondition condition = AttributeFilter.createCondition(getParameterAsString(AttributeFilter.PARAMETER_CONDITION_NAME));
			
			int a = 0;
			for (Attribute attribute : attributes) {
				if (condition != null) {
					if (condition.beforeScanCheck(attribute, parameterString, invert)) {
						unusedAttributes.add(attribute);
						selectionMask[a] = false;
					} else {
						selectionMask[a] = true;
					}
				} else {
					selectionMask[a] = true;
				}
				a++;
			}
			
			// now checking for every example
			if (condition.isNeedingScan()) {
				condition.initScanCheck();
				a = 0;
				for (Attribute attribute : attributes) {
					for (Example example: exampleSet) {
						if (condition.check(attribute, example)) {
							selectionMask[a] = false;
							unusedAttributes.add(attribute);
							break;
						}
					}
					a++;
				}
			}
		} catch (ConditionCreationException e) {
			throw new UserError(this, 904, parameterString, e.getMessage());
		}
		
		return new AttributeSelectionExampleSet(exampleSet, selectionMask);
    }

    public InnerOperatorCondition getInnerOperatorCondition() {
        return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { ExampleSet.class });
    }

    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public Class<?>[] getInputClasses() {
        return new Class[] {ExampleSet.class };
    }

    public Class<?>[] getOutputClasses() {
        Class[] innerResult = null;
        try {
            if (getParameterAsBoolean(PARAMETER_DELIVER_INNER_RESULTS)) {
            	if (getNumberOfOperators() > 0)
            		innerResult = getOperator(0).getOutputClasses();
            }
        } catch (NullPointerException e) {
            // hack to allow parameter retrieval in getInputClasses before
            // initialization has finished
            // after init (i.e. during process runtime) this method of course
            // works...
        } catch (ArrayIndexOutOfBoundsException e) {
            // dito
        }
        if (innerResult != null) {
            List<Class> completeOutput = new LinkedList<Class>();
            completeOutput.add(ExampleSet.class);
            for (Class clazz : innerResult) {
                if (!clazz.equals(ExampleSet.class))
                    completeOutput.add(clazz);
            }
            Class[] result = new Class[completeOutput.size()];
            completeOutput.toArray(result);
            return result;
        } else {
            return new Class[] { ExampleSet.class };
        }
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        
        ParameterType type = new ParameterTypeStringCategory(AttributeFilter.PARAMETER_CONDITION_NAME, "Implementation of the condition.", AttributeFilter.CONDITION_NAMES);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeString(AttributeFilter.PARAMETER_PARAMETER_STRING, "Parameter string for the condition, e.g. 'attribute=value' for the nominal value filter.", true);
		type.registerDependencyCondition(new EqualStringCondition(this, AttributeFilter.PARAMETER_CONDITION_NAME, true, AttributeFilter.CONDITION_NAMES[AttributeFilter.CONDITION_NUMERIC_VALUE_FILTER]));		
		type.setExpert(false);
		types.add(type);
		
        type = new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME_REGEX, "A regular expression which matches against all attribute names (including special attributes).", true);
        type.registerDependencyCondition(new EqualStringCondition(this, AttributeFilter.PARAMETER_CONDITION_NAME, true, AttributeFilter.CONDITION_NAMES[AttributeFilter.CONDITION_ATTRIBUTE_NAME_FILTER]));
        type.setExpert(false);
        types.add(type);
		
        type = new ParameterTypeBoolean(PARAMETER_INVERT_SELECTION, "Indicates if the specified attribute selection should be inverted.", false);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeBoolean(PARAMETER_PROCESS_SPECIAL_ATTRIBUTES, "Indicates if special attributes like labels etc. should also be processed.", false);
        type.setExpert(false);
        types.add(type);
        
        types.add(new ParameterTypeBoolean(PARAMETER_KEEP_SUBSET_ONLY, "Indicates if the attributes which did not match the regular expression should be removed by this operator.", false));
        
        types.add(new ParameterTypeBoolean(PARAMETER_DELIVER_INNER_RESULTS, "Indicates if the additional results (other than example set) of the inner operator should also be returned.", false));
        
        return types;
    }
}
