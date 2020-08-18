package com.alex_zaitsev.adg.io;

public class Filters {

    public static final int DEFAULT_SCALE = 1;
    public static final boolean DEFAULT_FILER_BY_CLASS = true;
    public static final boolean DEFAULT_PROCESS_INNER = false;

    private boolean filter_by_class = DEFAULT_FILER_BY_CLASS;
    private int scale = DEFAULT_SCALE;
    private String packageRange = null;
    private String packageName = null;
    private boolean processingInner = DEFAULT_PROCESS_INNER;
    private String[] ignoredObjects = null;

    public Filters(boolean filer_by_class, String packageRange, boolean processingInner, String[] ignoredObjects) {
        this.filter_by_class = filer_by_class;
        this.packageRange = packageRange;
        this.processingInner = processingInner;
        this.ignoredObjects = ignoredObjects;
    }

    // Added one constructor function with different parameters for filter-by-class set to false.
    public Filters(boolean filer_by_class, int scale, String packageRange, String[] ignoredObjects) {
        this.filter_by_class = filer_by_class;
        this.scale = scale;
        this.packageRange = packageRange;
        this.ignoredObjects = ignoredObjects;
    }

    public boolean getFilterByClass() {
        return filter_by_class;
    }

    public int getScale() {
        return scale;
    }

    public String getPackageRange() {
        return packageRange;
    }

    public void setPackageRange(String packageRange) {
        this.packageRange = packageRange;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isProcessingInner() {
        return processingInner;
    }

    public void setProcessingInner(boolean isProcessingInner) {
        this.processingInner = isProcessingInner;
    }

    public String[] getIgnoredObjects() {
        return ignoredObjects;
    }

    public void setIgnoredObjects(String[] ignoredObjects) {
        this.ignoredObjects = ignoredObjects;
    }
}