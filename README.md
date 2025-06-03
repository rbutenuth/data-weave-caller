# Call DataWeave from Java and execute DataWeave Unit tests.

This project allows to run  [DataWeave](https://dataweave.mulesoft.com/) code from Java. Additionally, you can write DataWeave unit tests and execute them like JUnit tests with the JUnit runner of your choice. 

You can find the documentation for DataWeave unit tests at MuleSoft: [DataWeave testing framework](https://docs.mulesoft.com/dataweave/2.4/dataweave-testing-framework).
There is additional documentation for the available [DataWeave asserts](https://docs.mulesoft.com/munit/latest/dataweave-assertions-library).

First you need some dependencies in your pom.xml:

```
<dependency>
	<groupId>org.junit.jupiter</groupId>
	<artifactId>junit-jupiter-api</artifactId>
	<version>5.13.0</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.junit.jupiter</groupId>
	<artifactId>junit-jupiter-engine</artifactId>
	<version>5.13.0</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>de.codecentric.mule.modules</groupId>
	<artifactId>data-weave-caller</artifactId>
	<version>1.2.0</version>
	<scope>test</scope>
</dependency>
```

To run your tests, you need a small Java adapter class:
```
package de.codecentric.dataweave;

import java.util.Collection;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import de.codecentric.dwcaller.test.DataWeaveTests;

public class CallDataWeaveTests extends DataWeaveTests {
	@TestFactory
	Collection<DynamicNode> dataWeaveTests() throws Exception {
		return some("src/test/resources/dataweave");
	}

}
```

Place it in `src/test/java`.

Functions which you want to use in your Mule application (and for which you want to write tests) can be placed in `src/main/resources/dataweave`, e.g. in a file `MyFunctions.dwl`:
   
```
fun sayHello(): String = (
	"Hello world!"
)

fun duplicate(s: String): String = (
	s ++ s
)

fun extractNode(o: Object): Any = (
	o.node
)
```

The unit test is placed in `src/test/resources/dataweave/DataWeaveTests.dwl`:

```
%dw 2.0
import * from dw::test::Tests
import * from dw::test::Asserts

import * from dataweave::MyFunctions

---
"my-functions" describedBy [
	"say-hello" in do {
		sayHello()
		must equalTo("Hello world!")
	},
	"duplicate-string" in do {
		duplicate("abc")
		must equalTo("abcabc")
	},	
	"extract-node" in do {
		extractNode(readUrl("classpath://testdata/input-data.json", "application/json"))
		must equalTo(readUrl("classpath://testdata/expected-result.json", "application/json"))
	}
]
```

The referenced sample data in `src/test/resources/sample_data`

To execute the tests, run the class `CallDataWeaveTests` with the test runner of your choice (e.g. in your IDE).

## Release notes

### Release 1.2.0 (2025-06-03)

- Dependency updates
- Migration to Java 17

### Release 1.1.2 (2024-07-05)

- Build with Data Weave testing framework 1.4.0-rc1

### Release 1.1.1 (2024-07-04)

- Build with Data Weave libraries 2.6.0 (instead of snapshot)

### Release 1.1.0 (2023-10-27)

- Build with versions matching Mule 4.5

### Release 1.0.11 (2023-10-27)

- Build with versions matching Mule 4.4

### Release 1.0.10 (2023-10-26)

- Version updates

### Release 1.0.9 (2023-06-13)

- Version updates
- Resolved classpath issues (separation of classes and test classes) 

### Release 1.0.8 (2022-12-06)

- Some versions did not arrive on Maven Central. 

### Release 1.0.6 (2022-12-02)

- Ignore non existing directors in synchronize (instead of throwing Exception)
- Add root objects, so access to vars etc. does not create compile error

- ### Release 1.0.5 (2022-12-01)

- Adding dwtestResources property when not set, points to src/test/resources
- Adding src/test/dw to folders which will by synchronized into target

### Release 1.0.4 (2022-11-24)

Initial release
