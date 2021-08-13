/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sintatical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Variables.Char;
import Variables.Float;
import Variables.Int;
import Variables.Variable;
import tokens.TagEnums;
import tokens.Token;

/**
 *
 * @author cristiano
 */
public class Analysis {
    private Lexical.Analysis lexical_analyser;
    private int currentLine;
    private int ifs_counter, whiles_counter, repeats_counter = 0;
    private String temp_str;

    public HashMap<String, Variable> variables;
    public ArrayList<Int> int_variables;
    public ArrayList<Float> float_variables;
    public ArrayList<Char> char_variables;

    public Analysis(Lexical.Analysis lexical_analyser) throws IOException {
        this.lexical_analyser = lexical_analyser;
        lexical_analyser.proxyScan();

        this.variables = new HashMap<>();
        this.int_variables = new ArrayList<>();
        this.float_variables = new ArrayList<>();
        this.char_variables = new ArrayList<>();

        this.temp_str = "";
    }

    public Token getCurrent() {
        return this.lexical_analyser.getLastToken();
    }

    public Token eatToken(int tagToMatch) throws IOException {
        if (tagToMatch == this.lexical_analyser.getLastToken().tag) 
            return this.lexical_analyser.proxyScan();
        

        throw new IOException(
                "linha " + this.currentLine + ": Erro Sintático: não esperado [" + this.lexical_analyser.getLastToken().toString()
                        + "] do tipo " + this.lexical_analyser.getLastToken().tag + "\nToken esperado" + tagToMatch);
    }

    public void program() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        eatToken(TagEnums.CLASS);
        eatToken(TagEnums.ID);

        procBody();

