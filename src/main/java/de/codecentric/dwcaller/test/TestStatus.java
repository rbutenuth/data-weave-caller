package de.codecentric.dwcaller.test;

/**
 * Stauts, matches values in Test.dwl in the data-weave-testing-framework.
 */
public enum TestStatus {
	OK(true), SKIP(true), ERROR(false), FAIL(false);
	
	private boolean success;

	private TestStatus(boolean success) {
		this.success = success;
	}
	
	/**
	 * @return Is it {@link OK} or {@link SKIP}?
	 */
	public boolean isSuccess() {
		return success;
	}
}
