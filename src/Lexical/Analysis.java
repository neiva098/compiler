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

    public Analysis(Reader fileReader) {
        this.st = new Env(null);
        this.fileReader = fileReader;
    }

    private static boolean isIgnoredDelimiter(char ch) {
        String[] values = new String[] { " ", "\t", "\r", "\b" };

        return Arrays.asList(values).contains(String.valueOf(ch));
    }

    private static boolean isBreakLine(char ch) {
        return ch == '\n';
    }

    private static boolean isEOFANSI(char ch) {
        return ch == 'ÿ';
    }

    private static boolean isEOF(char ch) {
        return isEOFANSI(ch) || ch == -1;
    }

    private Token handleNumbers(char ch) throws IOException {
        int value = 0;

        do {
            value = 10 * value + Character.digit(ch, 10);
            this.fileReader.readCh();
        } while (Character.isDigit(ch));

        return new Num(value);
    }

    private Token handleIdentifiers(char ch) throws IOException {
        StringBuffer sb = new StringBuffer();

        do {
            sb.append(ch);
            this.fileReader.readCh();
        } while (Character.isLetterOrDigit(ch));

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

        if (t != null) return t;

        return new Token(ch);
    }

    private static boolean isRelOp(char ch) {
        String[] values = new String[] { ">", "<", "=", "!" };

        return Arrays.asList(values).contains(String.valueOf(ch));
    }

    public Token scan() throws IOException {
        char ch = this.fileReader.readCh();

        while (isIgnoredDelimiter(ch))
            ch = this.fileReader.readCh();

        if (isBreakLine(ch))
            this.line++;

        if (isEOF(ch))
            throw new IOException("Unexpected EOF");

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

        Token t = new Token(ch);
        ch = ' ';
        return t;
    }

    private Token handleEComercial() throws IOException {
        if (this.fileReader.readCh('&'))
            return new Word("&&", TagEnums.AND);

        return new Token('&');
    }

    private Token handlePipe() throws IOException {
        if (this.fileReader.readCh('|'))
            return new Word("|", TagEnums.OR);

        return new Token('|');
    }

    private Token handleBar() throws IOException {
        if (this.fileReader.readCh('/')) {
            
            while (true) {
                char charInComment = fileReader.readCh();

                if (charInComment == '\n' || isEOF(charInComment)) break;
            }
            
            return new Word("//", TagEnums.COMMENT);
        }
            

        if (this.fileReader.readCh('*')) {
            while (true) {
                char charInComment = fileReader.readCh();

                if (charInComment == '*' && this.fileReader.readCh('/')) break;
            }

            return new Word("/*", TagEnums.COMMENT);
        }
            

        return new Token('/');
    }
}