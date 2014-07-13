package ru.stereohorse.cinimex.achillea;

import com.google.common.io.Files;
import org.junit.Test;

import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class XmlParsingTest {
    @Test
    public void testToCsv() throws XMLStreamException, IOException {
        XsdSchema schema = new XsdSchema(new File("docs/LoanService.xsd"));
        assertEquals(
                Files.asCharSource(new File("docs/expected.csv"), Charset.forName("windows-1251")).read(),
                new Csv(schema.getNodeByNameAttribute("createLoanRequest")).toString());
    }
}
