package Variables;

import tokens.TagEnums;

public class Float extends Variable {
    public float value;

    public Float(String id) {
        super(id, TagEnums.FLOAT);
    }

    public float getValue() {
        return value;
    }

    public float setValue(float value) {
        this.value = value;

        return this.value;
    }
}
