package Variables;

public class Variable {
    public int type;
    private String name;

    public Variable(String name, int type) {
        this.type = type;
        this.name = name;  
    }

    public String getName(){
        return name;
    }
}
