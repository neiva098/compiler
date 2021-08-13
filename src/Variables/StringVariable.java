package Variables;

import tokens.TagEnums;

public class StringVariable extends Variable {
    public String value;

    public StringVariable(String id) {
        super(id, TagEnums.STRING_TYPE);
    }
    public String getValue() {
        return value;
    }

    public String setValue(String value) {
        this.value = value;

        return this.value;
    }
}
