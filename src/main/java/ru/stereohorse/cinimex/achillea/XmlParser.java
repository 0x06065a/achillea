package ru.stereohorse.cinimex.achillea;

import ru.stereohorse.cinimex.achillea.model.XmlElement;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class XmlParser {
    public XmlElement getXmlElements(File file) throws IOException, XMLStreamException {
        InputStream input = new FileInputStream(file);
        try {
            return getXmlElements(input);
        } finally {
            input.close();
        }
    }

    public XmlElement getXmlElements(InputStream input) throws XMLStreamException {
        XmlElement root = null;

        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(input);
        try {
            root = parseNode(new XmlElement(), xmlEventReader);
        } finally {
            xmlEventReader.close();
        }

        return root;
    }

    private XmlElement parseNode(XmlElement root, XMLEventReader xmlEventReader) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            if (xmlEvent.isEndElement()) {
                return root;
            } else if (xmlEvent.isStartElement()) {
                XmlElement element = new XmlElement(xmlEvent.asStartElement().getName().getLocalPart());

                StartElement stElement = xmlEvent.asStartElement();
                Iterator attributes = stElement.getAttributes();
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute) attributes.next();
                    element.getAttributes().put(attribute.getName().getLocalPart(), attribute.getValue());
                }

                root.getChildren().add(parseNode(element, xmlEventReader));
            }
        }

        return root;
    }
}
