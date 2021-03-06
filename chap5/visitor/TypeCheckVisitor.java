package visitor;

import symbol.*;
import syntaxtree.*;
import visitor.BuildSymbolTableVisitor.ErrorMsg;

public class TypeCheckVisitor extends TypeDepthFirstVisitor {
	public SymbolTable symbolTable;

	// current class
	ClassSymbol currClass = null;

	// current method
	MethodSymbol currMethod = null;

	public class ErrorMsg {
		public boolean anyErrors = false;

		void complain(String msg) {
			anyErrors = true;
			System.out.println(msg);
		}
	}

	public ErrorMsg error = new ErrorMsg();

	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	public Type visit(ClassDeclSimple n) {
		currClass = symbolTable.lookup(n.i.toString());
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		currClass = null;
		return null;
	}

	public Type visit(ClassDeclExtends n) {
		currClass = symbolTable.lookup(n.i.toString());

		if (symbolTable.lookup(n.j.s) == null) {
			error.complain("Undeclared extension class '" + n.j.s + "' "
					+ n.position.toString());
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}

		currClass = null;
		return null;
	}

	public Type visit(MethodDecl n) {
		currMethod = currClass.lookupMethod(n.i.toString());

		n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}

		Type returnType = n.e.accept(this);
		if (!returnType.toString().equals(currMethod.returnType.toString())) {
			error.complain("Return expression does not match return type for method '"
					+ currMethod.getId() + "' " + n.position.toString());
		}

