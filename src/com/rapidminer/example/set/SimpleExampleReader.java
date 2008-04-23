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
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;

/**
 * This reader simply uses all examples from an example table.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimpleExampleReader.java,v 2.7 2006/04/05 08:57:22 ingomierswa
 *          Exp $
 */
public class SimpleExampleReader extends AbstractExampleReader {

	/** The parent example reader. */
	private DataRowReader dataRowReader;
	
	/** The current example set. */
	private ExampleSet exampleSet;
	
	/** Creates a simple example reader. */
	public SimpleExampleReader(DataRowReader drr, ExampleSet exampleSet) {
		this.dataRowReader = drr;
		this.exampleSet = exampleSet;
	}

	/** Returns true if there are more data rows. */
	public boolean hasNext() {
		return dataRowReader.hasNext();
	}

	/** Returns a new example based on the current data row. */
	public Example next() {
		if (!hasNext())
			return null;
		DataRow data = dataRowReader.next();
		if (data == null)
			return null;
		return new Example(data, exampleSet);
	}
}
