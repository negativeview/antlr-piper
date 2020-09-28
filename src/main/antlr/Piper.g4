grammar Piper;

STRUCT       : 'struct';
LET          : 'let';
RETURN       : 'return';
IF           : 'if';
FUNCTION     : 'function';
ELSE         : 'else';

ID           : [A-Za-z_][0-9A-Za-z_]*;
LCURLY       : '{';
RCURLY       : '}';
AT           : '@';
LPAREN       : '(';
RPAREN       : ')';
NUMBER       : [0-9]+;
COLON        : ':';
EQUAL        : '=';
DOUBLE_EQUAL : '==';
COMMA        : ',';
FAT_ARROW    : '=>';
ARROW        : '->';
DOT          : '.';
PLUS         : '+';
MINUS        : '-';
LT           : '<';
GT           : '>';
DOUBLE_DOT   : '..';
LSQUARE      : '[';
RSQUARE      : ']';
ASTERISK     : '*';
SLASH        : '/';
HASH         : '#';
WHITESPACE   : (' ' | '\t' | '\r' | '\n')+ -> channel(HIDDEN);
SEMICOLON    : ';';

// Only have to define top-level file things. Need to add classes and methods
// and whatnot.
file : ( struct_block | auto_casting_block | equality_block | function_definition )+;

// Struct blocks and their immediate needs.
struct_block: STRUCT ID LCURLY member_variables RCURLY;
member_variables: (member_variable COMMA)* member_variable?;
member_variable: member_uninitialized_variable | member_initialized_variable;
member_uninitialized_variable: decorators? ID COLON ID;
// This really needs to be a const expression, but we're leaving that for later.
member_initialized_variable: decorators? ID COLON ID EQUAL expression;

// Auto casting blocks and their immediate needs.
auto_casting_block: ID ( ARROW | FAT_ARROW ) ID LCURLY code_block RCURLY;

// Auto casting blocks and their immediate needs.
equality_block: ID DOUBLE_EQUAL ID LCURLY code_block RCURLY;

// Functions and their immediate needs.
function_definition: decorators? function_header LCURLY code_block RCURLY;
function_header: FUNCTION ID ( LPAREN function_arg_definitions RPAREN )? ( ARROW function_out_definitions )?;
function_arg_definitions : (function_arg_definition COMMA)* function_arg_definition?
                         | ID
                         ;
function_arg_definition: ID COLON ID;

function_out_definitions : ID
                         | LPAREN ID RPAREN
                         | LPAREN named_function_out_definitions RPAREN
                         ;
named_function_out_definitions : named_function_out_definition | named_function_out_definition named_function_out_definitions;
named_function_out_definition: ID COLON ID;

closure: LPAREN function_arg_definitions RPAREN ( ARROW function_out_definitions )? LCURLY code_block RCURLY;


// Decorators
decorators: decorator | decorator decorators;
decorator : range_decorator
          ;
range_decorator: AT 'Range' LPAREN NUMBER COMMA NUMBER RPAREN;

range_block : LSQUARE (ID | NUMBER) DOUBLE_DOT (ID | NUMBER) RSQUARE;

function_call : ID function_args
              | function_call DOT ID function_args
              | ID DOT ID function_args
              | range_block DOT ID function_args
              ;

// This is NOT TRUE, but what is the name for what I want?
function_name: left_expression | function_call;
function_args : LPAREN ( named_function_args | unnamed_function_args )? RPAREN;
named_function_args: named_function_arg | named_function_arg COMMA named_function_args;
// Can this really be arbitrary expressions?
named_function_arg: ID COLON expression;
unnamed_function_args: ID | closure | expression;

conditional: lt_conditional | gt_conditional | equal_conditional;
code_block: statement+;
lt_conditional: expression LT expression;
gt_conditional: expression GT expression;
equal_conditional: expression DOUBLE_EQUAL expression;

statement : if_statement
          | return_statement
          | assignment_statement
          | let_statement
          | expression SEMICOLON
          ;

let_statement : LET ID COLON expression EQUAL expression SEMICOLON;
if_statement : if_statement_without_else
             | if_statement_with_else
             ;
if_statement_without_else: IF LPAREN conditional RPAREN LCURLY code_block RCURLY;
if_statement_with_else: IF LPAREN conditional RPAREN LCURLY code_block RCURLY ELSE LCURLY code_block RCURLY;

return_statement: RETURN expression SEMICOLON;
assignment_statement: left_expression EQUAL expression SEMICOLON;

left_expression : '(' left_expression ')'
                | ID
                | left_expression DOT expression
                | left_expression LSQUARE expression RSQUARE
                ;

expression : '(' expression ')'
           | left_expression
           | expression (ASTERISK|SLASH) expression
           | expression (PLUS|MINUS) expression
           | expression DOUBLE_EQUAL expression
           | <assoc=right> expression '^' expression
           | NUMBER
           | ( NUMBER | ID ) DOUBLE_DOT ( NUMBER | ID )
           | function_call
           | closure
           ;

// expression: ( bare_number | bare_variable | dereference | add_exp )+;
// bare_number: NUMBER;
// bare_variable: ID;
// dereference: ID DOT ID;
// add_exp : expression PLUS expression;
