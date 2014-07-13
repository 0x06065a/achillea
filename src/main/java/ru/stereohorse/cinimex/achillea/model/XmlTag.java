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
    private static final String VOID = "";

    private final String name;

    public XmlTag() {
        this(VOID);
    }

    public XmlTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
