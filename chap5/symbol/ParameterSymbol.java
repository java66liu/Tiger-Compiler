package symbol;

import syntaxtree.*;

public class ParameterSymbol extends Symbol {
	public Type t;
	
	public void dumpContents(int indent) {
		indent(indent);
		System.out.println(t + " " + id);		
	}
}
