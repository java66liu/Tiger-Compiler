package syntaxtree;import visitor.Visitor;import visitor.TypeVisitor;public class VarDecl {  public Type t;  public Identifier i;  public Position position = null;    public VarDecl(Type at, Identifier ai, Position p) {    t=at; i=ai; position = p;  }  public void accept(Visitor v) {    v.visit(this);  }  public Type accept(TypeVisitor v) {    return v.visit(this);  }}