/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import Lexical.Reader;

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
            Lexical.Analysis la = new Lexical.Analysis(new Reader(args[0]));
            
            Sintatical.Analysis sa = new Sintatical.Analysis(la);
            sa.run();
            System.out.println("Fim da análise"); 
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }  
    }
    
}
