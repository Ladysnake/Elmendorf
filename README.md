# Elmendorf

A set of helpers for dealing with Minecraft's test framework in Fabric/Quilt mods.
The current version only includes a fix for "flaky tests" - that is, test cases that can be run more than once.

## Adding Elmendorf to your project

You can add the library by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { 
        name = "Ladysnake Mods"
        url = "https://maven.ladysnake.org/releases"
        content {
            includeGroup 'io.github.ladysnake'
            includeGroup 'org.ladysnake'
            includeGroupByRegex 'io\\.github\\.onyxstudios.*'
        }
    }
}

dependencies {
    modImplementation "org.ladysnake:elmendorf:${elmendorf_version}"
}
```

You can then add the library version to your `gradle.properties`file:

```properties
elmendorf_version = 0.x.y
```
