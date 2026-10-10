package com.kinetic.web.servlet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CsvExportTest {
    @Test
    public void formulasAreDefusedAndQuotesEscaped() {
        assertEquals("\"'=SUM(A1)\"", ExportServlet.csv("=SUM(A1)"));
        assertEquals("\"say \"\"hi\"\"\"", ExportServlet.csv("say \"hi\""));
        assertEquals("", ExportServlet.csv(null));
    }
}
