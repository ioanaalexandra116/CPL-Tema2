package cool.compiler;

import org.antlr.v4.runtime.Token;

public interface ASTVisitor<T> {
    T visit(Id id);
    T visit(Int intt);
    T visit(If iff);
    T visit(Bool bl);
    T visit(Plus ps);
    T visit(Stringg str);
    T visit (Minus mns);
    T visit(Assign asgn);
    T visit(Multiply mlt);
    T visit(Divide div);
    T visit(UnaryMinus unm);
    T visit(Program prg);
    T visit(ClassDef cl);
    T visit(Classes fd);
    T visit(Feature ft);
    T visit(Param pm);
    T visit(Method md);
    T visit(Att at);
    T visit(Paren pr);
    T visit(Relational rel);
    T visit(Not not);
    T visit(IsVoid isv);
    T visit(New neww);
    T visit(SelfDispatch dsp);
    T visit(Dispatch dsp);
    T visit(While whl);
    T visit(Let let);
    T visit(Case cs);
    T visit(Block blk);
}
