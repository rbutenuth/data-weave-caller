package de.codecentric.dwcaller.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper to synchronize file trees.
 */
public class SynchronizeUtil {
	private Set<File> nodesExpectedInDest;
	private Set<Pattern> doNotDeletePatterns;
	private boolean changesDetected;

	public SynchronizeUtil() {
		nodesExpectedInDest = new HashSet<>();
		doNotDeletePatterns = new HashSet<>();
		changesDetected = false;
	}

	public void addToExpected(File node) {
		nodesExpectedInDest.add(absoluteCanonical(node));
	}

	public void addToDoNotDeletePatterns(Pattern pattern) {
		doNotDeletePatterns.add(pattern);
	}

	public void syncFileOrDirectory(File sourceNode, File destNode) throws IOException {
		if (sourceNode.isDirectory()) {
			syncDirectory(sourceNode, destNode);
		} else if (sourceNode.isFile()) {
			syncFile(sourceNode, destNode);
		} else {
			throw new IOException("Don't know how to sync " + sourceNode);
		}
	}

	public void deleteUnexpectedNodes(File destination) throws IOException {
		if (destination.isDirectory()) {
			deleteUnexpectedDirectory(destination);
		} else if (destination.isFile()) {
			deleteUnexpectedFile(destination);
		} else {
			throw new IOException("Don't know how to handle " + destination);
		}
	}

	private void deleteUnexpectedFile(File file) throws IOException {
		if (!nodesExpectedInDest.contains(file.getAbsoluteFile().getCanonicalFile())) {
			for (Pattern p: doNotDeletePatterns) {
				if (p.matcher(file.getName()).matches()) {
					return;
				}
			}
			if (!file.delete()) {
				throw new IOException("Could not delete " + file);
			}
		}
	}

	private void deleteUnexpectedDirectory(File dir) throws IOException {
		for (File node : dir.listFiles()) {
			deleteUnexpectedNodes(node);
		}
		if (!nodesExpectedInDest.contains(dir.getAbsoluteFile().getCanonicalFile())) {
			if (dir.list().length == 0) {
				if (!dir.delete()) {
					throw new IOException("Could not delete " + dir);
				}
			}
		}
	}

	public boolean haveDectectedChanges() {
		return changesDetected;
	}

	private void syncDirectory(File sourceDir, File destDir) throws IOException {
		// In case we have a file with the same name as the directory: Delete
		if (destDir.isFile()) {
			deleteFileOrDirectory(destDir);
			changesDetected = true;
		}
		if (!destDir.isDirectory()) {
			if (!destDir.mkdir()) {
				throw new IOException("Could not create directory " + destDir);
			}
			changesDetected = true;
		}
		for (File sourceNode : sourceDir.listFiles()) {
			String name = sourceNode.getName();
			File destNode = new File(destDir, name);
			syncFileOrDirectory(sourceNode, destNode);
		}
		addToExpected(destDir);
	}

	private void syncFile(File source, File dest) throws IOException {
		if (haveToCopy(source, dest)) {
			copyFile(source, dest);
			changesDetected = true;
		}
		addToExpected(dest);
	}

	private static void copyFile(File sourceFile, File targetFile) throws IOException {
		try (InputStream source = new FileInputStream(sourceFile);
				OutputStream target = new FileOutputStream(targetFile)) {
			byte[] buffer = new byte[1 << 16];
			int read;
			do {
				read = source.read(buffer);
				if (read > 0) {
					target.write(buffer, 0, read);
				}
			} while (read > 0);
		}
	}

	private static void deleteFileOrDirectory(File node) throws IOException {
		if (node.isFile()) {
			Files.delete(node.toPath());
		} else if (node.isDirectory()) {
			for (File subNode : node.listFiles()) {
				deleteFileOrDirectory(subNode);
			}
		} else {
			throw new IOException("Don't know how to delete " + node);
		}
	}

	/**
	 * Check if we have to copy/sync a file. In case dest exists and is a directory
	 * or has to be synchronized: Delete it.
	 * 
	 * @param source Source file
	 * @param dest   Destination file
	 * @return Is source younger than dest or does the size differ, or dest does not
	 *         exist.
	 */
	private static boolean haveToCopy(File source, File dest) throws IOException {
		if (dest.exists()) {
			if (dest.isDirectory()) {
				deleteFileOrDirectory(dest);
				return true;
			}
			if (!dest.isFile()) {
				throw new IOException("Don't know what to do with " + dest + " at target position");
			}
			boolean haveTo = source.lastModified() > dest.lastModified() || source.length() != dest.length();
			if (haveTo) {
				deleteFileOrDirectory(dest);
			}
			return haveTo;
		} else {
			return true;
		}
	}

	private static File absoluteCanonical(File file) {
		file = file.getAbsoluteFile();
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return file;
		}
	}
}
