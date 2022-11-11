package de.codecentric.dwcaller.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mule.weave.v2.model.service.LoggingService;

public class SimpleLoggingService implements LoggingService {
	private PrintStream out;
	private List<Pattern> ignorePatterns;

	public SimpleLoggingService() {
		out = System.out;
		ignorePatterns = new ArrayList<>();
	}
	
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
