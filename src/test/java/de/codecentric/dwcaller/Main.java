package de.codecentric.dwcaller;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.mule.weave.v2.runtime.ArrayDataWeaveValue;
import org.mule.weave.v2.runtime.DataWeaveNameValue;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.DataWeaveValue;
import org.mule.weave.v2.runtime.ObjectDataWeaveValue;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.mule.weave.v2.runtime.SimpleDataWeaveValue;

import de.codecentric.dwcaller.test.TestResult;
import de.codecentric.dwcaller.test.TextReporter;
import de.codecentric.dwcaller.utils.WeaveRunner;
import de.codecentric.dwcaller.utils.WeaveRunnerBuilder;

/**
 * Class to test some stuff
 */
public class Main {
	
	public static void main(String[] args) {
		ScriptingBindings bindings = new ScriptingBindings();
		bindings.addBinding("payload", "{\"foo\": 42}", "application/json");

		WeaveRunner weaveRunner = new WeaveRunnerBuilder() //
				.withIgnorePattern(Pattern.compile("data-weave-testing-framework.*", Pattern.DOTALL)) //
				.build();
		DataWeaveScript script = weaveRunner.compile(new File("src/main/resources/test.dwl"), bindings);
		DataWeaveResult result = weaveRunner.runScript(script, bindings, "application/java");
		Object content = result.getContent();
		@SuppressWarnings("unchecked")
		TestResult test = new TestResult((Map<String, Object>)content);
		System.out.print(TextReporter.test2report(test));
		//System.out.println("out: " + new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}
	

	public static String toString(DataWeaveValue value) {
		StringBuilder sb = new StringBuilder();
		toString(value, sb, 0);
		return sb.toString();
	}

	private static void toString(DataWeaveValue value, StringBuilder sb, int depth) {
		if (value instanceof SimpleDataWeaveValue) {
			SimpleDataWeaveValue s = (SimpleDataWeaveValue) value;
			sb.append(s.valueAsString());
		} else if (value instanceof ObjectDataWeaveValue) {
			ObjectDataWeaveValue o = (ObjectDataWeaveValue) value;
			DataWeaveNameValue[] entries = o.entries();
			indent(sb, depth);
			sb.append("{\n");
			for (int i = 0; i < entries.length; i++) {
				indent(sb, depth + 1);
				sb.append(entries[i].name().name()).append(": ");
				toString(entries[i].value(), sb, depth + 2);
				if (i < entries.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			indent(sb, depth);
			sb.append("}\n");
		} else if (value instanceof ArrayDataWeaveValue) {
			ArrayDataWeaveValue a = (ArrayDataWeaveValue) value;
			DataWeaveValue[] elements = a.elements();
			indent(sb, depth);
			sb.append("[\n");
			for (int i = 0; i < elements.length; i++) {
				indent(sb, depth);
				toString(elements[i], sb, depth + 1);
				if (i < elements.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			indent(sb, depth);
			sb.append("]\n");
		} else {
			throw new IllegalArgumentException("no known type: " + value);
		}
	}

	private static void indent(StringBuilder sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
	}

}
