package ru.stereohorse.cinimex.achillea;

import org.junit.Test;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class XmlParsingTest {
    @Test
    public void testGetXsdElements() throws XMLStreamException, IOException {
        XsdSchema schema = new XsdSchema(new File("docs/LoanService.xsd"));
        System.out.println(schema.getAllTagNames());
    }
}
