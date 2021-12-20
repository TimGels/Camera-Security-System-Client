# Camera-Security-System-Client
[![ktlint](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/ktlint.yml/badge.svg)](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/ktlint.yml)
[![Detekt](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/detekt.yml/badge.svg)](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/detekt.yml)
[![Android CI](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/android.yml/badge.svg)](https://github.com/TimGels/Camera-Security-System-Client/actions/workflows/android.yml)
 
## Using detekt locally (Windows)
The following steps are needed to use detekt on your local Windows system.
1. Install [OpenJDK](https://jdk.java.net/17/)
2. Extract the zip file
3.  Move the JDK directory (i.e. `jdk-17.0.1`) to a suitable location

    Example: 
        
        C:\Program Files\Java\jdk-17.0.1
4. Add an environment variable with the name `JAVA_HOME` and your chosen location as value
    
    Example:
        
        Variable name: JAVA_HOME
        Variable value: C:\Program Files\Java\jdk-17.0.1

5. Close all shells such as PowerShell or Command Prompt
6. Open a shell and head to the project directory root (`Camera-Security-System-Client`)
7. Test if it works by running: `.\gradlew.bat detekt`

## Checking code formatting in Android Studio
1. Install the plugin `Detekt` from the Jetbrains [marketplace](https://plugins.jetbrains.com/plugin/10761-detekt)
2. Configure the plugin by enabling both `Enable Detekt` and `Enable formatting (ktlint) rules`

    ![afbeelding](https://user-images.githubusercontent.com/43609220/146745614-d3c36a9f-77f4-4ab6-8d3c-126aa1020937.png)
