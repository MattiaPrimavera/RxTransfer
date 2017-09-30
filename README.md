# RxTransfer
Never been this easy to download a file while tracking downloading progress! 

**RxTransfer** automatize the following tasks : 
- create destination file if not existing
- download the file from the url in a background thread 
- show error message on error 
- show message on successfull download
- download progress exposed in different ways

## Usage
Here's the different ways in which **download progress** can be tracked:
- within a `ProgressDialog` fragment 

```java
new RxDownload()
    .activity(this)
    .saveTo(testFilePath)
    .url(url)
    .showDialog() // Show Dialog fragment including ProgressBar
    .completedMessage("Operation Completed", mLayout)
    .start();
```

- within your own `ProgressBar` instance 

```java
new RxDownload()
    .activity(this)
    .saveTo(testFilePath)
    .progressInto(progress) // Provide ProgressBar instance
    .url(url)
    .start();
``` 

- Provide a Listener to track the progress

```java
new RxDownload()
    .activity(this)
    .saveTo(testFilePath)
    .listener(progress -> { // Update your progress bar through the listener
        progressBar.setProgress(progress);

        if(progress == 100)
            DialogBuilder.showMessage("Operation Completed", mLayout);
    })
    .url(url)
    .start();
```


## Setup
Add the JitPack repository in your build.gradle (top level module):

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:

```
dependencies {
    compile 'com.github.MattiaPrimavera:RxTransfer:0.11'
}
```
