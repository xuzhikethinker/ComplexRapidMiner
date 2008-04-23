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
package com.rapidminer.tools.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * By using this multiplier all written content is multiplied to the given
 * writers.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: StreamMultiplier.java,v 1.1 2007/05/27 21:58:59 ingomierswa Exp $
 */
public class StreamMultiplier extends OutputStream {

	/** The writers to write the contents to. */
	private OutputStream[] out;

	/** Creates a new writer multiplier. */
	public StreamMultiplier(OutputStream out1, OutputStream out2) {
		this(new OutputStream[] { out1, out2 });
	}
	
	/** Creates a new writer multiplier. */
	public StreamMultiplier(OutputStream[] out) {
		this.out = out;
	}

	/** Implements the abstract method of the superclass. */
	public void write(int b) throws IOException {
	    for (int i = 0; i < out.length; i++) {
	        out[i].write(b);
	    }
	}

	/** Closes all writers. */
	public void close() throws IOException {
		for (int i = 0; i < out.length; i++) {
			if ((!System.out.equals(out[i])) && (!System.err.equals(out[i])))
				out[i].close();
		}
	}

	/** Flushes all writers. */
	public void flush() throws IOException {
		for (int i = 0; i < out.length; i++) {
			out[i].flush();
		}
	}
}
