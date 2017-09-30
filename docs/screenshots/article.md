# RxTransfer - Easy HTTP download with Android 

Hi everybody, 

I finally found a little time to publish my first article on **Medium**, and a little occasion for it: presenting you a tiny new library idea for Android. 

**RxTransfer** is meant to simplify downloading files through **HTTP protocol**, and **track** downloading progress. 

When downloading a file on new Android devices, we normally need to take care of multiple concerns : 

- ask **writing permissions** for storing the file at runtime in new Android devices 
- perform the operation in a **background thread** and avoid memory leaks 
- **publish** progress updates from within the **background thread** to affect some UI component 
- handle connection or other possible **errors** 
- show a positive message on successful completed operation 

All this can be done with a few lines of code through **RxTransfer** : 

```java
  new RxDownload()
      .activity(this)
      .saveTo(filePath)
      .listener(progress -> { // Progress update: affect UI Component
          progressBar.setProgress(progress);

          if(progress == 100)
              DialogBuilder.showMessage("Operation Completed", mLayout);
      })
      .url(url) // url of File to download
      .start();
```

In case you don't wanna implement your own **UI**, but just show a basic **ProgressDialog** for keeping the user informed of the download operation, you may use 

```java
  new RxDownload()
      .activity(this)
      .saveTo(filePath)
      .url(url)
      .showDialog()
      .completedMessage("Operation Completed", mLayout)
      .start();
```

### Future Improvements 
- **RxUpload** class 
- **RxLoader** library integration to bind to activity lifecycle and handle orientation change
- propose a set of default **UI widgets** to directly include within the layout
