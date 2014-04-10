package ru.stereohorse.cinimex.achillea.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlElement {
    private String name;
    private List<XmlElement> children = new ArrayList<XmlElement>();
    private Map<String, String> attributes = new HashMap<String, String>();

    public XmlElement() {
    }

    public XmlElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<XmlElement> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        if (attributes.size() != 0) {
            sb.append("( ");
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                sb.append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\" ");
            }
            sb.append(')');
        }

        return sb.toString();
    }

    public String toTreeString() {
        return toString(new StringBuilder(), "").toString();
    }

    private StringBuilder toString(StringBuilder sb, String prefix) {
        sb.append(prefix).append(toString()).append('\n');

        for (XmlElement child : children) {
            sb = child.toString(sb, prefix + "  ");
        }

        return sb;
    }
}
