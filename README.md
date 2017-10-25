# SoundTouch_AndroidStudio
SoundTouch 是一个音频变速变调处理库，可以直接对PCM编码音频进行实时处理或文件处理。当前项目是从SoundTouch official site中下载，转移到Android Studio中使用的库。

## Gradle
```java
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
```java
dependencies {
    compile 'com.github.ashqal:SoundTouch_AndroidStudio:1.0.0'
}
```