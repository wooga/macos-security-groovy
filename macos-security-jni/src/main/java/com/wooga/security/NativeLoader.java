
package com.wooga.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

class NativeLoader {

	public static void loadLibrary(ClassLoader classLoader, String libName) {
		loadLibrary(classLoader, libName, null);
	}
	public static void loadLibrary(ClassLoader classLoader, String libName, String pathPrefix) {
		try {
			System.loadLibrary(libName);
		} catch (UnsatisfiedLinkError ex) {
			String path = pathPrefix != null ? new File(pathPrefix, libFilename(libName)).getPath() : libFilename(libName);
			URL url = classLoader.getResource(path);
			try {
				File file = Files.createTempFile("jni", libFilename(nameOnly(libName))).toFile();
				file.deleteOnExit();
				file.delete();
				try (InputStream in = url.openStream()) {
					Files.copy(in, file.toPath());
				}
				System.load(file.getCanonicalPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private static String libFilename(String libName) {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			return decorateLibraryName(libName, ".dll");
		} else if (osName.contains("mac")) {
			return decorateLibraryName(libName, ".dylib");
		}
		return decorateLibraryName(libName, ".so");
	}

	private static String nameOnly(String libName) {
		int pos = libName.lastIndexOf('/');
		if (pos >= 0) {
			return libName.substring(pos + 1);
		}
		return libName;
	}

	private static String decorateLibraryName(String libraryName, String suffix) {
		if (libraryName.endsWith(suffix)) {
			return libraryName;
		}
		int pos = libraryName.lastIndexOf('/');
		if (pos >= 0) {
			return libraryName.substring(0, pos + 1) + "lib" + libraryName.substring(pos + 1) + suffix;
		} else {
			return "lib" + libraryName + suffix;
		}
	}
}
