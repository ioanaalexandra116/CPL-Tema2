package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import cool.structures.*;

import java.util.ArrayList;
import java.util.List;

// Rădăcina ierarhiei de clase reprezentând nodurile arborelui de sintaxă
// abstractă (AST). Singura metodă permite primirea unui visitor.
public abstract class ASTNode {
    // Reținem un token descriptiv, pentru a putea afișa ulterior
    // informații legate de linia și coloana eventualelor erori semantice.
    Token token;
    ParserRuleContext ctx;

    ASTNode(Token token, ParserRuleContext ctx) {
        this.token = token;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

// Orice expresie.
class Program extends ASTNode {
    Classes classes;

    Program(Classes classes, ParserRuleContext ctx) {
        super(null, ctx);
        this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

}

class Classes extends ASTNode {
    ArrayList<ClassDef> classes = new ArrayList<>();

    Classes(ArrayList<ClassDef> classes, ParserRuleContext ctx) {
        super(null, ctx);
        if (classes != null)
            this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClassDef extends ASTNode {
    Id id;
    Id parent;
    List<Feature> features;

    ClassDef(Id id, List<Feature> features, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = id;
        this.features = features;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Feature extends ASTNode {
    Id id;
    String type;
    Att att;
    Method method;

    Feature(Id id, String type, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = id;
        this.type = type;
    }

    Feature(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    Feature(Att att, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.att = att;
    }

    Feature(Method method, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.method = method;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Att extends Feature {

    Boolean local;
    Id id;
    Id type;
    Expression value;

    Att(Id id, Id type, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = id;
        this.type = type;
        this.local = false;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Method extends Feature {
    Id id;
    List<Param> params;

    Id type;
    Expression idk;

    Method(Id id, Id type, List<Param> params, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = id;
        this.type = type;
        this.params = params;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Param extends ASTNode {
    Id id;
    Id type;

    Param(Id id, Id type, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = id;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
abstract class Expression extends ASTNode {
    Expression(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }
}

// Identificatori
class Id extends Expression {
    private IdSymbol symbol;
    private Scope scope;
    Id(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    IdSymbol getSymbol() {
        return symbol;
    }

    IdSymbol getParentSymbol() {
        return (IdSymbol) scope.getParent().lookup(symbol.getName());
    }

    void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    Scope getScope() {
        return scope;
    }

    void setScope(Scope scope) {
        this.scope = scope;
    }
}

// Literali întregi
class Stringg extends Expression {
    Stringg(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Int extends Expression {
    Int(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Construcția if.
class If extends Expression {
    // Sunt necesare trei câmpuri pentru cele trei componente ale expresiei.
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;

    If(Expression cond,
       Expression thenBranch,
       Expression elseBranch,
       Token start,
       ParserRuleContext ctx){
        super(start, ctx);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Bool extends Expression {
    Bool(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression expr;

    Paren(Expression expr,
          Token start,
          ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Plus extends Expression {
    Expression left;
    Expression right;

    Plus(Expression left,
         Expression right,
         Token start,
         ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Minus extends Expression {
    Expression left;
    Expression right;

    Minus(Expression left,
          Expression right,
          Token start,
          ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assign extends Expression {
    Id id;
    Expression expr;

    Assign(Id id,
           Expression expr,
           Token start,
           ParserRuleContext ctx) {
        super(start, ctx);
        this.id = id;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Multiply extends Expression {
    Expression left;
    Expression right;

    Multiply(Expression left,
             Expression right,
             Token start,
             ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Divide extends Expression {
    Expression left;
    Expression right;

    Divide(Expression left,
           Expression right,
           Token start,
           ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class UnaryMinus extends Expression {
    Expression expr;

    UnaryMinus(Expression expr,
               Token start,
               ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Relational extends Expression {
    Expression left;
    Expression right;
    Id op;

    Relational(Expression left,
               Expression right,
               Id op,
               Token start,
               ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression expr;

    Not(Expression expr,
        Token start,
        ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IsVoid extends Expression {
    Expression expr;

    IsVoid(Expression expr,
           Token start,
           ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    Id type;

    New(Id type,
        Token start,
        ParserRuleContext ctx) {
        super(start, ctx);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class SelfDispatch extends Expression {
    Id id;
    List<Expression> args;

    SelfDispatch(
             Id id,
             List<Expression> args,
             Token start,
             ParserRuleContext ctx) {
        super(start, ctx);
        this.id = id;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Dispatch extends Expression {
    Expression expr;
    String type;
    Id id_meth;
    List<Expression> args;

    Dispatch(Expression expr,
                Id id_meth,
             List<Expression> args,
             Token start,
             ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
        this.id_meth = id_meth;
        this.args = args;
    }

    Dispatch(Expression expr,
                String type,
                Id id_meth,
             List<Expression> args,
             Token start,
             ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
        this.type = type;
        this.id_meth = id_meth;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression cond;
    Expression body;

    While(Expression cond,
          Expression body,
          Token start,
          ParserRuleContext ctx) {
        super(start, ctx);
        this.cond = cond;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<Att> atts;
    Expression expr;
    int tag;

    Let(List<Att> atts,
        Expression expr,
        Token start,
        ParserRuleContext ctx) {
        super(start, ctx);
        this.atts = atts;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression expr;
    List<Id> types;
    List<Id> ids;
    List<Expression> exprs;
    int tag;

    Case(Expression expr, List<Id> types, List<Id> ids, List<Expression> exprs, Token start, ParserRuleContext ctx) {
        super(start, ctx);
        this.expr = expr;
        this.types = types;
        this.ids = ids;
        this.exprs = exprs;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> exprs;

    Block(List<Expression> exprs, Token start, ParserRuleContext ctx) {
        super(start, ctx);
        this.exprs = exprs;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}