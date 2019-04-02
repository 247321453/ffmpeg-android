package net.kk.ffmpeg;

import android.support.annotation.Keep;
import android.support.annotation.WorkerThread;
import android.view.Surface;

import java.io.Closeable;

/*
 * YUV420p：又叫planer平面模式，Y ，U，V分别再不同平面，也就是有三个平面。
 * YUV420p又分为：他们的区别只是存储UV的顺序不一样而已。
 * I420:又叫YU12，安卓的模式。存储顺序是先存Y，再存U，最后存V。YYYYUUUVVV
 * YV12:存储顺序是先存Y，再存V，最后存U。YYYVVVUUU
 *
 * YUV420sp：又叫bi-planer或two-planer双平面，Y一个平面，UV在同一个平面交叉存储。
 * YUV420sp又分为：他们的区别只是存储UV的顺序不一样而已。
 * NV12:IOS只有这一种模式。存储顺序是先存Y，再UV交替存储。YYYYUVUVUV
 * NV21:安卓的模式。存储顺序是先存Y，再存U，再VU交替存储。YYYYVUVUVU
 */
public class VideoPlayer implements Closeable {

    public interface CallBack {
        void onVideoProgress(double cur, double all);

        void onFrameCallBack(byte[] nv21Data, int width, int height);
    }

    static {
        System.loadLibrary("kkplayer");
    }

    private long nativePtr;
    private CallBack mCallBack;
    private String source;

    public VideoPlayer() {
        nativePtr = native_create();
    }

    public void setCallBack(Surface surface, CallBack dataCallBack, boolean needNv21Data) {
        mCallBack = dataCallBack;
        native_set_callback(nativePtr, surface, needNv21Data);
    }

    public String getSource() {
        return source;
    }

    public void setDataSource(String path) {
        source = path;
        native_set_datasource(nativePtr, path);
    }

    @WorkerThread
    public int play() {
        init();
        return native_play(nativePtr);
    }

    @WorkerThread
    public int preload() {
        init();
        return native_preload(nativePtr);
    }

    public void release() {
        native_release(nativePtr);
    }

    public void stop() {
        native_stop(nativePtr);
    }

    @Override
    public void close() {
        native_close(nativePtr);
    }

    public int seek(int ms) {
        return native_seek(nativePtr, ms);
    }

    public double getPlayTime() {
        return native_get_cur_time(nativePtr);
    }

    public double getVideoTime() {
        return native_get_all_time(nativePtr);
    }

    @Keep
    public void onFrameCallBack(byte[] nv21Data, int width, int height, double progress, double alltime) {
        if (mCallBack != null) {
            mCallBack.onVideoProgress(progress, alltime);
            if(nv21Data != null) {
                mCallBack.onFrameCallBack(nv21Data, width, height);
            }
        }
    }

    public boolean isPlaying() {
        return native_get_status(nativePtr) == 1;
    }

    private static void init() {
        if (!sInit) {
            sInit = true;
            native_init_ffmpeg();
        }
    }

//    public static int testPlay(Surface surface, String file){
//        return native_test_play(surface, file);
//    }

    private static boolean sInit;

    private static native void native_init_ffmpeg();

    private static native long native_create();

    private native void native_set_callback(long ptr, Surface surface, boolean callback);

    private native void native_set_datasource(long ptr, String path);

    private native int native_play(long ptr);

    private native int native_preload(long ptr);

    private native int native_get_status(long ptr);

    private native void native_stop(long ptr);

    private native void native_close(long ptr);

    private native void native_release(long ptr);

    private native double native_get_cur_time(long ptr);

    private native double native_get_all_time(long ptr);

    private native int native_seek(long ptr, double ms);

//    private static native int native_test_play(Surface surface, String path);
}