        eatToken(TagEnums.END_OF_FILE);
    }

    private static boolean isDeclaration(Token t) throws IOException {
        int[] values = new int[] { TagEnums.INT, TagEnums.FLOAT, TagEnums.STRING_TYPE, TagEnums.CARACTERE };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void procBody() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        if (Analysis.isDeclaration(this.lexical_analyser.getLastToken())) {
            procDeclarations();
        }

        if (this.lexical_analyser.getLastToken().tag != TagEnums.INIT) {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
            System.out.println("Esperava-se o token de tipo INT, FLOAT, STRING ou INIT");
            System.exit(1);
        }

        this.currentLine = this.lexical_analyser.getLines();

        eatToken(TagEnums.INIT);
        procStmt_List();

        this.currentLine = this.lexical_analyser.getLines();

        if (this.lexical_analyser.getLastToken().tag == TagEnums.STOP) {
            eatToken(TagEnums.STOP);

            this.currentLine = this.lexical_analyser.getLines();
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
            System.out.println("Esperava-se o token de tipo SEMICOLON ou END");
            System.exit(1);
        }
    }

    private void procDeclarations() throws IOException {
        procDecl();
        while (this.lexical_analyser.getLastToken().tag == TagEnums.SEMICOLON) {
            eatToken(TagEnums.SEMICOLON);
            if (Analysis.isDeclaration(this.lexical_analyser.getLastToken()) == false) {
                break;
            }
            procDecl();
        }
    }

    private void procDecl() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        int type = procType();

        // matchToken(this.lexical_analyser.getLastToken().tag);

        ArrayList<String> idList = procIdent_List();

        // matchToken(TagEnums.SEMICOLON);

        for (String s : idList) {
            this.variables.put(s, (new Variable(s, type)));
            switch (type) {
                case 260:
                    this.int_variables.add(new Int(s));
                    break;
                case 261:
                    this.float_variables.add(new Float(s));
                    break;
                case 295:
                    this.char_variables.add(new Char(s));
                    break;
                default:
                    break;
            }
        }

    }

    // <ident-list> ::= id {"," id}
    private ArrayList<String> procIdent_List() throws IOException {
        ArrayList<String> idList = new ArrayList<>();

        idList.add(this.lexical_analyser.getLastToken().toString());
        eatToken(TagEnums.ID);

        while (this.lexical_analyser.getLastToken().tag == TagEnums.COMMA) {
            eatToken(TagEnums.COMMA);

            idList.add(this.lexical_analyser.getLastToken().toString());
            eatToken(TagEnums.ID);
        }
        return idList;
    }

    // <type> ::= int | float | char
    private int procType() throws IOException {
        switch (this.lexical_analyser.getLastToken().tag) {
            case 260:
                eatToken(TagEnums.INT);
                return 260;
            case 261:
                eatToken(TagEnums.FLOAT);
                return 261;
            case 295:
                eatToken(TagEnums.CARACTERE);
                return 295;
            default:
                System.out.println("linha " + this.currentLine + ": Erro Sintático -  Lexema não esperado ["
                        + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
                System.out.println("Esperava-se o token de tipo INT, FLOAT ou CHAR");
                System.exit(1);
                return 0;
        }
    }

    // <stmt-list> ::= <stmt> {";" <stmt>}
    private void procStmt_List() throws IOException {
        procStmt();

        while (this.lexical_analyser.getLastToken().tag == TagEnums.SEMICOLON) {
            eatToken(TagEnums.SEMICOLON);
            if (this.lexical_analyser.getLastToken().tag == TagEnums.CLOSE_BRA || this.lexical_analyser.getLastToken().tag == TagEnums.STOP)
                break;
            procStmt();
        }
    }

    // <stmt> ::= <assign-stmt> | <if-stmt> | <while-stmt> | <repeat-stmt> |
    // <read-stmt> | <write-stmt>
    private void procStmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        switch (this.lexical_analyser.getLastToken().tag) {
            case 293:
                procAssign_Stmt();
                break;
            case 263:
                procIf_Stmt();
                break;
            case 268:
                procWhile_Stmt();
                break;
            case 269:
                procRepeat_Stmt();
                break;
            case 270:
                procRead_Stmt();
                break;
            case 271:
                procWrite_Stmt();
                break;
            default:
                System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                        + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
                System.out.println("Esperava-se o token de tipo ID, IF, WHILE, REPEAT, IN ou OUT");
                System.exit(1);
        }
    }

    private Variable procId() throws IOException {
        String idName = this.lexical_analyser.getLastToken().toString();
        eatToken(TagEnums.ID);
        Variable var = this.variables.get(idName);

        return var;
    }

    // <assign-stmt> ::= id "=" <simple_expr>
    private void procAssign_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        Variable var = procId();
        eatToken(TagEnums.ASSIGN);
        procSimple_Expr();

        if (var.type == 295) {
            for (Char cv : this.char_variables)
                if (cv.getName().equals(var.getName()))
                    cv.value = this.temp_str.charAt(0);
        }
        this.temp_str = "";
    }

    // <if-stmt> ::= if <condition> then <stmt-list> [ else <stmt-list>] end
    private void procIf_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        this.ifs_counter++;

        eatToken(TagEnums.IF);

        eatToken(TagEnums.OPEN_PAR);

        procCondition();

        eatToken(TagEnums.CLOSE_PAR);

        eatToken(TagEnums.OPEN_BRA);

        procStmt_List();

        eatToken(TagEnums.CLOSE_BRA);

        if (this.lexical_analyser.getLastToken().tag == TagEnums.ELSE) {
            this.currentLine = this.lexical_analyser.getLines();
            eatToken(TagEnums.ELSE);

            eatToken(TagEnums.OPEN_BRA);

            procStmt_List();

            eatToken(TagEnums.CLOSE_BRA);
        }

        this.currentLine = this.lexical_analyser.getLines();
    }

    // <condition> ::= <expression>
    private void procCondition() throws IOException {
        procExpression();
    }

    // <repeat-stmt> ::= repeat <stmt-list> <stmt-suffix>
    private void procRepeat_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        this.repeats_counter++;
        int this_counter = ifs_counter;

        eatToken(TagEnums.DO);
        eatToken(TagEnums.OPEN_BRA);

        procStmt_List();
        eatToken(TagEnums.CLOSE_BRA);
        procStmt_Suffix();
    }

    // <stmt-suffix> ::= until <condition>
    private void procStmt_Suffix() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        eatToken(TagEnums.WHILE);
        eatToken(TagEnums.OPEN_PAR);
        procCondition();
        eatToken(TagEnums.CLOSE_PAR);
    }

    // <while-stmt> ::= <stmt-prefix> <stmt-list> end
    private void procWhile_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        this.whiles_counter++;
        int this_counter = ifs_counter;

        procStmt_Prefix();

        procStmt_List();

        eatToken(TagEnums.STOP);
    }

    // <stmt-prefix> ::= while <condition> do
    private void procStmt_Prefix() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        eatToken(TagEnums.WHILE);
        procCondition();
        eatToken(TagEnums.DO);
    }

    // <read-stmt> ::= in "(" id ")"
    private void procRead_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        eatToken(TagEnums.READ);
        eatToken(TagEnums.OPEN_PAR);
        procId();
        eatToken(TagEnums.CLOSE_PAR);
    }

    // <write-stmt> ::= out "(" <writable> ")"
    private void procWrite_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        eatToken(TagEnums.WRITE);
        eatToken(TagEnums.OPEN_PAR);
        procWritable();
        eatToken(TagEnums.CLOSE_PAR);
        this.temp_str = "";
    }

    // <writable> ::= <simple-expr> | <literal>
    private void procWritable() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ID) {
            procSimple_Expr();
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.STRING_VALUE) {
            procLiteral();
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
            System.out.println("Esperava-se o token de tipo ID ou STRING");
            System.exit(1);
        }
    }

    // <expression> ::= <simple-expr> [ <relop> <simple-expr> ]
    private void procExpression() throws IOException {
        procSimple_Expr();

        if (this.lexical_analyser.getLastToken().tag == TagEnums.EQ
                || this.lexical_analyser.getLastToken().tag == TagEnums.GT
                || this.lexical_analyser.getLastToken().tag == TagEnums.GE
                || this.lexical_analyser.getLastToken().tag == TagEnums.LT
                || this.lexical_analyser.getLastToken().tag == TagEnums.LE
                || this.lexical_analyser.getLastToken().tag == TagEnums.NE) {

            procRelOp();

            procSimple_Expr();
        }
    }

    // <simple-expr> ::= <term> { <addop> <term> }
    private void procSimple_Expr() throws IOException {
        /**
         * É possível termos as expressoes de tipos INT = INT ( + | - | * ) INT FLOAT =
         * INT / INT FLOAT = FLOAT ( + | - | * | / ) INT FLOAT = INT ( + | - | * | / )
         * FLOAT FLOAT = FLOAT ( + | - | * | / ) FLOAT
         */
        procTerm();

        while (this.lexical_analyser.getLastToken().tag == TagEnums.ADD
                || this.lexical_analyser.getLastToken().tag == TagEnums.SUB
                || this.lexical_analyser.getLastToken().tag == TagEnums.OR) {
            procAddOp();

            procTerm();
        }
    }

    // <term> ::= <factor-a> { <mulop> <factor-a> }
    private void procTerm() throws IOException {
        procFactor_A();

        while (this.lexical_analyser.getLastToken().tag == TagEnums.MUL
                || this.lexical_analyser.getLastToken().tag == TagEnums.DIV
                || this.lexical_analyser.getLastToken().tag == TagEnums.AND) {
            procMulOp();

            procFactor_A();

        }
    }

    // <factor-a> ::= ["!" | "-"] <factor>
    private void procFactor_A() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NOT) {
            eatToken(TagEnums.NOT);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.SUB) {
            eatToken(TagEnums.SUB);
        }

        procFactor();
    }

    // <factor> ::= id | <constant> | "(" <expression> ")"
    private void procFactor() throws IOException {
        int stack_position;
        char ch;
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ID) {
            eatToken(TagEnums.ID);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.NUM || this.lexical_analyser.getLastToken().tag == TagEnums.CARACTERE) {
            procConstant();
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.OPEN_PAR) {
            eatToken(TagEnums.OPEN_PAR);
            procExpression();
            eatToken(TagEnums.CLOSE_PAR);
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo " + this.lexical_analyser.getLastToken().tag);
            System.out.println("Esperava-se o token de tipo NUM, CARACTERE ou OPEN_PAR");
            System.exit(1);
        }
    }

    // <relop> ::= "==" | ">" | ">=" | "<" | "<=" | "!="
    private void procRelOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.EQ) {
            eatToken(TagEnums.EQ);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.GT) {
            eatToken(TagEnums.GT);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.GE) {
            eatToken(TagEnums.GE);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.LT) {
            eatToken(TagEnums.LT);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.LE) {
            eatToken(TagEnums.LE);
        } else {
            eatToken(TagEnums.NE);
        }
    }

    // <addop> ::= "+" | "-" | ||
    private void procAddOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ADD) {
            eatToken(TagEnums.ADD);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.SUB) {
            eatToken(TagEnums.SUB);
        } else {
            eatToken(TagEnums.OR);
        }
    }

    // <mulop> ::= "*" | "/" | &&
    private void procMulOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.MUL) {
            eatToken(TagEnums.MUL);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.DIV) {
            eatToken(TagEnums.DIV);
        } else {
            eatToken(TagEnums.AND);
        }
    }

    // <constant> ::= num [ "." num ] | caractere
    private int procConstant() throws IOException {
        int type;
        String number;
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NUM) { // integer const
            type = 260;
            number = this.lexical_analyser.getLastToken().toString();
            eatToken(TagEnums.NUM);
            if (this.lexical_analyser.getLastToken().tag == TagEnums.DOT) { // float const
                type = 261;
                eatToken(TagEnums.DOT);
                number += "." + this.lexical_analyser.getLastToken().toString();
                eatToken(TagEnums.NUM);

                return type;
            }
        } else { // char const
            type = 295;
            temp_str += this.lexical_analyser.getLastToken().toString();
            eatToken(295);
        }
        return type;
    }

    // <literal> ::= string
    private void procLiteral() throws IOException {
        eatToken(TagEnums.STRING_VALUE);
    }

    public void showError() {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.UNEXPECTED_EOF) {
            // imprimir erro 1
        } else {
            // imprimir erro 2
        }
        System.exit(1);
    }
}
