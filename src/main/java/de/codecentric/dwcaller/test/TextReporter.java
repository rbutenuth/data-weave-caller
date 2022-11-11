package de.codecentric.dwcaller.test;

public class TextReporter {
	private static final String NL = System.lineSeparator();
	
	public static String test2report(TestResult test) {
		StringBuilder sb = new StringBuilder();
		Statistic stat = new Statistic();
		reportTest(sb, stat, test, 0);
		sb.append("Total:  ").append(stat.total).append(NL);
		sb.append("OK:     ").append(stat.ok).append(NL);
		sb.append("Not OK: ").append(stat.total - stat.ok).append(NL);
		return sb.toString();
	}

	private static void reportTest(StringBuilder sb, Statistic stat, TestResult test, int indent) {
		indent(sb, indent);
		sb.append(test.getName());
		if (test.isLeave()) {
			sb.append(": ").append(test.getStatus()).append(NL);
			indent(sb, indent + 2);
			sb.append("Time: " ).append(test.getTime()).append("ms").append(NL);
			if ("OK".equals(test.getStatus())) {
				stat.ok++;
			} else {
				indent(sb, indent + 2);
				sb.append("Error message: ").append(test.getErrorMessage()).append(NL);
				indent(sb, indent + 2);
				sb.append("Location: ").append(test.getSourceIdentifier()).append(", line ").append(test.getStart().getLine()).append(NL);
			}
			stat.total++;
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
		private int ok;
		private int total;
	}
}
