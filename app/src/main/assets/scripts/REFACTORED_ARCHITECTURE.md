\# BEAR-LOADER Refactored Architecture Guide



\## 🎯 Overview



The BEAR-LOADER codebase has been completely refactored into a clean, professional architecture with proper separation of concerns, interfaces, and modular design.



\## 📁 New Package Structure



```

com.bearmod.download/

├── interfaces/           # Clean interfaces

│   ├── IDownloadManager.java

│   └── IKeyAuthAPI.java

├── impl/                # Implementations

│   ├── BearDownloadManager.java

│   └── KeyAuthAPIImpl.java

├── tasks/               # Task execution

│   └── DownloadTask.java

├── config/              # Configuration management

│   └── FileMapping.java

└── utils/               # Utilities

&nbsp;   ├── BearDownloadUtils.java

&nbsp;   └── InstallUtils.java

```



\## 🚀 Usage Examples



\### Simple Game Resource Download

```java

// Download all resources for PUBG Global

BearDownloadUtils.downloadGameResources(context, "com.tencent.ig");

```



\### Custom Download with Progress Tracking

```java

IDownloadManager downloadManager = BearDownloadManager.getInstance(context);



IDownloadManager.DownloadRequest request = new IDownloadManager.DownloadRequest(

&nbsp;   "custom\_download",

&nbsp;   "Custom File", 

&nbsp;   "420371", // KeyAuth file ID

&nbsp;   "/path/to/file.zip",

&nbsp;   IDownloadManager.DownloadType.CUSTOM

);



downloadManager.downloadWithUI(request, new IDownloadManager.DownloadCallback() {

&nbsp;   @Override

&nbsp;   public void onComplete(String downloadId, File file) {

&nbsp;       FLog.info("✅ Download completed: " + file.getName());

&nbsp;   }

&nbsp;   

&nbsp;   @Override

&nbsp;   public void onError(String downloadId, String error) {

&nbsp;       FLog.error("❌ Download failed: " + error);

&nbsp;   }

});

```



\## 🔑 KeyAuth File ID Setup



\### Update File Mappings

In `FileMapping.java`, update with your actual KeyAuth file IDs:



```java

// Replace example IDs with your actual KeyAuth file IDs

PATCH\_CONFIG\_IDS.put("com.tencent.ig", "YOUR\_ACTUAL\_CONFIG\_ID");

STEALTH\_LIB\_IDS.put("com.tencent.ig", "YOUR\_ACTUAL\_STEALTH\_ID");

```



\## 🎯 Benefits



| Aspect | Before | After |

|--------|--------|-------|

| \*\*Interfaces\*\* | Tightly coupled | Clean interfaces |

| \*\*Threading\*\* | AsyncTask (deprecated) | Modern ExecutorService |

| \*\*Error Handling\*\* | Basic try-catch | Comprehensive callbacks |

| \*\*Configuration\*\* | Hardcoded values | Centralized FileMapping |

| \*\*Maintainability\*\* | Monolithic classes | Modular components |



This refactored architecture provides a solid foundation for professional development! 🏆 

