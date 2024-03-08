
[Documentation](https://pro4j.com) ·
[QuickStart](https://pro4j.com/quickstart.html)

[Android Examples](apps/app-marbles/src/main/java/com/pro4j/marbles/MainActivity.java#L29) ·
[Java Examples](promise/src/test/java/com/pro4j/promise/MemoryModelTest.java#L37) ·
[Kotlin Examples](promise/src/test/kotlin/com/pro4j/promise/KtBasicTest.kt) ·
[Java Extension](promise/src/main/java/com/pro4j/promise/PromiseExtJ.java) ·
[Kotlin Extension](promise/src/main/kotlin/com/pro4j/promise/extension.kt)

## Usage
- `.jar` promise library for java platform.
- `.aar` promise library for android platform (function is the same, just add minsdkversion above 23 restriction only).

## Folders
```
|--apps
|  Android sample app 
|--promise
|  Java Playground(write a unittest to take a quick experience)
```

## Commands
|  Command            |                                                                                     |
| ---------------- | --------------------------------------------------------------------------------------- |
| ./gradlew :pro4j-promise:testDebugUnitTest | Run unittest |
| ./gradlew :app-marbles:installDebug | Install on android devices |
|./gradlew :pro4j-promise:cleanTestDebugUnitTest  :pro4j-promise:testDebugUnitTest| Run unitTest with code coverage |
