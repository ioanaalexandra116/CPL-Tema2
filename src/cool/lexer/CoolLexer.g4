lexer grammar CoolLexer;

tokens { ERROR }

@header{
    package cool.lexer;	
}

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

// if clause keywords

IF : 'if' ;
THEN : 'then' ;
ELSE : 'else' ;
FI : 'fi' ;

// loops keywords

WHILE : 'while' ;
LOOP : 'loop' ;
POOL : 'pool' ;

// case keywords

CASE : 'case' ;
ESAC : 'esac' ;
OF : 'of' ;
ARROW : '=>' ;

// let keywords

LET : 'let' ;
IN : 'in' ;

// class keywords

NEW : 'new' ;
CLASS : 'class' ;
INHERITS : 'inherits' ;
DOT : '.' ;
AT : '@' ;

// block keywords

LBRACE : '{' ;
LPAREN : '(' ;
RBRACE : '}' ;
RPAREN : ')' ;

// operators

PLUS : '+' ;
MINUS : '-' ;
MULT : '*' ;
DIV : '/' ;
TILDA : '~' ;
NOT : 'not' ;
ISVOID : 'isvoid' ;
LT : '<' ;
EQ : '=' ;
LE : '<=' ;

// other keywords

COLON : ':' ;
SEMI : ';' ;
COMMA : ',' ;
ASSIGN : '<-' ;

// comments

fragment NEW_LINE : '\r'? '\n';
fragment TAB : '\t';

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
    : '(*'
      (BLOCK_COMMENT | .)*?
      ('*)' { skip();}
      | EOF { raiseError("EOF in comment"); })
    ;

BL_COM_START : '(*' ;
BL_COM_END : '*)' { raiseError("Unmatched *)");};

// types

fragment LETTER : [a-zA-Z] ;
fragment DIGIT : [0-9] ;

TYPE : 'Int' | 'String' | 'Bool' | 'Object' | [A-Z][a-zA-Z0-9_]*;

ID : (LETTER | '_')(LETTER | '_' | DIGIT)*;

INT : DIGIT+;

BOOL : 'true' | 'false' ;

STRING : '"' ('\\"' | '\\' NEW_LINE | .)*?
         (
         	'"' {
         		String str = getText();

         		String newStr = str
         			.substring(1, str.length() - 1)
         			.replace("\\r", "\r")
         			.replace("\\n", "\n")
         			.replace("\\t", "\t")
         			.replaceAll("\\\\(?!\\\\)", "");

         		if (newStr.length() > 1024) {

         			raiseError("String constant too long");
         			return;

         		} else if (newStr.contains("\u0000")) {
         			raiseError("String contains null character");
         			return;
         		} else {
         			setText(newStr);
         		}
         	}
         	| NEW_LINE { raiseError("Unterminated string constant"); }
         	| EOF { raiseError("EOF in string constant"); }
         	);

// whitespaces

WS
    :   [ \n\f\r\t]+ -> skip
    ;

INVALID_CHAR :   . { raiseError("Invalid character: " + getText()); };