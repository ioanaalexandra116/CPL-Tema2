package cool.compiler;

public class PrintTreeVisitor implements ASTVisitor<Void>{
    int indent = 0;

    private void printIndent(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }

    @Override
    public Void visit(Id id) {
        printIndent(id.token.getText());
        return null;
    }

    @Override
    public Void visit(Int intt) {
        printIndent(intt.token.getText());
        return null;
    }

    @Override
    public Void visit(If iff) {
        printIndent("if");
        indent++;
        iff.cond.accept(this);
        iff.thenBranch.accept(this);
        iff.elseBranch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Bool bl) {
        printIndent(bl.token.getText());
        return null;
    }

    @Override
    public Void visit(Plus ps) {
        printIndent("+");
        indent++;
        ps.left.accept(this);
        ps.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Minus mns) {
        printIndent("-");
        indent++;
        mns.left.accept(this);
        mns.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Assign asgn) {
        printIndent("<-");
        indent++;
        printIndent(asgn.id.token.getText());
        asgn.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Multiply mlt) {
        printIndent("*");
        indent++;
        mlt.left.accept(this);
        mlt.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Divide div) {
        printIndent("/");
        indent++;
        div.left.accept(this);
        div.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(UnaryMinus unm) {
        printIndent("~");
        indent++;
        unm.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Program prg) {
        printIndent("program");
        indent++;
        prg.classes.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(ClassDef cl) {
        printIndent("class");
        indent++;
        printIndent(cl.id.token.getText());
        if (cl.parent != null)
            printIndent(cl.parent.token.getText());
        for (var feature : cl.features)
            feature.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Classes fd) {
        for (var def : fd.classes)
            def.accept(this);
        return null;
    }

    @Override
    public Void visit(Feature ft) {
        if (ft.att != null)
            ft.att.accept(this);
        else if (ft.method != null)
            ft.method.accept(this);
        return null;
    }

    @Override
    public Void visit(Att at) {
        if (!at.local)
            printIndent("attribute");
        else
            printIndent("local");
        indent++;
        printIndent(at.id.token.getText());
        printIndent(at.type.token.getText());
        if (at.value != null)
            at.value.accept(this);
        indent--;
        return null;
    }


    @Override
    public Void visit(Method md) {
        printIndent("method");
        indent++;
        printIndent(md.id.token.getText());
        for (var param : md.params) {
            if (param != null)
                param.accept(this);
        }
        printIndent(md.type.token.getText());
        if (md.idk != null)
            md.idk.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Param pm) {
        printIndent("formal");
        indent++;
        printIndent(pm.id.token.getText());
        printIndent(pm.type.token.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Stringg str) {
        printIndent(str.token.getText());
        return null;
    }

    @Override
    public Void visit(Paren pr) {
        pr.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(Relational rel) {
        printIndent(rel.op.token.getText());
        indent++;
        rel.left.accept(this);
        rel.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Not not) {
        printIndent("not");
        indent++;
        not.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(IsVoid isv) {
        printIndent("isvoid");
        indent++;
        isv.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(New neww) {
        printIndent("new");
        indent++;
        printIndent(neww.type.token.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(SelfDispatch dsp) {
        printIndent("implicit dispatch");
        indent++;
        printIndent(dsp.id.token.getText());
        for (var arg : dsp.args)
            arg.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Dispatch dsp) {
        printIndent(".");
        indent++;
        dsp.expr.accept(this);
        if (dsp.type != null)
            printIndent(dsp.type);
        printIndent(dsp.id_meth.token.getText());
        for (var arg : dsp.args)
            arg.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(While whl) {
        printIndent("while");
        indent++;
        whl.cond.accept(this);
        whl.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Let let) {
        printIndent("let");
        indent++;
        for (var att : let.atts)
            att.accept(this);
        let.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Case cs) {
        printIndent("case");
        indent++;
        cs.expr.accept(this);
        for (int i = 0; i < cs.ids.size(); i++) {
            printIndent("case branch");
            indent++;
            printIndent(cs.types.get(i).token.getText());
            printIndent(cs.ids.get(i).token.getText());
            cs.exprs.get(i).accept(this);
            indent--;
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(Block blk) {
        printIndent("block");
        indent++;
        for (var expr : blk.exprs)
            expr.accept(this);
        indent--;
        return null;
    }
}
