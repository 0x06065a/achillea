package ru.stereohorse.cinimex.achillea.model;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

public class XmlNode {
    private String name;
    private List<XmlNode> children;
    private final Map<String, String> attributes = new HashMap<>();

    public XmlNode() {
    }

    public XmlNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<XmlNode> children) {
        this.children = children;
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(XMLEvent xmlEvent) {
        StartElement stElement = xmlEvent.asStartElement();
        Iterator attributes = stElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            this.attributes.put(attribute.getName().getLocalPart(), attribute.getValue());
        }
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

        for (XmlNode child : children) {
            sb = child.toString(sb, prefix + "  ");
        }

        return sb;
    }

    public Set<String> getAllNames() {
        Set<String> names = new HashSet<>();
        return getAllNames(names);
    }

    private Set<String> getAllNames(Set<String> names) {
        names.add(name);

        for (XmlNode child : children) {
            child.getAllNames(names);
        }

        return names;
    }
}
