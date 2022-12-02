package de.codecentric.dwcaller;

import java.util.Collection;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import de.codecentric.dwcaller.test.DataWeaveTests;

public class DemoTestRunner extends DataWeaveTests {
	@TestFactory
	Collection<DynamicNode> dataWeaveTests() throws Exception {
		return some("src/test/resources/dwtests");
	}

}