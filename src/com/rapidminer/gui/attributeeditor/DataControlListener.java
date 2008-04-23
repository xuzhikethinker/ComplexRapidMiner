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
package com.rapidminer.gui.attributeeditor;

/**
 * Can be used to listen for changes of the view area induced by a data control
 * object.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataControlListener.java,v 2.2 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public interface DataControlListener {

	/** The listener should change the viewport in this method. */
	public void update(int firstRow, int lastRow, int firstColumn, int lastColumn, int fractionDigits);

}
