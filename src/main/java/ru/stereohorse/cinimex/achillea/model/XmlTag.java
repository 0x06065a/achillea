package ru.stereohorse.cinimex.achillea.model;


public class XmlTag {
    public static final String ELEMENT = "element";
    public static final String COMPLEX_TYPE = "complexType";
    public static final String IMPORT = "import";
    public static final String SCHEMA = "schema";
    public static final String ATTRIBUTE = "attribute";
    public static final String SECTION = "section";
    public static final String EXTENSION = "extension";
    public static final String DOCUMENTATION = "documentation";
    public static final String CHOICE = "choice";

    private final String name;

    public XmlTag() {
        this(null);
    }

    public XmlTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isComplexTypeDeclaration(XmlNode parentNode) {
        return COMPLEX_TYPE.equals(getName()) && !ELEMENT.equals(parentNode.getTag().getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
