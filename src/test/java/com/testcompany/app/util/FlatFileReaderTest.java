package com.testcompany.app.util;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class FlatFileReaderTest {

    private static String RESOURCE_PATH = "src/test/resources/";
    private FlatFileReader flatFileReader;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void readFilefromResoures() {
    }

    @Test
    public void processDataFileBy_whenSmallDataProvided()
            throws IOException {
        flatFileReader.processDataFileBy(RESOURCE_PATH,"MiniTestData.txt", "IdForExtraction.txt",
                "ColumnsForExtraction.txt");

        Path outputPath = Paths.get(RESOURCE_PATH+ "temp_MiniTestData.txt");
        BufferedReader reader = Files.newBufferedReader(outputPath);
        String header = reader.readLine();
        String firstLine = reader.readLine();
        assertEquals("OURID\tOURCOL1\tOURCOL3\t", header);
        assertEquals("OURID1\tVAL11\tVAL13\t"	, firstLine);
    }

    @Test
    public void checkCreatedMap_createdUsingConfigFileName() throws IOException {
        Map<String, String> expectedcolumnConfig = new HashMap() ;
        expectedcolumnConfig.put("COL0", "OURID");
        expectedcolumnConfig.put("COL1", "OURCOL1");
        expectedcolumnConfig
                .put("COL3", "OURCOL3");

        Map<String, String> generatedColumnConfig = flatFileReader.createConfigMap(RESOURCE_PATH +
                "ColumnsForExtraction.txt");
        assertEquals(expectedcolumnConfig, generatedColumnConfig);
    }

    @Test
    public void checkExtractedHeaderInfo_fromProvidedConfigFile(){
        Map<String,String> expectedHeaderInfo = new HashMap() ;
        expectedHeaderInfo.put("headers", "OURID\tOURCOL1\tOURCOL3\t");
        expectedHeaderInfo.put("filter", "^(.*)\\t(.*)\\t.*\\t(.*)$");
        Map<String,String> generatedHeaderInfo = flatFileReader.extractHeaderInfo(RESOURCE_PATH +
                "ColumnsForExtraction.txt", "COL0\tCOL1\tCOL2\tCOL3");
        assertEquals(expectedHeaderInfo, generatedHeaderInfo);

    }
}