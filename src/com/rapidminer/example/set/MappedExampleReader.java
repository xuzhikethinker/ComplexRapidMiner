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

import java.util.Iterator;

import com.rapidminer.example.Example;


/**
 * This example reader is based on the given mapping and skips all examples which are not
 * part of the mapping. If an index occurs more than once the example will be returned the
 * number of desired times. For performance reasons the mapping array must have been sorted
 * beforehand. 
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: MappedExampleReader.java,v 1.1 2007/05/27 21:59:00 ingomierswa Exp $
 */
public class MappedExampleReader extends AbstractExampleReader {

    /** The example reader that provides a complete example set. */
    private Iterator<Example> parent;

    /** The used mapping. */
    private int[] mapping;

    /** The example that will be returned by the next invocation of next(). */
    private Example currentExample;

    /** Indicates if the current example was &quot;delivered&quot; by a call of {@link #next()}. */
    private boolean nextInvoked = true;
    
    /** The current index in the mapping. */
    private int index = -1;
    
    /** Constructs a new mapped example reader. */
    public MappedExampleReader(Iterator<Example> parent, int[] mapping) {
        this.parent = parent;
        this.currentExample = null;
        this.mapping = mapping;
    }

    public boolean hasNext() {
        if (this.nextInvoked) {
            this.nextInvoked = false;
            int oldMapping = -1;
            if (index >= this.mapping.length - 1)
                return false;
            if (index != -1)
                 oldMapping = this.mapping[index];
            this.index++;
            int newMapping = this.mapping[index];
   
            if (newMapping != oldMapping) {
                do {
                    if (!parent.hasNext())
                        return false;
                    this.currentExample = parent.next();
                    oldMapping++;
                } while (oldMapping < newMapping);
            } else {
                return true;
            }
        }
        return true;
    }

    public Example next() {
        if (hasNext()) {
            this.nextInvoked = true;
            return currentExample;
        } else {
            return null;
        }
    }
}

