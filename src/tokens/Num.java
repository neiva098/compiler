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
public class Num extends Token {

    private final int value;

    public Num(int value) {
        super(TagEnums.NUM);
        
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public String toString() {
        return "" + value;
    }

}
