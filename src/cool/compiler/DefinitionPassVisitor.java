package cool.compiler;

import cool.structures.*;

import java.util.HashSet;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = null;
    private final HashSet<String> illegalParents;
    private int tagCounter = 0;
    private boolean isLet = false;
    public DefinitionPassVisitor() {
        illegalParents = new HashSet<>();
        illegalParents.add(TypeSymbol.INT.getName());
        illegalParents.add(TypeSymbol.BOOL.getName());
        illegalParents.add(TypeSymbol.STRING.getName());
        illegalParents.add(TypeSymbol.SELF_TYPE.getName());
    }

    @Override
    public Void visit(Id id) {
        if (isLet) {
            id.setScope(currentScope.getParent());
        }
        else id.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(Int intt) {
        return null;
    }

    @Override
    public Void visit(If iff) {
        return null;
    }

    @Override
    public Void visit(Bool bl) {
        return null;
    }

    @Override
    public Void visit(Plus ps) {
        ps.left.accept(this);
        ps.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Stringg str) {
        return null;
    }

    @Override
    public Void visit(Minus mns) {
        mns.left.accept(this);
        mns.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Assign asgn) {
        asgn.id.accept(this);
        asgn.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(Multiply mlt) {
        mlt.left.accept(this);
        mlt.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Divide div) {
        div.left.accept(this);
        div.right.accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryMinus unm) {
        unm.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(Program prg) {
        SymbolTable.defineBasicClasses();
        currentScope = SymbolTable.globals;
        prg.classes.accept(this);
        return null;
    }

    @Override
    public Void visit(ClassDef cl) {
        var className = cl.id.token.getText();
        var parentName = "Object";
        if (cl.parent != null) {
            parentName = cl.parent.token.getText();
        }
        var type = new TypeSymbol(className, parentName);
        currentScope = type;

        if (className.equals("SELF_TYPE")) {
            SymbolTable.error(
                    cl.ctx,
                    cl.id.token,
                    "Class has illegal name " + className
            );
            return null;
        }
        if (SymbolTable.globals.lookup(className) != null) {
            SymbolTable.error(
                    cl.ctx,
                    cl.id.token,
                    "Class " + className + " is redefined"
            );
            return null;
        } else {
            SymbolTable.globals.add(type);
            currentScope.add(type);
        }

        if (className.equals("Object") || className.equals("IO") || className.equals("Int") || className.equals("Bool") || className.equals("String")) {
            SymbolTable.error(
                    cl.ctx,
                    cl.id.token,
                    "Class " + className + " is redefined"
            );
            return null;
        }
        if (illegalParents.contains(parentName) && cl.parent != null) {
            SymbolTable.error(
                    cl.ctx,
                    cl.parent.token,
                    "Class " + className + " has illegal parent " + parentName
            );
            return null;
        }
        cl.features.forEach(f -> f.accept(this));
        if (currentScope != null) {
            currentScope = currentScope.getParent();
        }
        IdSymbol symbol = new IdSymbol(className);
        symbol.setType(type);
        cl.id.setSymbol(symbol);
        cl.id.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(Classes fd) {
        currentScope = new DefaultScope(currentScope);
        fd.classes.forEach(c -> c.accept(this));
        return null;
    }

    @Override
    public Void visit(Feature ft) {
        ft.accept(this);
        return null;
    }

    @Override
    public Void visit(Param pm) {
        MethodSymbol currentMethod = (MethodSymbol) currentScope;
//        System.out.println(currentMethod.getName() + "id: " + pm.id.token.getText());
        TypeSymbol currentClass = (TypeSymbol) currentMethod.getParent();
        IdSymbol symbol = new IdSymbol(pm.id.token.getText());
        symbol.setType(new TypeSymbol(pm.type.token.getText(), null));
        if (pm.id.token.getText().equals("self")) {
            SymbolTable.error(
                    pm.ctx,
                    pm.id.token,
                    "Method " + currentMethod.getName() + " of class " + currentClass.getName() + " has formal parameter with illegal name " + pm.id.token.getText()
            );
            return null;
        }
        if (pm.type.token.getText().equals("SELF_TYPE")) {
            SymbolTable.error(
                    pm.ctx,
                    pm.type.token,
                    "Method " + currentMethod.getName() + " of class " + currentClass.getName() + " has formal parameter " + pm.id.token.getText() + " with illegal type " + pm.type.token.getText()
            );
            return null;
        }
        if (!currentMethod.add(symbol)) {
            SymbolTable.error(
                    pm.ctx,
                    pm.id.token,
                    "Method " + currentMethod.getName() + " of class " + currentClass.getName() + " redefines formal parameter " + pm.id.token.getText()
            );
            return null;
        }
        pm.id.setSymbol(symbol);
        pm.id.setScope(currentMethod);
        return null;
    }

    @Override
    public Void visit(Method md) {
        TypeSymbol currentClass = (TypeSymbol) currentScope;
        MethodSymbol symbol = new MethodSymbol(md.id.token.getText());
        symbol.setReturnType(new TypeSymbol(md.type.token.getText(), null));
        symbol.setParent(currentClass);
        currentScope = symbol;
        if (!currentClass.addMethod(symbol)) {
            SymbolTable.error(
                    md.ctx,
                    md.id.token,
                    "Class " + currentClass.getName() + " redefines method " + md.id.token.getText()
            );
            currentScope = currentScope.getParent();
            return null;
        }
        md.params.forEach(p -> p.accept(this));
        // TODO: visit body
        md.idk.accept(this);
        currentScope = currentScope.getParent();
        md.id.setSymbol(symbol);
        md.id.setScope(currentClass);
        return null;
    }

    @Override
    public Void visit(Att at) {
        TypeSymbol currentClass = (TypeSymbol) currentScope;
        IdSymbol symbol = new IdSymbol(at.id.token.getText());
        symbol.setType(new TypeSymbol(at.type.token.getText(), null));
        if (at.id.token.getText().equals("self")) {
            SymbolTable.error(
                    at.ctx,
                    at.id.token,
                    "Class " + currentClass.getName() + " has attribute with illegal name " + at.id.token.getText()
            );
            return null;
        }
        if (!currentClass.add(symbol)) {
            SymbolTable.error(
                    at.ctx,
                    at.id.token,
                    "Class " + currentClass.getName() + " redefines attribute " + at.id.token.getText()
            );
            return null;
        }
        at.id.setSymbol(symbol);
        at.id.setScope(currentScope);
        if (at.value != null) {
            at.value.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Paren pr) {
        return null;
    }

    @Override
    public Void visit(Relational rel) {
        return null;
    }

    @Override
    public Void visit(Not not) {
        return null;
    }

    @Override
    public Void visit(IsVoid isv) {
        return null;
    }

    @Override
    public Void visit(New neww) {
        return null;
    }

    @Override
    public Void visit(SelfDispatch dsp) {
        return null;
    }

    @Override
    public Void visit(Dispatch dsp) {
        return null;
    }

    @Override
    public Void visit(While whl) {
        return null;
    }

    @Override
    public Void visit(Let let) {
        isLet = true;
        MethodSymbol letSymbol = new MethodSymbol("let-" + tagCounter);
        let.tag = tagCounter;
        tagCounter++;
        letSymbol.setParent(currentScope);
        currentScope = letSymbol;
        for (var decl : let.atts) {
            MethodSymbol symbol = new MethodSymbol(decl.id.token.getText());
            if (decl.id.token.getText().equals("self")) {
                SymbolTable.error(
                        decl.ctx,
                        decl.id.token,
                        "Let variable has illegal name " + decl.id.token.getText()
                );
                continue;
            }
            symbol.setParent(currentScope);
            symbol.setType(new TypeSymbol(decl.type.token.getText(), null));
            decl.id.setSymbol(symbol);
            decl.id.setScope(symbol);
            currentScope = symbol;
//            letSymbol.add(symbol);
            currentScope.add(symbol);
            if (decl.value != null) {
                decl.value.accept(this);
            }
        }

        // TODO: visit body
        let.expr.accept(this);
        for (var decl : let.atts) {
            currentScope = currentScope.getParent();
        }

        currentScope = currentScope.getParent();
        isLet = false;
        return null;
    }

    @Override
    public Void visit(Case cs) {
        MethodSymbol caseSymbol = new MethodSymbol("case-" + tagCounter);
        cs.tag = tagCounter;
        tagCounter++;
        caseSymbol.setParent(currentScope);
        currentScope = caseSymbol;

        for (int i = 0; i < cs.ids.size(); i++) {
            MethodSymbol symbol = new MethodSymbol(cs.ids.get(i).token.getText());
            if (cs.ids.get(i).token.getText().equals("self")) {
                SymbolTable.error(
                        cs.ids.get(i).ctx,
                        cs.ids.get(i).token,
                        "Case variable has illegal name " + cs.ids.get(i).token.getText()
                );
                continue;
            }
            if (cs.types.get(i).token.getText().equals("SELF_TYPE")) {
                SymbolTable.error(
                        cs.types.get(i).ctx,
                        cs.types.get(i).token,
                        "Case variable " + cs.ids.get(i).token.getText() + " has illegal type " + cs.types.get(i).token.getText()
                );
                continue;
            }
            symbol.setParent(currentScope);
            cs.ids.get(i).setSymbol(symbol);
            cs.ids.get(i).setScope(symbol);
            caseSymbol.add(symbol);
            currentScope.add(symbol);
        }
        // visit body
        cs.exprs.forEach(e -> e.accept(this));
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Block blk) {
        return null;
    }
}
