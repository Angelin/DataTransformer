package com.testcompany.app.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class FlatFileReader {

    private static String RESOURCE_PATH;
    private static FileSystemProvider provider(Path path) {
        return path.getFileSystem().provider();
    }

    /* @param   resourcePath
     *          the path to the resources directory
     * @param   dataFileName
     *          the data file to be transformed
     * @param   idFileName
     *          the config file with row identifiers to be extracted
     * @param   colConfigFileName
     *          the config file with columns  to be extracted
     * @return  the lines from the file as a {@code Stream}
     */
    public static void processDataFileBy(String resourcePath, String dataFileName, String idFileName,
                                         String colConfigFileName) throws IOException {
        RESOURCE_PATH = resourcePath;

        // Using Path api for non-blocking I/O API for better performance
        Path idPath = Paths.get(RESOURCE_PATH + idFileName);
        Path dataPath = Paths.get(RESOURCE_PATH + dataFileName);
        Stream<String> idStream = Files.lines(idPath, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = getWriterFor("temp_" + dataFileName); //write to a new output file

        // to process large data files
        Stream<String> datalines = Files.lines(dataPath, StandardCharsets.UTF_8);
        String headers = datalines.findFirst().get(); //reading the first line for headers
        Map<String, String> headerInfo = extractHeaderInfo(RESOURCE_PATH + colConfigFileName, headers);
        bufferedWriter.write(headerInfo.get("headers")+ "\n"); // write the modified header to the o/p file

        //create row filter
        Pattern filterRegex = Pattern.compile(headerInfo.get("filter"));

        /*
        Use Streams for Java runtime to split the main stream into substream and process in parallel leveraging
        the multi core systems
         */
        Set<Map.Entry<String, String>> idMap = createConfigMap(idStream);
        idMap.parallelStream().forEach(entrySet -> {
                    try {
                        Predicate<String> idFilterPredicate = Pattern
                                .compile("^" + entrySet.getKey() + "\t.*$")
                                .asPredicate();

                        Files.lines(dataPath, StandardCharsets.UTF_8).parallel()
                                .filter(idFilterPredicate)
                                .forEach(line -> {
                                    try {
                                        //renaming the unique indentifiers as per the config file
                                        line = line.replace(entrySet.getKey(),
                                                entrySet.getValue());
                                        Matcher matcher = filterRegex.matcher(line);

                                        if(matcher.find()) {
                                            MatchResult result = matcher.toMatchResult();
                                            StringBuffer sb = new StringBuffer(); // because it is thread-safe
                                            int resultsCnt = result.groupCount();
                                            int i =1;
                                            while(i <= resultsCnt){
                                                sb.append(result.group(i++) + "\t");
                                            }
                                            bufferedWriter.write(sb.toString() + "\n");
                                        }

                                    }catch(Exception e){
                                        System.out.println(e.getMessage());
                                    }
                                });

                    } catch (IOException e) {

                    }
                });

        bufferedWriter.close();
    }

    /* @param   configStream
     *          streams containing the with columns  to be extracted
     * @return  transformed config to structure Set<Map<key, value>> handy for processing
     */
    private static Set<Map.Entry<String, String>> createConfigMap(Stream<String> configStream) {
        return configStream.map(str -> str.split("\t"))
                .collect(toMap(str -> str[0], str -> str[1])).entrySet();
    }

    /* @param   colConfigFileName
     *          the config file with columns  to be extracted
     * @param  headerLine
     *          Line with old headers
     * @return  Map with
     *          1. filter : holds regex to grep the required columns
     *          2. header: modified headers
     *
     */
    public static Map<String,String> extractHeaderInfo(String colConfigFileName, String headerLine) {
        Map<String,String> headersInfo = new HashMap<String,String>();

        try {
            // reading the column config from the paased file
            Map<String, String> columnConfig = createConfigMap(colConfigFileName);
            StringBuffer extractedHeaders = new StringBuffer();
            StringBuffer colRegexPattern = new StringBuffer("^");

            //creating the regex pattern for the column data to be extracted
            String glue = "";
            for (String header : headerLine.split("\t")) {
                if (columnConfig.keySet().contains(header)) {
                    extractedHeaders.append(header + "\t");
                    colRegexPattern.append(glue + "(.*)");
                }else{
                    colRegexPattern.append(glue + ".*");
                }
                glue = "\\t";
            }
            colRegexPattern.append("$");
            headersInfo.put("filter",colRegexPattern.toString() ); // returning the filter

            //renaming the extracted headers according to the config
            for (Map.Entry<String, String> entry : columnConfig.entrySet()) {
                //replacing the old cloumns with new column names
                headerLine = headerLine.replace(entry.getKey(), entry.getValue());
            }
            headersInfo.put("headers",headerLine);// returning the modifed headers
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return headersInfo;
    }

    /* @param   colConfigFileName
     *          filename containing the with columns  to be extracted
     * @return  transformed config to structure Map, key, value handy for processing
     */
    public static Map<String, String> createConfigMap(String colConfigFileName) throws IOException {
        Path conConfigPath = Paths.get(colConfigFileName);
        Stream<String> lines = Files.lines(conConfigPath, StandardCharsets.UTF_8);

        return lines.map(str -> str.split("\t"))
                                    .collect(toMap(str -> str[0], str -> str[1]));
    }

    /* @param fileName
     *
     * @return Buffered writer for the given filename
     *
     */
    private static BufferedWriter getWriterFor(String fileName) throws IOException {
        Path writePath = Paths.get(RESOURCE_PATH + fileName);
        //writes the o/p stream using charset utf-8
        return Files.newBufferedWriter(writePath);
    }

}
