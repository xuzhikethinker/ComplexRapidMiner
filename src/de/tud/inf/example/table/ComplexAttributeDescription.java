package de.tud.inf.example.table;



/**
 * stores information about dependencies between attribute values
 * @author Antje Gruner
 *
 */

public class ComplexAttributeDescription {
	private int[] attIds;
	private int[] paramIds;
	private String name;
	private String symbol;
	private String hint;
	/**
	 * 
	 * @param attIds attribute indexes
	 * @param paramIds parameter indexes
	 * @param symbol symbol of complex value function
	 * @param name name of complex attribute
	 */
	public ComplexAttributeDescription(int[] attIds, int[] paramIds, String symbol, String name, String hint) {
		this.attIds = attIds;
		this.paramIds = paramIds;
		this.symbol = symbol;
		this.name = name;
		this.hint = hint;
	}

	public int[] getAttributeIndexes() {
		return attIds;
	}
	
	public int[] getParamIndexes() {
		return paramIds;
	}
	
	
	public String getSymbol(){
		return symbol;
	}
	
	public String getName(){
		return name;
	}
	
	public String getHint(){
		return hint;
	}
}