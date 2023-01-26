package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.schneewittchen.rosandroid.model.repositories.rosRepo.Ros2Repository;

import org.ros2.rcljava.RCLJava;
import org.ros2.rcljava.executors.Executor;
import org.ros2.rcljava.executors.SingleThreadedExecutor;

import java.util.Timer;
import java.util.TimerTask;

public class Ros2Service extends Service {
    private static final String TAG = Ros2Service.class.getSimpleName();
    private final IBinder binder = new LocalBinder();

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private Executor rosExecutor;
    private Timer timer;
    private Handler handler;

    private static long SPINNER_DELAY = 0;
    private static long SPINNER_PERIOD_MS = 200;

    public Ros2Service() {
    }

    @Override
    public void onCreate() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert powerManager != null;

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ROSANDROID:" + TAG);
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        int wifiLockType = WifiManager.WIFI_MODE_FULL;

        try {
            wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, "Unable to acquire high performance wifi lock.");
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;

        wifiLock = wifiManager.createWifiLock(wifiLockType, TAG);
        wifiLock.acquire();

        this.handler = new Handler(getMainLooper());
        RCLJava.rclJavaInit();
        this.rosExecutor = new SingleThreadedExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Runnable runnable = new Runnable() {
                    public void run() {
                        rosExecutor.spinSome();
                    }
                };
                handler.post(runnable);
            }
        }, SPINNER_DELAY, SPINNER_PERIOD_MS);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void registerNode(AbstractNode node) {
        Log.i(TAG, "Register Node: " + node.getTopic().name);
        rosExecutor.addNode(node);
    }

    public void unregisterNode(AbstractNode node) {
        Log.i(TAG, "Unregister Node: " + node.getTopic().name);
        rosExecutor.removeNode(node);
    }

    public class LocalBinder extends Binder {
        public Ros2Service getService() {
            return Ros2Service.this;
        }
    }
}
