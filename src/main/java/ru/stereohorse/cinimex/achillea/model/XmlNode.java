package ru.stereohorse.cinimex.achillea.model;

import com.google.common.base.Strings;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;


public class XmlNode {
    private final XsdSchema schema;
    private final String tag;

    private String textValue;
    private int line;

    private List<XmlNode> children;
    private final Map<String, String> attributes;

    public static final XmlNode VOID = new XmlNode(
            null, Tag.VOID,
            Collections.<String, String>emptyMap(),
            Collections.<XmlNode>emptyList()
    );

    public XmlNode(XsdSchema schema) {
        this(schema, Tag.VOID);
    }

    public XmlNode(XsdSchema schema, String tag) {
        this(schema, tag, new HashMap<String, String>(), null);
    }

    private XmlNode(XsdSchema schema, String tag, Map<String, String> attributes, List<XmlNode> children) {
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

    public String getTag() {
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
            javax.xml.stream.events.Attribute attribute = (javax.xml.stream.events.Attribute) attributes.next();
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
                return condition.equals(node.attributes.get(XmlNode.Attribute.NAME));
            }
        });
    }

    public String getXmlLocalType() {
        String xmlType = getXmlType();
        if (xmlType == null) {
            return null;
        }

        return xmlType.replaceAll("^.*?:", "");
    }

    public static interface Predicate {
        public boolean satisfies(XmlNode node, String condition);
    }

    public String getXmlType() {
        String xmlType = attributes.get(XmlNode.Attribute.TYPE);
        if (Strings.isNullOrEmpty(xmlType)) {
            xmlType = attributes.get(XmlNode.Attribute.BASE);
        }

        if (Strings.isNullOrEmpty(xmlType)) {
            return null;
        }

        return xmlType;
    }

    public List<String> getCalculatedXmlTypes() {
        String xmlType = getXmlType();
        if (xmlType == null) {
            return Collections.emptyList();
        }

        String schemaTnsPrefix = schema.getTnsPrefix();
        if (xmlType.startsWith(schemaTnsPrefix + ":")) {
            if (schema.getNsPrefixes().isEmpty()) {
                return Arrays.asList(schema.getTnsPrefix() + xmlType.substring(schemaTnsPrefix.length()));
            } else {
                List<String> calculatedTypes = new ArrayList<>();
                for (String nsPrefix : schema.getNsPrefixes()) {
                    calculatedTypes.add(nsPrefix + xmlType.substring(schemaTnsPrefix.length()));
                }
                return calculatedTypes;
            }
        }

        return Arrays.asList(xmlType);
    }

    public static class Tag {
        public static final String ELEMENT = "element";
        public static final String COMPLEX_TYPE = "complexType";
        public static final String SIMPLE_TYPE = "simpleType";
        public static final String IMPORT = "import";
        public static final String SCHEMA = "schema";
        public static final String ATTRIBUTE = "attribute";
        public static final String SECTION = "section";
        public static final String DOCUMENTATION = "documentation";
        public static final String CHOICE = "choice";
        private static final String VOID = "";
    }

    public static class Attribute {
        public static final String NAME = "name";
        public static final String SCHEMA_LOCATION = "schemaLocation";
        public static final String BASE = "base";
        public static final String NAMESPACE = "namespace";
        public static final String TYPE = "type";
        public static final String TNS = "targetNamespace";
        public static final String MIN_OCCURS = "minOccurs";
        public static final String MAX_OCCURS = "maxOccurs";
    }
}
