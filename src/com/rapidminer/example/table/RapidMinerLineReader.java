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
package com.rapidminer.example.table;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * A simple line converter for reading data from BufferedReaders. Each line is
 * separated into columns using a pattern matcher based on regular expressions.
 * In addition, comments might be also defined. Everything after a comment
 * character is completely ignored.
 * 
 * Quotes might also be used. If a columns starts with a quote (&quot;) the end
 * of the quoted region is searched and the corresponding columns build a new
 * column which replaces the old ones. Quoting is added for compatibility
 * reasons only. Since parsing is slower if quoting is used, quotes should not
 * be used at all. If possible please use and define a column separator which is
 * not part of your data.
 * 
 * @author Ingo Mierswa
 * @version $Id: RapidMinerLineReader.java,v 1.1 2007/05/27 22:01:17 ingomierswa Exp $
 */
public class RapidMinerLineReader {
	
	/** A regular expression pattern which is used for splitting the columns. */
	private Pattern separatorPattern;

	/** The possible character for comment lines. */
	private String[] commentChars;

	/** Indicates if quotes should be regarded (slower!). */
	private boolean useQuotes = false;

	/** The current line number. */
	private int lineNumber = 1;

	/** Indicates if quoting (&quot;) can be used to form. */
	public RapidMinerLineReader(String separatorsRegExpr, char[] commentChars, boolean useQuotes) {
		this.separatorPattern = Pattern.compile(separatorsRegExpr);		
		this.commentChars = new String[commentChars.length];
		for (int i = 0; i < commentChars.length; i++)
			this.commentChars[i] = Character.toString(commentChars[i]);
		this.useQuotes = useQuotes;
	}

	/**
	 * Ignores comment and empty lines and returns the first line not starting
	 * with a comment. Returns null if no such line exists. Throws an
	 * IOException if the line does not provide the given expected number of
	 * columns. This check will not be performed if the given parameter value
	 * is -1.
	 */
	public String[] readLine(BufferedReader in, int expectedNumberOfColumns) throws IOException {
		String line = null;
		while (line == null) {
			line = in.readLine();
			if (line == null)
				break; // eof
			line = line.trim();
			// check for comments
			for (int c = 0; c < commentChars.length; c++)
				if (line.indexOf(commentChars[c]) >= 0) {
					line = line.substring(0, line.indexOf(commentChars[c]));
				}
			// comment or empty line --> next line
			if (line.trim().length() == 0) {
				line = null;
			}
		}
		if (line == null)
			return null;

		String[] columns = separatorPattern.split(line);
		if (useQuotes)
			columns = Tools.mergeQuotedSplits(line, columns, "\"");
		if (expectedNumberOfColumns != -1) {
			if (columns.length < expectedNumberOfColumns) {
				throw new IOException("Data format error in line " + lineNumber + ": the line does not provide the expected number of columns (was: " + columns.length + ", expected: " + expectedNumberOfColumns + ")! Stop reading...");
			} else if (columns.length > expectedNumberOfColumns) {
				// only a warning since this might be desired if the data should
				// be loaded only partially
				LogService.getGlobal().log("Possible data format error: a line did not provide the expected number of columns (was: " + columns.length + ", expected: " + expectedNumberOfColumns + ")!", LogService.WARNING);
			}
		}
		lineNumber++;
		return columns;
	}
}
