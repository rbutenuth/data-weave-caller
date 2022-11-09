package de.codecentric.dwcaller.utils;

import java.io.File;

public class DataWeaveUtils {

	/**
	 * Returns the DW home directory if exists it can be overwritten with env
	 * variable DW_HOME
	 *
	 * @return The home directory
	 */
	public File getDWHome() {
		File defaultDWHomeDir = getDefaultDWHome();
		if (!defaultDWHomeDir.exists()) {
			defaultDWHomeDir.mkdir();
		}
		return defaultDWHomeDir;
	}

	public File getDefaultDWHome() {
		File homeUser = getUserHome();
		File defaultDWHomeDir = new File(homeUser, ".dw");
		return defaultDWHomeDir;
	}

	public File getUserHome() {
		return new File(System.getProperty("user.home"));
	}

	/**
	 * Returns the DW home directory if exists it can be overwritten with env
	 * variable DW_HOME
	 *
	 * @return The home directory
	 */
	public File getWorkingHome() {
		File tmpDirectory = new File(getDWHome(), "tmp");
		if (!tmpDirectory.exists()) {
			tmpDirectory.mkdirs();
		}
		return tmpDirectory;
	}

	/**
	 * Returns the directory where all default jars are going to be present. It can
	 * be overwriten with DW_LIB_PATH
	 *
	 * @return The file
	 */
	public File getLibPathHome() {
		return new File(getDWHome(), "libs");

	}

	public File getCacheHome() {
		return new File(getDWHome(), "cache");
	}
}
