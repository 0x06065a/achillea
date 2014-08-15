package ru.stereohorse.cinimex.achillea.model;


import com.google.common.base.Strings;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;


public class XsdSchema {
    private static final String ERR_FMT_IMPORT = "Cannot import [%s] from [%s]";
    private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";

    private static final List<String> BUILT_IN_DATA_TYPES = Arrays.asList("string", "boolean", "decimal", "float",
            "double", "duration", "dateTime", "time", "date", "gYearMonth", "gYear", "gMonthDay", "gDay", "gMonth",
            "hexBinary", "base64Binary", "anyURI", "anyType", "QName", "NOTATION", "normalizedString", "token", "language",
            "NMTOKEN", "NMTOKENS", "Name", "NCName", "ID", "IDREF", "IDREFS", "ENTITY", "ENTITIES", "integer",
            "nonPositiveInteger", "negativeInteger", "long", "int", "short", "byte", "nonNegativeInteger",
            "unsignedLong", "unsignedInt", "unsignedShort", "unsignedByte", "positiveInteger");

    private final XmlNode root = new XmlNode(this);
    private final List<XmlNode> imports = new ArrayList<>();
    private final Map<String, List<String>> namespaces = new HashMap<>();
    private final List<XsdSchema> linkedSchemas = new ArrayList<>();
    private final Map<String, XmlNode> xmlTypes = new HashMap<>();

    private final List<String> nsPrefixes;
    private String tnsPrefix;

    private final File file;

    @SuppressWarnings("unchecked")
    public XsdSchema(File file) throws IOException, XMLStreamException {
        this(file, Collections.EMPTY_LIST);
    }

    private XsdSchema(File file, List<String> nsPrefixes) throws IOException, XMLStreamException {
        this.nsPrefixes = nsPrefixes;
        this.file = file;

        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(input);
            try {
                root.setChildren(parse(xmlEventReader, null));
            }  finally {
                xmlEventReader.close();
            }
        }

        for (XmlNode imp : imports) {
            String schemaLocation = imp.getAttribute(XmlNode.Attribute.SCHEMA_LOCATION);
            String nsURI = imp.getAttribute(XmlNode.Attribute.NAMESPACE);
            List<String> linkedSchemaNsPrefixes = namespaces.get(nsURI);

            if (Strings.isNullOrEmpty(schemaLocation)) {
                continue;
            }

            try {
                linkedSchemas.add(new XsdSchema(new File(file.getParent(), schemaLocation), linkedSchemaNsPrefixes));
            } catch (IOException | XMLStreamException e) {
                throw new RuntimeException(String.format(ERR_FMT_IMPORT, schemaLocation, file.getAbsolutePath()), e);
            }
        }

        String xsdNamespace = namespaces.get(XSD_URI).get(0);
        for (String type : BUILT_IN_DATA_TYPES) {
            xmlTypes.put(String.format("%s:%s", xsdNamespace, type), XmlNode.VOID);
        }
    }

    private List<XmlNode> parse(XMLEventReader xmlEventReader, XmlNode parent) throws XMLStreamException {
        List<XmlNode> nodes = Collections.emptyList();

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement()) {
                return nodes;
            } else if (xmlEvent.isCharacters() && parent != null) {
                parent.setTextValue(xmlEvent.asCharacters().getData());
            } else if (xmlEvent.isStartElement()) {
                XmlNode node = parse(xmlEvent);
                node.setChildren(parse(xmlEventReader, node));

                if (nodes.isEmpty()) {
                    nodes = new ArrayList<>();
                }
                nodes.add(node);
            }
        }

        return nodes;
    }

    private XmlNode parse(XMLEvent xmlEvent) {
        String tag = xmlEvent.asStartElement().getName().getLocalPart();
        XmlNode node = new XmlNode(this, tag);

        node.setAttributes(xmlEvent);
        node.setLine(xmlEvent.getLocation().getLineNumber());

        parseNamespaces(xmlEvent);

        if (tag != null) {
            switch (tag) {
                case XmlNode.Tag.COMPLEX_TYPE:
                case XmlNode.Tag.SIMPLE_TYPE:
                    String typeName = node.getAttribute(XmlNode.Attribute.NAME);

                    if (nsPrefixes.isEmpty()) {
                        registerType(node, tnsPrefix, typeName);
                    } else {
                        for (String nsPrefix : nsPrefixes) {
                            registerType(node, nsPrefix, typeName);
                        }
                    }
                    break;

                case XmlNode.Tag.IMPORT:
                    imports.add(node);
                    break;

                case XmlNode.Tag.SCHEMA:
                    List<String> tnsPrefixes = namespaces.get(node.getAttribute(XmlNode.Attribute.TNS));
                    if (tnsPrefixes != null && !tnsPrefixes.isEmpty()) {
                        tnsPrefix = tnsPrefixes.get(0);
                    }
                    break;
            }
        }

        return node;
    }

    private void registerType(XmlNode node, String prefix, String typeName) {
        xmlTypes.put(String.format("%s:%s", prefix, typeName), node);
    }

    private void parseNamespaces(XMLEvent xmlEvent) {
        Iterator namespaces = xmlEvent.asStartElement().getNamespaces();
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();

            String nsUri = ns.getNamespaceURI();
            if (this.namespaces.get(nsUri) == null) {
                this.namespaces.put(nsUri, new ArrayList<String>());
            }

            this.namespaces.get(nsUri).add(ns.getPrefix());
        }
    }

    public XmlNode getRoot() {
        return root;
    }

    public XmlNode getXmlType(List<String> aliases) {
        for (String alias : aliases) {
            XmlNode typeNode = xmlTypes.get(alias);
            if (typeNode != null) {
                return typeNode;
            }
        }

        for (XsdSchema schema : linkedSchemas) {
            XmlNode typeNode = schema.getXmlType(aliases);
            if (typeNode != null) {
                return typeNode;
            }
        }

        return null;
    }

    public XmlNode getNodeByNameAttribute(String name) {
        return root.getNodeByNameAttribute(name);
    }

    public File getFile() {
        return file;
    }

    public String getTnsPrefix() {
        return tnsPrefix;
    }

    public List<String> getNsPrefixes() {
        return nsPrefixes;
    }
}
