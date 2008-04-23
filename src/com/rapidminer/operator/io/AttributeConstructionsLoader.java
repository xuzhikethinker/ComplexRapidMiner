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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import com.rapidminer.example.AttributeParser;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Loads an attribute set from a file and constructs the desired features. If
 * keep_all is false, original attributes are deleted before the new ones are
 * created. This also means that a feature selection is performed if only a
 * subset of the original features was given in the file.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeConstructionsLoader.java,v 1.12 2006/04/05 08:57:25 ingomierswa
 *          Exp $
 */
public class AttributeConstructionsLoader extends Operator {


	/** The parameter name for &quot;Filename for the attribute constructions file.&quot; */
	public static final String PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE = "attribute_constructions_file";

	/** The parameter name for &quot;If set to true, all the original attributes are kept, otherwise they are removed from the example set.&quot; */
	public static final String PARAMETER_KEEP_ALL = "keep_all";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public AttributeConstructionsLoader(OperatorDescription description) {
		super(description);
	}

	/** Loads the attribute set from a file and constructs desired features. */
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = (ExampleSet) getInput(ExampleSet.class).clone();

		File file = getParameterAsFile(PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE);
		if (file != null) {
			try {
				AttributeParser parser = new AttributeParser(exampleSet.getExampleTable());
                InputStream in = new FileInputStream(file);
				parser.parseAll(in);
                in.close();
				if (!getParameterAsBoolean(PARAMETER_KEEP_ALL)) {
					exampleSet.getAttributes().clearRegular();
				}
				parser.generateAll(exampleSet);
			} catch (java.io.IOException e) {
				throw new UserError(this, e, 302, new Object[] { file.getName(), e.getMessage() });
			}
		}

		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE, "Filename for the attribute constructions file.", "att", false));
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL, "If set to true, all the original attributes are kept, otherwise they are removed from the example set.", false));
		return types;
	}
}
