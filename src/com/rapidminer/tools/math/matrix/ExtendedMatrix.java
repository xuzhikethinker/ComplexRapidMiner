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
package com.rapidminer.tools.math.matrix;

import java.util.Iterator;

import com.rapidminer.operator.ResultObject;


/**
 * Some additional matrix functionality.
 * 
 * @author Michael Wurst
 * @version $Id: ExtendedMatrix.java,v 1.1 2007/05/27 22:03:34 ingomierswa Exp $
 * 
 */
public interface ExtendedMatrix<Ex, Ey> extends Matrix<Ex, Ey>, ResultObject {

    /**
     * Get the number of entries greater than zero in row x.
     * 
     * @param x the row index
     * @return int
     */
    public int getNumYEntries(Ex x);

    /**
     * Get the number of entries greater than zero in column y.
     * 
     * @param y the column index
     * @return int
     */
    public int getNumXEntries(Ey y);

    /**
     * Get all y labels with entry (x,y) greater zero.
     * 
     * @param x
     * @return Iterator
     */
    public Iterator<Ey> getYEntries(Ex x);

    /**
     * Get all x labels with entry (x,y) greater zero.
     * 
     * @param y
     * @return Iterator
     */
    public Iterator<Ex> getXEntries(Ey y);

}
