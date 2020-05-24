# ambient [![Build Status](https://travis-ci.com/akainth015/ambient.svg?branch=master)](https://travis-ci.com/akainth015/ambient)
A plugin for IntelliJ that enables assignment import and submission

Written with Kotlin, designed with Java + IntelliJ Form Designer. Build orchestrated by Gradle.

To start helping with the plugin, just start editing the source files.
To test the plugin, run `./gradlew runIde`, which will launch IntelliJ Community Edition with the version specified in `build.gradle.kts`.

The plugin will automatically be published upon merge to `master` by Travis.

## How to Use Ambient
### Snarfing
You can snarf by getting the URL for your snarf site and then pasting it into the field in the File > New > Module dialog.
![](images/snarf.png)

### Submitting to WebCAT
Get your submission URL from Home > My Profile > Personalized Service URLs. Copy the URL from Eclipse, and paste that into the assignment source field of the Tools > Submit Assignment Dialog.

![](images/submit.png) 