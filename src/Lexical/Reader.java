/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexical;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

/**
 *
 * @author cristiano
 */
public class Reader {

    private PushbackReader file;
    private char currentChar = ' ';
    private String readedText;

    public Reader(String fileName) throws FileNotFoundException {
        try {
            this.file = new PushbackReader(new FileReader(fileName));
            this.readedText = "";
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");

            throw e;
        }
    }

    public char readCh() throws IOException {
        this.currentChar = (char) file.read();
        this.readedText = readedText.concat(String.valueOf(currentChar));

        return this.getCh();
    }

    public boolean readCh(char c) throws IOException {
        this.currentChar = this.readCh();

        if (this.getCh() != c) {
            this.file.unread(this.currentChar);
            
            this.readedText = this.readedText.substring(0, this.readedText.length());
            
            return false;
        }

        this.currentChar = ' ';
        
        return true;
    }

    /**
     * @return the ch
     */
    public char getCh() {
        return currentChar;
    }

    public void unRead() throws IOException {
        this.file.unread(this.getCh());
    }

    public int getCharIndex() {
        return this.readedText.length() + 1;
    }
}
