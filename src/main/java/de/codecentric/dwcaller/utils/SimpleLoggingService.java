package de.codecentric.dwcaller.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mule.weave.v2.model.service.LoggingService;

/**
 * Logging service to <code>System.out</code> or a given {@link PrintStream}.
 */
public class SimpleLoggingService implements LoggingService {
	private PrintStream out;
	private List<Pattern> ignorePatterns;

	/**
	 * Logging to a given stream.
	 * @param destination Destination stream.
	 */
	public SimpleLoggingService(PrintStream destination) {
		out = destination;
		ignorePatterns = new ArrayList<>();
	}
	
	/**
	 * Logging to <code>System.out</code>.
	 */
	public SimpleLoggingService() {
		this(System.out);
	}
	
	/**
	 * Messages matching this pattern shall not be printed.
	 * @param pattern The pattern matched against the complete message.
	 */
	public void addIgnorePattern(Pattern pattern) {
		ignorePatterns.add(pattern);
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public void logError(String msg) {
		log(msg);
	}

	@Override
	public void logInfo(String msg) {
		log(msg);
	}

	@Override
	public void logWarn(String msg) {
		log(msg);
	}
	
	private void log(String msg) {
		for (Pattern p : ignorePatterns) {
			if (p.matcher(msg).matches()) {
				return;
			}
		}
		out.println(msg);
	}
}
