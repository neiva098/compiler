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
    private char ch = ' ';

    public Reader(String fileName) throws FileNotFoundException {
        try {
            this.file = new PushbackReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");

            throw e;
        }
    }

    public char readCh() throws IOException {
        this.ch = (char) file.read();

        return this.getCh();
    }

    public boolean readCh(char c) throws IOException {
        this.ch = this.readCh();

        if (this.getCh() != c) {
            this.file.unread(this.ch);
            return false;
        }

        this.ch = ' ';
        
        return true;
    }

    /**
     * @return the ch
     */
    public char getCh() {
        return ch;
    }
}
