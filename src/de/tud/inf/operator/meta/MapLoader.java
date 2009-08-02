package de.tud.inf.operator.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.ComplexExampleTable;
import de.tud.inf.example.table.MapAttribute;
import de.tud.inf.parameters.ParameterTypeFiles;

public class MapLoader extends OperatorChain{
	
	public static String PARAMETER_FILES = "files";
	
	public static String PARAMETER_MAP_FILE = "map file";
	
	public static String PARAMETER_MAP_TYPE = "map type";
	
	public static String PARAMETER_BASE_FOLDER = "data folder";
	
	public static String PARAMETER_FOLDER = "folder";
	
	public static String[] MAP_TYPES = {
		"density",
		"cdo",
		"netto",
		"image",
		"misc"
	};
	
	public static String PARAMETER_DELIMITER = "delimiter";

	public MapLoader(OperatorDescription description) {
		super(description);
		
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		
		int numberOfMaps;
		/** read the 'ordernumbers and orderlines' from the map file */
		int mapType = getParameterAsInt("map type");
		
		// get maps to process
		List<String> orderNumbers = new ArrayList<String>();
		List<String> orderLines =  new ArrayList<String>();
		if(this.getParameterAsString(PARAMETER_FILES) != null && this.getParameterAsString(PARAMETER_FILES).length() > 0) {
			
		} else {
			
		}
		
		ComplexExampleTable complexExampleTable;
		
		File mapFile = getParameterAsFile(PARAMETER_MAP_FILE);
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(mapFile));
		
		String zeile = null;
		
			while ((zeile = in.readLine()) != null) {
				String[] string = zeile.split(getParameterAsString(PARAMETER_DELIMITER));
				if (mapType == 0) {
					
					String[] string2 = string[0].split("_");
					orderNumbers.add(string2[0]);
					orderLines.add(string2[1]);
					throw new OperatorException("density maps not supported at the moment");
				} else {
					orderNumbers.add(string[0]);
				}
			}
		
		
		numberOfMaps = orderNumbers.size();
		//String fileString = this.getParameterAsString(PARAMETER_FILES);
		
		Operator exSource = this.getOperator(0);
		
		/*
		 * create ExampleTable for the map
		 */
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(AttributeFactory.createAttribute("map_name", Ontology.NOMINAL));
		
		MemoryExampleTable parent = new MemoryExampleTable(attributes);
		complexExampleTable = new ComplexExampleTable(parent);
		
		/**create and add complex Map Attribute */
		MapAttribute mapAttr = (MapAttribute)AttributeFactory.createAttribute("map", Ontology.ATTRIBUTE_VALUE_TYPE.MAP);
		complexExampleTable.addComplexAttribute(mapAttr);
		attributes.add(mapAttr);
		
		Attribute[] attr = new Attribute[attributes.size()];
		attr = attributes.toArray(attr);
		
		Object[] data = new Object[attr.length];
		
		DataRowFactory fac = null;
		
