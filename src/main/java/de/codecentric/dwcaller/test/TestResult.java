package de.codecentric.dwcaller.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node in a tree with unit test results.
 */
public class TestResult {
	private String name;
	private int time;
	private TestStatus status;
	private String errorMessage;
	private String text;
	private String sourceIdentifier;
	private Location start;
	private Location end;
	private List<TestResult> tests;

	public TestResult() {
		tests = new ArrayList<>();
		name = "";
		sourceIdentifier = "";
		start = Location.UNKNOWN;
		end = Location.UNKNOWN;
	}

	public TestResult(String name) {
		this();
		this.name = name;
	}

	public TestResult(String name, TestStatus status, String errorMessage) {
		this(name);
		this.status = status;
		this.errorMessage = errorMessage;
	}

	/**
	 * Create an instance based on the code in data-weave-test-framework, file Tests.dwl.
	 * @param data JSON like data structure with test information.
	 */
	@SuppressWarnings("unchecked")
	public TestResult(Map<String, Object> data) {
		this();
		name = (String) data.get("name");
		time = (int) data.get("time");
		status = TestStatus.valueOf((String) data.get("status"));
		errorMessage = (String) data.get("errorMessage");
		text = (String) data.get("text");
		Map<String, Object> locationObject = (Map<String, Object>) data.get("location");
		if (locationObject != null) {
			start = new Location((Map<String, Object>) locationObject.get("start"));
			end = new Location((Map<String, Object>) locationObject.get("end"));
			sourceIdentifier = (String) locationObject.get("sourceIdentifier");
		}
		List<Map<String, Object>> testList = (List<Map<String, Object>>) data.get("tests");
		if (testList != null) {
			for (Map<String, Object> testObject : testList) {
				tests.add(new TestResult(testObject));
			}
		}
	}

	/**
	 * @return Name of test
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Test execution time in milliseconds.
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @return Test status.
	 */
	public TestStatus getStatus() {
		return status;
	}

	/**
	 * @return Error message or null.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return Text describing test, may be null.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return Path info of test, parts separated by ::.
	 */
	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	/**
	 * @return Start location of test in file.
	 */
	public Location getStart() {
		return start;
	}

	/**
	 * @return End location of test in file.
	 */
	public Location getEnd() {
		return end;
	}

	/**
	 * @return Children, empty for leave node (but never null).
	 */
	public List<TestResult> getTests() {
		return tests;
	}

	/**
	 * @param test Child to add.
	 */
	public void addTest(TestResult test) {
		tests.add(test);
	}

	/**
	 * @return No children?
	 */
	public boolean isLeave() {
		return tests.isEmpty();
	}

	/**
	 * @return Are all tests in the tree starting from this node successful? (OK or SKIP)
	 */
	public boolean isAllSuccess() {
		if (isLeave()) {
			return status.isSuccess();
		} else {
			boolean success = true;
			for (TestResult t : tests) {
				success &= t.isAllSuccess();
			}
			return success;
		}
	}
}
