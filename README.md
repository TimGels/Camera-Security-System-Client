# Camera-Security-System-Client

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