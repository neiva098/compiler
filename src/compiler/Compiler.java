/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import Lexical.Analysis;
import Lexical.Reader;
import tokens.TagEnums;
import tokens.Token;
/**
 *
 * @author cristiano
 */
public class Compiler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Insira o caminho de um arquivo de teste");            
        } 
        
        try {
            Analysis la = new Analysis(new Reader(args[0]));
            
            System.out.println("Sequência de caracteres TokenType\n");

            Token lex = la.scan();
            System.out.println("Token: " + lex.toString() + "\t" + lex.tag);
            
            while(lex.tag != TagEnums.END_OF_FILE) {
                lex = la.scan();
                System.out.println("Token: "+ lex.toString() +"\t"+lex.tag);
            }

            la.getSt().imprime();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }      
    }
    
}
