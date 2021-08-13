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

import Variables.StringVariable;
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

    private String sum_string;

    public HashMap<String, Variable> variables;

    public ArrayList<Int> int_variables;
    public ArrayList<Float> float_variables;
    public ArrayList<StringVariable> string_variables;

    public Analysis(Lexical.Analysis lexical_analyser) throws IOException {
        this.lexical_analyser = lexical_analyser;
        lexical_analyser.proxyScan();

        this.variables = new HashMap<>();
        this.int_variables = new ArrayList<>();
        this.float_variables = new ArrayList<>();
        this.string_variables = new ArrayList<>();

        this.sum_string = "";
    }

    public Token eatToken(int tagToMatch) throws IOException {
        if (tagToMatch == this.lexical_analyser.getLastToken().tag)
            return this.lexical_analyser.proxyScan();

        throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Sintático: não esperado ["
                + this.lexical_analyser.getLastToken().toString() + "] do tipo "
                + this.lexical_analyser.getLastToken().tag + "\nToken esperado " + tagToMatch);
    }

    public void run() throws IOException {
        eatToken(TagEnums.CLASS);
        eatToken(TagEnums.ID);

        eatBody();

        eatToken(TagEnums.END_OF_FILE);
    }

    private static boolean isDeclaration(Token t) throws IOException {
        int[] values = new int[] { TagEnums.INT, TagEnums.FLOAT, TagEnums.STRING_TYPE, TagEnums.CARACTERE };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private static boolean isEndStmntList(Token t) throws IOException {
        int[] values = new int[] { TagEnums.CLOSE_BRA, TagEnums.STOP };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void eatBody() throws IOException {
        if (Analysis.isDeclaration(this.lexical_analyser.getLastToken())) {
            eatDecl_list();
        }

        eatToken(TagEnums.INIT);

        eatStmt_List();

        eatToken(TagEnums.STOP);
    }

    private void eatDecl_list() throws IOException {
        eatDecl();

        while (this.lexical_analyser.getLastToken().tag == TagEnums.SEMICOLON || this.lexical_analyser.getLastToken().tag  == TagEnums.CLOSE_BRA) {
            eatToken(TagEnums.SEMICOLON);

            if (Analysis.isDeclaration(this.lexical_analyser.getLastToken()) == false)
                return;

            eatDecl();
        }
    }

    private void eatDecl() throws IOException {
        int type = eatType();

        ArrayList<String> idList = eatIdent_List();

        for (String s : idList) {
            this.variables.put(s, (new Variable(s, type)));
            switch (type) {
                case TagEnums.INT:
                    this.int_variables.add(new Int(s));
                    break;
                case TagEnums.FLOAT:
                    this.float_variables.add(new Float(s));
                    break;
                case TagEnums.STRING_TYPE:
                    this.string_variables.add(new StringVariable(s));
                    break;
                default:
                    break;
            }
        }

    }

    private ArrayList<String> eatIdent_List() throws IOException {
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

    private int eatType() throws IOException {
        switch (this.lexical_analyser.getLastToken().tag) {
            case TagEnums.INT:
                eatToken(TagEnums.INT);
                return TagEnums.INT;
            case TagEnums.FLOAT:
                eatToken(TagEnums.FLOAT);
                return TagEnums.FLOAT;
            case TagEnums.STRING_TYPE:
                eatToken(TagEnums.STRING_TYPE);
                return TagEnums.STRING_TYPE;
            default:
                throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Sintático: não esperado ["
                        + this.lexical_analyser.getLastToken().toString() + "] do tipo "
                        + this.lexical_analyser.getLastToken().tag + "\nToken esperado  INT, FLOAT ou STRING");
        }
    }

    private void eatStmt_List() throws IOException {
        eatStmt();

        while (this.lexical_analyser.getLastToken().tag == TagEnums.SEMICOLON) {
            eatToken(TagEnums.SEMICOLON);

            if (isEndStmntList(this.lexical_analyser.getLastToken()))
                break;

            eatStmt();
        }
    }

    private void eatStmt() throws IOException {
        switch (this.lexical_analyser.getLastToken().tag) {
            case TagEnums.ID:
                eatAssign_Stmt();
                break;
            case TagEnums.IF:
                eatIF();
                break;
            case TagEnums.DO:
                eatDoWhile();
                break;
            case TagEnums.READ:
                eatRead();
                break;
            case TagEnums.WRITE:
                eatWrite();
                break;
            default:
                throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Sintático: não esperado ["
                        + this.lexical_analyser.getLastToken().toString() + "] do tipo "
                        + this.lexical_analyser.getLastToken().tag + "\nToken esperado  ID, IF, DO, READ ou WRITE");
        }
    }

    private Variable eatId() throws IOException {
        String idName = this.lexical_analyser.getLastToken().toString();

        eatToken(TagEnums.ID);

        Variable var = this.variables.get(idName);

        return var;
    }

    private void eatAssign_Stmt() throws IOException {
        Variable var = eatId();

        eatToken(TagEnums.ASSIGN);
        eatSimple_Expr();

        if (var != null && var.type == 262)
            this.setStringValue(var.getName());

    }

    private void setStringValue(String id) {
        for (StringVariable cv : this.string_variables)
            if (cv.getName().equals(id))
                cv.value = this.sum_string;
        this.sum_string = "";
    }

    private void eatIF() throws IOException {
        eatToken(TagEnums.IF);

        eatToken(TagEnums.OPEN_PAR);

        eatCondition();

        eatToken(TagEnums.CLOSE_PAR);

        eatToken(TagEnums.OPEN_BRA);

        eatStmt_List();

        eatToken(TagEnums.CLOSE_BRA);

        if (this.lexical_analyser.getLastToken().tag == TagEnums.ELSE) {

            this.eatElse();
        }

        try {
            eatStmt();
        } catch (Exception e) {} 
    }

    private void eatElse() throws IOException {
        eatToken(TagEnums.ELSE);

        eatToken(TagEnums.OPEN_BRA);

        eatStmt_List();

        eatToken(TagEnums.CLOSE_BRA);
    }

    private void eatCondition() throws IOException {
        eatExpression();
    }

    private void eatDoWhile() throws IOException {
        eatToken(TagEnums.DO);
        eatToken(TagEnums.OPEN_BRA);

        eatStmt_List();
        eatToken(TagEnums.CLOSE_BRA);

        eatToken(TagEnums.WHILE);
        eatToken(TagEnums.OPEN_PAR);
        eatCondition();
        eatToken(TagEnums.CLOSE_PAR);
    }

    private void eatRead() throws IOException {
        eatToken(TagEnums.READ);
        eatToken(TagEnums.OPEN_PAR);
        eatId();
        eatToken(TagEnums.CLOSE_PAR);
    }

    private void eatWrite() throws IOException {
        eatToken(TagEnums.WRITE);
        eatToken(TagEnums.OPEN_PAR);
        eatWritable();
        eatToken(TagEnums.CLOSE_PAR);
        this.sum_string = "";
    }

    private void eatWritable() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ID) {
            eatSimple_Expr();
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.STRING_VALUE) {
            eatLiteral();
        } else {
            throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Sintático: não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo "
                    + this.lexical_analyser.getLastToken().tag + "\nToken esperado  ID ou STRING");
        }
    }

    private static boolean isRelOp(Token t) throws IOException {
        int[] values = new int[] { TagEnums.GT, TagEnums.GE, TagEnums.LT, TagEnums.LE, TagEnums.NE, TagEnums.EQ };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void eatExpression() throws IOException {
        eatSimple_Expr();

        if (isRelOp(this.lexical_analyser.getLastToken())) {

            eatRelOp();

            eatSimple_Expr();
        }
    }

    private static boolean isSEOp(Token t) throws IOException {
        int[] values = new int[] { TagEnums.ADD, TagEnums.SUB, TagEnums.OR };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void eatSimple_Expr() throws IOException {
        eatTerm();

        while (isSEOp(this.lexical_analyser.getLastToken())) {
            eatAddOp();

            eatTerm();
        }
    }

    private static boolean isTermOp(Token t) throws IOException {
        int[] values = new int[] { TagEnums.MUL, TagEnums.DIV, TagEnums.AND };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private void eatTerm() throws IOException {
        eatFactor_A();

        while (isTermOp(this.lexical_analyser.getLastToken())) {
            eatMulOp();

            eatFactor_A();
        }
    }

    private void eatFactor_A() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NOT) {
            eatToken(TagEnums.NOT);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.SUB) {
            eatToken(TagEnums.SUB);
        }

        eatFactor();
    }

    private void eatFactor() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ID) {
            eatToken(TagEnums.ID);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.STRING_VALUE) {
            eatLiteral();
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NUM
                || this.lexical_analyser.getLastToken().tag == TagEnums.CARACTERE) {
            eatConstant();
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.OPEN_PAR) {
            eatToken(TagEnums.OPEN_PAR);
            eatExpression();
            eatToken(TagEnums.CLOSE_PAR);
            return;
        } else {
            throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Sintático: não esperado ["
                    + this.lexical_analyser.getLastToken().toString() + "] do tipo "
                    + this.lexical_analyser.getLastToken().tag + "\nToken esperado  NUM, STR ou OPEN_PAR");
        }
    }

    private void eatRelOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.EQ) {
            eatToken(TagEnums.EQ);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.GT) {
            eatToken(TagEnums.GT);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.GE) {
            eatToken(TagEnums.GE);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.LT) {
            eatToken(TagEnums.LT);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.LE) {
            eatToken(TagEnums.LE);
            return;
        }
        eatToken(TagEnums.NE);

    }

    private void eatAddOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.ADD) {
            eatToken(TagEnums.ADD);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.SUB) {
            eatToken(TagEnums.SUB);
            return;
        }
        eatToken(TagEnums.OR);
    }

    private void eatMulOp() throws IOException {
        if (this.lexical_analyser.getLastToken().tag == TagEnums.MUL) {
            eatToken(TagEnums.MUL);
            return;
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.DIV) {
            eatToken(TagEnums.DIV);
            return;
        }
        eatToken(TagEnums.AND);

    }

    private int eatConstant() throws IOException {
        int type;
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NUM) {
            type = 260;
            eatToken(TagEnums.NUM);
            if (this.lexical_analyser.getLastToken().tag == TagEnums.DOT) {
                type = 261;
                eatToken(TagEnums.DOT);
                eatToken(TagEnums.NUM);

                return type;
            }
            return type;
        }

        type = TagEnums.STRING_VALUE;
        sum_string += this.lexical_analyser.getLastToken().toString();
        eatToken(TagEnums.STRING_VALUE);

        return type;
    }

    private void eatLiteral() throws IOException {
        eatToken(TagEnums.STRING_VALUE);
    }
}
