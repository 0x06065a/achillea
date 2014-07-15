package ru.stereohorse.cinimex.achillea;

import com.google.common.io.Files;
import org.junit.Test;

import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;
import ru.stereohorse.cinimex.achillea.model.formats.CsvFormatter;
import ru.stereohorse.cinimex.achillea.model.formats.FullFormat;
import ru.stereohorse.cinimex.achillea.model.formats.MappingStyleFormat;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class CsvParsingTest {
    private static final String TEST_RESOURCES_ENCODING = "utf-8";

    @Test
    public void testCreateLoanRequestComplexTypeFullFormat() throws Exception {
        testFullFormat("resources/loanService/LoanService.xsd", "createLoanRequest", "createLoanRequest.csv");
    }

    @Test
    public void testRatesElement() throws Exception {
        testFullFormat("resources/Rates_20120229.xsd", "Rates", "rates.csv");
    }

    @Test
    public void testRatesComplexType() throws Exception {
        testFullFormat("resources/Rates_20120229.xsd", "ct_Rates", "rates.csv");
    }

    @Test
    public void testCreateLoanRequestComplexTypeMappingStyle() throws Exception {
        testMappingStyleFormat("resources/loanService/LoanService.xsd", "createLoanRequest", "createLoanRequestMappingStyle.csv");
    }

    private void testFullFormat(String xsd, String elementName, String expectedCsv) throws Exception {
        assertCsvEquals(xsd, elementName, expectedCsv, new FullFormat());
    }

    private void testMappingStyleFormat(String xsd, String elementName, String expectedCsv) throws Exception {
        assertCsvEquals(xsd, elementName, expectedCsv, new MappingStyleFormat());
    }

    private void assertCsvEquals(String xsd, String elementName, String expectedCsv, CsvFormatter formatter) throws Exception {
        XsdSchema schema = new XsdSchema(new File(xsd));
        File expectedCsvFile = new File(getClass().getResource(expectedCsv).toURI());

        assertEquals(
                Files.asCharSource(expectedCsvFile, Charset.forName(TEST_RESOURCES_ENCODING)).read(),
                formatter.format(new Csv(schema.getNodeByNameAttribute(elementName))));
    }
}
