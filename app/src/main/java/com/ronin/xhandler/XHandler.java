package com.ronin.xhandler;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Printer;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: 对Handler {@link Handler}类的扩展，提供了一个工作线程的消息收发队列。不影响Handler的正常功能。
 * 提供了 {@link #handleMessageOnWorker(Message)}去处理工作线程的消息。
 * 注意：
 * (1)、工作线程的消息发送方法名是以**OnWorker结尾（例如：{@link #sendEmptyMessageOnWorker(int)}）
 * (2)、可以配置工作线程的优先级，线程池的策略和大小。
 * (3)、可以很方便的在主线程和工作线程中进行切换
 * ------------------------------
 * @Version：V1.0.0
 * @Author: ronindong
 * @Date: 2017/6/30.
 */

public class XHandler extends Handler {

    /**
     * Context的弱引用处理
     */
    private WeakReference<? extends Context> mContextWeakReference;

    /**
     * 工作线程handler
     */
    private Handler mHandler;


    public XHandler() {
        init();
    }

    public XHandler(Context context) {
        if (context == null) {
            throw new NullPointerException("Context cannot be NullPointer!");
        }
        mContextWeakReference = new WeakReference<>(context);
        init();
    }

    /**
     * 初始化work thread
     */
    private void init() {
        new WokerThread(this);
    }

    /**
     * 处理工作线程的消息任务
     *
     * @param msg
     */
    public void handleMessageOnWorker(Message msg) {
    }

    /**
     * @param message
     * @return
     */
    public final String getMessageNameOnWorker(Message message) {
        return mHandler.getMessageName(message);
    }

    /**
     * @return
     */
    public final Message obtainMessageOnWorker() {
        return Message.obtain(mHandler);
    }

    public final Message obtainMessageOnWorker(int what) {
        return Message.obtain(mHandler, what);
    }

    public final Message obtainMessageOnWorker(int what, Object obj) {
        return Message.obtain(mHandler, what, obj);
    }

    public final Message obtainMessageOnWorker(int what, int arg1, int arg2) {
        return Message.obtain(mHandler, what, arg1, arg2);
    }

    public final Message obtainMessageOnWorker(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(mHandler, what, arg1, arg2, obj);
    }

    /**
     * @param r
     * @return
     */
    private static Message getPostMessage(Runnable r) {
        Message m = Message.obtain();
        m.what = WokerThread.CODE_MSG;
        m.obj = r;
        return m;
    }

    public final boolean postOnWorker(Runnable r) {
        return postDelayedOnWorker(r, 0);
    }

    public final boolean postDelayedOnWorker(Runnable r, long delayMillis) {
        return mHandler.sendMessageDelayed(getPostMessage(r), delayMillis);
    }


    public final boolean postAtTimeOnWorker(Runnable r, long uptimeMillis) {
        return mHandler.sendMessageAtTime(getPostMessage(r), uptimeMillis);
    }

    public final boolean postAtFrontOfQueueOnWorker(Runnable r) {
        return mHandler.postAtFrontOfQueue(r);
    }

    public final boolean sendMessageOnWorker(Message msg) {
        return mHandler.sendMessage(msg);
    }

    public final boolean sendEmptyMessageOnWorker(int what) {
        return mHandler.sendEmptyMessageDelayed(what, 0);
    }

    public final boolean sendEmptyMessageAtTimeOnWorker(int what, long uptimeMillis) {
        return mHandler.sendEmptyMessageAtTime(what, uptimeMillis);
    }

    public final boolean sendMessageDelayedOnWorker(Message msg, long delayMillis) {
        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendMessageAtTimeOnWorker(Message msg, long uptimeMillis) {
        return mHandler.sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageAtFrontOfQueueOnWorker(Message msg) {
        return mHandler.sendMessageAtFrontOfQueue(msg);
    }

    public final void removeCallbacksAndMessagesOnWorker(Object token) {
        mHandler.removeCallbacksAndMessages(token);
    }

    public final void removeCallbackOnWorker(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    public final void removeCallbackOnWorker(Runnable r, Object token) {
        mHandler.removeCallbacks(r, token);
    }

    public final void removeMessagesOnWorker(int what) {
        mHandler.removeMessages(what);
    }

    public final void removeMessagesOnWorker(int what, Object object) {
        mHandler.removeMessages(what, object);
    }

    public final boolean hasMessagesOnWorker(int what) {
        return mHandler.hasMessages(what);
    }

    public final boolean hasMessagesOnWorker(int what, Object object) {
        return mHandler.hasMessages(what, object);
    }

    public final Looper getLooperOnWorker() {
        return mHandler.getLooper();
    }

    public final void dumpOnWorker(Printer pw, String prefix) {
        mHandler.dump(pw, prefix);
    }


    /**
     *
     */
    private static class WokerThread {
        /**
         * 工作线程的名称
         */
        private static final String NAME_WORKER_THREAD = "XHandler_WORKER_THREAD";
        /**
         * 工作线程post Message的what值
         */
        private static final int CODE_MSG = 0XABF8910;
        /**
         * 工作线程
         */
        private HandlerThread mHandlerThread;
        /**
         * 工作线程的优先级
         */
        private final int mPriority;
        /**
         * 线程池
         */
        private ExecutorService mService;
        /**
         * 默认线程池大小
         */
        private final int nThreads = 5;
        private XHandler mXHandler;

        public WokerThread(XHandler xHandler) {
            this.mXHandler = xHandler;

            mPriority = Process.THREAD_PRIORITY_DEFAULT;
            mHandlerThread = new HandlerThread(NAME_WORKER_THREAD, mPriority);
            mHandlerThread.start();
            mService = Executors.newFixedThreadPool(nThreads);
            xHandler.mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == CODE_MSG) {
                        if (msg.obj != null && msg.obj instanceof Runnable) {
                            Runnable r = (Runnable) msg.obj;
                            if (mService != null) {
                                mService.execute(r);
                            } else {
                                r.run();
                            }
                        } else {
                            mXHandler.handleMessageOnWorker(msg);
                        }
                    } else {
                        mXHandler.handleMessageOnWorker(msg);
                    }
                }
            };
        }


    }

}
