package com.alex_zaitsev.adg;

import com.alex_zaitsev.adg.io.*;
import com.alex_zaitsev.adg.filter.*;

import java.io.File;
import java.util.regex.Matcher;

public class FilterProvider {

    private Filters inputFilters;

    public FilterProvider(Filters inputFilters) {
        this.inputFilters = inputFilters;
    }

    /**
     * include filter
     * @return RegexFilter filter
     */
    public Filter<String> makePathFilter() {
        String replacement = Matcher.quoteReplacement(File.separator);
	    replacement = Matcher.quoteReplacement(replacement);
        // file/path/on/windows/looks/like/this
        // Reference: https://github.com/alexzaitsev/apk-dependency-graph/issues/60#issuecomment-565487508
        String packageRangeAsPath = inputFilters.getPackageRange().replaceAll("/", replacement); // Regex for Unix: \\.
        String packageRangeRegex = ".*" + packageRangeAsPath + ".*";

        return new RegexFilter(packageRangeRegex);
    }

    /**
     * for filtering full class names and package names
     * @return RegexFilter filter
     */
    public Filter<String> makeClassPathFilter() {
        return new RegexFilter(".*" + inputFilters.getPackageRange() + ".*");
    }

    /**
     * exclude filter
     * @return InverseRegexFilter filter, the opposite of RegexFilter.
     */
    public Filter<String> makeIgnoredFilter() {

        // when being filtered by package and nothing will be ignored
//        if (!inputFilters.getFilterByClass()) {
//            return null;
//        }

        String[] ignoredObjects = inputFilters.getIgnoredObjects();
        if (ignoredObjects == null) {
            return null;
        }

        return new InverseRegexFilter(ignoredObjects);
    }
}