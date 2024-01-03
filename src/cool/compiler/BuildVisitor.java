package cool.compiler;
import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class BuildVisitor extends CoolParserBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        var prog = new Program(null, ctx);

        prog.classes = (Classes) visit(ctx.prog_classes);

        return prog;
    }

    @Override
    public ASTNode visitClasses(CoolParser.ClassesContext ctx) {
        var result = new Classes(null, ctx);

        for (var classdef : ctx.classdef())
            if (classdef != null)
                result.classes.add((ClassDef) visit(classdef));
            else {
                System.err.println("Null classdef");
                System.exit(1);
            }

        return result;
    }

    @Override
    public ASTNode visitClassdef(CoolParser.ClassdefContext ctx) {
        List<Feature> features = new ArrayList<>();
        for (var feature : ctx.feature())
            features.add((Feature) visit(feature));

        //var result = new ClassDef((Id) ctx.name, features, ctx.start);
        Id id = new Id(ctx.name, ctx);
        var result = new ClassDef(id, features, ctx.start, ctx);

        if (ctx.inherited_class != null) {
            Id parent = new Id(ctx.inherited_class, ctx);
            result.parent = parent;
        }

        return result;
    }

    @Override
    public ASTNode visitFeature(CoolParser.FeatureContext ctx) {
        if (ctx.attribute() != null)
            return visit(ctx.attribute());
        else if (ctx.method() != null)
            return visit(ctx.method());
        else {
            System.err.println("feature error");
            System.exit(1);
            return null;
        }
    }

    @Override
    public ASTNode visitAttribute(CoolParser.AttributeContext ctx) {
        Id id = new Id(ctx.att_name, ctx);
        Id type = new Id(ctx.type_att, ctx);
        var result = new Att(id, type, ctx.start, ctx);

        if (ctx.value != null)
            result.value = (Expression) visit(ctx.value);

        return result;
    }

    @Override
    public ASTNode visitMethod(CoolParser.MethodContext ctx) {
        List<Param> params = new ArrayList<>();
        if (ctx.param != null) {
            for (var param : ctx.vardef)
                params.add((Param) visit(param));
        }

        Id id = new Id(ctx.meth_name, ctx);
        Id type = new Id(ctx.meth_type, ctx);
        var result = new Method(id, type, params, ctx.start, ctx);

        if (ctx.meth_value != null) {
            result.idk = (Expression) visit(ctx.meth_value);
        }

        return result;
    }

    @Override
    public ASTNode visitParam(CoolParser.ParamContext ctx) {
        Id id = new Id(ctx.param_name, ctx);
        Id type = new Id(ctx.param_type, ctx);
        return new Param(id, type, ctx.start, ctx);
    }

    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx.start, ctx);
    }

    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new Int(ctx.start, ctx);
    }

    @Override
    public ASTNode visitBool(CoolParser.BoolContext ctx) {
        return new Bool(ctx.start, ctx);
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new Stringg(ctx.start, ctx);
    }

    @Override
    public ASTNode visitParen(CoolParser.ParenContext ctx) {
        return new Paren((Expression) visit(ctx.paren), ctx.start, ctx);
    }

    @Override
    public ASTNode visitPlusminus(CoolParser.PlusminusContext ctx) {
        var term2 = (Expression) visit(ctx.ord2_term2);
        var term1 = (Expression) visit(ctx.ord2_term1);
        if (ctx.op.getText().equals("-"))
            return new Minus(term1, term2, ctx.start, ctx);
        return new Plus(term1, term2, ctx.start, ctx);
    }

    @Override
    public ASTNode visitMuldiv(CoolParser.MuldivContext ctx) {
        if (ctx.op.getText().equals("/"))
            return new Divide((Expression) visit(ctx.ord1_term1),
                    (Expression) visit(ctx.ord1_term2),
                    ctx.start, ctx);
        return new Multiply((Expression) visit(ctx.ord1_term1),
                (Expression) visit(ctx.ord1_term2),
                ctx.start, ctx);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        return new If((Expression) visit(ctx.cond),
                (Expression) visit(ctx.thenBranch),
                (Expression) visit(ctx.elseBranch),
                ctx.start, ctx);
    }

    @Override
    public ASTNode visitUnar_minus(CoolParser.Unar_minusContext ctx) {
        return new UnaryMinus((Expression) visit(ctx.unar_minus), ctx.start, ctx);
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        var term2 = (Expression) visit(ctx.rel_term2);
        var term1 = (Expression) visit(ctx.rel_term1);
        Id id = new Id(ctx.op, ctx);
        return new Relational(term1, term2, id, ctx.start, ctx);
    }

    @Override
    public ASTNode visitUnar_not(CoolParser.Unar_notContext ctx) {
        return new Not((Expression) visit(ctx.unar_not), ctx.start, ctx);
    }

    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        Id id = new Id(ctx.var_name, ctx);
        return new Assign(id,
                (Expression) visit(ctx.assign_value),
                ctx.start, ctx);
    }

    @Override
    public ASTNode visitUnar_isvoid(CoolParser.Unar_isvoidContext ctx) {
        return new IsVoid((Expression) visit(ctx.unar_isvoid), ctx.start, ctx);
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        Id type = new Id(ctx.new_type, ctx);
        return new New(type, ctx.start, ctx);
    }

    @Override
    public ASTNode visitSelf_dispatch(CoolParser.Self_dispatchContext ctx) {
        List<Expression> args = new ArrayList<>();
        if (ctx.formals != null) {
            for (var arg : ctx.formals)
                args.add((Expression) visit(arg));
        }

        Id id = new Id(ctx.id, ctx);
        return new SelfDispatch(id, args, ctx.start, ctx);
    }

    @Override
    public ASTNode visitDispatch(CoolParser.DispatchContext ctx) {
        List<Expression> args = new ArrayList<>();
        if (ctx.formals != null) {
            for (var arg : ctx.formals)
                args.add((Expression) visit(arg));
        }

        Id id = new Id(ctx.id_meth, ctx);

        if (ctx.type != null)
            return new Dispatch((Expression) visit(ctx.id),
                    ctx.type.getText(),
                    id,
                    args,
                    ctx.start,
                    ctx);

        return new Dispatch((Expression) visit(ctx.id),
                id,
                args,
                ctx.start,
                ctx);
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        return new While((Expression) visit(ctx.cond),
                (Expression) visit(ctx.loop_value),
                ctx.start, ctx);
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        List<Att> atts = new ArrayList<>();
        if (ctx.vardef != null) {
            for (var att : ctx.vardef)
                atts.add((Att) visit(att));
            for (var att : atts)
                att.local = true;
        }

        return new Let(atts,
                (Expression) visit(ctx.let_value),
                ctx.start, ctx);
    }

    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        List<Id> ids = new ArrayList<>();
        List<Id> types = new ArrayList<>();
        List<Expression> cases = new ArrayList<>();
        if (ctx.ids != null) {
            for (var branch : ctx.ids) {
                Id id = new Id(branch, ctx);
                types.add(id);
            }
            for (var branch : ctx.types) {
                Id type = new Id(branch, ctx);
                ids.add(type);
            }
            for (var branch : ctx.expressions)
                cases.add((Expression) visit(branch));
        }

        return new Case((Expression) visit(ctx.case_expr), ids, types, cases, ctx.start, ctx);
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        if (ctx.expressions != null) {
            for (var expr : ctx.expressions)
                exprs.add((Expression) visit(expr));
        }

        return new Block(exprs, ctx.start, ctx);
    }
}
