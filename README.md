# DownloadCore

### 用法

        allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
        }

        dependencies {
            implementation 'com.github.buhuiming xxx:1.0.0'
        }

#### 1、 初始化配置
        val parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        val downloadRequest = DownloadRequest(context)
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(2)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setDownloadParentPath(parentPath)
            .setDownloadInTheBackground(null)//传空，则退出APP，停止下载；传Notification，则开启前台Service下载
            .build()
        downloadRequest?.newRequest(downloadConfig)

#### 2、 添加、开始下载
        downloadRequest?.startDownload(url, { 
            onInitialize { model->
            
            }
            onWaiting { model->
            
            }
            onProgress { model->
                Timber.d(
                    "totalLength: " + model.totalLength.toString() + ", totalReadBytes: " +
                    model.downLoadLength.toString() + ", progress: " + model.progress
                )
            }
            onStop { model->

            }
            onComplete { model->

            }
            onFail { model, throwable ->
            
            }
        })
         
#### 3、 重新下载
        downloadRequest?.reStartDownload(url, {})
         
#### 4、 暂停下载
        downloadRequest?.pauseDownload(url, {})
         
#### 5、 删除下载(文件会被删除)
        downloadRequest?.deleteDownload(url, {})

#### 6、 检查下载是否完成
        DownLoadUtil.checkExistFullFile(context, url, parentPath)

#### 7、 检查是否存在未完成下载(0 < progress < 100)
        DownLoadUtil.getExistFileProgress(context, url, parentPath)

### TO DO

#### 1、 网络波动

#### 2、 重复文件下载