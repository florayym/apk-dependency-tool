package com.alex_zaitsev.adg.io;

import java.io.File;

public class ArgumentReader {

	private static final String ARG_APK = "-i";
	private static final String ARG_PROJ = "-d";
	private static final String ARG_FILTER = "-f";
	private static final String ARG_RES_JS = "-o";

	private static final String USAGE_STRING = "Usage:\n" +
			ARG_APK + " path : path to the apk file awaiting analysis\n" +
			ARG_PROJ + " path : path to the decompiled project\n" +
			ARG_FILTER + " filters : path to the your filter.json file\n" +
			ARG_RES_JS + " path : path to the analyzed.js file";

	private String[] args;
	
	public ArgumentReader(String[] args) {
		this.args = args;
	}

	public Arguments read() {
		String apkPath = null, decompiledProjectPath = null, filtersPath = null, resultPath = null;

		for (int i = 0; i < args.length; i++) {
			if (i < args.length - 1) {
				if (args[i].equals(ARG_APK)) {
                    apkPath = args[i + 1];
				} else if (args[i].equals(ARG_PROJ)) {
					decompiledProjectPath = args[i + 1];
				} else if (args[i].equals(ARG_FILTER)) {
					filtersPath = args[i + 1];
                } else if (args[i].equals(ARG_RES_JS)) {
					resultPath = args[i + 1];
				}
			}
		}
		if (apkPath == null || decompiledProjectPath == null || resultPath == null) {
			System.err.println(ARG_APK + ", " + ARG_PROJ + " and " + ARG_RES_JS + " must be provided!");
			System.err.println(USAGE_STRING);
			return null;
		}

		if (!checkFiles(new String[] {apkPath})) {
			return null;
		}
		if (filtersPath != null && !checkFiles(new String[] {filtersPath})) {
			return null;
		}

		return new Arguments(apkPath, decompiledProjectPath, filtersPath, resultPath);
	}

	private boolean checkFiles(String[] files) {
		for (String fileName: files) {
			File file = new File(fileName);
			if (!file.exists()) {
				System.err.println(file + " is not found!");
				return false;
			}
		}
		return true;
	}
}
