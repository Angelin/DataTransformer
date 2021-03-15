# DataTransformer
This is java project built using Maven. The data file and transformation config files must be present in the resources folder and the transformed data file will be written to the same directory

##Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing.

### Prerequisites
Java 1.9

### Installing
1. Clone the Project
2. build the project using maven
 Go to Terminall> mvn install to 
3. Data requirements 
You need to have below files in the resources folder of the maven project. The project has the below file as a sample where
ColumnsForExtraction.txt	- Config file with columns to beextract and has new names in the second column
IdForExtraction.txt		    - Config file with reequired identifies to extract the corresponsidn rows to and has new identifiers in the second column
MiniTestData.txt		      - A text file with data to be transformed using the above two config files
temp_MiniTestData.txt     - This is the output file that will be generated on successful excecution of the project

```
**Example**

1. Data file to be transformed
COL0	COL1	COL2	COL3
ID1	VAL11	VAL12	VAL12
ID2	VAL21	VAL22	VAL23
...

2. This is a column configuration file that lists the columns that we want to extract. We want to translate the columns to 'our' names. So this file contains two columns: first column with original label, second column with 'our' labels.

Example (skip column COL2):

COL0	OURID
COL1	OURCOL1
COL3	OURCOL3

3. This is row configuration file that lists the data vendor specific identifiers, so the rows that we want to extract. Similar to point 2: these are transformed to the values in column 2

Example (skip ID1):

ID2	OURID2

4. the project read the above three files and produces output in the same structure: first row with 'our' column labels, further rows with the data we wanted to extract. The output file records don't have to be in the same order as the input.

Example: expected output based on the examples above:

OURID	OURCOL1	OURCOL3
OURID2	VAL21	VAL23
```

## Running the tests
Go to project direcitry> mvn test 

## Built With

Maven 4.0.0


