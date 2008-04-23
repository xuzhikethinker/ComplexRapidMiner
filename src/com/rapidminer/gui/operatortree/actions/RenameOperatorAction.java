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
package com.rapidminer.gui.operatortree.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: RenameOperatorAction.java,v 1.1 2007/05/27 22:02:33 ingomierswa Exp $
 */
public class RenameOperatorAction extends ConditionalAction {

	private static final long serialVersionUID = -7839680821600037532L;

	private static final String ICON_NAME = "element_into_input.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}

	private OperatorTree operatorTree;
	
	public RenameOperatorAction(OperatorTree operatorTree, IconSize size) {
		super("Rename", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Rename the selected operator");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		setCondition(OPERATOR_SELECTED, MANDATORY);
		this.operatorTree = operatorTree;
	}

	public void actionPerformed(ActionEvent e) {
		this.operatorTree.renameOperator();
	}
}
