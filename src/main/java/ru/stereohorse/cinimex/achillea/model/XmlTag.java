package ru.stereohorse.cinimex.achillea.model;


public enum XmlTag {
    COMPLEX_TYPE("complexType"),
    IMPORT("import"),
    ATTRIBUTE("attribute"),
    ELEMENT("element"),
    SECTION("section"),
    EXTENSION("extension"),
    SCHEMA("schema"),
    DOCUMENTATION("documentation"),
    CHOICE("choice"),
    UNKNOWN("");

    private final String name;

    private XmlTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static XmlTag of(String name) {
        for (XmlTag tag : XmlTag.values()) {
            if (tag.getName().equals(name)) {
                return tag;
            }
        }

        return UNKNOWN;
    }

    public boolean isComplexTypeDeclaration(XmlNode parentNode) {
        return this == XmlTag.COMPLEX_TYPE && parentNode.getTag() != XmlTag.ELEMENT;
    }
}
