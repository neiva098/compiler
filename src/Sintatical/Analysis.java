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

import Variables.StringVariable;
import Variables.Types;
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

    private PrintWriter writer;

    public Analysis(Lexical.Analysis lexical_analyser) throws IOException {
        this.lexical_analyser = lexical_analyser;
        lexical_analyser.proxyScan();

        this.variables = new HashMap<>();
        this.int_variables = new ArrayList<>();
        this.float_variables = new ArrayList<>();
        this.string_variables = new ArrayList<>();

        this.writer = new PrintWriter("assembly-code.vm", "UTF-8");

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

        this.writer.println("START");

        eatBody();

        eatToken(TagEnums.END_OF_FILE);

        this.writer.println("STOP");
        this.writer.close();
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

        while (this.lexical_analyser.getLastToken().tag == TagEnums.SEMICOLON
                || this.lexical_analyser.getLastToken().tag == TagEnums.CLOSE_BRA) {
            eatToken(TagEnums.SEMICOLON);

            if (Analysis.isDeclaration(this.lexical_analyser.getLastToken()) == false)
                return;

            eatDecl();
        }

        if (!this.int_variables.isEmpty())
            writer.println("PUSHN " + this.int_variables.size());

        for (int i = 0; i < this.float_variables.size(); i++)
            writer.println("PUSHF 0.0");
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
        int stack_position;
        Variable var = eatId();

        if (var == null) this.semanticalError("Variavel nao declarada");

        eatToken(TagEnums.ASSIGN);
        int tipo_expr = eatSimple_Expr();

        if (var.type == 262)
            this.setStringValue(var.getName());

        if (var.type != tipo_expr) {
            if (var.type == Types.FLOAT && tipo_expr == Types.INT) {
                this.writer.println("ITOF");
            } else {
                this.semanticalError(
                        "Atribuição de valor [" + tipo_expr + "] numa variável do tipo [" + var.type + "]");

            }
        }
        if (var.type == Types.INT) {
            stack_position = this.get_intvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
        if (var.type == Types.FLOAT) {
            stack_position = this.get_floatvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
        if (var.type == Types.STRING) {
            for (StringVariable cv : this.string_variables)
                if (cv.getName().equals(var.getName()))
                    cv.value = this.sum_string;
        }
        this.sum_string = "";

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
        } catch (Exception e) {
        }
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
        int stack_position;
        eatToken(TagEnums.READ);
        eatToken(TagEnums.OPEN_PAR);
        Variable var = eatId();
        eatToken(TagEnums.CLOSE_PAR);

        this.writer.println("READ");
        if (var == null) this.semanticalError("Read variavel não declarada" + this.lexical_analyser.getLastToken().toString());
        if (var.type == Types.INT) {
            this.writer.println("ATOI");
            stack_position = this.get_intvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
        if (var.type == Types.FLOAT) {
            this.writer.println("ATOF");
            stack_position = this.get_floatvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
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

    private int eatExpression() throws IOException {
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;

        tipo_operando_atual = eatSimple_Expr();

        if (isRelOp(this.lexical_analyser.getLastToken())) {
            if (tipo_operando_atual == Types.STRING)
                this.semanticalError("Operador relacional somente pode ser usado com operandos numericos");

            tipo_operacao = this.lexical_analyser.getLastToken().tag;
            eatRelOp();

            tipo_novo_operando = eatSimple_Expr();
            if (tipo_novo_operando == Types.STRING)
                this.semanticalError("Operador relacional somente pode ser usado com operandos numericos");

            if (tipo_operando_atual == Types.INT && tipo_novo_operando == Types.INT) {
                switch (tipo_operacao) {
                    case TagEnums.EQ:
                        this.writer.println("EQUAL");
                        break;
                    case TagEnums.GT:
                        this.writer.println("SUP");
                        break;
                    case TagEnums.GE:
                        this.writer.println("SUPEQ");
                        break;
                    case TagEnums.LT:
                        this.writer.println("INF");
                        break;
                    case TagEnums.LE:
                        this.writer.println("INFEQ");
                        break;
                    case TagEnums.NE:
                        this.writer.println("EQUAL");
                        this.writer.println("NOT");
                        break;
                    default:
                        break;
                }
            } else if (tipo_operando_atual == Types.FLOAT && tipo_novo_operando == Types.FLOAT) {
                switch (tipo_operacao) {
                    case TagEnums.EQ:
                        this.writer.println("EQUAL");
                        break;
                    case TagEnums.GT:
                        this.writer.println("FSUP");
                        break;
                    case TagEnums.GE:
                        this.writer.println("FSUPEQ");
                        break;
                    case TagEnums.LT:
                        this.writer.println("FINF");
                        break;
                    case TagEnums.LE:
                        this.writer.println("FINFEQ");
                        break;
                    case TagEnums.NE:
                        this.writer.println("EQUAL");
                        this.writer.println("NOT");
                        break;
                    default:
                        break;
                }
            } else {
                this.semanticalError("Operador relacional requer ambos operandos reais ou inteiros");
            }
        }
        return tipo_operando_atual;
    }

    private static boolean isSEOp(Token t) throws IOException {
        int[] values = new int[] { TagEnums.ADD, TagEnums.SUB, TagEnums.OR };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private int eatSimple_Expr() throws IOException {
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;
        tipo_operando_atual = eatTerm();

        while (isSEOp(this.lexical_analyser.getLastToken())) {
            tipo_operacao = this.lexical_analyser.getLastToken().tag;
            eatAddOp();

            if (tipo_operacao == TagEnums.OR) {
                this.writer.println("NOT");
            }

            tipo_novo_operando = eatTerm();

            if (tipo_operacao == TagEnums.ADD || tipo_operacao == TagEnums.SUB) {
                if (tipo_operando_atual == Types.INT && tipo_novo_operando == Types.INT) {
                    tipo_operando_atual = Types.INT;
                    switch (tipo_operacao) {
                        case TagEnums.ADD:
                            this.writer.println("ADD");
                            break;
                        case TagEnums.SUB:
                            this.writer.println("SUB");
                            break;
                        default:
                            break;
                    }
                } else if (tipo_operando_atual == Types.FLOAT && tipo_novo_operando == Types.FLOAT) {
                    tipo_operando_atual = Types.FLOAT;
                    switch (tipo_operacao) {
                        case TagEnums.ADD:
                            this.writer.println("FADD");
                            break;
                        case TagEnums.SUB:
                            this.writer.println("FSUB");
                            break;
                        default:
                            break;
                    }
                }
            }

            if (tipo_operacao == TagEnums.OR) {
                this.writer.println("NOT");
                this.writer.println("MUL");
                this.writer.println("NOT");
            }
        }
        return tipo_operando_atual;
    }

    private static boolean isTermOp(Token t) throws IOException {
        int[] values = new int[] { TagEnums.MUL, TagEnums.DIV, TagEnums.AND };

        return Arrays.stream(values).anyMatch(i -> i == t.tag);
    }

    private int eatTerm() throws IOException {
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;

        tipo_operando_atual = eatFactor_A();

        while (isTermOp(this.lexical_analyser.getLastToken())) {
            tipo_operacao = this.lexical_analyser.getLastToken().tag;
            eatMulOp();

            tipo_novo_operando = eatFactor_A();

            if (tipo_operacao == TagEnums.MUL || tipo_operacao == TagEnums.DIV) {

                if (tipo_operando_atual == Types.STRING || tipo_novo_operando == Types.STRING) {
                    this.semanticalError(
                            "Valor do tipo String não pode ser usado como operando numa expressão numérica");
                }

                if (tipo_operando_atual == Types.INT && tipo_novo_operando == Types.INT) {
                    tipo_operando_atual = Types.INT;
                    switch (tipo_operacao) {
                        case TagEnums.MUL:
                            this.writer.println("MUL");
                            break;
                        case TagEnums.DIV:
                            this.writer.println("DIV");
                            break;
                        default:
                            break;
                    }
                } else if (tipo_operando_atual == TagEnums.FLOAT && tipo_novo_operando == TagEnums.FLOAT) {
                    tipo_operando_atual = TagEnums.FLOAT;
                    switch (tipo_operacao) {
                        case TagEnums.MUL:
                            this.writer.println("FMUL");
                            break;
                        case TagEnums.DIV:
                            this.writer.println("FDIV");
                            break;
                        default:
                            break;
                    }
                } else {
                    this.semanticalError("Ambos operando numéricos precisam inteiros ou reais");
                }
            } else {
                this.writer.println("MUL");
            }
        }
        return tipo_operando_atual;
    }

    private int eatFactor_A() throws IOException {
        int tipo_id;
        int sinal = this.lexical_analyser.getLastToken().tag;
        if (this.lexical_analyser.getLastToken().tag == TagEnums.NOT) {
            eatToken(TagEnums.NOT);
        } else if (this.lexical_analyser.getLastToken().tag == TagEnums.SUB) {
            eatToken(TagEnums.SUB);
        }

        tipo_id = eatFactor();

        if (sinal == TagEnums.NOT) {
            this.writer.println("NOT");
        }
        if (sinal == TagEnums.SUB) {
            if (tipo_id == Types.INT) {
                this.writer.println("PUSHI -1");
                this.writer.println("MUL");
            }
            if (tipo_id == Types.FLOAT) {
                this.writer.println("PUSHF -1.0");
                this.writer.println("FMUL");
            }
        }
        return tipo_id;
    }

    private void semanticalError(String error) throws IOException {
        throw new IOException("linha " + this.lexical_analyser.getLines() + ": Erro Semântico - " + error);
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

    private String get_StringVar(String varName) {
        for (StringVariable cv : this.string_variables)
            if (cv.getName().equals(varName)) {
                return cv.value;
            }
        return "";
    }

    private int eatFactor() throws IOException {
        int stack_position;
        int tipo_id = 0;

        if (this.lexical_analyser.getLastToken().tag == TagEnums.ID) {
            String idName = this.lexical_analyser.getLastToken().toString();
            Variable var = this.variables.get(idName);
            if (var == null)
                this.semanticalError("Variável utilizada antes da sua declaração [" + idName + "]");

            tipo_id = var.type;
            if (var.type == Types.INT) {
                stack_position = this.get_intvar_index(var.getName());
                this.writer.println("PUSHL " + stack_position);
            }
            if (var.type == Types.FLOAT) {
                stack_position = this.get_floatvar_index(var.getName());
                this.writer.println("PUSHL " + stack_position);
            }
            if (var.type == Types.STRING)
                this.sum_string += this.get_StringVar(var.getName());

            eatToken(TagEnums.ID);
        }

        if (this.lexical_analyser.getLastToken().tag == TagEnums.NUM
                || this.lexical_analyser.getLastToken().tag == TagEnums.STRING_VALUE) {
            return eatConstant();
        }
        if (this.lexical_analyser.getLastToken().tag == TagEnums.OPEN_PAR) {
            eatToken(TagEnums.OPEN_PAR);
            tipo_id = eatExpression();
            eatToken(TagEnums.CLOSE_PAR);
            return tipo_id;
        } 

        return tipo_id; 
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
        String s = this.lexical_analyser.getLastToken().toString();

        this.writer.println("PUSHS \"" + s + "\"");
        this.writer.println("WRITES");

        eatToken(TagEnums.STRING_VALUE);
    }
}
