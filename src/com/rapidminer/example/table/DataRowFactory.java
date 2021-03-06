/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.example.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.LogService;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.table.ComplexAttribute;
import de.tud.inf.example.table.RelationalAttribute;


/**
 * Factory class for DataRow objects. One factory should be used for one
 * ExampleTable only. This class is necessary to customize implementations of
 * DataRowReader to create DataRows of arbitrary type.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: DataRowFactory.java,v 1.10 2008/07/13 23:25:24 ingomierswa Exp $
 */
public class DataRowFactory {

 	public static final String[] TYPE_NAMES = { 
 		"double_array", "float_array", 
 		"long_array", "int_array", "short_array", "byte_array", 
 		"boolean_array", 
 		"double_sparse_array", "float_sparse_array", 
 		"long_sparse_array", "int_sparse_array", "short_sparse_array", "byte_sparse_array", 
 		"boolean_sparse_array",
 		"sparse_map"
 	};

	public static final int FIRST_TYPE_INDEX = 0;

	public static final int TYPE_DOUBLE_ARRAY = 0;

    public static final int TYPE_FLOAT_ARRAY = 1;

    public static final int TYPE_LONG_ARRAY = 2;

    public static final int TYPE_INT_ARRAY = 3;
    
    public static final int TYPE_SHORT_ARRAY = 4;
    
    public static final int TYPE_BYTE_ARRAY = 5;
    
    public static final int TYPE_BOOLEAN_ARRAY = 6;
    
    public static final int TYPE_DOUBLE_SPARSE_ARRAY = 7;
    
    public static final int TYPE_FLOAT_SPARSE_ARRAY = 8;
    
    public static final int TYPE_LONG_SPARSE_ARRAY = 9;

    public static final int TYPE_INT_SPARSE_ARRAY = 10;

	public static final int TYPE_SHORT_SPARSE_ARRAY = 11;

	public static final int TYPE_BYTE_SPARSE_ARRAY = 12;
	
	public static final int TYPE_BOOLEAN_SPARSE_ARRAY = 13;
	
	public static final int TYPE_SPARSE_MAP = 14;
	
	public static final int TYPE_RELATIONAL_EXTENDED = 15;
	
	public static final int LAST_TYPE_INDEX = 15;
	

	/**
	 * The type can be one out of 
	 * TYPE_DOUBLE_ARRAY, TYPE_FLOAT_ARRAY, TYPE_LONG_ARRAY, TYPE_INT_ARRAY, TYPE_SHORT_ARRAY, TYPE_BYTE_ARRAY, TYPE_BOOLEAN_ARRAY,
     * TYPE_DOUBLE_SPARSE_ARRAY, TYPE_FLOAT_SPARSE_ARRAY, TYPE_LONG_SPARSE_ARRAY, TYPE_INT_SPARSE_ARRAY, TYPE_SHORT_SPARSE_ARRAY, TYPE_BYTE_SPARSE_ARRAY, TYPE_BOOLEAN_SPARSE_ARRAY,
     * or TYPE_SPARSE_MAP.
	 */
	private int type;

	/** The decimal point character. */
	private char decimalPointCharacter = '.';
	
	/** relational attribute instance seperators */
	private final String valueSep = ",";
	private final String tupleSep = "\n";
	
	
	/**
	 * @param type
	 *            must be one out of 
	 *            TYPE_DOUBLE_ARRAY, TYPE_FLOAT_ARRAY, TYPE_LONG_ARRAY, TYPE_INT_ARRAY, TYPE_SHORT_ARRAY, TYPE_BYTE_ARRAY, TYPE_BOOLEAN_ARRAY, 
	 *            TYPE_DOUBLE_SPARSE_ARRAY, TYPE_FLOAT_SPARSE_ARRAY, TYPE_SHORT_SPARSE_ARRAY, TYPE_BYTE_SPARSE_ARRAY, TYPE_BOOLEAN_SPARSE_ARRAY,
	 *            or TYPE_SPARSE_MAP.
	 * @deprecated Please do not use this constructor any longer. Use the constructor {@link #DataRowFactory(int, char)} instead. 
	 */
	@Deprecated
	public DataRowFactory(int type) {
		this(type, '.');
	}
	
