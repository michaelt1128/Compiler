public class Symbol {
    private boolean array;
    private String id;
    private Type type;
    private int arraySize;

    public Symbol(boolean array, String id, Type type, int arraySize) {
        this.array = array;
        this.id = id;
        this.type = type;
        this.arraySize = arraySize;
    }

    public Symbol(boolean array, String id, Type type) {
        this.array = array;
        this.id = id;
        this.type = type;
        this.arraySize = 0;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }
}
