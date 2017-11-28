import java.util.ArrayList;

public class Symbol {
    private boolean array;
    private String id;
    private Type type;
    private int arraySize;
    private ArrayList<Symbol> parameterTypes;

    public ArrayList<Symbol> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(ArrayList<Symbol> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }


    public Symbol(boolean array, String id, Type type, int arraySize) {
        this.array = array;
        this.id = id;
        this.type = type;
        this.arraySize = arraySize;
        this.parameterTypes = new ArrayList<Symbol>();
    }

    public Symbol(boolean array, String id, Type type) {
        this.array = array;
        this.id = id;
        this.type = type;
        this.arraySize = 0;
        this.parameterTypes = new ArrayList<Symbol>();
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
