/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexical;

import java.io.IOException;
import java.util.Arrays;
import symbols.Env;
import tokens.Num;
import tokens.TagEnums;
import tokens.Token;
import tokens.Word;

/**
 *
 * @author cristiano
 */
public class Analysis {
    private int line = 1;
    private final Reader fileReader;
    private final Env st;
    private Token currentToken;

    public Analysis(Reader fileReader) {
        this.st = new Env(null);
        this.fileReader = fileReader;

        this.reservedWords();
    }

    private void reserve(Word w) {
        this.st.put(w.toString(), w);
    }

    public Env getSt() {
        return this.st;
    }

    private void reservedWords() {
        this.reserve(new Word("class", TagEnums.CLASS)); 
        this.reserve(new Word("init", TagEnums.INIT)); 
        this.reserve(new Word("stop", TagEnums.STOP)); 
        this.reserve(new Word("int", TagEnums.INT)); 
        this.reserve(new Word("float", TagEnums.FLOAT)); 
        this.reserve(new Word("string", TagEnums.STRING_TYPE)); 
        this.reserve(new Word("if", TagEnums.IF)); 
        this.reserve(new Word("else", TagEnums.ELSE)); 
        this.reserve(new Word("while", TagEnums.WHILE)); 
        this.reserve(new Word("do", TagEnums.DO)); 
        this.reserve(new Word("read", TagEnums.READ)); 
        this.reserve(new Word("write", TagEnums.WRITE)); 
        this.reserve(new Word("=", TagEnums.ASSIGN)); 
        this.reserve(new Word("==", TagEnums.EQ)); 
        this.reserve(new Word("!=", TagEnums.NE)); 
        this.reserve(new Word(">=", TagEnums.GE)); 
        this.reserve(new Word(">", TagEnums.GT)); 
        this.reserve(new Word("<=", TagEnums.LE)); 
        this.reserve(new Word("<", TagEnums.LT)); 
        this.reserve(new Word("+", TagEnums.ADD)); 
        this.reserve(new Word("-", TagEnums.SUB)); 
        this.reserve(new Word("*", TagEnums.MUL)); 
        this.reserve(new Word("/", TagEnums.DIV)); 
        this.reserve(new Word("&&", TagEnums.AND)); 
        this.reserve(new Word("||", TagEnums.OR)); 
        this.reserve(new Word("!", TagEnums.NOT));  
        this.reserve(new Word(",", TagEnums.COMMA));  
        this.reserve(new Word(".", TagEnums.DOT)); 
        this.reserve(new Word(";", TagEnums.SEMICOLON)); 
        this.reserve(new Word(":", TagEnums.COLON)); 
        this.reserve(new Word("(", TagEnums.OPEN_PAR)); 
        this.reserve(new Word(")", TagEnums.CLOSE_PAR)); 
        this.reserve(new Word("{", TagEnums.OPEN_BRA)); 
        this.reserve(new Word("}", TagEnums.CLOSE_BRA)); 
        this.reserve(new Word("", TagEnums.INVALID_TOKEN));
        this.reserve(new Word("", TagEnums.END_OF_FILE)); 
        this.reserve(new Word("", TagEnums.UNEXPECTED_EOF));
    }

    private static boolean isIgnoredDelimiter(char ch) {
        String[] values = new String[] { " ", "\t", "\r", "\b"};


        return Arrays.asList(values).contains(String.valueOf(ch));
    }

    private static boolean isBreakLine(char ch) {
        int intValueOfCh = ch;
        return ch == '\n' || intValueOfCh == 10;
    }

    private static boolean isEOFANSI(char ch) {
        return ch == 'ÿ';
    }

    private static boolean isEOF(char ch) {
        return isEOFANSI(ch) || ch == -1 || ch == 65535;
    }

    private Token handleNumbers(char ch) throws IOException {
        int value = 0;

        do {
            value = 10 * value + Character.digit(this.fileReader.getCh(), 10);
            this.fileReader.readCh();
        } while (Character.isDigit(this.fileReader.getCh()));

        this.fileReader.unRead();

        return new Num(value);
    }

