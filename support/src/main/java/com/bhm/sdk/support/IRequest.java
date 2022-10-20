package com.bhm.sdk.support;

/**
 * @author Buhuiming
 * @date :2022/10/19 15:21
 */
public interface IRequest {

    void newRequest(DownloadConfig config);

    boolean startDownload(String url);

    boolean startAllDownloads();

    boolean pauseDownload(String url);

    boolean pauseAllDownloads();

    boolean removeDownload(String url);

    boolean removeAllDownloads();
}
