package de.tud.inf.operator;

import java.util.ArrayList;
import java.util.List;
import de.tud.inf.operator.capabilites.Capability;

public class ExampleSetCapability implements Capability{

	private List<Capability> inner;
	
	public ExampleSetCapability(List<Capability> inner) {
		
		this.inner = inner;
		
	}
	
	public ExampleSetCapability() {
		
		inner = new ArrayList<Capability>();
	}

	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == EXAMPLE_SET_CAPABILITY_TYPE) {
			if(inner == null || inner.size() == 0)
				return true;
			if(toCheck.getInnerCapabilities() == null || toCheck.getInnerCapabilities().size() == 0)
				return true;
			
			for(Capability child : inner) {
				boolean valid = false;
				for(Capability toCheckInner : toCheck.getInnerCapabilities()) {
					if(child.checkCapability(toCheckInner)) {
						valid = true;
						break;
					}
				}
				if(!valid)
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	
	public int getType() {
		
		return Capability.EXAMPLE_SET_CAPABILITY_TYPE;
	}
	
	public void addCapability(Capability cap) {
		
		inner.add(cap);
	}
	
	public List<Capability> getInnerCapabilities() {
		return inner;
	}

}