		currMethod = null;
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		if (!(n.e.accept(this) instanceof BooleanType)) {
			error.complain("If statement condition must be of type boolean "
					+ n.position.toString());
		}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		if (!(n.e.accept(this) instanceof BooleanType)) {
			error.complain("While statement condition must be of type boolean "
					+ n.position.toString());
		}
		n.s.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type varType = lookupType(n.i.s);
		Type expType = n.e.accept(this);
		if (varType instanceof IdentifierType) {
			ClassSymbol cs = symbolTable.lookup(((IdentifierType) varType).s);
			if (cs == null
					|| (!cs.getId().equals(expType.toString()) && (cs.baseClass != null && !cs.baseClass.getId().equals(expType.toString())))) {
				error.complain("Type mismatch in assignment of '"
						+ n.i.toString() + "' " + n.position.toString());
			}
		} else {
			if (varType == null || expType == null
					|| !varType.toString().equals(expType.toString())) {
				error.complain("Type mismatch in assignment of '"
						+ n.i.toString() + "' " + n.position.toString());
			}
		}

		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		// Type arrayType = n.i.accept(this);
		Type arrayType = lookupType(n.i.s);
		Type indexerType = n.e1.accept(this);
		Type valueType = n.e2.accept(this);
		if (arrayType == null) {
			error.complain("Undeclared identifier '" + n.i.s + "' "
					+ n.position.toString());
			return null;
		}
		if (!(arrayType instanceof IntArrayType)) {
			error.complain("Indexed access to elements is only valid for type int [] "
					+ n.position.toString());
		}
		if (!(indexerType instanceof IntegerType)) {
			error.complain("Array index must be of type int "
					+ n.position.toString());
		}
		if (!(valueType instanceof IntegerType)) {
			error.complain("Type mismatch during array assignment "
					+ n.position.toString());
		}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		if (!(n.e1.accept(this) instanceof BooleanType)) {
			error.complain("Left side of operator '&&' must be of type boolean "
					+ n.position.toString());
		}
		if (!(n.e2.accept(this) instanceof BooleanType)) {
			error.complain("Right side of operator '&&' must be of type boolean "
					+ n.position.toString());
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			error.complain("Left side of operator '<' must be of type int");
		}
		if (!(n.e2.accept(this) instanceof IntegerType)) {
			error.complain("Right side of operator '<' must be of type int");
		}
		return new BooleanType();
	}

	public Type visit(Plus n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			error.complain("Left side of operator '+' must be of type int");
		}
		if (!(n.e2.accept(this) instanceof IntegerType)) {
			error.complain("Right side of operator '+' must be of type int");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			error.complain("Left side of operator '-' must be of type int");
		}
		if (!(n.e2.accept(this) instanceof IntegerType)) {
			error.complain("Right side of operator '-' must be of type int");
		}
		return new IntegerType();
	}

	public Type visit(Times n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			error.complain("Left side of operator '*' must be of type int");
		}
		if (!(n.e2.accept(this) instanceof IntegerType)) {
			error.complain("Right side of operator '*' must be of type int");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type arrayType = n.e1.accept(this);
		Type indexerType = n.e2.accept(this);
		if (!(arrayType instanceof IntArrayType)) {
			error.complain("Indexed access to elements is only valid for type int [] "
					+ n.position.toString());
		}
		if (!(indexerType instanceof IntegerType)) {
			error.complain("Array index must be of type int "
					+ n.position.toString());
		}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type expType = n.e.accept(this);
		if (!(expType instanceof IntArrayType)) {
			error.complain("Invalid use of array 'length' property "
					+ n.position.toString());
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Type instanceType = n.e.accept(this);
		if (instanceType == null || !(instanceType instanceof IdentifierType)) {
			error.complain("Attempted method call on something that is not an object instance "
					+ n.position.toString());
		}
		ClassSymbol instance = (ClassSymbol) symbolTable
				.lookup(((IdentifierType) instanceType).s);
		MethodSymbol method = instance.lookupMethod(n.i.s);
		if (method == null) {
			error.complain("Unknown method '" + n.i.s + "' "
					+ n.position.toString());
			return null;
		}
		int paramCount = n.el.size();
		int declaredParamCount = method.numParams();
		if (paramCount != declaredParamCount) {
			error.complain("No overload of method '" + method.getId()
					+ "' accepts " + paramCount + " parameters "
					+ n.position.toString());
			return null;
		}
		for (int i = 0; i < n.el.size(); i++) {
			Type paramType = n.el.elementAt(i).accept(this);
			ParameterSymbol declaredParam = method.getParam(i);

			if (paramType instanceof IdentifierType) {
				ClassSymbol cs = symbolTable
						.lookup(((IdentifierType) paramType).s);
				if (cs == null
						|| (!cs.getId().equals(declaredParam.toString()) && (cs.baseClass != null && !cs.baseClass.getId().equals(declaredParam.t.toString())))) {
					error.complain("Argument " + i + " of " + instance.getId()
							+ "." + method.getId() + " must be of type "
							+ declaredParam.t.toString() + " "
							+ n.position.toString());
				}
			} else {
				if (!paramType.toString().equals(declaredParam.t.toString())) {
					error.complain("Argument " + i + " of " + instance.getId()
							+ "." + method.getId() + " must be of type "
							+ declaredParam.t.toString() + " "
							+ n.position.toString());
				}
			}
		}
		return method.returnType;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType(n.position);
	}

	public Type visit(True n) {
		return new BooleanType(n.position);
	}

	public Type visit(False n) {
		return new BooleanType(n.position);
	}

	// String s;
	public Type visit(IdentifierExp n) {
		Type t = lookupType(n.s);
		if (t == null) {
			error.complain("IdentifierExp '" + n.s + "' not declared "
					+ n.position.toString());
		}
		return t;
	}

	public Type visit(This n) {
		// Keyword 'this' refers to the current class.
		if (currClass == null) {
			error.complain("Attempted use of keyword 'this' outside of an object instance scope "
					+ n.position.toString());
		}
		return new IdentifierType(currClass.getId(), null);
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type t = n.e.accept(this);
		if (!(t instanceof IntegerType)) {
			error.complain("Array instantiation can only be used with type int "
					+ n.position.toString());
		}
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		ClassSymbol c = symbolTable.lookup(n.i.s);
		if (c == null) {
			error.complain("Unknown class name '" + n.i.s + "' " + n.position.toString());
		}
		return n.i.accept(this);
	}

	// Exp e;
	public Type visit(Not n) {
		n.e.accept(this);
		return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		// return null;
		return lookupType(n.s);
	}

	protected Type lookupType(String id) {
		if (currMethod != null && currMethod.lookupLocal(id) != null) {
			return currMethod.lookupLocal(id).t;
		}
		if (currMethod != null && currMethod.lookupParam(id) != null) {
			return currMethod.lookupParam(id).t;
		}
		if (currClass != null && currClass.lookupField(id) != null) {
			return currClass.lookupField(id).t;
		}
		if (symbolTable.lookup(id) != null) {
			// it's a class name...
			return new IdentifierType(id, null);
		}

		return null;
	}
}
