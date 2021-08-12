package Variables;

import tokens.TagEnums;

public class Int extends Variable {
    public int value;
    
    public Int(String name) {
        super(name, TagEnums.INT);
    }

    public int getValue() {
        return value;
    }

    public int setValue(int value) {
        this.value = value;

        return this.value;
    }
}
