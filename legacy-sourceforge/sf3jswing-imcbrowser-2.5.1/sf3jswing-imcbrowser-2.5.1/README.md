# README #

This software complies with the open source [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
* Image Multi-Converter and Browser, is a picture manager and converter for your desktop. It allows lists of files to be converted into any other picture format (i.e. JPG, GIF, PNG, BMP, TIFF, WBMP, etc.). It can also send your pictures to a web server or any supported blog/book.
* [Linux repository](https://build.opensuse.org/package/show/home:broumbroum23/sf3jswing-imcbrowser)

### What is this repository for? ###

* Anyone feels free to download and reuse the source code.
* The redistribution of this software is however limited to specific terms and conditions (see license above).

### How do I get set up? ###

* Summary of set up
It requires the latest JDK to build on any compatible operating system.
As it relies on the same build configuration, you can follow the API build instructions.
* Runtime configuration :
PC : Minimum configuration : PC with Windows 7 or higher. Java will be installed.
MAC : Minimum configuration : an Intel Mac with Mac OS X.
* Dependencies :
sf3jswing-imcrowser depends on [es-sdk-jdk-binding](https://bitbucket.org/b23prodtm/es-sdk-jdk-binding), sf3jswing-jigaxtended and sf3jswing-jxakernel API, which can be found at sourceforge.
* How to run tests :
Go to the folder where it was extracted to and after a successful build run the command "ant run" or "ant run-applet" to test.
* Deployment instructions :
Go to the folder where it was extracted to and after a successful build run the command "ant install" to get a distributable package (look at the dist/ folder). 
Linux systems will however copy themselves into /usr/share/games/ with "ant install".

### Contribution guidelines ###

You can fork this repository or create a [pull request](https://bitbucket.org/b23prodtm/sf3jswing-imcbrowser/pull-requests) if you mind contributing with new features. 

### Author ###
Tiana Rakoto.