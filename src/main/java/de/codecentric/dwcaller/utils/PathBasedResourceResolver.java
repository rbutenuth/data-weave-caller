package de.codecentric.dwcaller.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.mule.weave.v2.parser.ast.variables.NameIdentifier;
import org.mule.weave.v2.sdk.NameIdentifierHelper;
import org.mule.weave.v2.sdk.WeaveResource;
import org.mule.weave.v2.sdk.WeaveResourceResolver;

import scala.Option;
import scala.Some;
import scala.io.BufferedSource;
import scala.io.Codec;
import scala.io.Source;

public class PathBasedResourceResolver implements WeaveResourceResolver {
	private List<ContentResolver> paths = new ArrayList<>();

	public PathBasedResourceResolver(File libDir) {
		if (libDir.exists()) {
			File[] files = libDir.listFiles();
			if (files != null) {
				for (File f : files) {
					paths.add(createContentResolver(f));
				}
			}
		}
	}

	public PathBasedResourceResolver(Collection<File> files) {
		for (File f : files) {
			paths.add(createContentResolver(f));
		}
	}

	public void addContent(ContentResolver cr) {
		paths.add(cr);
	}

	@Override
	public Option<WeaveResource> resolve(NameIdentifier name) {
		for (ContentResolver cr : paths) {
			Option<InputStream> maybeResource = cr.resolve(name);
			if (maybeResource.isDefined()) {
				String filePath = NameIdentifierHelper.toWeaveFilePath(name, "/"); // Use Unix based system
				return Option.apply(WeaveResource.apply(filePath, toString(maybeResource.get())));
			}
		}
		return Option.empty();
	}

	public String toString(InputStream is) {
		try (BufferedSource s = Source.fromInputStream(is, new Codec(StandardCharsets.UTF_8))) {
			return s.mkString();
		}
	}

	public Option<InputStream> resolve(String filePath) {
		NameIdentifier ni = NameIdentifierHelper.fromWeaveFilePath(filePath);
		for (ContentResolver cr : paths) {
			Option<InputStream> resolved = cr.resolve(ni);
			if (resolved.isDefined()) {
				return resolved;
			}
		}
		return Option.empty();
	}

	interface ContentResolver {
		public Option<InputStream> resolve(NameIdentifier path);
	}

	public static ContentResolver createContentResolver(File f) {
		if (f.isDirectory()) {
			return new DirectoryContentResolver(f);
		} else {
			return new JarContentResolver(f);
		}
	}

	static class DirectoryContentResolver implements ContentResolver {
		private File directory;

		public DirectoryContentResolver(File directory) {
			this.directory = directory;
		}

		@Override
		public Option<InputStream> resolve(NameIdentifier ni) {
			String path = NameIdentifierHelper.toWeaveFilePath(ni, File.separator);
			File file = new File(directory, path);
			if (file.isFile()) {
				try {
					return Some.apply(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					return Option.empty();
				}
			} else {
				return Option.empty();
			}
		}
	}

	static class JarContentResolver implements ContentResolver {
		private File jarFile;

		public JarContentResolver(File jarFile) {
			this.jarFile = jarFile;
		}

		@Override
		public Option<InputStream> resolve(NameIdentifier ni) {
			try {
				ZipFile zipFile = new ZipFile(jarFile);
				String path = NameIdentifierHelper.toWeaveFilePath(ni, "/"); // Use Unix based system
				String zipEntry;
				if (path.startsWith("/")) {
					zipEntry = path.substring(1);
				} else {
					zipEntry = path;
				}
				ZipEntry pathEntry = zipFile.getEntry(zipEntry);
				if (pathEntry != null) {
					return Some.apply(zipFile.getInputStream(pathEntry));
				} else {
					return Option.empty();
				}
			} catch (IOException e) {
				return Option.empty();
			}
		}
	}
}
