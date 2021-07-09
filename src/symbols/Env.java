/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbols;

/**
 *
 * @author cristiano
 */
import java.util.*;
import tokens.Token;

public class Env {
    private HashMap<String, Token> table = new HashMap<String, Token>();
    protected Env prev;

    public Env(Env n) {
        prev = n;
    }

    public void put(String identifier, Token t) {
        table.put(identifier, t);
    }

    public Token get(String identifier) {
        for (Env e = this; e != null; e = e.prev) {
            Token found = (Token) e.table.get(identifier);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public void imprime() {
        table.forEach((key,value) -> System.out.println("id: " + key + " = " + "padrão: " + value.toString() + " tag: " + value.tag));
    }
}
