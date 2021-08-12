package Variables;

import tokens.TagEnums;

public class Char extends Variable {
    public char value;

    public Char(String id) {
        super(id, TagEnums.STRING_TYPE);
    }

    public char getValue() {
        return value;
    }

    public char setValue(char value) {
        this.value = value;

        return this.value;
    }
}
