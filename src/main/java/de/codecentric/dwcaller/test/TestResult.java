package de.codecentric.dwcaller.test;

import java.util.ArrayList;
import java.util.Collection;
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
	private Collection<TestResult> tests;

	public TestResult() {
		tests = new ArrayList<>();
	}

	public TestResult(String name) {
		this();
		this.name = name;
	}

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

	public String getName() {
		return name;
	}

	public int getTime() {
		return time;
	}

	public TestStatus getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getText() {
		return text;
	}

	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		return end;
	}

	public Collection<TestResult> getTests() {
		return tests;
	}

	public void addTest(TestResult test) {
		tests.add(test);
	}

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
