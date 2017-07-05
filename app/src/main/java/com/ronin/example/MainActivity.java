package com.ronin.example;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ronin.xhandler.XHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static final int MSG_01 = 0X001;
    public static final int MSG_02 = 0X002;
    public static final int MSG_03 = 0X003;
    public static final int MSG_04 = 0X004;
    public static final int MSG_05 = 0X005;


    private LinearLayout mLayout;
    private Button btnPostOnWorker, btnPostOnUI, btnSendMsgOnWorker, btnSendMsgOnUI;

    private XHandler mXHandler = new XHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_01:
                    Toast.makeText(MainActivity.this, "UI线程发送消息-toast", Toast.LENGTH_LONG).show();
                    break;
                case MSG_02:
                    Toast.makeText(MainActivity.this, "工作线程切换到主线程的-toast", Toast.LENGTH_LONG).show();
                    break;
                case MSG_03:

                    break;
                case MSG_04:

                    break;
                case MSG_05:

                    break;
            }
        }

        @Override
        public void handleMessageOnWorker(Message msg) {
            super.handleMessageOnWorker(msg);
            switch (msg.what) {
                case MSG_01:
                    Log.e(TAG, "handleMessageOnWorker: 工作线程发送消息");
                    break;
                case MSG_02:
                    Log.e(TAG, "handleMessageOnWorker: UI线程post切换到工作线程");
                    break;
                case MSG_03:

                    break;
                case MSG_04:

                    break;
                case MSG_05:

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mXHandler.postOnWorker(new Runnable() {
            @Override
            public void run() {
                //工作线程中模拟耗时操作，不影响UI线程的运行
                simulateTimeout();
                initView();

            }
        });


        //UI线程中模拟耗时操作,APP启动会出现白屏
        simulateTimeout();
        initView();


    }

    /**
     * 输出内存信息日志
     */
    private void printMemoryInfo() {
        ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        int memClass = activityManager.getMemoryClass();//64，以m为单位
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        Log.e(TAG, "availMem: " + memoryInfo.availMem / (1024 * 1024) + "MB");
        Log.e(TAG, "memClass: " + memClass + "MB");
        Log.e(TAG, "maxMemory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
        Log.e(TAG, "totalMemory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
        Log.e(TAG, "freeMemory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
    }

    private void initView() {
        long sTime = Debug.threadCpuTimeNanos();

        mLayout = (LinearLayout) findViewById(R.id.layout);
        btnPostOnWorker = (Button) findViewById(R.id.btn_post_onworker);
        btnPostOnUI = (Button) findViewById(R.id.btn_post_onui);
        btnSendMsgOnWorker = (Button) findViewById(R.id.btn_send_msg_onworker);
        btnSendMsgOnUI = (Button) findViewById(R.id.btn_send_msg_onui);

        btnPostOnWorker.setOnClickListener(this);
        btnPostOnUI.setOnClickListener(this);
        btnSendMsgOnWorker.setOnClickListener(this);
        btnSendMsgOnUI.setOnClickListener(this);

        //测试模拟，控件初始化耗时
        for (int i = 0; i < 200; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        }

        long eTime = Debug.threadCpuTimeNanos();
        //计算初始化控件所需要的时间
        double delta = (eTime - sTime) / Math.pow(10, 6);
        Log.d(TAG, "Thread:" + Thread.currentThread().getName()
                + ",initView run time: " + delta + "ms");

    }


    /**
     * 模拟耗时操作
     */
    private void simulateTimeout() {
        try {
            Log.d(TAG, "simulateTimeout: start...");
            Thread.sleep(3000);
            Log.d(TAG, "simulateTimeout: end...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_post_onworker) {
            mXHandler.postOnWorker(new Runnable() {
                @Override
                public void run() {
                    simulateTimeout();
                    //耗时操作之后，从工作线程切换到UI线程，更新UI

                    /**
                     * 可以在postOnWorker中执行耗时的操作，如网络请求，文件读写等。
                     * 操作完成后，可以通过XHandler发送消息到主线程进行相应UI更新
                     */
                    mXHandler.sendEmptyMessage(MSG_02);
                }
            });
        } else if (id == R.id.btn_post_onui) {
            mXHandler.post(new Runnable() {
                @Override
                public void run() {
                    simulateTimeout();
                    //耗时操作之后，从主线程切换到工作线程中
                    mXHandler.sendEmptyMessageOnWorker(MSG_02);
                }
            });
        } else if (id == R.id.btn_send_msg_onworker) {
            //发送消息到工作线程中，在handleMessageOnWorker中处理消息
            mXHandler.sendEmptyMessageOnWorker(MSG_01);

        } else if (id == R.id.btn_send_msg_onui) {
            mXHandler.sendEmptyMessage(MSG_01);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mXHandler.removeCallbacksAndMessages(null);
        mXHandler.removeCallbacksAndMessagesOnWorker(null);
    }
}
