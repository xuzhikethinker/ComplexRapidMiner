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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DelegateAttribute;
import com.rapidminer.example.table.NominalMapping;

/**
 * This attribute replaces the nominal value mapping by the given one.
 * 
 * @author Ingo Mierswa
 * @version $Id: RemappedAttribute.java,v 1.1 2007/07/13 22:52:12 ingomierswa Exp $
 */
public class RemappedAttribute extends DelegateAttribute {

    private static final long serialVersionUID = 1167054050312730345L;
    
    private NominalMapping overlayedMapping;
    
    public RemappedAttribute(Attribute parent, NominalMapping overlayedMapping) {
        super(parent);
        this.overlayedMapping = overlayedMapping;
    }
    
    public void setValue(DataRow row, double value) {
        if (isNominal()) {
            String nominalValue = overlayedMapping.mapIndex((int)value);
            int newValue = super.getMapping().mapString(nominalValue);
            super.setValue(row, newValue);
        } else {
            super.setValue(row, value);
        }
    }
    
    public double getValue(DataRow row) {
        if (isNominal()) {
            double value = super.getValue(row);
            String nominalValue = super.getMapping().mapIndex((int)value);
            return overlayedMapping.mapString(nominalValue);
        } else {
            return super.getValue(row);
        }
    }
    
    public NominalMapping getMapping() {
        return overlayedMapping;
    }
}
