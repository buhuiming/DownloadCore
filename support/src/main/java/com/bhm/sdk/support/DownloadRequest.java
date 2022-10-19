package com.bhm.sdk.support;

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:19
 */
public class DownloadRequest implements IRequest{

    @Override
    public void newRequest(DownloadConfig config) {
        DownloadManager.getInstance().init(config);
    }

    @Override
    public boolean startDownload(String url) {
        return false;
    }

    @Override
    public boolean startDownloads(String[] urls) {
        return false;
    }

    @Override
    public boolean startAllDownloads() {
        return false;
    }

    @Override
    public boolean pauseDownload(String url) {
        return false;
    }

    @Override
    public boolean pauseDownloads(String[] urls) {
        return false;
    }

    @Override
    public boolean pauseAllDownloads() {
        return false;
    }

    @Override
    public boolean removeDownload(String url) {
        return false;
    }

    @Override
    public boolean removeDownloads(String[] urls) {
        return false;
    }

    @Override
    public boolean removeAllDownloads() {
        return false;
    }
}
