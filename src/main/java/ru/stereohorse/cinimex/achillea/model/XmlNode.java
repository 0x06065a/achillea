package ru.stereohorse.cinimex.achillea.model;

import com.google.common.base.Strings;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;


public class XmlNode {
    private final XmlTag tag;
    private int line;
    private List<XmlNode> children;
    private final Map<String, String> attributes;
    private final XsdSchema schema;
    private String textValue;

    public static final XmlNode EXTERNAL = new XmlNode(
            null, null,
            Collections.<String, String>emptyMap(),
            Collections.<XmlNode>emptyList()
    );

    public XmlNode(XsdSchema schema) {
        this(schema, new XmlTag());
    }

    public XmlNode(XsdSchema schema, XmlTag tag) {
        this(schema, tag, new HashMap<String, String>(), null);
    }

    private XmlNode(XsdSchema schema, XmlTag tag, Map<String, String> attributes, List<XmlNode> children) {
        this.tag = tag;
        this.schema = schema;
        this.attributes = attributes;
        this.children = children;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public XsdSchema getSchema() {
        return schema;
    }

    public XmlTag getTag() {
        return tag;
    }

    public String getFormattedTextValue() {
        if (textValue == null) {
            return null;
        }

        return textValue.replaceAll("\\s*\\n+\\s*", " ").replace(";", ",").trim();
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public void setChildren(List<XmlNode> children) {
        this.children = children;
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public String getAttribute(String name, String defaultVal) {
        String val = attributes.get(name);
        if (Strings.isNullOrEmpty(val)) {
            return defaultVal;
        }

        return val;
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
        sb.append(tag);

        if (attributes.size() != 0) {
            sb.append("( ");
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                sb.append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\" ");
            }
            sb.append(')');
        }

        return sb.toString();
    }

    private XmlNode getNode(String condition, Predicate predicate) {
        if (Strings.isNullOrEmpty(condition)) {
            return null;
        }

        if (predicate.satisfies(this, condition)) {
            return this;
        }

        for (XmlNode child : children) {
            XmlNode node = child.getNode(condition, predicate);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

    public XmlNode getNodeByNameAttribute(String name) {
        return getNode(name, new Predicate() {
            @Override
            public boolean satisfies(XmlNode node, String condition) {
                return condition.equals(node.attributes.get(XmlAttribute.NAME));
            }
        });
    }

    public String getXmlLocalType() {
        String xmlType = getXmlType();
        if (xmlType == null) {
            return null;
        }

        return xmlType.replaceAll(".*?:", "");
    }

    public static interface Predicate {
        public boolean satisfies(XmlNode node, String condition);
    }

    public String getXmlType() {
        String xmlType = attributes.get(XmlAttribute.TYPE);
        if (Strings.isNullOrEmpty(xmlType)) {
            xmlType = attributes.get(XmlAttribute.BASE);
        }

        if (Strings.isNullOrEmpty(xmlType)) {
            return null;
        }

        return xmlType;
    }

    public String getCalculatedXmlType() {
        String xmlType = getXmlType();
        if (xmlType == null) {
            return null;
        }

        String schemaTnsPrefix = schema.getTnsPrefix();
        if (xmlType.startsWith(schemaTnsPrefix + ":")) {
            String prefix = Strings.isNullOrEmpty(schema.getNsPrefix()) ? schema.getTnsPrefix() : schema.getNsPrefix();
            return prefix + xmlType.substring(schemaTnsPrefix.length());
        }

        return xmlType;
    }
}
