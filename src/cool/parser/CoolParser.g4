parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program
    :   prog_classes=classes EOF
    ;

classes
    :   (classdef SEMI)+
    ;

classdef
    :   CLASS name=TYPE (INHERITS inherited_class=TYPE)? LBRACE (feature_list+=feature SEMI)* RBRACE
    ;

feature
    :   attribute
    ?
    |   method
    ;

attribute
    :   att_name=ID COLON type_att=TYPE (ASSIGN value=expr)?
    ;

method
    :   meth_name=ID LPAREN (vardef+=param (COMMA vardef+=param)*)? RPAREN COLON meth_type=TYPE LBRACE (meth_value=expr)? RBRACE
    ;

param : param_name=ID COLON param_type=TYPE;

expr
    :   NEW new_type=TYPE                                                                              # new
    |   id=expr (AT type=TYPE)? DOT id_meth=ID LPAREN (formals+=expr (COMMA formals+=expr)*)? RPAREN   # dispatch
    |   id=ID LPAREN (formals+=expr (COMMA formals+=expr)*)? RPAREN                                    # self_dispatch
    |   TILDA unar_minus=expr                                                                          # unar_minus
    |   IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI	                                   # if
    |   LPAREN paren=expr RPAREN                                                                       # paren
    |   ord1_term1=expr op=(MULT| DIV) ord1_term2=expr                                                 # muldiv
    |   ord2_term1=expr op=(PLUS | MINUS) ord2_term2=expr                                              # plusminus
    |   rel_term1=expr op=(EQ | LE | LT) rel_term2=expr                                                # relational
    |   NOT unar_not=expr                                                                              # unar_not
    |   ISVOID unar_isvoid=expr                                                                        # unar_isvoid
    |   var_name=ID ASSIGN assign_value=expr                                                           # assign
    |   WHILE cond=expr LOOP loop_value=expr POOL                                                      # while
    |   LET (vardef+=attribute (COMMA vardef+=attribute)*)? IN let_value=expr                          # let
    |   CASE case_expr=expr OF (ids+=ID COLON types+=TYPE ARROW expressions+=expr SEMI)+ ESAC          # case
    |   LBRACE (expressions+=expr SEMI)* RBRACE                                                        # block
    |	ID                                                                                             # id
    |   STRING                                                                                         # string
    |	INT                                                                                            # int
    |   BOOL                                                                                           # bool
    |   STRING                                                                                         # string
    ;
