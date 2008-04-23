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
package com.rapidminer.gui.tools;


import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;

/**
 * A renderer for operator list cells that displays the operator's icon and name.
 *
 * @author Helge Homburg, Ingo Mierswa
 * @version $Id: OperatorListCellRenderer.java,v 1.1 2007/05/27 21:59:32 ingomierswa Exp $
 */
public class OperatorListCellRenderer extends DefaultListCellRenderer {   

	private static final long serialVersionUID = -4236587258844548010L;
	
    private boolean coloredBackground;
    
    public OperatorListCellRenderer(boolean coloredBackground) {
        this.coloredBackground = coloredBackground;
    }
    
	public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
		OperatorDescription operatorDescription	= (OperatorDescription)value;
		Component component = super.getListCellRendererComponent(list, operatorDescription.getName(), index, isSelected, cellHasFocus);
		JLabel label = (JLabel)component;
		try {
			label.setIcon(new ImageIcon(operatorDescription.getIcon()));
		} catch (Exception e) {
			// error --> no icon	
		}
        if (coloredBackground && (!isSelected) && (index % 2 != 0))
            label.setBackground(SwingTools.LIGHTEST_BLUE);
        label.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        
        Operator operator = null;
        try {
        	operator = operatorDescription.createOperatorInstance();
        } catch (OperatorCreationException e) {
            // tries to create operator
            LogService.getGlobal().log("Problem during creation of operator instance: " + e.getMessage(), LogService.WARNING);
        }
        StringBuffer toolTipText = new StringBuffer("<b>Description:</b> " + operatorDescription.getDescription());
        if (operator != null) {
        	toolTipText.append(Tools.getLineSeparator() + "<b>Input:</b> " + SwingTools.getStringFromClassArray(operator.getInputClasses()));
        	toolTipText.append(Tools.getLineSeparator() + "<b>Output:</b> " + SwingTools.getStringFromClassArray(operator.getOutputClasses()));
        }
        label.setToolTipText(SwingTools.transformToolTipText(toolTipText.toString()));
		return label;     
    }
}

 
