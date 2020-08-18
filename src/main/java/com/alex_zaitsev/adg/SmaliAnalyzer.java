package com.alex_zaitsev.adg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alex_zaitsev.adg.io.Arguments;
import com.alex_zaitsev.adg.io.Filters;
import com.alex_zaitsev.adg.filter.Filter;

import static com.alex_zaitsev.adg.util.CodeUtils.isClassGenerated;
import static com.alex_zaitsev.adg.util.CodeUtils.isClassInner;
import static com.alex_zaitsev.adg.util.CodeUtils.getOuterClass;
import static com.alex_zaitsev.adg.util.CodeUtils.isClassAnonymous;
import static com.alex_zaitsev.adg.util.CodeUtils.getAnonymousNearestOuter;
import static com.alex_zaitsev.adg.util.CodeUtils.getEndGenericIndex;
import static com.alex_zaitsev.adg.util.CodeUtils.getClassSimpleName;
import static com.alex_zaitsev.adg.util.CodeUtils.isInstantRunEnabled;
import static com.alex_zaitsev.adg.util.CodeUtils.isSmaliFile;
import static com.alex_zaitsev.adg.util.CodeUtils.getPackage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SmaliAnalyzer {

	private Arguments arguments;
	private Filters filters;
	private Filter<String> pathFilter;
	private Filter<String> classPathFilter;
	private Filter<String> ignoredFilter;

	public SmaliAnalyzer(Arguments arguments, 
						 Filters filters,
						 Filter<String> pathFilter,
						 Filter<String> classPathFilter,
						 Filter<String> ignoredFilter) {
		this.arguments = arguments;
		this.filters = filters;
		this.pathFilter = pathFilter;
		this.classPathFilter = classPathFilter;
		this.ignoredFilter = ignoredFilter;
	}

	private Map<String, Set<String>> dependencies = new HashMap<>();

	public boolean run() {
		System.out.println("Analyzing dependencies...");

		File projectDir = new File(arguments.getDecompiledProjectPath());
		if (projectDir.exists()) {
			if (isInstantRunEnabled(arguments.getDecompiledProjectPath())) {
				System.err.println("Enabled Instant Run feature detected. " +
					"We cannot decompile it. Please, disable Instant Run and rebuild your app.");
			} else {
				traverseSmaliCodeDir(projectDir);
				return true;
			}
		} else {
			System.err.println(projectDir + " does not exist!");
		}
		return false;
	}

	private void traverseSmaliCodeDir(@NotNull File dir) {
		File[] listOfFiles = dir.listFiles();
		assert listOfFiles != null;
		for (int i = 0; i < listOfFiles.length; i++) {
			File currentFile = listOfFiles[i];
			if (isSmaliFile(currentFile)) {
				if (isPathFilterOk(currentFile)) { // IMP: exclude files that is outside the range
					processSmaliFile(currentFile);
				}
			} else if (currentFile.isDirectory()) {
				traverseSmaliCodeDir(currentFile);
			}
		}
	}

	private boolean isPathFilterOk(@NotNull File file) {
		return isPathFilterOk(file.getAbsolutePath());
	}

	private boolean isPathFilterOk(String filePath) {
		return pathFilter == null || pathFilter.filter(filePath);
	}

	@Contract("null -> true")
	private boolean isClassPathFilterOk(String classPath) {
		return classPath == null || classPathFilter.filter(classPath);
	}

	private boolean isIgnoredFilterOk(String objectName) {
		return ignoredFilter == null || ignoredFilter.filter(objectName);
	}

	/**
	 * The last filter. Do not show anonymous classes (their dependencies belongs to outer class),
	 * generated classes, avoid circular dependencies
	 * @param simpleClassName class name to inspect
	 * @param fileName full class name
	 * @return true if class is good with these conditions
	 */
	private boolean isClassOk(String simpleClassName, String fileName) {
		return !isClassAnonymous(simpleClassName) && !isClassGenerated(simpleClassName)
				&& !fileName.equals(simpleClassName); // therefore a -> a will not happen
	}

	private void processSmaliFile(@NotNull File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));

			// get the first line, starts with .class, denotes the class name and the package it belongs to.
			String packageName = getPackage(true, filters.getScale(), br.readLine());

			// Anonymous class: Java can just new an interface,
			// and insert the implementation code into the block behind the new,
			// which is seen as a class with the name, ClassName$1, the char after the last $ must be a number
			if (isClassAnonymous(fileName)) {
				fileName = getAnonymousNearestOuter(fileName); // e.g. from a$b$2$1 get a$b
			}

			if (!isIgnoredFilterOk(fileName)) { // FIXME: this condition, the input fileName, is problematic
				return;
			}

			Set<String> classNames = new HashSet<>();
			Set<String> dependencyNames = new HashSet<>();

			for (String line; (line = br.readLine()) != null;) {
				try {
					classNames.clear();

					parseAndAddClassNames(classNames, line);

					// filtering
					for (String fullClassName : classNames) {
						if (fullClassName == null) {
							continue;
						}

						if (filters.getFilterByClass()) {
							/* NOTE FOR CLASS FILTERING */
							if (isClassPathFilterOk(fullClassName)) {
								String simpleClassName = getClassSimpleName(fullClassName);
								if (isIgnoredFilterOk(simpleClassName) && isClassOk(simpleClassName, fileName)) {
									dependencyNames.add(simpleClassName);
								}
							}
						} else {
							/* NOTE For package filtering */
							String dependencyPackageName = getPackage(false, filters.getScale(), fullClassName);
							if (isIgnoredFilterOk(dependencyPackageName) && isClassOk(getClassSimpleName(fullClassName), fileName)
									&& isClassPathFilterOk(dependencyPackageName)) {
								dependencyNames.add(dependencyPackageName);
							}
						}
					}
				} catch (Exception e) {
					System.err.println("Error '" + e.getMessage() + "' occurred.");
				}
			}

			// inner/nested class always depends on the outer class
			if (filters.getFilterByClass() && isClassInner(fileName)) {
				dependencyNames.add(getOuterClass(fileName));
			}

			if (!dependencyNames.isEmpty()) {
				if (filters.getFilterByClass()) {
					addDependencies(fileName, dependencyNames);
				} else {
					addDependencies(packageName, dependencyNames);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Cannot found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Cannot read " + file.getAbsolutePath());
		}
	}

	/**
	 * After adding scale, the number of dependency is actually decreasing by a large number,
	 * So, how to optimize, that is, to skip the hundreds of repetitive loops.
	 *
	 * The direction of scaling. --> or <--
	 *
	 * @param classNames provide all the class names that are in this line of smali
	 * @param line of smali file
	 */
	private void parseAndAddClassNames(Set<String> classNames, @NotNull String line) {
		int index = line.indexOf("L");
		while (index != -1) {
			int colonIndex = line.indexOf(";", index);
			if (colonIndex == -1) {
				break;
			}

			String className = line.substring(index + 1, colonIndex);
			if (className.matches("[\\w\\d/$<>]*")) {
				int startGenericIndex = className.indexOf("<");
				if (startGenericIndex != -1 && className.charAt(startGenericIndex + 1) == 'L') { // FIXME: this condition will never be met?!
					// generic
					int startGenericInLineIndex = index + startGenericIndex + 1; // index of "<" in the original string
					int endGenericInLineIndex = getEndGenericIndex(line, startGenericInLineIndex);
					String generic = line.substring(startGenericInLineIndex + 1, endGenericInLineIndex);
					parseAndAddClassNames(classNames, generic);
					index = line.indexOf("L", endGenericInLineIndex);
					className = className.substring(0, startGenericIndex);
				} else {
					index = line.indexOf("L", colonIndex);
				}
			} else {
				index = line.indexOf("L", index+1);
				continue;
			}

			classNames.add(className);
		}
	}

	private void addDependencies(String className, Set<String> dependenciesList) { // TODO: className
		Set<String> depList = dependencies.get(className);
		if (depList == null) {
			// add this class and its dependencies
			dependencies.put(className, dependenciesList);
		} else {
			// if this class is already added - update its dependencies
			depList.addAll(dependenciesList);
		}
	}

	public Map<String, Set<String>> getDependencies() {
		if (!filters.getFilterByClass() || filters == null || filters.isProcessingInner()) {
			return dependencies;
		}
		return getFilteredDependencies();
	}

	@NotNull
	private Map<String,Set<String>> getFilteredDependencies() {
		Map<String, Set<String>> filteredDependencies = new HashMap<>();
		for (String key : dependencies.keySet()) {
			if (!key.contains("$")) {
				Set<String> dependencySet = new HashSet<>();
				for (String dependency : dependencies.get(key)) {
					if (!dependency.contains("$")) {
						dependencySet.add(dependency);
					}
				}
				if (dependencySet.size() > 0) {
					filteredDependencies.put(key, dependencySet);
				}
			}
		}
		return filteredDependencies;
	}
}
