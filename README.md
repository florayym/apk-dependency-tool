# Apk Dependency Graph (ADG)

**Class / package** dependency visualizer. Only `.apk` file is needed.  
Class coupling is one of the significant code metrics that shows how easy is to change, maintain and test the code. This tool helps to view whole picture of the project.

**Table of contents**
* [Usage](#Usage)
* [Compile](#Compile)
* [Demos](#Demos)
* [Credits](#Credits)

## Usage

Some helpful scripts are prepared for you. All you need to do is to download and unpack [the latest release](https://github.com/alexzaitsev/apk-dependency-graph/releases) and type the next command in your command line:  

### Filter Setting

Create your `filter.json`, set the following fields:

```json
{
    "filter-by-class": false,
    "package-scale": 4,
    "package-range": "com/huawei/android",
    "package-name": "",
    "show-inner-objects": false,
    "ignored-objects": ["java.*", "javax.*", ".*Dagger.*", ".*Inject.*", ".*ViewBinding$", ".*Factory$", ".*_.*", "^R$", "^R\\$.*"]
}
```

### Run

*For Windows*:

```shell
run.bat full\path\to\your\app-release.apk full\path\to\your\filter.json
```

Where:
* `run.bat` is a path to script in your local repository
* `full\path\to\your\app-release.apk` is a full path to the apk file you want to analyze
* `full\path\to\your\filter.json` is a full path to the filter file

The tool is provided with the [default filterset](https://github.com/alexzaitsev/apk-dependency-graph/blob/master/filters/default.json). However, you're **highly encouraged** to customize it. Read [filter instructions](https://github.com/alexzaitsev/apk-dependency-graph/blob/master/filters/instructions.txt) for the details.

*For IDE, run `com.alex_zaitsev.adg.Main` with program configurations*:

```shell
-i full\path\to\output\directory\decompiled-project
-o full\path\to\working\directory\apk-dependency-graph\gui\analyzed.js
-a full\path\to\your\app-release.apk
-f full\path\to\your\filter.json
```

*For Unix*:

```shell
./run.sh full/path/to/the/apk/app-release.apk full/path/to/the/filterset.json
```

Wait until the command finishes:

```shell
Baksmaling classes.dex...
Baksmaling classes2.dex...
    :
    :
Baksmaling classes*.dex...
Analyzing dependencies...
Success! Now open gui/index.html in your browser.
```

It will decompile your apk and create `output/directory/decompiled-project` folder in the same folder where the script is. After this it will
analyze the smali code and generate `gui/analyzed.js` file which contains all dependencies.  
**Now open `gui/index.html` in your browser and enjoy!**

## Compile

At least **Java 8** is needed to compile and run the `.jar` file.

Classes will be generated to `build/classes` folder and `.jar` file will appear in the `build/libs` folder.

## Demos

## Credits

Thanks to [alexzaitsev/apk-dependency-graph](https://github.com/alexzaitsev/apk-dependency-graph). which has done a
 fantastic job in analysing dependencies based on class.  
There is the same tool for iOS: [PaulTaykalo/objc-dependency-visualizer](https://github.com/PaulTaykalo/objc-dependency-visualizer)  
I have used `gui/index.html` of that project. Thanks Paul for the great tool.