    private Token handleIdentifiers(char ch) throws IOException {
        StringBuffer sb = new StringBuffer();

        while (Character.isLetterOrDigit(this.fileReader.getCh()) ||  this.fileReader.getCh() == '_') {
            sb.append(this.fileReader.getCh());
            
            this.fileReader.readCh();
        }

        this.fileReader.unRead();

        String s = sb.toString();
        Word w = (Word) st.get(s);

        if (w != null)
            return w;

        w = new Word(s, TagEnums.ID);
        st.put(s, w);

        return w;
    }

    private Token handleRelOp(char ch) throws IOException {
        String lex = String.valueOf(ch);

        if (this.fileReader.readCh('='))
            lex += '=';

        Token t = st.get(lex);

        if (t != null)
            return t;

        return new Token(ch);
    }

    private static boolean isRelOp(char ch) {
        String[] values = new String[] { ">", "<", "=", "!" };

        return Arrays.asList(values).contains(String.valueOf(ch));
    }

    public Token scan() throws IOException {
        char ch = this.fileReader.readCh();

        if (isIgnoredDelimiter(ch))
            return this.scan();

        if (isBreakLine(ch)) {
            this.line++;
            
            return this.scan();
        }

        if (isEOF(ch)) {
            return new Token(TagEnums.END_OF_FILE);
        }

        if (ch == '&')
            return this.handleEComercial();

        if (isRelOp(ch))
            return this.handleRelOp(ch);

        if (ch == '|')
            return this.handlePipe();

        if (ch == '/')
            return this.handleBar();

        if (Character.isDigit(ch))
            return this.handleNumbers(ch);

        if (Character.isLetter(ch))
            return this.handleIdentifiers((ch));

        if (ch == '"')
            return this.handleString();

        Token t = st.get(String.valueOf(ch));
        
        if(t != null){
            return t; 
        } 
        
        t = new Token(ch);
        st.put(String.valueOf(ch), t);          
        
        return t;
    }

    public void setLt(Token t) {
        this.currentToken = t;
    }

    public Token proxyScan() throws IOException {
        this.currentToken = this.scan();



        return this.currentToken;
    }

    private Token handleEComercial() throws IOException {
        if (this.fileReader.readCh('&'))
            return new Word("&&", TagEnums.AND);

        return new Token('&');
    }

    private Token handleString() throws IOException {
        StringBuffer sb = new StringBuffer();

        while (this.fileReader.readCh() != '"') {
            if (Analysis.isEOF(this.fileReader.getCh())) this.imprimeErro(new Word(sb.toString(), TagEnums.UNEXPECTED_EOF));
        
            sb.append(this.fileReader.getCh());
        }

        String s = sb.toString();
        Word w = (Word) st.get(s);

        if (w != null)
            return w;

        w = new Word(s, TagEnums.STRING_VALUE);
        st.put(s, w);

        return w;
    }

    private Token handlePipe() throws IOException {
        if (this.fileReader.readCh('|'))
            return new Word("||", TagEnums.OR);

        return new Token('|');
    }

    private Token handleBar() throws IOException {
        if (this.fileReader.readCh('/')) {

            while (true) {
                char charInComment = fileReader.readCh();

                if (charInComment == '\n' || isEOF(charInComment))
                    break;
            }

            return this.scan();
        }

        if (this.fileReader.readCh('*')) {
            while (true) {
                char charInComment = fileReader.readCh();

                if (isEOF(charInComment)) 
                    throw new IOException("[" + this.line + "]" + ": Erro Léxico - fim do arquivo inesperado");

                if (charInComment == '*' && this.fileReader.readCh('/'))
                    break;
            }

            return this.scan();
        }

        return new Word("/", TagEnums.DIV);
    }

    private void imprimeErro(Word w) throws IOException {
        throw new IOException("[" + this.line + "]" + ": Erro Léxico - token inválido -> " + w.toString());
    }

    public int getLines() {
        return this.line;
    }

    public Token getLastToken() {
        return this.currentToken;
    }
}
