package cool.compiler;

import cool.structures.*;

import java.util.*;

public class ResolutionPassVisitor implements ASTVisitor<TypeSymbol> {
    HashMap<String, String> classToParent = new HashMap<>();
    ArrayList<ClassDef> classesArray = new ArrayList<>();

    public ArrayList<Scope> getInheritanceChain(TypeSymbol currentScope) {
        ArrayList<Scope> inheritanceChain = new ArrayList<>();
        var parent = SymbolTable.globals.lookup(currentScope.getParentName());
        while (parent != null) {
            Scope scope = (Scope) parent;
            inheritanceChain.add(scope);
            parent = SymbolTable.globals.lookup(((TypeSymbol) parent).getParentName());
        }
        return inheritanceChain;
    }
    @Override
    public TypeSymbol visit(Id id) {
        Scope currentScope = id.getScope();
        if (currentScope == null) {
            return null;
        }
        if (currentScope.lookup(id.token.getText()) == null) {
            SymbolTable.error(
                    id.ctx,
                    id.token,
                    "Undefined identifier " + id.token.getText()
            );
            return null;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Int intt) {
        return null;
    }

    @Override
    public TypeSymbol visit(If iff) {
        return null;
    }

    @Override
    public TypeSymbol visit(Bool bl) {
        return null;
    }

    @Override
    public TypeSymbol visit(Plus ps) {
        ps.left.accept(this);
        ps.right.accept(this);
        return null;
    }

    @Override
    public TypeSymbol visit(Stringg str) {
        return null;
    }

    @Override
    public TypeSymbol visit(Minus mns) {
        return null;
    }

    @Override
    public TypeSymbol visit(Assign asgn) {
        asgn.id.accept(this);
        asgn.expr.accept(this);
        return null;
    }

    @Override
    public TypeSymbol visit(Multiply mlt) {
        return null;
    }

    @Override
    public TypeSymbol visit(Divide div) {
        return null;
    }

    @Override
    public TypeSymbol visit(UnaryMinus unm) {
        return null;
    }

    @Override
    public TypeSymbol visit(Program prg) {
        prg.classes.accept(this);
        return null;
    }

    public HashSet hasCycle(String startClass) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        if (hasCycleUtil(startClass, visited, recursionStack)) {
            return new LinkedHashSet<>(recursionStack);
        }

        return null;
    }

    private boolean hasCycleUtil(String className, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(className)) {
            return true;  // Cycle detected
        }

        recursionStack.add(className);
        visited.add(className);

        // Check the parent class for cycles
        String parentClass = classToParent.get(className);
        if (parentClass != null && hasCycleUtil(parentClass, visited, recursionStack)) {
            return true;  // Cycle detected in the parent class
        }

        // Remove the current class from the recursion stack after processing its parent
        recursionStack.remove(className);

