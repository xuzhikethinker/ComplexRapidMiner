package de.tud.inf.operator;

import java.util.ArrayList;
import java.util.List;
import de.tud.inf.operator.capabilites.Capability;

public class IdCapability implements Capability{

	private List<Capability> inner;
	
	public IdCapability(List<Capability> inner) {
		this.inner = inner;
	}
	
	public IdCapability() {
		inner = new ArrayList<Capability>();
	}
	
	public void addCapability(Capability cap) {
		inner.add(cap);
		
	}

	
	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == ID_CAPABILITY_TYPE) {
			if(inner == null || inner.size() == 0)
				return true;
			for(Capability child : inner) {
				boolean valid = false;
				for(Capability toCheckChild : toCheck.getInnerCapabilities()) {
					if(child.checkCapability(toCheckChild)) {
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

	public List<Capability> getInnerCapabilities() {
		
		return inner;
	}

	
	public int getType() {
		
		return ID_CAPABILITY_TYPE;
	}

}
