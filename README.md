## Setup
In order to run the app properly you need to make your api token available.
Add `gpr.pixabay.apikey = {API_KEY}` to your global Gradle properties `~/.gradle/gradle.properties`

``` shell
gpr.pixabay.apikey=API_KEY
```

## Commands
Run tests via:
```shell
./gradlew check
```

Run instrumented tests via (with a running emulator):
```shell
./gradlew :app-android:connectedCheck
```