        return false;
    }



    @Override
    public TypeSymbol visit(ClassDef cl) {
        if (cl.id.getSymbol() == null) {
            return null;
        }
        for (var ft : cl.features) {
            ft.accept(this);
        }
        if (cl.parent != null) {
            cl.parent.accept(this);
            if (!classToParent.containsKey(cl.id.token.getText())) {
                classToParent.put(cl.id.token.getText(), cl.parent.token.getText());
            }
        } else {
            classToParent.put(cl.id.token.getText(), "Object");
        }

        if (cl.parent != null && SymbolTable.globals.lookup(cl.parent.token.getText()) == null) {
            SymbolTable.error(
                    cl.ctx,
                    cl.parent.token,
                    "Class " + cl.id.token.getText() + " has undefined parent " + cl.parent.token.getText()
            );
            return null;
        }
        classesArray.add(cl);

        HashSet<String> cycle = hasCycle(cl.id.token.getText());
        if (cycle != null) {
            for (String className : cycle) {
                int i = 0;
                while (!classesArray.get(i).id.token.getText().equals(className)) {
                    i++;
                }
                ClassDef cycleClass = classesArray.get(i);
                SymbolTable.error(
                        cycleClass.ctx,
                        cycleClass.id.token,
                        "Inheritance cycle for class " + className
                );
            }
            return null;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Classes classes) {;
        for (var cl : classes.classes) {
            cl.accept(this);
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Feature ft) {
        ft.id.accept(this);
        return null;
    }

    @Override
    public TypeSymbol visit(Param pm) {
        MethodSymbol currentMethod = (MethodSymbol) pm.id.getScope();
        if (currentMethod == null) {
            return null;
        }
        TypeSymbol currentClass = (TypeSymbol) currentMethod.getParent();
        if (SymbolTable.globals.lookup(pm.type.token.getText()) == null) {
            SymbolTable.error(
                    pm.ctx,
                    pm.type.token,
                    "Method " + currentMethod.getName() + " of class " + currentClass.getName() + " has formal parameter " + pm.id.token.getText() + " with undefined type " + pm.type.token.getText()
            );
            return null;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Method md) {
        TypeSymbol currentScope = (TypeSymbol) md.id.getScope();
        if (currentScope == null) {
            return null;
        }
        if (SymbolTable.globals.lookup(md.type.token.getText()) == null) {
            SymbolTable.error(
                    md.ctx,
                    md.type.token,
                    "Class " + currentScope + " has method " + md.id.token.getText() + " with undefined return type " + md.type.token.getText()
            );
            return null;
        }
        ArrayList<Scope> inheritanceChain = getInheritanceChain(currentScope);
        for (Scope scope : inheritanceChain) {
            TypeSymbol methClass = (TypeSymbol) scope;
            var methodFromParent = methClass.lookupMethod(md.id.token.getText());
            if (methodFromParent != null) {
                if (methodFromParent.getFormals().size() != md.params.size()) {
                    SymbolTable.error(
                            md.ctx,
                            md.id.token,
                            "Class " + currentScope + " overrides method " + md.id.token.getText() + " with different number of formal parameters"
                    );
                    return null;
                }
                if (!methodFromParent.getReturnType().getName().equals(md.type.token.getText())) {
                    SymbolTable.error(
                            md.ctx,
                            md.type.token,
                            "Class " + currentScope + " overrides method " + md.id.token.getText() + " but changes return type from " + methodFromParent.getReturnType().getName() + " to " + md.type.token.getText()
                    );
                    return null;
                }
                if (methodFromParent.getFormals().size() == md.params.size()) {
                    for (int i = 0; i < methodFromParent.getFormals().size(); i++) {
                        if (!methodFromParent.getFormals().get(i).getType().getName().equals(md.params.get(i).type.token.getText())) {
                            SymbolTable.error(
                                    md.ctx,
                                    md.params.get(i).type.token,
                                    "Class " + currentScope + " overrides method " + md.id.token.getText() + " but changes type of formal parameter " + md.params.get(i).token.getText() + " from " + methodFromParent.getFormals().get(i).getType().getName() + " to " + md.params.get(i).type.token.getText()
                            );
                            return null;
                        }
                    }
                }
            }
        }

        md.params.forEach(p -> p.accept(this));
        md.idk.accept(this);
        return null;
    }

    @Override
    public TypeSymbol visit(Att at) {
        TypeSymbol currentScope = (TypeSymbol) at.id.getScope();
        if (currentScope == null) {
            return null;
        }
        if (SymbolTable.globals.lookup(at.type.token.getText()) == null) {
            SymbolTable.error(
                    at.ctx,
                    at.type.token,
                    "Class " + currentScope + " has attribute " + at.id.token.getText() + " with undefined type " + at.type.token.getText()
            );
            return null;
        }
        var parent = SymbolTable.globals.lookup(currentScope.getParentName());
        while (parent != null) {
            Scope scope = (Scope) parent;
            if (scope.lookup(at.id.token.getText()) != null) {
                SymbolTable.error(
                        at.ctx,
                        at.id.token,
                        "Class " + currentScope + " redefines inherited attribute " + at.id.token.getText()
                );
                return null;
            }
            parent = SymbolTable.globals.lookup(((TypeSymbol) parent).getParentName());
        }

        return null;
    }

    @Override
    public TypeSymbol visit(Paren pr) {
        return null;
    }

    @Override
    public TypeSymbol visit(Relational rel) {
        return null;
    }

    @Override
    public TypeSymbol visit(Not not) {
        return null;
    }

    @Override
    public TypeSymbol visit(IsVoid isv) {
        return null;
    }

    @Override
    public TypeSymbol visit(New neww) {
        return null;
    }

    @Override
    public TypeSymbol visit(SelfDispatch dsp) {
        return null;
    }

    @Override
    public TypeSymbol visit(Dispatch dsp) {
        return null;
    }

    @Override
    public TypeSymbol visit(While whl) {
        return null;
    }

    @Override
    public TypeSymbol visit(Let let) {
        var letSymbol = let.atts.get(0).id.getScope().getParent();
        for (var att : let.atts) {
            if (SymbolTable.globals.lookup(att.type.token.getText()) == null) {
                SymbolTable.error(
                        att.ctx,
                        att.type.token,
                        "Let variable " + att.id.token.getText() + " has undefined type " + att.type.token.getText()
                );
                return null;
            }
            if (att.value != null)
                att.value.accept(this);
        }
        let.expr.accept(this);
        return null;
    }

    @Override
    public TypeSymbol visit(Case cs) {
        var caseSymbol = cs.ids.get(0).getScope().getParent();
        for (int i = 0; i < cs.ids.size(); i++) {
            if (SymbolTable.globals.lookup(cs.types.get(i).token.getText()) == null) {
                SymbolTable.error(
                        cs.types.get(i).ctx,
                        cs.types.get(i).token,
                        "Case variable " +  cs.ids.get(i).token.getText() + " has undefined type " + cs.types.get(i).token.getText()
                );
                return null;
            }
            cs.exprs.get(i).accept(this);
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Block blk) {
        return null;
    }
}
