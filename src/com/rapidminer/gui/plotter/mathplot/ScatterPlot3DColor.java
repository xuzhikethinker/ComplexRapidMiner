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
package com.rapidminer.gui.plotter.mathplot;

import java.awt.Color;
import java.util.Iterator;

import org.math.plot.Plot3DPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;


/** This plotter can be used to create 3D scatter plots where a 4th dimension can be shown by using a color scale. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: ScatterPlot3DColor.java,v 1.2 2007/07/15 22:06:25 ingomierswa Exp $
 */
public class ScatterPlot3DColor extends JMathPlotter3D {

	private static final long serialVersionUID = 6967871061963724679L;

	public ScatterPlot3DColor(){
		super();
	}
	
	public ScatterPlot3DColor(DataTable dataTable){
		super(dataTable);
	}
	
	public void update() {
		int colorColumn = -1;
		if (getAxis(0)!= -1&& getAxis(1)!= -1 && getAxis(2) != -1) {
			getPlotPanel().removeAllPlots();
			for (int currentVariable = 0; currentVariable < countColumns(); currentVariable ++){
				if (getPlotColumn(currentVariable)) {
					double min = Double.POSITIVE_INFINITY;
					double max = Double.NEGATIVE_INFINITY;
					colorColumn = currentVariable;
					DataTable table = getDataTable();
					synchronized (table) {
						Iterator<DataTableRow> iterator = table.iterator();
						//search for bounds
						while (iterator.hasNext()) {
							DataTableRow row = iterator.next();
							double value = row.getValue(currentVariable);
							min = Math.min(min, value);
							max = Math.max(max, value);
						}
						iterator = getDataTable().iterator();
						while (iterator.hasNext()) {
							double[][] data = new double[1][3];
							DataTableRow row = iterator.next();
							data[0][0] = row.getValue(getAxis(0));
							data[0][1] = row.getValue(getAxis(1));
							data[0][2] = row.getValue(getAxis(2));	
							double colorValue = getPointColorValue(table, row, currentVariable, min, max);
							Color color = getPointColor(colorValue);
							((Plot3DPanel)getPlotPanel()).addScatterPlot(getDataTable().getColumnName(currentVariable), color, data);
						}
					}
				}
			}
		}
		if (colorColumn != -1)
			getLegendComponent().setLegendColumn(getDataTable(), colorColumn);
	}

	public boolean hasLegend(){
		return false;
	}	
	
	public boolean hasRapidMinerValueLegend() {
		return true;
	}
	
	public int getNumberOfAxes() {
		return 3;
	}

	public String getAxisName(int index) {
		switch (index) {
			case 0:
				return "x-Axis";
			case 1:
				return "y-Axis";
			case 2:
				return "z-Axis";
			default:
				return "empty";
		}
	}

	public String getPlotName(){
		return "Color";
	}	
}

