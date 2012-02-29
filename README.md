# Swagger Client Code-Generator


## Overview
This is a project to build the Swagger code-gen library which can be used to automatically
generate client libraries from a Swagger-compliant server.  It also contains a testing
framework which allows the client library to query an API server and validate expected results 
You can find out more about both the spec and the framework at http://swagger.wordnik.com.  For 
more information about Wordnik's APIs, please visit http://developer.wordnik.com.  

### Prerequisites

You need the following installed and available in your $PATH:

- Java 1.6 or greater (http://java.oracle.com)
- Apache ant 1.7 or greater (http://ant.apache.org/)
- Scala 2.9.1 or greater (http://www.scala-lang.org/downloads)

You also need to set an environment variable for SCALA_HOME:

```bash
export SCALA_HOME={PATH_TO_YOUR_SCALA_DEPLOYMENT}
```

### To build the codegen library

If you don't have the Apache Ivy dependency manager installed, run this build script:

```bash
ant -f install-ivy
```

This will copy the ivy ant lib into your antlib directory.  Now you can build the artifact:

```bash
ant
```

This will create the swagger-codegen library in your build folder.  


### To build java client source files

```bash
./bin/generate-java-lib.sh {server-url} {api_key} {output-package} {output-dir}
```

for example:
```bash
./bin/generate-java-lib.sh http://petstore.swagger.wordnik.com/api/ special-key com.foo.mydriver generated-files
```

### Other languages
#### scala
```bash
./bin/generate-scala-lib.sh http://petstore.swagger.wordnik.com/api "" "client" "generated-files"
```

#### javascript
```bash
./bin/generate-js-lib.sh http://petstore.swagger.wordnik.com/api "" "" "generated-files"
```

#### actionscript
```bash
./bin/generate-as3-lib.sh http://petstore.swagger.wordnik.com/api "" "client" "generated-files"
```

#### PHP
```bash
./bin/generate-php-lib.sh http://petstore.swagger.wordnik.com/api "" "client" "generated-files"
```

#### Python
```bash
./bin/generate-python-lib.sh http://petstore.swagger.wordnik.com/api "" "client" "generated-files"
```

The main class for the generator is at `src/main/java/com/wordnik/swagger/codegen/config/java/JavaLibCodeGen.java`

The code-gen uses the 
[antlr string template library](http://www.stringtemplate.org)
for generating the output files.

The Wordnik team is working on generating libraries for Ruby, ActionScript 3, Android, PHP and JavaScript, which will be open-sourced in the coming weeks.

### The Swagger client test framework

The testing framework helps you to test Swagger generated client libraries using declarative test scripts. The same 
scripts can be used to test client libraries in different languages.  The framework can be used for client and server
regression testing.

For example, first build the client library from the sample app:
```bash
./bin/generate-java-lib.sh http://petstore.swagger.wordnik.com/api/ special-key com.foo.mydriver generated-files
```

Use the sample build script to build a jar from the client files:
```bash
cp conf/java/sample/*.xml ./generated-files
cd generated-files
ant
```

This creates a complete client library jar.  You can now run the tests:

```bash
./bin/test-java-lib.sh http://petstore.swagger.wordnik.com/api/ special-key conf/java/sample/lib-test-script.json \
    conf/java/sample/lib-test-data.json com.foo.mydriver.model.TestData com.foo.mydriver.api \
    generated-files/build/swagger-sample-java-lib-1.0.jar

Summary -->  Total Test Cases: 9 Failed Test Cases: 0
Details: 
1.1 : Create User :  passed  
1.2 : Login User :  passed  
1.3 : Find user by name :  passed   
1.4 : Delete user by name :  passed  
2.1 : Add pet :  passed  
2.2 : Find pet by id :  passed  
2.3 : Find pet by status :  passed  
3.1 : Find order by id :  passed  
3.2 : Place order :  passed 
```

In detail, there are two components in the test framework:

- Test Script
- Test Data

#### Test script details

Test script is written in JSON structure. The JSON consists of following elements:

##### Resources.  This is a list of resources considered in the test. Each resource object consists of following properties:

- id: a unique test script ID
- name: name of the resource, used in displaying the test result
- httpMethod: HTTP method used in invoking this resource
- path: path of the resource
- suggested method name: By default this refers to method name of the API in resource classes

##### Test suites.  This is a logical way of grouping related test cases. Each test suite consists of following properties:

- id: unique id of the test script, displayed in the test report
- name: name of the test suite. Used in test report
- test cases: List of test cases with in each suite. Each test case consists of following properties:

  - id: unique with in the test suite. Used for reporting and tracking output data
  - name: Name of the test case
  - resource id: references the resource id in the resources section
  - input: Input is a JSON object with each property in the object map to query, path or post parameters. 
    For POST data, the name of the property should be supplied as postData. The value for each property can refer 
    to input file or output from previous test cases or actual values.   
  - assertions: list of assertions that needs to be evaluated after test case is executed. 

Each assertion contains

- actual output, specified with reference to output of the current test case using syntax similar to object graph navigation language 
- condition , support values are equal (==), not equal (!=), less than (<), lesser than or equal (<=),  greater than (>), greater than or equal (>=) 
- expected output. Specified using actual values or values referring previous outputs or input data file

Test data file is documented using a Test Data Object which is generated as part of Java client library code-gen.  This 
class provides list getters and setters for each model object available in the resource description.  It is called "TestData" 
and it is available in model package of the java library code generation output.
 
Chaining results of test cases:

- Reference to data in input file is done with prefix `${input.`, followed by object graph navigation syntax. 
Example: to refer a first user object in test data file use the syntax `${input.userList[0]}` 
- To refer a individual property of user object use the syntax `${input.userList[0].username}`
- Reference to output of test cases is done using combination test case path and OGNL. Reference to test cases output 
is prefixed with `${output.`
- To refer an output of test case 1 in test suite 2, the syntax will be `${output(1.2)}`.  Individual attributes can 
be accessed using OGNL syntax. Example: `${output(1.1).username}` 

#### Reporting Test Results

A Summary will be reported with each test run.  For instance: 

```bash
Sample: "Summary -->  Total Test Cases: 9 Failed Test Cases: 0"
```

In detail section each test case and its status (passed/failed) are reported. Failures include an exception trace.  Test case path is 
combination of test suite id and test case id separated by "."