	/**
	 * @param type
	 *            must be one out of 
	 *            TYPE_DOUBLE_ARRAY, TYPE_FLOAT_ARRAY, TYPE_LONG_ARRAY, TYPE_INT_ARRAY, TYPE_SHORT_ARRAY, TYPE_BYTE_ARRAY, TYPE_BOOLEAN_ARRAY, 
	 *            TYPE_DOUBLE_SPARSE_ARRAY, TYPE_FLOAT_SPARSE_ARRAY, TYPE_LONG_SPARSE_ARRAY, TYPE_INT_SPARSE_ARRAY, TYPE_SHORT_SPARSE_ARRAY, TYPE_BYTE_SPARSE_ARRAY, TYPE_BOOLEAN_SPARSE_ARRAY,
	 *            or TYPE_SPARSE_MAP.
	 * @param decimalPointCharacter the letter for decimal points, usually '.'
	 */
	public DataRowFactory(int type, char decimalPointCharacter) {
		if ((type < FIRST_TYPE_INDEX) || (type > LAST_TYPE_INDEX))
			throw new IllegalArgumentException("Illegal data row type: " + type);
		this.type = type;
		this.decimalPointCharacter = decimalPointCharacter;
	}

	/** Creates a new DataRow with the given initial capacity. */
	public DataRow create(int size) {
		DataRow row = null;
		switch (type) {
			case TYPE_DOUBLE_ARRAY:
				row = new DoubleArrayDataRow(new double[size]);
				break;
            case TYPE_FLOAT_ARRAY:
                row = new FloatArrayDataRow(new float[size]);
                break;
            case TYPE_LONG_ARRAY:
                row = new LongArrayDataRow(new long[size]);
                break;
            case TYPE_INT_ARRAY:
                row = new IntArrayDataRow(new int[size]);
                break;
            case TYPE_SHORT_ARRAY:
                row = new ShortArrayDataRow(new short[size]);
                break;
            case TYPE_BYTE_ARRAY:
                row = new ByteArrayDataRow(new byte[size]);
                break;
            case TYPE_BOOLEAN_ARRAY:
                row = new BooleanArrayDataRow(new boolean[size]);
                break;
            case TYPE_DOUBLE_SPARSE_ARRAY:
            	row = new DoubleSparseArrayDataRow(size >> 2);
                break;
            case TYPE_FLOAT_SPARSE_ARRAY:
                row = new FloatSparseArrayDataRow(size >> 2);
                break;
            case TYPE_LONG_SPARSE_ARRAY:
            	row = new LongSparseArrayDataRow(size >> 2);
            	break;
            case TYPE_INT_SPARSE_ARRAY:
            	row = new IntSparseArrayDataRow(size >> 2);
            	break;
            case TYPE_SHORT_SPARSE_ARRAY:
            	row = new ShortSparseArrayDataRow(size >> 2);
            	break;
            case TYPE_BYTE_SPARSE_ARRAY:
            	row = new ByteSparseArrayDataRow(size >> 2);
            	break;
            case TYPE_BOOLEAN_SPARSE_ARRAY:
            	row = new BooleanSparseArrayDataRow(size >> 2);
            	break;
			case TYPE_SPARSE_MAP:
				row = new SparseMapDataRow();
				break;
			default:
		}
		return row;
	}

	/**
	 * Creates a data row from an array of Strings. If the corresponding
	 * attribute is nominal, the string is mapped to its index, otherwise it is
	 * parsed using <code>Double.parseDouble(String)</code> .
	 * 
	 * @see FileDataRowReader
	 */
	public DataRow create(String[] strings, Attribute[] attributes) {
		DataRow dataRow = create(strings.length);
		//if null, attribute set contains no relational attributes
		Map<Integer,double [][]> rlValues = null;
		for (int i = 0; i < strings.length; i++) {
			if (strings[i] != null)
				strings[i] = strings[i].trim();
			if ((strings[i] != null) && (strings[i].length() > 0) && (!strings[i].equals("?"))) {
				if (attributes[i].isNominal()) {
					dataRow.set(attributes[i], attributes[i].getMapping().mapString(strings[i]));
				}
				else if(attributes[i].isRelational()){
					dataRow.initRelationalMap();
					if(rlValues == null) rlValues = new HashMap<Integer, double[][]>();
					//then string[i] contains arbitrary tuples of innerAttributs.size() - number of attributes
					//saves them as double.. get attribute information from ComplexAttribute.getInnerAttributes.type
					RelationalAttribute ca = (RelationalAttribute)attributes[i];
					
					//in overall dataRow set index "pointer" on entry in Map rlValue at position i (later table index)
					
					//separate tuples of relational attribute
					String[] tuples = strings[i].split(tupleSep);
					//separate instances of innerAttributes
					double[][] rlValue = new double[tuples.length][];
					for(int tpl = 0;tpl<rlValue.length;tpl++)
						rlValue[tpl] = new double[ca.getInnerAttributes().size()];
					//now relValues is well instantiated
					Attribute iAtt = null;
					for(int ia =0;ia < ca.getInnerAttributes().size();ia++){
							iAtt = ca.getInnerAttributes().get(ia);
							if(iAtt.isNominal())
								for(int t=0;t<tuples.length;t++)
								  rlValue[t][ia] = iAtt.getMapping().mapString(tuples[t].split(valueSep)[ia]);
							else
								for(int t=0;t<tuples.length;t++)
								  rlValue[t][ia] = string2Double(tuples[t].split(valueSep)[ia],this.decimalPointCharacter);
					}
					// i == tableIndex??
					rlValues.put(i,rlValue);
				}else {
					dataRow.set(attributes[i], string2Double(strings[i], this.decimalPointCharacter));
				}
			} else {
				if(attributes[i].isRelational())
					dataRow.initRelationalMap();
				if(attributes[i].isNominal())
					dataRow.set(attributes[i], attributes[i].getMapping().mapString(""));
				else dataRow.set(attributes[i], Double.NaN);
			}
		}
		dataRow.trim();
		if(rlValues != null){
			dataRow.setRelationalValues(rlValues);
		}
		return dataRow;
	}
	
