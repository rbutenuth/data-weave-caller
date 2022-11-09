package de.codecentric.dwcaller.test;

public class TextReporter {
	private static final String NL = System.lineSeparator();
	
	public static String test2report(Test test) {
		StringBuilder sb = new StringBuilder();
		reportTest(sb, test, 0);
		return sb.toString();
	}

	private static void reportTest(StringBuilder sb, Test test, int indent) {
		indent(sb, indent);
		sb.append(test.getName());
		if (test.isLeave()) {
			sb.append(": ").append(test.getStatus()).append(NL);
			indent(sb, indent + 2);
			sb.append("Time: " ).append(test.getTime()).append("ms").append(NL);
			if ("FAIL".equals(test.getStatus())) {
				indent(sb, indent + 2);
				sb.append("Error message: ").append(test.getErrorMessage()).append(NL);
				indent(sb, indent + 2);
				sb.append("Location: ").append(test.getSourceIdentifier()).append(", line ").append(test.getStart().getLine()).append(NL);
			}
		} else {
			sb.append(NL);
			for (Test t : test.getTests()) {
				reportTest(sb, t, indent + 2);
			}
		}
	}
	
	private static void indent(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(' ');
		}
	}
}