		//String[] files = fileString.split(";");
		if(exSource instanceof ExampleSource) {
			fac = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, ExampleSource.PARAMETER_DECIMAL_POINT_CHARACTER.charAt(0));
			int mapI = 0;
			for(String filename : orderNumbers) {
				
				Attribute x;
				Attribute y;
				Attribute z;
				
				// create map name
				String mapName = "";
				if (mapType <= 2) {
					mapName = MAP_TYPES[mapType] + "_" + filename;
					if (mapType == 0)
						mapName += "_" + orderLines.get(mapI);
				}
				else 
					mapName = filename;
				
				// create file string
				File dir = new File (".");
				//String fileString = dir.getCanonicalPath() + System.getProperty("file.separator")+ "sample" + System.getProperty("file.separator") + "fingerprints" + System.getProperty("file.separator") + getParameterAsString("folder") + System.getProperty("file.separator");
				String fileString = getParameterAsString(PARAMETER_BASE_FOLDER) + System.getProperty("file.separator");
				if (mapType == 3)
					fileString += mapName;
				else
					fileString += mapName + ".aml";
				File file = new File(fileString);
				
				exSource.setParameter(ExampleSource.PARAMETER_ATTRIBUTES, fileString);
				
				IOContainer output = exSource.apply(new IOContainer());
				double[] origin = new double[2];
				double[] zvalues;
				/*
				 * Assumption: ExampleSet has is ordered according to the x and y values
				 */
				ExampleSet examples = output.get(ExampleSet.class);
				// get the Attributes for the x,y,z value
				x = examples.getAttributes().get("X");
				y = examples.getAttributes().get("Y");
				z = examples.getAttributes().get("Z");

				zvalues = new double[examples.size()];
				// -> the first example is the origin
				Example ex = examples.getExample(0);
				origin[0] = ex.getValue(x);
				origin[1] = ex.getValue(y);
				double lastX = origin[0];
				double lastY = origin[1];
				double xValue;
				double yValue;
				
				double spacing[] = new double[2];
				int dimension[] = {1,1};
				int dimcounter = 0;
				int i = 0;
				for(Example e : examples) {
					xValue = e.getValue(x);
					yValue = e.getValue(y);
					
					if(lastX != xValue) {
						if(spacing[0] >0) {
							if(spacing[0] != xValue -lastX)
								throw new OperatorException("spacing in x direction not equidistant over the whole example set");
						} else 
							spacing[0] = xValue -lastX;
						lastX = xValue;
						if(dimension[0] == 1)
							dimension[1] = dimcounter;
						else
							if(dimcounter != dimension[1])
								throw new OperatorException("The number of y values differ");
						dimension[0]++;
						dimcounter = 1;
						lastY = yValue;
					} else {
						dimcounter++;
					}
					
					if(lastY != yValue) {
						if(spacing[1] > 0)
							if(spacing[1] != yValue - lastY)
								throw new OperatorException("spacing in y direction not equidistant over the whole example set");
							else;
						else
							spacing[1] = yValue - lastY;
						lastY = yValue;
					}
					
					zvalues[i] = e.getValue(z);
					i++;
				}
				if(dimension[0] == 1)
					dimension[1] = dimcounter;
				
				// create new Map
				MapValue mapVal = new MapValue(spacing, origin, dimension, zvalues);
				
				this.chackMap(examples, mapVal);
				
				
				// add new DataRow
				data[0] = mapName;
				data[1] = mapVal;
				
				DataRow row = fac.create(data, attr);
				parent.addDataRow(row);
				mapI++;
			}
		}
		} catch (IOException e1) {
			throw new OperatorException(e1.getCause().getMessage());
		}
		
		return new IOObject[]{complexExampleTable.createExampleSet()};
	}

	@Override
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[]{}, new Class[]{ExampleSet.class});
	}

	@Override
	public int getMaxNumberOfInnerOperators() {
		
		return 1;
	}

	@Override
	public int getMinNumberOfInnerOperators() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Class<?>[] getInputClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?>[] getOutputClasses() {
		
		return new Class<?>[]{ExampleSet.class};
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType>  types= super.getParameterTypes();
		
		ParameterType type;
		
		//type = new ParameterTypeFiles(PARAMETER_FILES,"The files from which the data will be read","",false);
		//types.add(type);
		
		type = new ParameterTypeFile(PARAMETER_MAP_FILE, "", "txt", false);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeDirectory(PARAMETER_BASE_FOLDER,"The folder in which the map data are located","");
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeString(PARAMETER_FOLDER, "", "cdo");
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_MAP_TYPE, "", MAP_TYPES,1);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeString(PARAMETER_DELIMITER, "", ";");
		type.setExpert(false);
		types.add(type);
		
		
		
		return types;
	}
	
	public void chackMap(ExampleSet es,MapValue v) throws OperatorException {
		Attribute x = es.getAttributes().get("X");
		Attribute y = es.getAttributes().get("Y");
		Attribute z = es.getAttributes().get("Z");
		
		double zMap;
		for(Example e : es) {
			zMap = v.getValueAt(e.getValue(x),e.getValue(y));
			if(zMap != e.getValue(z))
				throw new OperatorException("z value was: " + zMap + " but should be: " + e.getValue(z));
		}
	}

}