	/**
	 * Creates a data row from an Object array. The classes of the object must
	 * match the value type of the corresponding {@link Attribute}. If the
	 * corresponding attribute is nominal, <code>data[i]</code> will be cast
	 * to String. If it is numerical, it will be cast to Number.
	 * 
	 * @throws ClassCastException
	 *             if data class does not match attribute type
	 * @see DatabaseDataRowReader
	 */
	public DataRow create(Object[] data, Attribute[] attributes) {
		
		/*
		 * changed, because data and attributes can contain ComplexValue and ComplexAttributes
		 */
		/** split the attributes in Complex and SimpleAttributes **/
		List<Attribute> complexAttributes = new ArrayList<Attribute>();
		List<Attribute> simpleAttributes = new ArrayList<Attribute>();
		List complexObjects = new ArrayList();
		List simpleObjects = new ArrayList();
		int dataRowSize = 0;
		for(int i = 0;i<attributes.length;i++) {
			Attribute a = attributes[i];
			if(a.isComplex()) {
				ComplexAttribute ca = (ComplexAttribute)a;
				complexAttributes.add(a);
				complexObjects.add(data[i]);
				dataRowSize += ca.getInnerAttributeCount() + ca.getParameterCount();
			} else {
				simpleAttributes.add(a);
				simpleObjects.add(data[i]);
				dataRowSize++;
			}
		}
		Attribute[] attr = new Attribute[attributes.length];
		attributes = simpleAttributes.toArray(attr);
		data = simpleObjects.toArray();
		DataRow dataRow = create(dataRowSize);
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				if (attributes[i].isNominal()) {
					dataRow.set(attributes[i], attributes[i].getMapping().mapString(((String) data[i]).trim()));
				} else {
					dataRow.set(attributes[i], ((Number) data[i]).doubleValue());
				}
			} else {
				dataRow.set(attributes[i], Double.NaN);
			}
		}
		// set the Complex Attributes
		for(int i =0;i<complexAttributes.size();i++) {
			dataRow.set((ComplexAttribute)complexAttributes.get(i), (ComplexValue)complexObjects.get(i));
		}
		dataRow.trim();
		return dataRow;
	}

	
	/**
	* Creates a data row from an Object array. The classes of the object must
	* match the value type of the corresponding {@link Attribute}. If the
	* corresponding attribute is nominal, <code>data[i]</code> will be cast
	* to String. If it is numerical, it will be cast to Number.
	* 
	* @throws ClassCastException
	* if data class does not match attribute type
	* @see DatabaseDataRowReader
	*/
	public DataRow create(Double[] data, Attribute[] attributes) {
		DataRow dataRow = create(data.length);
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				if (attributes[i].isNominal()) {
					dataRow.set(attributes[i], attributes[i].getMapping().mapString((String.valueOf(data[i])).trim()));
				} else {
					dataRow.set(attributes[i], ((Number) data[i]).doubleValue());
				}
			} else {
				dataRow.set(attributes[i], Double.NaN);
			}
		}
		dataRow.trim();
		return dataRow;
	}

	
	/** Returns the type of the created data rows. */
	public int getType() {
		return type;
	}

	// --------------------------------------------------------------------------------

	private static final double string2Double(String str, char decimalPointCharacter) {
		if (str == null)
			return Double.NaN;
		try {
			str = str.replace(decimalPointCharacter, '.');
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			LogService.getGlobal().log("DataRowFactory.string2Double(String): '" + str + "' is not a valid number!", LogService.ERROR);
			return Double.NaN;
		}
	}
}
