package com.testcompany.app;

import java.io.IOException;

import static com.testcompany.app.util.FlatFileReader.processDataFileBy;

public class App 
{
    public static void main( String[] args ) throws IOException {
        processDataFileBy("src/resources/","MiniTestData.txt", "IdForExtraction.txt", "ColumnsForExtraction.txt" );
    }

}
