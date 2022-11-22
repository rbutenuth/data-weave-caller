package de.codecentric.dwcaller.test;

/**
 * Simple text reporter.
 */
public class TextReporter {
	private static final String NL = System.lineSeparator();

	/**
	 * Make a String representation from a tree of tests.
	 * @param test Root
	 * @return Several lines with results.
	 */
	public static String test2report(TestResult test) {
		StringBuilder sb = new StringBuilder();
		Statistic stat = new Statistic();
		reportTest(sb, stat, test, 0);
		sb.append("Total: ").append(stat.getTotalNumberOfTests()).append(NL);
		for (TestStatus s : TestStatus.values()) {
			sb.append(String.format("%-7s", s.toString() + ":")).append(stat.getNumberOf(s)).append(NL);
		}
		if (!test.isAllSuccess()) {
			sb.append(">>>>>>>>>>> There are ERRORS/FAILURES!").append(NL);
		}
		return sb.toString();
	}

	private static void reportTest(StringBuilder sb, Statistic stat, TestResult test, int indent) {
		indent(sb, indent);
		sb.append(test.getName());
		if (test.isLeave()) {
			stat.add(test.getStatus());
			sb.append(": ").append(test.getStatus().toString());
			sb.append(", Time: ").append(test.getTime()).append("ms").append(NL);
			if (!test.getStatus().isSuccess()) {
				indent(sb, indent + 2);
				sb.append("Error message: ").append(test.getErrorMessage()).append(NL);
				indent(sb, indent + 2);
				sb.append("Location: ").append(test.getSourceIdentifier()).append(", line ").append(test.getStart().getLine()).append(NL);
			}
		} else {
			sb.append(NL);
			for (TestResult t : test.getTests()) {
				reportTest(sb, stat, t, indent + 2);
			}
		}
	}

	private static void indent(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(' ');
		}
	}

	private static class Statistic {
		private int[] count = new int[TestStatus.values().length];

		public void add(TestStatus testStatus) {
			count[testStatus.ordinal()]++;
		}

		public int getNumberOf(TestStatus testStatus) {
			return count[testStatus.ordinal()];
		}

		public int getTotalNumberOfTests() {
			int sum = 0;
			for (TestStatus s : TestStatus.values()) {
				sum += count[s.ordinal()];
			}
			return sum;
		}
	}
}
