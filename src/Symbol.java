public class Symbol {
    private boolean array;
    private String id;
    private String value;
    private int arraySize;

    public Symbol(boolean array, String id, String value, int arraySize) {
        this.array = array;
        this.id = id;
        this.value = value;
        this.arraySize = arraySize;
    }

    public Symbol(boolean array, String id, String value) {
        this.array = array;
        this.id = id;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }
}
