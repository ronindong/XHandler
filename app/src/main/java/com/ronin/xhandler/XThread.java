package com.ronin.xhandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by ronindong on 2017/7/13.
 */

public final class XThread {
    /**
     * 默认线程池大小
     */
    private static final int nThreads = 10;
    /**
     * 线程池
     */
    private final static ExecutorService mService;

    static {
        mService = Executors.newFixedThreadPool(nThreads);
    }

    private XThread() {
      throw new IllegalAccessError("");
    }


    /**
     * 线程池执行线程
     *
     * @param r
     */
    public static void execute(Runnable r) {
        if (mService != null) {
            mService.execute(r);
        }
    }


    /**
     * 执行线程并获取返回值
     *
     * @param callable
     * @param <T>
     * @return
     */
    public static <T> T submit(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<>(callable);
        if (mService != null) {
            mService.submit(task);
            mService.shutdown();
        }
        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        Integer num = XThread.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                return 100;
            }
        });

        assert num != null;
        System.out.println(num.intValue());

    }


}
