package com.alex_zaitsev.adg.io;

public class Arguments {
    
    private String apkFilePath;
    private String decompiledProjectPath;
    private String resultPath;
    private String filtersPath;

    public Arguments(String apkPath, String decompiledProjectPath, String filtersPath, String resultPath) {
        this.apkFilePath = apkPath;
        this.decompiledProjectPath = decompiledProjectPath;
        this.filtersPath = filtersPath;
        this.resultPath = resultPath;
    }

    public String getApkFilePath() {
        return this.apkFilePath;
    }

    public void setApkFilePath(String apkFilePath) {
        this.apkFilePath = apkFilePath;
    }

    public String getDecompiledProjectPath() {
        return decompiledProjectPath;
    }

    public void setDecompiledProjectPath(String decompiledProjectPath) {
        this.decompiledProjectPath = decompiledProjectPath;
    }

    public String getFiltersPath() {
        return filtersPath;
    }

    public void setFiltersPath(String filtersPath) {
        this.filtersPath = filtersPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }
}
