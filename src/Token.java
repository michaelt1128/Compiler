public class Token {
    private String value = "";
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Token(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public void print() {
        if (!this.type.equals(Type.COMMENT)) {
            System.out.println(this.type + ": '" + this.value + "'");
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
