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
package com.rapidminer.example;

import java.io.Serializable;
import java.util.Iterator;

import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.example.table.SparseFormatDataRowReader;


/**
 * An example consists of a DataRow and some convenience methods to access the data. 
 * Hence, all values are actually doubles, symbolic values are
 * mapped to integers stored in doubles.<br>
 * Since {@link ExampleSet}s are only a view on {@link ExampleTable}s,
 * Examples are generated on the fly by {@link ExampleReader}s. Since they only
 * contain the currently selected attributes operators need not to consider
 * attribute selections or example subsets (samplings).
 * 
 * @author Ingo Mierswa
 * @version $Id: Example.java,v 1.3 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class Example implements Serializable {

	private static final long serialVersionUID = 7761687908683290928L;

	/** Separator used in the getAttributesAsString() method (tab). */
	public static final String SEPARATOR = " ";
	
	/** Separates indices from values in sparse format (colon). */
	public static final String SPARSE_SEPARATOR = ":";
	
 
	/** The data for this example. */
	private DataRow data;

	/** The parent example set holding all attribute information for this data row. */
	private ExampleSet parentExampleSet;
	
	/**
	 * Creates a new Example that uses the data stored in a DataRow. The attributes 
	 * correspond to the regular and special attributes.
	 */
	public Example(DataRow data, ExampleSet parentExampleSet) {
		this.data = data;
		this.parentExampleSet = parentExampleSet;
	}

	/** Returns the data row which backs up the example in the example table. */
	public DataRow getDataRow() {
		return this.data;
	}
	
	/** Delivers the attributes. */
	public Attributes getAttributes() {
		return this.parentExampleSet.getAttributes();
	}
	
	// --------------------------------------------------------------------------------

	/**
	 * Returns the value of attribute a. In the case of nominal attributes, the delivered double value
     * corresponds to an internal index
	 */
    public double getValue(Attribute a) {
		return data.get(a);
	}
    
    /** Returns the nominal value for the given attribute. 
     * 
     *  @throws AttributeTypeException if the given attribute has the wrong value type
     */
    public String getNominalValue(Attribute a) {
        if (!a.isNominal()) {
            throw new AttributeTypeException("Extraction of nominal example value for non-nominal attribute '" + a.getName() + "' is not possible.");
        }
        return a.getMapping().mapIndex((int)getValue(a));
    }

    /** Returns the numerical value for the given attribute. 
     * 
     *  @throws AttributeTypeException if the given attribute has the wrong value type
     */
    public double getNumericalValue(Attribute a) {
        if (a.isNominal()) {
            throw new AttributeTypeException("Extraction of numerical example value for non-numerical attribute '" + a.getName() + "' is not possible.");
        }
        return getValue(a);
    }

	/**
	 * Sets the value of attribute a. The attribute a need not necessarily be
	 * part of the example set the example is taken from, although this is no
	 * good style.
	 */
	public void setValue(Attribute a, double value) {
		data.set(a, value);
	}

	/**
	 * Sets the value of attribute a which must be a nominal attribute. The
	 * attribute a need not necessarily be part of the example set the example
	 * is taken from, although this is no good style.
	 */
	public void setValue(Attribute a, String str) {
		if (!a.isNominal())
			throw new AttributeTypeException("setValue(Attribute, String) only supported for nominal values!");
		if (str != null)
			setValue(a, a.getMapping().mapString(str));
		else
			setValue(a, Double.NaN);
	}
	
    /** Returns true if both nominal values are the same (if both attributes
     *  are nominal) or if both real values are the same (if both attributes
     *  are real values) or false otherwise. */
    public boolean equalValue(Attribute first, Attribute second) {
        if (first.isNominal() && second.isNominal()) {
            return getValueAsString(first).equals(getValueAsString(second));
        } else if ((!first.isNominal()) && (!second.isNominal())) {
            return getValue(first) == getValue(second);
        } else {
            return false;
        }
    }
    
    // ---------------------------------------------------------------------------------
    
	public double getLabel() {
		return getValue(getAttributes().getLabel());
	}

	public void setLabel(double value) {
		setValue(getAttributes().getLabel(), value);
	}
	
	public double getPredictedLabel() {
		return getValue(getAttributes().getPredictedLabel());
	}

	public void setPredictedLabel(double value) {
		setValue(getAttributes().getPredictedLabel(), value);
	}
	
	public double getId() {
		return getValue(getAttributes().getId());
	}

	public void setId(double value) {
		setValue(getAttributes().getId(), value);
	}
	
	public double getWeight() {
		return getValue(getAttributes().getWeight());
	}
	
	public void setWeight(double value) {
		setValue(getAttributes().getWeight(), value);
	}
	
	public double getConfidence(String classValue) {
		return getValue(getAttributes().getSpecial(Attributes.CONFIDENCE_NAME + "(" + classValue + ")"));	
	}

	public void setConfidence(String classValue, double confidence) {
		setValue(getAttributes().getSpecial(Attributes.CONFIDENCE_NAME + "(" + classValue + ")"), confidence);	
	}
	
	// --------------------------------------------------------------------------------

	/**
	 * <p>Returns the value of this attribute as string representation, i.e. the
	 * number as string for numerical attributes and the correctly mapped
	 * categorical value for nominal values. The used number of fraction 
	 * digits is unlimited
	 * (see {@link NumericalAttribute#DEFAULT_NUMBER_OF_DIGITS}).
	 * Nominal values containing whitespaces will not be quoted.</p>
     * 
     * <p>Please note that this method should not be used in order to get the 
     * nominal values, please use 
     * {@link #getNominalValue(Attribute)} instead.</p>
	 */
	public String getValueAsString(Attribute attribute) {
		return getValueAsString(attribute, NumericalAttribute.UNLIMITED_NUMBER_OF_DIGITS, false);
	}

	/**
	 * <p>Returns the value of this attribute as string representation, i.e. the
	 * number as string for numerical attributes and the correctly mapped
	 * categorical value for nominal values. If the value is numerical the given
	 * number of fraction digits is used. If the value is numerical, the given
	 * number of fraction digits is used. This value must be either one out of
	 * {@link NumericalAttribute#DEFAULT_NUMBER_OF_DIGITS} or
	 * {@link NumericalAttribute#UNLIMITED_NUMBER_OF_DIGITS} or a number greater
	 * or equal to 0. The boolean flag indicates if nominal values containing
	 * whitespaces should be quoted with double quotes.</p>
     * 
     * <p>Please note that this method should not be used in order to get the 
     * nominal values, please use 
     * {@link #getNominalValue(Attribute)} instead.</p>
	 */
	public String getValueAsString(Attribute attribute, int fractionDigits, boolean quoteWhitespace) {
		double value = getValue(attribute);
		return attribute.getAsString(value, fractionDigits, quoteWhitespace);
	}
	
    /** Returns a dense string representation with all possible fraction digits.
     *  Nominal values containing whitespaces will be quoted with double 
     *  quotes.
     */
    public String toString() {
    	return toDenseString(NumericalAttribute.UNLIMITED_NUMBER_OF_DIGITS, true);
    }

	/**
	 * This method returns a dense string representation of the example. It
	 * first returns the values of all special attributes and then the values of
	 * all regular attributes.
	 */
	public String toDenseString(int fractionDigits, boolean quoteWhitespace) {
		StringBuffer result = new StringBuffer();
		Iterator<Attribute> a = getAttributes().allAttributes();
		while (a.hasNext()) {
			result.append(getValueAsString(a.next(), fractionDigits, quoteWhitespace) + SEPARATOR);
		}
		return result.toString();
	}

	/**
	 * Returns regular and some special attributes (label, id, and example
	 * weight) in sparse format.
	 * 
	 * @param format
	 *            one of the formats specified in
	 *            {@link SparseFormatDataRowReader}
	 */
	public String toSparseString(int format, int fractionDigits, boolean quoteWhitespace) {
		StringBuffer str = new StringBuffer();
		// label
		Attribute labelAttribute = getAttributes().getSpecial(Attributes.LABEL_NAME);
		if ((format == SparseFormatDataRowReader.FORMAT_YX) && (labelAttribute != null)) {
			str.append(getValueAsString(labelAttribute, fractionDigits, quoteWhitespace) + " ");
		}
		// id
		Attribute idAttribute = getAttributes().getSpecial(Attributes.ID_NAME);
		if (idAttribute != null) {
			str.append("id:" + getValueAsString(idAttribute, fractionDigits, quoteWhitespace) + " ");
		}
		// weight
		Attribute weightAttribute = getAttributes().getSpecial(Attributes.WEIGHT_NAME);
		if (weightAttribute != null) {
			str.append("w:" + getValueAsString(weightAttribute, fractionDigits, quoteWhitespace) + " ");
		}
		// attributes
		str.append(getAttributesAsSparseString(SEPARATOR, SPARSE_SEPARATOR, fractionDigits, quoteWhitespace) + " ");
		// label (format xy & prefix)
		if ((format == SparseFormatDataRowReader.FORMAT_PREFIX) && (labelAttribute != null)) {
			str.append("l:" + getValueAsString(labelAttribute, fractionDigits, quoteWhitespace));
		}
		if ((format == SparseFormatDataRowReader.FORMAT_XY) && (labelAttribute != null)) {
			str.append(getValueAsString(labelAttribute, fractionDigits, quoteWhitespace));
		}
		return str.toString();
	}
	
	/**
	 * Returns the attribute values in the format <br>
	 * <center>index:value index:value</center><br>
	 * Index starts with 1.
	 * 
	 * @param separator
	 *            separates attributes
	 * @param indexValueSeparator
	 *            separates index and value.
	 * @param fractionDigits the number of fraction digits used, if -1 all possible digits are used
	 */
	/* pp */ String getAttributesAsSparseString(String separator, String indexValueSeparator, int fractionDigits, boolean quoteWhitespace) {
		StringBuffer str = new StringBuffer();
		boolean first = true;
		int counter = 1;
		for (Attribute attribute : getAttributes()) {
			double value = getValue(attribute);
			if (!Tools.isDefault(attribute.getDefault(), value)) {
				if (!first) {
					str.append(separator);
				}
				first = false;
				str.append(counter + indexValueSeparator + getValueAsString(attribute, fractionDigits, quoteWhitespace));
			}
			counter++;
		}
		return str.toString();
	}
}
