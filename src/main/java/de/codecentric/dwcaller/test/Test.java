package de.codecentric.dwcaller.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Test {
	private String name;
	private int time;
	private String status;
	private String errorMessage;
	private String text;
	private String sourceIdentifier;
	private Location start;
	private Location end;
	private Collection<Test> tests;
	
	public Test() {
		tests = new ArrayList<>();
	}
	
	public Test(String name) {
		this();
		this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	public Test(Map<String, Object> data) {
		this();
		name = (String) data.get("name");
		time = (int) data.get("time");
		status = (String)data.get("status");
		errorMessage = (String)data.get("errorMessage");
		text = (String)data.get("text");
		Map<String, Object> locationObject = (Map<String, Object>) data.get("location");
		if (locationObject != null) {
			start = new Location((Map<String, Object>)locationObject.get("start"));
			end = new Location((Map<String, Object>)locationObject.get("end"));
			sourceIdentifier = (String)locationObject.get("sourceIdentifier");
		}
		List<Map<String, Object>> testList = (List<Map<String, Object>>) data.get("tests");
		if (testList != null) {
			for (Map<String, Object> testObject : testList) {
				tests.add(new Test(testObject));
			}
		}
	}

	public String getName() {
		return name;
	}

	public int getTime() {
		return time;
	}

	public String getStatus() {
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

	public Collection<Test> getTests() {
		return tests;
	}
	
	public void addTest(Test test) {
		tests.add(test);
	}
	
	public boolean isLeave() {
		return tests.isEmpty();
	}
}
