# Call DataWeave from Java and execute DataWeave Unit tests.

This project allows to run  [DataWeave](https://dataweave.mulesoft.com/) code from Java. Additionally, you can write DataWeave unit tests and execute them like JUnit tests with the JUnit runner of your choice. 

You can find the documentation for DataWeave unit tests at MuleSoft: [DataWeave testing framework](https://beta.docs.mulesoft.com/beta-dataweave/dataweave/2.4/dataweave-testing-framework). There is additional documentation for the available [DataWeave asserts]((https://docs.mulesoft.com/munit/2.3/dataweave-assertions-library).

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

### Release 1.0.4 (2022-11-24)


Initial release
