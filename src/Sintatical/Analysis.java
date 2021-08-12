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
    private PrintWriter writer;
    private int ifs_counter, whiles_counter, repeats_counter = 0;
    private String temp_str;

    public HashMap<String, Variable> variables;
    public ArrayList<Int> int_variables;
    public ArrayList<Float> float_variables;
    public ArrayList<Char> char_variables;

    public Analysis(Lexical.Analysis lexical_analyser) throws IOException {
        this.lexical_analyser = lexical_analyser;
        this.currentToken = lexical_analyser.proxyScan();
        this.writer = new PrintWriter("assembly-code.vm", "UTF-8");

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

        this.writer.println("START");

        procBody();

        matchToken(TagEnums.END_OF_FILE);

        this.writer.println("STOP");

        this.writer.close();
    }

    private static boolean isDeclaration(Token t)  throws IOException {
        int[] values = new int[] { TagEnums.INT, TagEnums.FLOAT, TagEnums.STRING_TYPE, TagEnums.CARACTERE};

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
            System.out.println("Esperava-se o token de tipo DECLARE ou BEGIN");
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

        if (!this.int_variables.isEmpty())
            writer.println("PUSHN " + this.int_variables.size());

        for (Float fv : this.float_variables)
            writer.println("PUSHF 0.0");
    }

    private void procDecl() throws IOException {
        this.currentLine = this.lexical_analyser.getLines();

        int type = procType();

        // matchToken(currentToken.tag);

        ArrayList<String> idList = procIdent_List();

        // matchToken(TagEnums.SEMICOLON);
        

        for (String s : idList) {
            if (this.variables.containsKey(s)) {
                System.out.println("linha " + this.currentLine
                        + ": Erro Semântico - Variável não pode ser declarada com o nome de outra \"" + s + "\"");
                System.exit(1);
            }
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
            if (currentToken.tag == TagEnums.STOP) break;
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
        if (var == null) {
            System.out.println("linha " + this.currentLine
                    + ": Erro Semântico - Variável utilizada antes da sua declaração [" + idName + "]");
            System.exit(1);
        }
        return var;
    }

    // <assign-stmt> ::= id "=" <simple_expr>
    private void procAssign_Stmt() throws IOException {
        int stack_position;
        this.currentLine = this.lexical_analyser.getLines();
        Variable var = procId();
        matchToken(TagEnums.ASSIGN);
        int tipo_expr = procSimple_Expr();

        if (var.type == tipo_expr) {
            // Tipos adequados, operacao de atribuicao com sucesso
        } else {
            if (var.type == 261 && tipo_expr == 260) {
                this.writer.println("ITOF");
                // Excecao aceita
            } else {
                // Erro
                System.out.println("linha " + this.currentLine + ": Erro Semântico - Atribuição de valor [" + tipo_expr
                        + "] numa variável do tipo [" + var.type + "]");
                System.exit(1);

            }
        }
        if (var.type == 260) {
            stack_position = this.get_intvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
        if (var.type == 261) {
            stack_position = this.get_floatvar_index(var.getName());
            this.writer.println("STOREL " + stack_position);
        }
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
        int this_counter = ifs_counter;

        this.writer.println("IF" + this_counter + ":");

        matchToken(TagEnums.IF);
        procCondition();

        this.writer.println("JZ ELSE" + this_counter);

        // matchToken(TagEnums.THEN);
        procStmt_List();

        this.writer.println("JUMP ENDIF" + this_counter);

        this.writer.println("ELSE" + this_counter + ":");

        if (currentToken.tag == TagEnums.ELSE) {
            this.currentLine = this.lexical_analyser.getLines();
            matchToken(TagEnums.ELSE);
            procStmt_List();
        }
        this.currentLine = this.lexical_analyser.getLines();
        matchToken(TagEnums.STOP);

        this.writer.println("ENDIF" + this_counter + ":");
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

        this.writer.println("REPEAT" + this_counter + ":");

        procStmt_List();
        procStmt_Suffix();
        this.writer.println("JZ REPEAT" + this_counter);
        this.writer.println("ENDREPEAT" + this_counter + ":");
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

        this.writer.println("WHILE" + this_counter + ":");

        procStmt_Prefix();

        this.writer.println("JZ ENDWHILE" + this_counter);

        procStmt_List();

        this.writer.println("JUMP WHILE" + this_counter);

        matchToken(TagEnums.STOP);

        this.writer.println("ENDWHILE" + this_counter + ":");
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
        int stack_position;
        matchToken(TagEnums.READ);
        matchToken(TagEnums.OPEN_PAR);
        Variable var = procId();
        matchToken(TagEnums.CLOSE_PAR);
        this.writer.println("READ");
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
        int type;
        if (currentToken.tag == TagEnums.ID) {
            type = procSimple_Expr();
            if (type == Types.INT)
                this.writer.println("WRITEI");
            if (type == Types.FLOAT)
                this.writer.println("WRITEF");
            // if (type == Types.BOOLEAN)
            //     this.writer.println("WRITEI");
            if (type == Types.CHAR) {
                this.writer.println("PUSHS \"" + this.temp_str + "\"");
                this.writer.println("WRITES");
            }
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
    private int procExpression() throws IOException {
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;

        tipo_operando_atual = procSimple_Expr();

        if (currentToken.tag == TagEnums.EQ || currentToken.tag == TagEnums.GT
                || currentToken.tag == TagEnums.GE || currentToken.tag == TagEnums.LT
                || currentToken.tag == TagEnums.LE || currentToken.tag == TagEnums.NE) {

            if (tipo_operando_atual == Types.CHAR) {
                System.out.println("linha " + this.currentLine
                        + ": Erro Semântico - Operador relacional somente pode ser usado com operandos numericos");
                System.exit(1);
            }

            tipo_operacao = this.currentToken.tag;

            procRelOp();

            tipo_novo_operando = procSimple_Expr();

            if (tipo_novo_operando == Types.CHAR) {
                System.out.println("linha " + this.currentLine
                        + ": Erro Semântico - Operador relacional somente pode ser usado com operandos numéricos");
                System.exit(1);
            }

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
                System.out.println("linha " + this.currentLine
                        + ": Erro Semântico - Operador relacional requer ambos operandos reais ou inteiros");
                System.exit(1);
            }

            return Types.BOOLEAN;
        }
        return tipo_operando_atual;
    }

    // <simple-expr> ::= <term> { <addop> <term> }
    private int procSimple_Expr() throws IOException {
        /**
         * É possível termos as expressoes de tipos INT = INT ( + | - | * ) INT FLOAT =
         * INT / INT FLOAT = FLOAT ( + | - | * | / ) INT FLOAT = INT ( + | - | * | / )
         * FLOAT FLOAT = FLOAT ( + | - | * | / ) FLOAT
         */
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;
        tipo_operando_atual = procTerm();

        while (currentToken.tag == TagEnums.ADD || currentToken.tag == TagEnums.SUB
                || currentToken.tag == TagEnums.OR) {

            tipo_operacao = this.currentToken.tag;
            procAddOp();

            if (tipo_operacao == TagEnums.OR) {
                this.writer.println("NOT");
            }

            tipo_novo_operando = procTerm();

            if (tipo_operacao == TagEnums.ADD || tipo_operacao == TagEnums.SUB) {

                if (tipo_operando_atual == Types.CHAR || tipo_novo_operando == Types.CHAR) {
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Valor do tipo CHAR não pode ser usado como operando numa expressão numérica");
                    System.exit(1);
                } else if (tipo_operando_atual == Types.BOOLEAN || tipo_novo_operando == Types.BOOLEAN) {
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Valor do tipo BOOLEAN não pode ser usado como operando numa expressão numérica");
                    System.exit(1);
                } else if (tipo_operando_atual == Types.INT && tipo_novo_operando == Types.INT) {
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
                } else {
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Ambos operando numéricos precisam inteiros ou reais");
                    System.exit(1);
                }
            } else {
                if (tipo_operando_atual != Types.BOOLEAN || tipo_novo_operando != Types.BOOLEAN) {
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Operador booleano OR somente pode ser usado com operandos booleanos");
                    System.exit(1);
                }
                this.writer.println("OR");
            }

            if (tipo_operacao == TagEnums.OR) {
                this.writer.println("NOT");
                this.writer.println("MUL");
                this.writer.println("NOT");
            }
        }
        return tipo_operando_atual;
    }

    // <term> ::= <factor-a> { <mulop> <factor-a> }
    private int procTerm() throws IOException {
        int tipo_operando_atual, tipo_novo_operando;
        int tipo_operacao;
        tipo_operando_atual = procFactor_A();

        while (currentToken.tag == TagEnums.MUL || currentToken.tag == TagEnums.DIV
                || currentToken.tag == TagEnums.AND) {

            tipo_operacao = this.currentToken.tag;
            procMulOp();

            tipo_novo_operando = procFactor_A();

            if (tipo_operacao == TagEnums.MUL || tipo_operacao == TagEnums.DIV) {

                if (tipo_operando_atual == Types.CHAR || tipo_novo_operando == Types.CHAR) {
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Valor do tipo CHAR não pode ser usado como operando numa expressão numérica");
                    System.exit(1);
                }
                if (tipo_operando_atual == Types.BOOLEAN || tipo_novo_operando == Types.BOOLEAN) {
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Valor do tipo BOOLEAN não pode ser usado como operando numa expressão numérica");
                    System.exit(1);
                } else if (tipo_operando_atual == Types.INT && tipo_novo_operando == Types.INT) {
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
                } else if (tipo_operando_atual == Types.FLOAT && tipo_novo_operando == Types.FLOAT) {
                    tipo_operando_atual = Types.FLOAT;
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
                    // Erro
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Ambos operando numéricos precisam inteiros ou reais");
                    System.exit(1);
                }
            } else {
                if (tipo_operando_atual != Types.BOOLEAN || tipo_novo_operando != Types.BOOLEAN) {
                    System.out.println("linha " + this.currentLine
                            + ": Erro Semântico - Operador booleano AND somente pode ser usado com operandos booleanos");
                    System.exit(1);
                }
                this.writer.println("MUL");
            }
        }
        return tipo_operando_atual;
    }

    // <factor-a> ::= ["!" | "-"] <factor>
    private int procFactor_A() throws IOException {
        int type;
        int sinal = currentToken.tag;
        if (currentToken.tag == TagEnums.NOT) {
            matchToken(TagEnums.NOT);
        } else if (currentToken.tag == TagEnums.SUB) {
            matchToken(TagEnums.SUB);
        } else {
            // faca nada
        }

        type = procFactor();

        if (sinal == TagEnums.NOT) {
            this.writer.println("NOT");
        }
        if (sinal == TagEnums.SUB) {
            if (type == Types.INT) {
                this.writer.println("PUSHI -1");
                this.writer.println("MUL");
            }
            if (type == Types.FLOAT) {
                this.writer.println("PUSHF -1.0");
                this.writer.println("FMUL");
            }
        }
        return type;
    }

    // <factor> ::= id | <constant> | "(" <expression> ")"
    private int procFactor() throws IOException {
        int stack_position;
        char ch;
        int type;
        if (currentToken.tag == TagEnums.ID) {
            String idName = this.currentToken.toString();
            Variable var = this.variables.get(idName);
            if (var == null) {
                System.out.println("linha " + this.currentLine
                        + ": Erro Semântico - Variável utilizada antes da sua declaração [" + idName + "]");
                System.exit(1);
            }
            type = var.type;
            if (var.type == 260) {
                stack_position = this.get_intvar_index(var.getName());
                this.writer.println("PUSHL " + stack_position);
            }
            if (var.type == 261) {
                stack_position = this.get_floatvar_index(var.getName());
                this.writer.println("PUSHL " + stack_position);
            }
            if (var.type == 295)
                this.temp_str += this.get_charvar_char(var.getName());

            matchToken(TagEnums.ID);
        } else if (currentToken.tag == TagEnums.NUM || currentToken.tag == TagEnums.CARACTERE) {
            type = procConstant();
        } else if (currentToken.tag == TagEnums.OPEN_PAR) {
            matchToken(TagEnums.OPEN_PAR);
            type = procExpression();
            matchToken(TagEnums.CLOSE_PAR);
        } else {
            System.out.println("linha " + this.currentLine + ": Erro Sintático - Lexema não esperado ["
                    + this.currentToken.toString() + "] do tipo " + this.currentToken.tag);
            System.out.println("Esperava-se o token de tipo NUM, CARACTERE ou OPEN_PAR");
            System.exit(1);
            return 0;
        }
        return type;
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

                this.writer.println("PUSHF " + number);
                return type;
            }
            this.writer.println("PUSHI " + number);
        } else { // char const
            type = 295;
            temp_str += this.currentToken.toString();
            matchToken(295);
        }
        return type;
    }

    // <literal> ::= string
    private void procLiteral() throws IOException {
        String s = this.currentToken.toString();
        this.writer.println("PUSHS \"" + s + "\"");
        this.writer.println("WRITES");
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
