/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokens;

/**
 *
 * @author cristiano
 */
public class Word extends Token {
    private String lexeme = "";
    
    public Word(String lexeme, int tag) {
        super(tag);
        
        this.lexeme = lexeme;
    }


    public String toString() {
        return lexeme;
    }
}
