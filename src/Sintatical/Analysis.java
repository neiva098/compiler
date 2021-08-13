/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sintatical;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Variables.Char;
import Variables.Float;
import Variables.Int;
import Variables.Types;
import Variables.Variable;
import tokens.TagEnums;
import tokens.Token;

/**
 *
 * @author cristiano
 */
public class Analysis {
    private Lexical.Analysis lexical_analyser;
    private Token currentToken;
    private int currentLine;
    private int ifs_counter, whiles_counter, repeats_counter = 0;
    private String temp_str;

    public HashMap<String, Variable> variables;
    public ArrayList<Int> int_variables;
    public ArrayList<Float> float_variables;
    public ArrayList<Char> char_variables;

    public Analysis(Lexical.Analysis lexical_analyser) throws IOException {
        this.lexical_analyser = lexical_analyser;
        this.currentToken = lexical_analyser.proxyScan();

        this.variables = new HashMap<>();
        this.int_variables = new ArrayList<>();
        this.float_variables = new ArrayList<>();
        this.char_variables = new ArrayList<>();

        this.temp_str = "";
    }

    public Token getCurrent() {
        return currentToken;
    }

    public void setCurrent(Token current) {
        this.currentToken = current;
    }

    public void matchToken(int tag) throws IOException {
        if (tag == this.currentToken.tag) {
            this.currentToken = this.lexical_analyser.proxyScan();

        } else {
            if (this.currentToken.tag == TagEnums.END_OF_FILE) {
                System.out.println("linha " + this.currentLine
                        + ": Erro Sintático - Fim de arquivo inesperado (simbolo de EOF dentro de algum producao nao completada)");

                System.exit(1);
            } else {
                System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                        + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);

                System.out.println("Esperava-se o token de tipo " + tag);

                System.exit(1);
            }
        }
    }

    public void program() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        matchToken(TagEnums.CLASS);
        matchToken(TagEnums.ID);

        procBody();

        matchToken(TagEnums.END_OF_FILE);
    }

    private static boolean isDeclaration(Token t) throws IOException {
        int[] values = new int[] { TagEnums.INT, TagEnums.FLOAT, TagEnums.STRING_TYPE, TagEnums.CARACTERE };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void procBody() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        if (Analysis.isDeclaration(currentToken)) {
            procDeclarations();
        }

        if (currentToken.tag != TagEnums.INIT) {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
            System.out.println("Esperava-se o token de tipo INT, FLOAT, STRING ou INIT");
            System.exit(1);
        }

        this.currentLine = this.lexical_analyser.getLines();

        matchToken(TagEnums.INIT);
        procStmt_List();

        this.currentLine = this.lexical_analyser.getLines();

        if (currentToken.tag == TagEnums.STOP) {
            matchToken(TagEnums.STOP);

            this.currentLine = this.lexical_analyser.getLines();
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
            System.out.println("Esperava-se o token de tipo SEMICOLON ou END");
            System.exit(1);
        }
    }

    private void procDeclarations() throws IOException {
        procDecl();
        while (currentToken.tag == TagEnums.SEMICOLON) {
            matchToken(TagEnums.SEMICOLON);
            if (Analysis.isDeclaration(currentToken) == false) {
                break;
            }
            procDecl();
        }
    }

    private void procDecl() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        int type = procType();

        // matchToken(currentToken.tag);

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

        idList.add(this.currentToken.toString());
        matchToken(TagEnums.ID);

        while (currentToken.tag == TagEnums.COMMA) {
            matchToken(TagEnums.COMMA);

            idList.add(this.currentToken.toString());
            matchToken(TagEnums.ID);
        }
        return idList;
    }

    // <type> ::= int | float | char
    private int procType() throws IOException {
        switch (currentToken.tag) {
            case 260:
                matchToken(TagEnums.INT);
                return 260;
            case 261:
                matchToken(TagEnums.FLOAT);
                return 261;
            case 295:
                matchToken(TagEnums.CARACTERE);
                return 295;
            default:
                System.out.println("linha " + this.currentLine + ": Erro Sintático -  Lexema não esperado ["
                        + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
                System.out.println("Esperava-se o token de tipo INT, FLOAT ou CHAR");
                System.exit(1);
                return 0;
        }
    }

    // <stmt-list> ::= <stmt> {";" <stmt>}
    private void procStmt_List() throws IOException {
        procStmt();

        while (currentToken.tag == TagEnums.SEMICOLON) {
            matchToken(TagEnums.SEMICOLON);
            if (currentToken.tag == TagEnums.STOP)
                break;
            procStmt();
        }
    }

    // <stmt> ::= <assign-stmt> | <if-stmt> | <while-stmt> | <repeat-stmt> |
    // <read-stmt> | <write-stmt>
    private void procStmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        switch (currentToken.tag) {
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
                        + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
                System.out.println("Esperava-se o token de tipo ID, IF, WHILE, REPEAT, IN ou OUT");
                System.exit(1);
        }
    }

    private Variable procId() throws IOException {
        String idName = this.currentToken.toString();
        matchToken(TagEnums.ID);
        Variable var = this.variables.get(idName);

        return var;
    }

    // <assign-stmt> ::= id "=" <simple_expr>
    private void procAssign_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        Variable var = procId();
        matchToken(TagEnums.ASSIGN);
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

        matchToken(TagEnums.IF);

        procCondition();

        procStmt_List();

        if (currentToken.tag == TagEnums.ELSE) {
            this.currentLine = this.lexical_analyser.getLines();
            matchToken(TagEnums.ELSE);
            procStmt_List();
        }

        this.currentLine = this.lexical_analyser.getLines();

        matchToken(TagEnums.STOP);
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

        matchToken(TagEnums.DO);

        procStmt_List();
        procStmt_Suffix();
    }

    // <stmt-suffix> ::= until <condition>
    private void procStmt_Suffix() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        matchToken(TagEnums.WHILE);
        procCondition();
    }

    // <while-stmt> ::= <stmt-prefix> <stmt-list> end
    private void procWhile_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        this.whiles_counter++;
        int this_counter = ifs_counter;

        procStmt_Prefix();

        procStmt_List();

        matchToken(TagEnums.STOP);
    }

    // <stmt-prefix> ::= while <condition> do
    private void procStmt_Prefix() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        matchToken(TagEnums.WHILE);
        procCondition();
        matchToken(TagEnums.DO);
    }

    // <read-stmt> ::= in "(" id ")"
    private void procRead_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        matchToken(TagEnums.READ);
        matchToken(TagEnums.OPEN_PAR);
        procId();
        matchToken(TagEnums.CLOSE_PAR);
    }

    // <write-stmt> ::= out "(" <writable> ")"
    private void procWrite_Stmt() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();
        matchToken(TagEnums.WRITE);
        matchToken(TagEnums.OPEN_PAR);
        procWritable();
        matchToken(TagEnums.CLOSE_PAR);
        this.temp_str = "";
    }

    // <writable> ::= <simple-expr> | <literal>
    private void procWritable() throws IOException {
        if (currentToken.tag == TagEnums.ID) {
            procSimple_Expr();
        } else if (currentToken.tag == TagEnums.STRING_VALUE) {
            procLiteral();
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
            System.out.println("Esperava-se o token de tipo ID ou STRING");
            System.exit(1);
        }
    }

    // <expression> ::= <simple-expr> [ <relop> <simple-expr> ]
    private void procExpression() throws IOException {
        procSimple_Expr();

        if (currentToken.tag == TagEnums.EQ || currentToken.tag == TagEnums.GT || currentToken.tag == TagEnums.GE
                || currentToken.tag == TagEnums.LT || currentToken.tag == TagEnums.LE
                || currentToken.tag == TagEnums.NE) {

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

        while (currentToken.tag == TagEnums.ADD || currentToken.tag == TagEnums.SUB
                || currentToken.tag == TagEnums.OR) {
            procAddOp();

            procTerm();
        }
    }

    // <term> ::= <factor-a> { <mulop> <factor-a> }
    private void procTerm() throws IOException {
        procFactor_A();

        while (currentToken.tag == TagEnums.MUL || currentToken.tag == TagEnums.DIV
                || currentToken.tag == TagEnums.AND) {
            procMulOp();

            procFactor_A();

        }
    }

    // <factor-a> ::= ["!" | "-"] <factor>
    private void procFactor_A() throws IOException {
        int type;
        
        if (currentToken.tag == TagEnums.NOT) {
            matchToken(TagEnums.NOT);
        } else if (currentToken.tag == TagEnums.SUB) {
            matchToken(TagEnums.SUB);
        }

        procFactor();
    }

    // <factor> ::= id | <constant> | "(" <expression> ")"
    private void procFactor() throws IOException {
        int stack_position;
        char ch;
        if (currentToken.tag == TagEnums.ID) {
            matchToken(TagEnums.ID);
        } else if (currentToken.tag == TagEnums.NUM || currentToken.tag == TagEnums.CARACTERE) {
            procConstant();
        } else if (currentToken.tag == TagEnums.OPEN_PAR) {
            matchToken(TagEnums.OPEN_PAR);
            procExpression();
            matchToken(TagEnums.CLOSE_PAR);
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
            System.out.println("Esperava-se o token de tipo NUM, CARACTERE ou OPEN_PAR");
            System.exit(1);
        }
    }

    // <relop> ::= "==" | ">" | ">=" | "<" | "<=" | "!="
    private void procRelOp() throws IOException {
        if (currentToken.tag == TagEnums.EQ) {
            matchToken(TagEnums.EQ);
        } else if (currentToken.tag == TagEnums.GT) {
            matchToken(TagEnums.GT);
        } else if (currentToken.tag == TagEnums.GE) {
            matchToken(TagEnums.GE);
        } else if (currentToken.tag == TagEnums.LT) {
            matchToken(TagEnums.LT);
        } else if (currentToken.tag == TagEnums.LE) {
            matchToken(TagEnums.LE);
        } else {
            matchToken(TagEnums.NE);
        }
    }

    // <addop> ::= "+" | "-" | ||
    private void procAddOp() throws IOException {
        if (currentToken.tag == TagEnums.ADD) {
            matchToken(TagEnums.ADD);
        } else if (currentToken.tag == TagEnums.SUB) {
            matchToken(TagEnums.SUB);
        } else {
            matchToken(TagEnums.OR);
        }
    }

    // <mulop> ::= "*" | "/" | &&
    private void procMulOp() throws IOException {
        if (currentToken.tag == TagEnums.MUL) {
            matchToken(TagEnums.MUL);
        } else if (currentToken.tag == TagEnums.DIV) {
            matchToken(TagEnums.DIV);
        } else {
            matchToken(TagEnums.AND);
        }
    }

    // <constant> ::= num [ "." num ] | caractere
    private int procConstant() throws IOException {
        int type;
        String number;
        if (currentToken.tag == TagEnums.NUM) { // integer const
            type = 260;
            number = this.currentToken.toString();
            matchToken(TagEnums.NUM);
            if (currentToken.tag == TagEnums.DOT) { // float const
                type = 261;
                matchToken(TagEnums.DOT);
                number += "." + this.currentToken.toString();
                matchToken(TagEnums.NUM);

                return type;
            }
        } else { // char const
            type = 295;
            temp_str += this.currentToken.toString();
            matchToken(295);
        }
        return type;
    }

    // <literal> ::= string
    private void procLiteral() throws IOException {
        matchToken(TagEnums.STRING_VALUE);
    }

    public void showError() {
        if (currentToken.tag == TagEnums.UNEXPECTED_EOF) {
            // imprimir erro 1
        } else {
            // imprimir erro 2
        }
        System.exit(1);
    }

    private int get_intvar_index(String varName) {
        for (Int iv : this.int_variables)
            if (iv.getName().equals(varName))
                return this.int_variables.indexOf(iv);
        return -1;
    }

    private int get_floatvar_index(String varName) {
        for (Float fv : this.float_variables)
            if (fv.getName().equals(varName))
                return this.float_variables.indexOf(fv) + this.int_variables.size();
        return -1;
    }

    private char get_charvar_char(String varName) {
        for (Char cv : this.char_variables)
            if (cv.getName().equals(varName)) {
                return cv.value;
            }
        return '\0';
    }

}
