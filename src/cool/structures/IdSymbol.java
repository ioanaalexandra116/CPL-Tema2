package cool.structures;

public class IdSymbol extends Symbol {
    protected TypeSymbol type;

    public IdSymbol(String name) {
        super(name);
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    public IdSymbol getParentSymbol() {
        if (this.type == null) {
            return null;
        }
        IdSymbol parentSymbol = new IdSymbol(this.type.getParentName());
        return parentSymbol;
    }

}