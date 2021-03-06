package de.tud.inf.operator.fingerprints.gsc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityAdapter;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;

import de.tud.inf.example.set.attributevalues.ConstantArrayValue;
import de.tud.inf.example.table.ConstantArrayAttribute;

public class GscManhattenDistanceWithRot extends SimilarityAdapter implements ExampleBasedSimilarityMeasure{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3610986628556529815L;
	
	private Attribute fingerPrintAttr;
	private Set<String> ids;
	private ExampleSet es;

	public void init(ExampleSet exampleSet) throws OperatorException {
		ids = new HashSet<String>(exampleSet.size(),1.0f);
		this.es = exampleSet;
		fingerPrintAttr = exampleSet.getAttributes().get("fingerprint");
		if(!(fingerPrintAttr instanceof ConstantArrayAttribute))
			throw new OperatorException("SimilarityMeasure only definded for ConstantArrayAttribute");
		for(Example e : exampleSet)
			ids.add(IdUtils.getIdFromExample(e));
		
	}

	
	public double similarity(Example x, Example y) {
		return similarity((ConstantArrayValue)x.getComplexValue(fingerPrintAttr), (ConstantArrayValue)y.getComplexValue(fingerPrintAttr));
	}

	
	public Iterator<String> getIds() {
		
		return ids.iterator();
	}

	
	public int getNumberOfIds() {
		
		return ids.size();
	}

	
	public boolean isDistance() {
		
		return false;
	}

	
	public boolean isSimilarityDefined(String x, String y) {
		return (ids.contains(x) && ids.contains(y));
	}

	
	public double similarity(String x, String y) {
		if(!isSimilarityDefined(x, y))
			return Double.NaN;
		Example ex = IdUtils.getExampleFromId(es, x);
		Example ey = IdUtils.getExampleFromId(es, y);
		
		return similarity((ConstantArrayValue)ex.getComplexValue(fingerPrintAttr), (ConstantArrayValue)ey.getComplexValue(fingerPrintAttr));
	}
	
	public double similarity(ConstantArrayValue x, ConstantArrayValue y) {
		
		double[][] valX = x.getValues();
		double[][] valY = y.getValues();
		
		List<Double> distList = new ArrayList<Double>(valX.length);
		
		for(int i = 0;i<valX.length;i++) {
			double dist = 0.0;
			for(int j = 0;j<valX.length;j++) {
				
				dist += manhattanDistance(valX[j], valY[j]);
			}
			
			dist /= valX.length;
			distList.add(dist);
			
			double[] temp = valX[0];
			for(int j = 1;j<valX.length;j++) {
				valX[j-1] = valX[j];
			}
			valX[valX.length-1] = temp;
		}
		
		Collections.sort(distList);
		
		return distList.get(0);
	}
	
	public double manhattanDistance(double[] e1, double[] e2) {
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				sum = sum + Math.abs(e1[i] - e2[i]);
				counter++;
			}
		}
		double d = sum;
		if (counter > 0)
			return d;
		else
			return Double.NaN;
	}

}
