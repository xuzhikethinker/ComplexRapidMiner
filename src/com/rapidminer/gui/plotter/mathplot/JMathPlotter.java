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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.math.plot.PlotPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.plotter.PlotterLegend;


/** The abstract super class for all plotters using the JMathPlot library. The actual plotting must
 *  be done in the method {@link #paintComponent(Graphics)} where some helper methods defined in this
 *  class can be used. Another method usually implemented is {@link #getNumberOfAxes()}.
 *  
 *  @author Ingo Mierswa, Sebastian Land
 *  @version $Id: JMathPlotter.java,v 1.2 2007/07/15 22:06:25 ingomierswa Exp $
 */
public abstract class JMathPlotter extends PlotterAdapter {
	
	/** Indicates the position of the JMathPlot legend. */
	private static final String LEGEND_POSITION = "NORTH";
	
	/** The currently used data table object. */
	private DataTable dataTable;

	/** The actual plotter panel of JMathPlot. */
	private PlotPanel plotpanel;
	
	/** The plotter legend which can be used to display the values with respect to the used colors. */
	private PlotterLegend legend;
	
	/** Indicates which columns will be plotted. */
	private boolean[] columns = new boolean[0];
	
	/** The used axes columns. */
	private int[] axis = new int[] { -1, -1 };
	
	
	/** Creates a new JMathPlotter. Removes the data view button and adds the legend under the plotter panel
	 *  if the method {@link #hasLegend()} returns true. If the method {@link #hasRapidMinerValueLegend()} returns
	 *  true the usual RapidMiner color legend will be used ({@link PlotterLegend}). */
	public JMathPlotter() {
		this.plotpanel = createPlotPanel();
        // removes the icon for dataview in the toolbar
		this.plotpanel.plotToolBar.remove(5);
		this.plotpanel.removePlotToolBar();
		if (hasLegend())
			this.plotpanel.addLegend(LEGEND_POSITION);
		
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		if (hasRapidMinerValueLegend()) {
			legend = new PlotterLegend(this);
			c.weighty = 0;			
			JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			legendPanel.setBackground(Color.white);
			legendPanel.add(legend);
			layout.setConstraints(legendPanel, c);
			add(legendPanel);
		}
		c.weighty = 1;
		layout.setConstraints(plotpanel, c);
		add(plotpanel);
		
		this.axis = new int[getNumberOfAxes()];
		for (int i = 0; i < this.axis.length; i++)
			this.axis[i] = -1;
	}
	
	/** Creates the new plotter and sets the data table. */
	public JMathPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}
	
	/** Must be implemented by subclasses in order to support 2D or 3D plots. */
	protected abstract PlotPanel createPlotPanel();
	
	protected abstract void update();
	
	// =============================
	// helper method for subclasses
	// =============================
	
	protected PlotterLegend getLegendComponent() {
		return this.legend;
	}
	
	protected boolean hasLegend() {
		return true;
	}
		
	protected boolean hasRapidMinerValueLegend() {
		return false;
	}
	
	protected DataTable getDataTable() {
		return this.dataTable;
	}
	
	protected int countColumns() {
		return this.columns.length;
	}
	
	protected PlotPanel getPlotPanel() {
		return this.plotpanel;
	}
	
	// ==============================================
	
	public void setAxis(int index, int dimension) {
		if (index >= 0 && index < getNumberOfAxes()) {
			if (axis[index] != dimension) {
				axis[index] = dimension;
			}
		}
		repaint();
	}

	public int getAxis(int index) {
		if (index >= 0 && index < getNumberOfAxes()) {
			if (this.axis == null)
				return -1;
			else
				return this.axis[index];
		} else {
			return -1;
		}
	}
	
	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		columns = new boolean[dataTable.getNumberOfColumns()]; 
	}
	
	public Icon getIcon(int index) {
		return null;
	}

	public int getNumberOfAxes() {
		return 2;
	}

	public String getAxisName(int index) {
		switch (index) {
			case 0:
				return "x-Axis";
			case 1:
				return "y-Axis";
			default:
				return "none";
		}
	}

	public void setPlotColumn(int index, boolean plot) {
		if (getValuePlotSelectionType() == MULTIPLE_SELECTION) {
			if (this.columns[index] != plot) {
				if (index != -1)
					columns[index] = plot;
			}
		} else {
			this.columns = new boolean[columns.length];
			if (index != -1)
				this.columns[index] = plot;
		}
		repaint();
	}

	public boolean getPlotColumn(int index) {
		return columns[index];
	}

	public JComponent getOptionsComponent(int index) {
		if (index == 0) {
			return this.plotpanel.plotToolBar;
		} else {
			return null;
		}
	}

	public boolean hasSaveImageButton() {
		return true;
	}
	
	public void repaint() {
		update();
		super.repaint();
	}
}
