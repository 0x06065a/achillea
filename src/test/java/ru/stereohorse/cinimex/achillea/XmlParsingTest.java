package ru.stereohorse.cinimex.achillea;

import com.google.common.io.Files;
import org.junit.Test;

import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class XmlParsingTest {
    public static final String TEST_RESOURCES_ENCODING = "utf-8";

    @Test
    public void testCreateLoanRequestComplexType() throws Exception {
        assertCsvEquals("resources/loanService/LoanService.xsd", "createLoanRequest", "createLoanRequest.csv");
    }

    @Test
    public void testRatesElement() throws Exception {
        assertCsvEquals("resources/Rates_20120229.xsd", "Rates", "rates.csv");
    }

    @Test
    public void testRatesComplexType() throws Exception {
        assertCsvEquals("resources/Rates_20120229.xsd", "ct_Rates", "rates.csv");
    }

    private void assertCsvEquals(String xsd, String elementName, String expectedCsv) throws Exception {
        XsdSchema schema = new XsdSchema(new File(xsd));
        File expectedCsvFile = new File(getClass().getResource(expectedCsv).toURI());

        assertEquals(
                Files.asCharSource(expectedCsvFile, Charset.forName(TEST_RESOURCES_ENCODING)).read(),
                new Csv(schema.getNodeByNameAttribute(elementName)).toString());
    }
}
