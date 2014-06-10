package ru.stereohorse.cinimex.achillea.model;


import com.google.common.base.Strings;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;


public class XsdSchema {
    private final Map<String, XmlNode> complexTypes = new HashMap<>();
    private final List<XsdSchema> children = new ArrayList<>();
    private final File file;

    private final XmlNode root = new XmlNode();

    public XsdSchema(File file) throws IOException, XMLStreamException {
        this.file = file;

        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(input);
            try {
                root.setChildren(parse(xmlEventReader));
            } finally {
                xmlEventReader.close();
            }
        }
    }

    private List<XmlNode> parse(XMLEventReader xmlEventReader) throws XMLStreamException {
        List<XmlNode> nodes = Collections.emptyList();

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement()) {
                return nodes;
            } else if (xmlEvent.isStartElement()) {
                XmlNode node = new XmlNode(xmlEvent.asStartElement().getName().getLocalPart());
                node.setAttributes(xmlEvent);
                node.setChildren(parse(xmlEventReader));

                parse(node);

                if (nodes.isEmpty()) {
                    nodes = new ArrayList<>();
                }
                nodes.add(node);
            }
        }

        return nodes;
    }

    private void parse(XmlNode node) {
        if ("complexType".equals(node.getName())) {
            String name = node.getAttributes().get("name");
            if (!Strings.isNullOrEmpty(name)) {
                complexTypes.put(name, node);
            }
        } else if ("import".equals(node.getName())) {
            String schemaLocation = node.getAttributes().get("schemaLocation");
            if (!Strings.isNullOrEmpty(schemaLocation)) {
                try {
                    children.add(new XsdSchema(new File(file.getParent(), schemaLocation)));
                } catch (IOException | XMLStreamException e) {
                    throw new RuntimeException(String.format("Cannot import [%s] from [%s]",
                            schemaLocation, file.getAbsolutePath()));
                }
            }
        }
    }

    public XmlNode getRoot() {
        return root;
    }

    public Map<String, XmlNode> getComplexTypes() {
        return complexTypes;
    }

    public XmlNode getComplexType(String name) {
        XmlNode node = complexTypes.get(name);
        if (node == null) {
            for (XsdSchema schema : children) {
                node = schema.getComplexType(name);
                if (node != null) {
                    return node;
                }
            }
        }

        return node;
    }

    /**
     * Debug
     */
    public Set<String> getAllTagNames() {
        Set<String> names = root.getAllNames();

        for (XsdSchema child : children) {
            names.addAll(child.getAllTagNames());
        }

        return names;
    }


    public void toCsv(File file) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                writer.write("HELLO!");
            }
        }
    }
}
