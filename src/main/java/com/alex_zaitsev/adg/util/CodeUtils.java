package com.alex_zaitsev.adg.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class CodeUtils {

	@Contract(value = "null -> false", pure = true)
	public static boolean isClassGenerated(String className) {
		return className != null && className.contains("$$");
	}

	@Contract("null -> false")
	public static boolean isClassInner(String className) {
		return className != null && className.contains("$") && !isClassAnonymous(className) && !isClassGenerated(className);
	}

	@NotNull
	public static String getOuterClass(@NotNull String className) {
		return className.substring(0, className.lastIndexOf("$"));
	}

	@Contract("null -> false")
	public static boolean isClassAnonymous(String className) {
		return className != null && className.contains("$")
				&& StringUtils.isNumber(className.substring(className.lastIndexOf("$") + 1, className.length()));
	}

	@Nullable
	public static String getAnonymousNearestOuter(@NotNull String className) {
		String[] classes = className.split("\\$");
		for (int i = 0; i < classes.length; i++) {
			if (StringUtils.isNumber(classes[i])) {
				String anonHolder = "";
				for (int j = 0; j < i; j++) {
					anonHolder += classes[j] + (j == i - 1 ? "" : "$");
				}
				return anonHolder;
//				StringBuilder anonHolder = new StringBuilder();
//				for (int j = 0; j < i; j++) {
//					anonHolder.append(classes[j]).append("$");
//				}
//				return anonHolder.substring(0, anonHolder.length() - 1).toString();
			}
		}
		return null;
	}

	public static int getEndGenericIndex(String line, int startGenericIndex) {
		int endIndex = line.indexOf(">", startGenericIndex);
		for (int i = endIndex + 2; i < line.length(); i += 2) {
			if (line.charAt(i) == '>') {
				endIndex = i;
			}
		}
		return endIndex;
	}

	public static String getClassSimpleName(@NotNull String fullClassName) {
		String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf("/") + 1,
				fullClassName.length());
		int startGenericIndex = simpleClassName.indexOf("<");
		if (startGenericIndex != -1) {
			simpleClassName = simpleClassName.substring(0, startGenericIndex);
		}
		return simpleClassName;
	}

	public static boolean isInstantRunEnabled(String projectPath) {
		File unknownDir = new File(projectPath, "unknown");
		if (unknownDir.exists() && unknownDir.isDirectory()) { // false and false
			for (File file : Objects.requireNonNull(unknownDir.listFiles())) {
				if (file.getName().equals("instant-run.zip")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSmaliFile(@NotNull File file) {
		return file.isFile() && file.getName().endsWith(".smali");
	}

	@NotNull
	public static String getPackage(boolean raw, int scale, @NotNull String classLine) {
		int beginIndex = raw ? classLine.indexOf('L') + 1 : 0;
		String packageName = classLine.substring(beginIndex, classLine.lastIndexOf('/') + 1);

		// NOTE from left to right
		int endIndex = 0;
		int theLastIndex = packageName.length() - 1;
		while (endIndex < theLastIndex && scale > 0) {
			endIndex = packageName.indexOf('/', endIndex + 1);
			scale--;
		}
		return packageName.substring(0, endIndex);

		// NOTE from right to left
//		String scaledPackageName = packageName;
//		int lastIndex = 0;
//		for (int i = 0; i < scale; i++) {
//			lastIndex = scaledPackageName.lastIndexOf('/');
//			if (lastIndex == -1){
//				break;
//			}
//			scaledPackageName = scaledPackageName.substring(0, lastIndex);
//		}
//		return packageName.substring(lastIndex + 1);
	}
}
