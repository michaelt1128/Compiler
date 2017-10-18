public class Token {
    private String value = "";
    private Type type;

    Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    Token(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    void print(int blockDepth) {
        if (!this.type.equals(Type.COMMENT)) {
            if (blockDepth > 0 && this.type.equals(Type.ID)) {
                System.out.println(this.type + ": '" + this.value + "' depth: " + blockDepth);
            } else {
                System.out.println(this.type + ": '" + this.value + "'");
            }
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
