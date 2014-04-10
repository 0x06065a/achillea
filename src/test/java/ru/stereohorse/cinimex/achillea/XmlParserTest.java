package ru.stereohorse.cinimex.achillea;

import org.junit.Test;
import ru.stereohorse.cinimex.achillea.model.XmlElement;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class XmlParserTest {
    public static final String XSD = "LoanService.xsd";

    private XmlParser xmlParser = new XmlParser();

    @Test
    public void testGetXsdElements() throws XMLStreamException, IOException {
        XmlElement root = xmlParser.getXmlElements(getClass().getResourceAsStream(XSD));
        System.out.println(root.toTreeString());
    }
}
