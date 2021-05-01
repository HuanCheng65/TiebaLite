package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.activities.MainActivity;

public class CrashUtil {
    public static final String TAG = "CrashUtil";

    public static void newCrash(Context context, Throwable throwable) {
        long time = getTime(context);
        saveException(context, throwable);
        if (System.currentTimeMillis() - time > 30 * 1000L) restart(context);
    }

    @Nullable
    private static PackageInfo getLocalPackageInfo(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getApplicationContext()
                    .getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getCrashReport(Context context, Throwable ex) {
        StringBuilder exceptionStr = new StringBuilder();
        PackageInfo pinfo = getLocalPackageInfo(context);
        if (pinfo != null) {
            if (ex != null) {
                //app版本信息
                exceptionStr.append("App Version：" + pinfo.versionName);
                exceptionStr.append("_" + pinfo.versionCode + "\n");
                //手机系统信息
                exceptionStr.append("OS Version：" + Build.VERSION.RELEASE);
                exceptionStr.append("_");
                exceptionStr.append(Build.VERSION.SDK_INT + "\n");
                //手机制造商
                exceptionStr.append("Vendor: " + Build.MANUFACTURER + "\n");
                //手机型号
                exceptionStr.append("Model: " + Build.MODEL + "\n");
                String errorStr = ex.getLocalizedMessage();
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.getMessage();
                }
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.toString();
                }
                exceptionStr.append("Exception: " + errorStr + "\n");
                StackTraceElement[] elements = ex.getStackTrace();
                if (elements != null) {
                    for (int i = 0; i < elements.length; i++) {
                        exceptionStr.append(elements[i].toString() + "\n");
                    }
                }
            } else {
                exceptionStr.append("no exception. Throwable is null\n");
            }
            return exceptionStr.toString();
        } else {
            return "";
        }
    }

    @SuppressWarnings("ApplySharedPref")
    private static void saveException(@NonNull Context context, Throwable throwable) {
        context.getSharedPreferences("crash", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putLong("time", System.currentTimeMillis())
                .putString("message", getCrashMessage(throwable))
                .putString("crash", getCrashReport(context, throwable))
                .commit();
    }

    private static String getCrashMessage(Throwable ex) {
        String errorStr = ex.getLocalizedMessage();
        if (TextUtils.isEmpty(errorStr)) {
            errorStr = ex.getMessage();
        }
        if (TextUtils.isEmpty(errorStr)) {
            errorStr = ex.toString();
        }
        return errorStr;
    }

    public static String getCrashMessage(Context context) {
        return context.getSharedPreferences("crash", Context.MODE_PRIVATE)
                .getString("message", "");
    }

    @SuppressWarnings("WrongConstant")
    private static void restart(@NonNull Context context) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager != null) {
            PendingIntent restartIntent = PendingIntent.getActivity(
                    context.getApplicationContext(),
                    0,
                    intent,
                    0
            );
            mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent);
        }
        ((BaseApplication) context.getApplicationContext()).removeAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static long getTime(@NonNull Context context) {
        return context.getSharedPreferences("crash", Context.MODE_PRIVATE).getLong("time", 0L);
    }

    @Nullable
    public static String getCrash(@NonNull Context context) {
        return context.getSharedPreferences("crash", Context.MODE_PRIVATE).getString("crash", null);
    }

    public static void clear(@NonNull Context context) {
        context.getSharedPreferences("crash", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public static class CrashHandler implements Thread.UncaughtExceptionHandler {
        @SuppressLint("StaticFieldLeak")
        private static CrashHandler sInstance = null;
        boolean crashing = false;
        private Thread.UncaughtExceptionHandler mDefaultHandler;
        private Context mContext;

        private CrashHandler() {
        }

        public static CrashHandler getInstance() {
            if (sInstance == null) {
                synchronized (CrashHandler.class) {
                    if (sInstance == null) {
                        synchronized (CrashHandler.class) {
                            sInstance = new CrashHandler();
                        }
                    }
                }
            }
            return sInstance;
        }

        /**
         * 初始化默认异常捕获
         *
         * @param context context
         */
        public void init(Context context) {
            mContext = context;
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (crashing) {
                return;
            }
            crashing = true;
            e.printStackTrace();
            if (!handleException(e) && mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(t, e);
            }
        }

        private boolean handleException(Throwable e) {
            if (e == null) {
                return false;
            }
            try {
                newCrash(mContext, e);
            } catch (Exception ex) {
                return false;
            }
            return true;
        }
    }
}