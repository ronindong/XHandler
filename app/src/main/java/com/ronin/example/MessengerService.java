package com.ronin.example;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessengerService extends Service {

    private Messenger messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x001) {

                Message message = Message.obtain(msg);
                try {
                    message.arg1 = msg.arg1 + msg.arg2;
                    Log.e("MainActivity", "Service sum: "+message.arg1);
                    message.replyTo.send(message);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
            super.handleMessage(msg);

        }
    });

    public MessengerService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}
