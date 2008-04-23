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
package com.rapidminer.gui.plotter;

/** Helper class for the plotter point positions and colors. The positions are defined in plotter space. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: ExamplePlotterPoint.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class ExamplePlotterPoint {
    
    private int x;
    private int y;
    private int dataTableIndex;
    private int currentPertubatedX;
    private int currentPertubatedY;
    
    public ExamplePlotterPoint (int dataTableIndex, int x, int y) {
        this.x = x;
        this.y = y;
        this.dataTableIndex = dataTableIndex;
    }
    
    public int getX() {
        return x;
    }
    
    public int getDataTableIndex() {
        return dataTableIndex;
    }
    
    public int getY() {
        return y;
    }
    
    public int getCurrentPertubatedX() {
        return currentPertubatedX;
    }
    
    public void setCurrentPertubatedX(int currentPertubatedX) {
        this.currentPertubatedX = currentPertubatedX;
    }
    
    public int getCurrentPertubatedY() {
        return currentPertubatedY;
    }
    
    public void setCurrentPertubatedY(int currentPertubatedY) {
        this.currentPertubatedY = currentPertubatedY;
    }
    
    public boolean contains(int x, int y) {
        if ((Math.abs(this.getCurrentPertubatedX() - x) < 3) && (Math.abs(this.getCurrentPertubatedY() - y) < 3)){
            return true;
        }
        return false;
    }
}
