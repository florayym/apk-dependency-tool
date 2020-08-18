package com.alex_zaitsev.adg.io;

import com.alex_zaitsev.adg.filter.Filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FiltersReader {

    private static final String FILTER_BY_CLASS = "filter-by-class";
    private static final String FILTER_PACKAGE_SCALE = "package-scale";
    private static final String FILTER_PACKAGE_RANGE = "package-range";
    private static final String FILTER_PACKAGE_NAME = "package-name";
	private static final String FILTER_SHOW_INNER_OBJECTS = "show-inner-objects";
    private static final String FILTER_IGNORED_OBJECTS = "ignored-objects";

    private String filtersFilePath;

    public FiltersReader(String filtersFilePath) {
        this.filtersFilePath = filtersFilePath;
    }

    /**
     * Parses the your filter.json file and produces Filters object according to your filter standard.
     */
    public Filters read() {
        boolean filer_by_class = Filters.DEFAULT_FILER_BY_CLASS;
        int scale = 0;
        String packageRange = null;
        String packageName = null;
        boolean showInnerClasses = Filters.DEFAULT_PROCESS_INNER;
        String[] ignoredObjectsArr = null;

		try {
			String content = new String(Files.readAllBytes(Paths.get(filtersFilePath)));
			String mainObject = content.replace("{", "").replace("}", "").trim();
			String[] rawParams = mainObject.split(",");

			for (String rawParam: rawParams) {
			    if (rawParam.contains(FILTER_BY_CLASS)) {
			        filer_by_class = Boolean.parseBoolean(rawParam.trim().split(":")[1].trim());
                }
			    if (rawParam.contains(FILTER_PACKAGE_SCALE)) {
			        scale = Integer.parseInt(rawParam.trim().split(":")[1].trim());
                }
				if (rawParam.contains(FILTER_PACKAGE_RANGE)) {
                    packageRange = rawParam.trim().split(":")[1].trim().replace("\"", "");
                }
				if (rawParam.contains(FILTER_PACKAGE_NAME)) {
				    // TODO add packageName usage
				    packageName = rawParam.trim().split(":")[1].trim().replace("\"", "");
                }
                if (rawParam.contains(FILTER_SHOW_INNER_OBJECTS)) {
                    showInnerClasses = Boolean.parseBoolean(rawParam.trim().split(":")[1].trim().replace("\"", ""));
                }
            }
            if (mainObject.contains(FILTER_IGNORED_OBJECTS)) {
                String ignoredObjects = mainObject.substring(mainObject.indexOf('[') + 1, mainObject.lastIndexOf(']'));
                ignoredObjectsArr = ignoredObjects.split(",");
                for (int i = 0; i < ignoredObjectsArr.length; i++) {
                    ignoredObjectsArr[i] = ignoredObjectsArr[i].replace("\"", "").trim();
                }
            }
		} catch (Exception e) {
			System.err.println("An error happened during " + filtersFilePath + " processing!");
			e.printStackTrace();
			return null;
        }

        if (packageRange == null || packageRange.isEmpty()) { // FIXME what is the problem of package-range being empty?
            System.err.println("Warning! 'package-range' option cannot be empty. Check " + filtersFilePath + ".");
        }
        if (ignoredObjectsArr == null) {
            System.out.println("Warning! Processing without excluding any classes or packages.");
        }

		if (filer_by_class) {
            if (showInnerClasses) {
                System.out.println("Warning! Processing including inner classes.");
            }

            return new Filters(true, packageRange, showInnerClasses, ignoredObjectsArr);
        }

		if (scale < 1) {
		    System.err.println("Warning! 'scale' option should be greater than or equal 1.");
		    return null;
        }

		return new Filters(false, scale, packageRange, ignoredObjectsArr);
	}
}