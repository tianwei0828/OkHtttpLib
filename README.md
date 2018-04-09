# OkHtttpLib
#### 基于OkHttp链式调用的网络请求库，支持同步的GET 、POST；异步的GET、POST；同时支持多线程断点续传以及针对下载前手机存储空间的判断，下载后MD5或者SHA1验证。
说明：
  所有回调都是在UI线程
#### 使用说明
```java
在Application中初始化
OkHttpRequest.init(this);
```

```java
同步GET请求
 //返回的是Bean
 Bean  bean =  OkHttpRequest.get("url")
                .tag("请求的tag,可以根据tag取消请求")
                .handers("请求头")
                .beanClass("封装请求返回的javaBean")
                .build()
                .executeToBean();

 //返回的是String
 String result =OkHttpRequest.get("url")
                .tag("请求的tag,可以根据tag取消请求")
                .handers("请求头")
                .build()
                .executeToString();
```
```java
异步的GET请求
//返回的是Bean
OkHttpRequest.get("url")
                .tag("请求的tag,可以根据tag取消请求")
                .handers("请求头")
                .beanClass("封装请求返回的javaBean")
                .build()
                .enqueueToBean(new BeanCallBack<"封装请求返回的javaBean">() {
                    @Override
                    public void onSuccess(Object callback) {
                        
                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });
 //返回的是String                
 OkHttpRequest.get("url")
                .tag("请求的tag,可以根据tag取消请求")
                .handers("请求头")
                .build()
                .enqueueToString(new StringCallBack() {
                    @Override
                    public void onSuccess(String response) {
                        
                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });
```

```java
同步POST
//返回的是Bean
Bean bean = OkHttpRequest.post("url")
                .tag("tag")
                .handers("handers")
                .json("json")      //上传的是Json
                .params("params")  //上传的是键值对
                .beanClass("bean")
                .build()
                .executeToBean();
 //返回的是String                  
 String string = OkHttpRequest.post("url")
                .tag("tag")
                .handers("handers")
                .json("json")      //上传的是Json
                .params("params")  //上传的是键值对
                .build()
                .executeToString();
```

```java
异步的POST
//返回的是Bean
OkHttpRequest.post("url")
                .tag("tag")
                .handers("handers")
                .json("json")      //上传的是Json
                .params("params")  //上传的是键值对
                .beanClass("bean")
                .build()
                .executeToBean(new BeanCallBack<Bean>(){
                    @Override
                    public void onSuccess(Bean callback) {
                        
                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });
//返回的是String        
OkHttpRequest.post("url")
                .tag("tag")
                .handers("handers")
                .json("json")      //上传的是Json
                .params("params")  //上传的是键值对
                .build()
                .executeToString(new StringCallBack(){

                    @Override
                    public void onSuccess(String response) {
                        
                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });
```

```java
文件上传
OkHttpRequest.upload("url")
                .tag("tag")
                .handers("handers")
                .params("params")
                .file("Pair")          //根据file上传
                .file("Pair")          //上传多个文件只需添加多个file
                .filePath("path")      //根据文件路径上传  
                .build()
                .upload(new UploadCallBack() {
                    @Override
                    public void onTimeOut() {
                        
                    }

                    @Override
                    public void onStart(long total) {

                    }

                    @Override
                    public void onLoading(long current, long total) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onSuccess(String callback) {

                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });

```

```java
文件下载
 OkHttpRequest.download("ulr")
                .tag("tag")
                .handers("handers")
                .params("params")
                .json("json")
                .name("下载后保存文件的名称")
                .target("下载文件保存的路径")
                .md5("如果需要md5校验,则传入下载文件的md5值")
                .sha1("如果需要sha1校验,则传入下载文件的sha1值")
                .build()
                .download(new DownloadCallBack() {
                    @Override
                    public void onSdCardLockMemory(long total, long avaiable) {
                        
                    }

                    @Override
                    public void onStartCheck() {

                    }

                    @Override
                    public void onCheckFailed(String msg) {

                    }

                    @Override
                    public void onStart(long total) {

                    }

                    @Override
                    public void onLoading(long current, long total) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onSuccess(String callback) {

                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });
```


```java
多线程断点下载
OkHttpRequest.multiDownload("ulr")
                .tag("tag")
                .handers("handers")
                .params("params")
                .json("json")
                .name("下载后保存文件的名称")
                .target("下载文件保存的路径")
                .threadCount("下载线程的数量")
                .md5("如果需要md5校验,则传入下载文件的md5值")
                .sha1("如果需要sha1校验,则传入下载文件的sha1值")
                .build()
                .download(new DownloadCallBack() {
                    @Override
                    public void onSdCardLockMemory(long total, long avaiable) {

                    }

                    @Override
                    public void onStartCheck() {

                    }

                    @Override
                    public void onCheckFailed(String msg) {

                    }

                    @Override
                    public void onStart(long total) {

                    }

                    @Override
                    public void onLoading(long current, long total) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onSuccess(String callback) {

                    }

                    @Override
                    public void onFailed(ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {

                    }
                });

```
#### 取消请求
```java
取消单个请求
 OkHttpRequest.cancelCallByTag("tag");
取消全部请求
 OkHttpRequest.cancelAllCalls();
```
