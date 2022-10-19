package com.bhm.sdk.support;

import android.telecom.Call;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:49
 */
class DownloadManager {

    static final String SP_FILE_NAME = "download_list";

    private static DownloadManager instance;

    private final HashMap<String, Call> downloadCallHashMap;

    private OkHttpClient okHttpClient;

    static DownloadManager getInstance() {
        synchronized (DownloadManager.class) {
            if (instance == null) {
                instance = new DownloadManager();
            }
        }
        return instance;
    }

    private DownloadManager() {
        downloadCallHashMap = new HashMap<>();
    }

    void init(DownloadConfig config) {
        okHttpClient = new OkHttpClient.Builder()
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .build();
        okHttpClient.dispatcher().setMaxRequestsPerHost(config.getMaxDownloadSize());//每个主机最大请求数为
        okHttpClient.dispatcher().setMaxRequests(config.getMaxDownloadSize());//最大并发请求数为
    }
}
