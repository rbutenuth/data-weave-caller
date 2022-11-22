package de.codecentric.dwcaller.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import de.codecentric.dwcaller.TestRunner;

/**
 * Extend this class and write a method which calls {@link #all()} or {@link #some(String[])}
 * to execute DataWeave tests. Your method must be annotated with {@link TestFactory} 
 * 
 * Example:
 * <pre>
 * 	@TestFactory
 *  Collection<DynamicNode> dataWeaveTests() throws Exception {
 *    return all();
 *  }
 *  </pre>
 */
public abstract class DataWeaveTests {

	/**
	 * @param names Directory or file names with tests.
	 * @return All tests found in sub directories and given by name.
	 * @throws IOException From DataWeave
	 */
	protected Collection<DynamicNode> some(String... names) throws IOException {
		TestResult result = simplifyTree(TestRunner.run(names));
		List<DynamicNode> tests = new ArrayList<>();
		addTests(tests, result);
		return tests;
	}

	/**
	 * @return All tests found in the directories below <code>src/test</code>
	 * @throws IOException From DataWeave
	 */
	protected Collection<DynamicNode> all() throws IOException {
		return some(new String[0]);
	}

	/**
	 * Starting at the root, eliminate nodes with only one child and return the child instead.
	 * @param tree root to simplify
	 * @return Simplified tree
	 */
	private TestResult simplifyTree(TestResult tree) {
		if (!tree.isLeave() && tree.getTests().size() == 1) {
			return simplifyTree(tree.getTests().get(0));
		} else {
			return tree;
		}
	}

	private void addTests(List<DynamicNode> tests, TestResult result) {
		if (result.isLeave()) {
			switch (result.getStatus()) {
			case OK:
			case SKIP:
				String name = result.getName();
				if (result.getStatus() == TestStatus.SKIP) {
					name += " SKIP";
				}
				tests.add(DynamicTest.dynamicTest(name, new Executable() {
					@Override
					public void execute() {
						// already executed
					}
				}));
				break;
			case ERROR:
			case FAIL:
				tests.add(DynamicTest.dynamicTest(result.getName(), new Executable() {
					@Override
					public void execute() throws Throwable {
						Throwable e = new Throwable(result.getErrorMessage());
						StackTraceElement[] trace = new StackTraceElement[1];
						trace[0] = new StackTraceElement(className(result.getSourceIdentifier()), result.getName(), result.getSourceIdentifier(),
								result.getStart().getLine());
						e.setStackTrace(trace);
						throw e;
					}

					private String className(String name) {
						int start = name.lastIndexOf("::");
						return start == -1 ? name : name.substring(start + 2);
					}
				}));
				break;
			default:
				fail("unknown status: " + result.getStatus());
				break;
			}
		} else {
			List<DynamicNode> nodes = new ArrayList<>();
			for (TestResult test : result.getTests()) {
				addTests(nodes, test);
			}
			tests.add(DynamicContainer.dynamicContainer(result.getName(), nodes));
		}
	}
}
