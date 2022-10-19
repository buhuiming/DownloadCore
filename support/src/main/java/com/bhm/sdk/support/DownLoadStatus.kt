package com.bhm.sdk.support

/**
 * 下载状态
 * @author buhuiming
 * @since 2022/10/19 v1.0.0
 */
enum class DownLoadStatus {
    INITIAL,  //未开始/初始状态
    WAITING,  //等待中
    STOP,  //下载失败
    DOWNING,  //下载中
    COMPETE,  //下载完成/成功
    FAIL //下载失败
}