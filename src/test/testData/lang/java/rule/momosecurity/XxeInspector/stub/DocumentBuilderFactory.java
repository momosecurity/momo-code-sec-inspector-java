package javax.xml.parsers;

public abstract class DocumentBuilderFactory {

    protected DocumentBuilderFactory () {
    }

    public static DocumentBuilderFactory newInstance() {
        return ;
    }

    public abstract void setFeature(String name, boolean value);

    public abstract DocumentBuilder newDocumentBuilder();
}