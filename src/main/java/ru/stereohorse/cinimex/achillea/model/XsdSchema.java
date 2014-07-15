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
    private final Map<String, String> namespaces = new HashMap<>();
    private final List<XsdSchema> linkedSchemas = new ArrayList<>();
    private final Map<String, XmlNode> xmlTypes = new HashMap<>();

    private final String nsPrefix;
    private String tnsPrefix;

    private final File file;

    public XsdSchema(File file) throws IOException, XMLStreamException {
        this(file, "");
    }

    private XsdSchema(File file, String nsPrefix) throws IOException, XMLStreamException {
        this.nsPrefix = nsPrefix;
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
            String linkedSchemaNsPrefix = namespaces.get(nsURI);

            try {
                linkedSchemas.add(new XsdSchema(new File(file.getParent(), schemaLocation), linkedSchemaNsPrefix));
            } catch (IOException | XMLStreamException e) {
                throw new RuntimeException(String.format(ERR_FMT_IMPORT, schemaLocation, file.getAbsolutePath()), e);
            }
        }

        String xsdNamespace = namespaces.get(XSD_URI);
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
                    String typeName = node.getAttribute(XmlNode.Attribute.NAME);
                    String prefix = Strings.isNullOrEmpty(nsPrefix) ? tnsPrefix : nsPrefix;
                    xmlTypes.put(String.format("%s:%s", prefix, typeName), node);
                    break;

                case XmlNode.Tag.IMPORT:
                    imports.add(node);
                    break;

                case XmlNode.Tag.SCHEMA:
                    tnsPrefix = namespaces.get(node.getAttribute(XmlNode.Attribute.TNS));
                    break;
            }
        }

        return node;
    }

    private void parseNamespaces(XMLEvent xmlEvent) {
        Iterator namespaces = xmlEvent.asStartElement().getNamespaces();
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();
            this.namespaces.put(ns.getNamespaceURI(), ns.getPrefix());
        }
    }

    public XmlNode getRoot() {
        return root;
    }

    public XmlNode getXmlType(String name) {
        XmlNode node = xmlTypes.get(name);
        if (node == null) {
            for (XsdSchema schema : linkedSchemas) {
                node = schema.getXmlType(name);
                if (node != null) {
                    return node;
                }
            }
        }

        return node;
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

    public String getNsPrefix() {
        return nsPrefix;
    }
}
