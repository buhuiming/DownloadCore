# DownloadCore

#### 支持设置多文件同时下载
#### 支持设置仅WiFi时下载
#### 支持设置后台下载，自定义Notification
#### 支持设置重复文件下载

![55f2ed59eaf3137d536a84151d78e63](https://user-images.githubusercontent.com/30099293/197972403-47566ee6-6d92-4e5d-a5b6-f099b1197131.jpg)



### 用法

        allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
        }

        dependencies {
            implementation 'com.github.buhuiming:DownloadCore:1.0.2'
        }

#### 1、 初始化配置
        val parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        val downloadRequest = DownloadRequest(context)
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(3)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setLogger(true)
            .setDownloadParentPath(parentPath)
            .setDownloadOverWiFiOnly(true) //仅WiFi时下载
            .setDownloadInTheBackground(null, 111111)//传空，则退出APP，停止下载；传Notification，则开启前台Service下载
            .build()
        downloadRequest?.newRequest(downloadConfig)

        //为每一个下载 添加下载监听
        downloadRequest?.registerCallback(fileName, object : DownloadObserver(context) {
            override fun onInitialize(dLFModel: DownLoadFileModel) {
                super.onInitialize(dLFModel)
                Timber.d("onInitialize: " + dLFModel.downLoadUrl)
             }

            override fun onWaiting(dLFModel: DownLoadFileModel) {
                super.onWaiting(dLFModel)
                Timber.d("onWaiting: " + dLFModel.downLoadUrl)
            }

            override fun onStop(dLFModel: DownLoadFileModel) {
                super.onStop(dLFModel)
                Timber.d("onStop")
            }

            override fun onComplete(dLFModel: DownLoadFileModel) {
                super.onComplete(dLFModel)
                Timber.d("onComplete")
            }

            override fun onProgress(dLFModel: DownLoadFileModel) {
                super.onProgress(dLFModel)
                Timber.d("url: " + dLFModel.downLoadUrl)
                Timber.d(
                    "totalLength: " + dLFModel.totalLength.toString() + ", totalReadBytes: " +
                            dLFModel.downLoadLength.toString() + ", progress: " + dLFModel.progress
                )
            }

            override fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
                super.onFail(dLFModel, throwable)
                Timber.e("onFail" + throwable.message)
            }
        })
#### 2、 添加、开始下载
        downloadRequest?.startDownload(url, fileName)
         
#### 3、 重新下载
        downloadRequest?.reStartDownload(url, fileName)
         
#### 4、 暂停下载
        downloadRequest?.pauseDownload(url, fileName)
         
#### 5、 删除下载(文件会被删除)
        downloadRequest?.deleteDownload(url, fileName)

#### 6、 检查下载是否完成
        DownLoadUtil.checkExistFullFile(context, fileName, parentPath)

#### 7、 检查是否存在未完成下载(0 < progress < 100)
        DownLoadUtil.getExistFileProgress(context, fileName, parentPath)

#### 7、 取消某个下载监听，关闭所有下载监听
        downloadRequest?.unRegisterCallback(fileName)

        downloadRequest?.close()

#### 7、 更多请参考Demo

### TO DO

#### 1、 网络变化监控
