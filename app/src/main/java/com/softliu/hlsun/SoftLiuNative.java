package com.softliu.hlsun;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SoftLiuNative {

    public static int PermissionRequestID = 20102;

    public static String TAG = "SoftLiu";

    public static Activity m_activity;

    public SoftLiuNative(Activity a) {
        m_activity = a;
        System.out.println("SoftLiuNative initialised with activity " + a);
    }

    public static SoftLiuNative Init(Activity a) {
        return new SoftLiuNative(a);
    }

    /**
     * 获取指定应用程序 ApplicationInfo
     * 参数一对应应用程序的包名
     * 参数二 应用程序对应的标识 通常为 0
     */
    public String GetAPKPath(String packageName) {
        PackageManager pm = m_activity.getPackageManager();
        try {
            ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
            if (app != null) {
                Log.d(TAG, "GetAPKPath: " + packageName + " is " + app.sourceDir);
                return app.sourceDir;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String GetCurrentAPKPath() {
        Log.d(TAG, "GetCurrentAPKPath: " + m_activity.getPackageCodePath());
        return m_activity.getPackageCodePath();
    }

    public String GetBuildleVersion() {
        try {
            PackageManager pm = m_activity.getPackageManager();
            if (pm != null) {
                PackageInfo packageInfo = pm.getPackageInfo(m_activity.getPackageName(), 0);
                return packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
    * 获取App的名字
    * */
    public String GetAppName() {
        try {
            PackageManager packageManager = m_activity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(m_activity.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return m_activity.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图标 bitmap
     */
    public byte[] GetIconBytes() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = m_activity.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(m_activity.getPackageName(), 0);
            Drawable d = packageManager.getApplicationIcon(applicationInfo);
            BitmapDrawable bd = (BitmapDrawable) d;
            Bitmap bm = bd.getBitmap();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return byteStream.toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
            System.out.println("GetIconBitmap Error: " + e.getMessage());
        }
        return null;
    }

    /*
     * 获取设备 唯一ID
     * */
    public String GetUniqueDeviceIdentifier() {
        try {
            TelephonyManager phoneMg = (TelephonyManager) m_activity.getSystemService(Context.TELEPHONY_SERVICE);
            if (phoneMg != null) {
                try {
                    return phoneMg.getDeviceId();
                } catch (Exception error) {
                    System.out.println("GetUniqueDeviceIdentifier failed with exception: " + error.getMessage());
                }
            }
        } catch (Exception error) {
            System.out.println("GetUniqueDeviceIdentifier() Error: " + error.getMessage());
        }
        return null;
    }

    @SuppressLint("HardwareIds")
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public String GetAndroidID() {
        try {
            return Settings.Secure.getString(m_activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception error) {
            System.out.println("GetAndroidID() Error: " + error.getMessage());
        }
        return null;
    }

    public String GetMACAddress() {
        try {
            WifiManager mgr = (WifiManager) m_activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (mgr != null) {
                WifiInfo info = mgr.getConnectionInfo();
                if (info != null) {
                    return info.getMacAddress();
                }
                System.out.println("GetMACAddress() getConnectionInfo()==null");
            } else {
                System.out.println("GetMACAddress() getSystemService(Context.WIFI_SERVICE)==null");
            }
        } catch (Exception e) {
            System.out.println("GetMACAddress() ERROR:" + e.toString());
        }
        return null;
    }

    public void ShowMessageBox(final String title, final String message, final int msg_id) {
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SoftLiuNative.m_activity);
                        builder.setTitle(title);
                        builder.setMessage(message);
                        builder.setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (msg_id != -1) {
                                            UnityPlayer.UnitySendMessage("SoftLiuNativeReceiver", "MessageBoxClick", "" + msg_id + ":OK");
                                        }
                                        dialog.cancel();
                                    }
                                });
                        builder.show();
                    } catch (Exception e) {
                        System.out.println("ShowMessageBox.run: " + e.toString());
                    }

                }
            };
            m_activity.runOnUiThread(runnable);

            System.out.println("ShowMessageBox: " + title + " msg=" + message);
        } catch (Exception e) {
            System.out.println("ShowMessageBoxWithButtons: " + e.toString());
        }
    }

    public void ShowMessageBoxWithButtons(final String title, final String message, final String ok_button, final String cancel_button, final int msg_id) {
        try {
            System.out.println("ShowMessageBoxWithButtons: " + title + " msg=" + message);

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SoftLiuNative.m_activity);
                        builder.setTitle(title);
                        builder.setMessage(message);
                        builder.setCancelable(false)
                                .setPositiveButton(ok_button, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (msg_id != -1) {
                                            UnityPlayer.UnitySendMessage("SoftLiuNativeReceiver", "MessageBoxClick", "" + msg_id + ":OK");
                                        }
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton(cancel_button, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        if (msg_id != -1) {
                                            UnityPlayer.UnitySendMessage("SoftLiuNativeReceiver", "MessageBoxClick", "" + msg_id + ":CANCEL");
                                        }
                                        dialog.cancel();
                                    }
                                });
                        builder.show();
                    } catch (Exception e) {
                        System.out.println("ShowMessageBoxWithButtons.run: " + e.toString());
                    }

                }
            };
            m_activity.runOnUiThread(runnable);
        } catch (Exception e) {
            System.out.println("ShowMessageBoxWithButtons: " + e.toString());
        }
    }

    public static void openURL(String URL) {
        Intent i = new Intent("android.intent.action.VIEW");
        i.setData(Uri.parse(URL));
        m_activity.startActivity(i);
    }

    @SuppressLint("WrongConstant")
    public boolean isAppInstalled(String packageName) {
        PackageManager pm = m_activity.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.CERT_INPUT_SHA256);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    @SuppressLint("WrongConstant")
    public String getAppVersion(String packageName) {
        PackageManager pm = m_activity.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.INSTALL_REASON_POLICY);
            return pi.versionName;
        } catch (Exception e) {
        }
        return "NA";
    }

    @RequiresApi(21)
    public static boolean IsActivityRunning(String activityName) {
        ActivityManager activityManager = (ActivityManager) m_activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        System.out.println("Starting Task search: " + activityName);
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("Task search: " + ((ActivityManager.AppTask) tasks.get(i)).getTaskInfo().origActivity.getPackageName());
            if (((ActivityManager.AppTask) tasks.get(i)).getTaskInfo().origActivity.getPackageName().equalsIgnoreCase(activityName))
                return true;
        }
        return false;
    }

    public static int GetNumCertificates() {
        try {
            @SuppressLint("WrongConstant") PackageInfo packageInfo = m_activity.getPackageManager().getPackageInfo(m_activity.getPackageName(), PackageManager.GET_RESOLVED_FILTER);
            return packageInfo.signatures.length;
        } catch (Exception e) {
            Log.e("FGOLNative", "GetNumCertificates failed with exception: " + e.toString());
        }
        return 0;
    }

    @SuppressLint("WrongConstant")
    public static String GetCertificateSignatureSHA(int index) {
        try {
            PackageInfo packageInfo = m_activity.getPackageManager().getPackageInfo(m_activity.getPackageName(), PackageManager.GET_RESOLVED_FILTER);

            if (index >= packageInfo.signatures.length) {
                Log.e("FGOLNative", "GetCertificateSignatureSHA index out of range.");
                return "";
            }

            Signature signature = packageInfo.signatures[index];
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(signature.toByteArray());

            byte[] publicKey = md.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                if (i != 0) {
                    hexString.append(":");
                }
                String appendString = Integer.toHexString(0xFF & publicKey[i]);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
            }

            Log.d("FGOLNative", "Cert signature " + index + ": " + hexString.toString().toUpperCase());
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            Log.e("FGOLNative", "Requesting cert signatures from APK failed with exception");
        }
        return "";
    }


    public static boolean HasPermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(m_activity, permission);
        if (permissionCheck == 0) {
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static boolean IsAndroidTVDevice() {
        UiModeManager uiModeManager = (UiModeManager) m_activity.getSystemService(Context.UI_MODE_SERVICE);
        PackageManager pm = m_activity.getPackageManager();
        String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";
        boolean isTV = false;

        if (uiModeManager.getCurrentModeType() == 4) {
            return true;
        }

        try {
            if (pm.hasSystemFeature(AMAZON_FEATURE_FIRE_TV)) {
                return true;
            }
        } catch (Exception localException) {
        }

        return false;
    }


    public static void TryShowPermissionExplanation(final String permissions, final String messageTitle, final String messageInfo) {
        System.out.println("Requesting permissions: " + permissions);

        String[] permissionRequestArray = permissions.split(",");
        ArrayList<String> permissionRequiredArray = new ArrayList();

        for (int i = 0; i < permissionRequestArray.length; i++) {
            if (HasPermission(permissionRequestArray[i])) {
                UnityPlayer.UnitySendMessage("SoftLiuNativeReceiver", "PermissionReceivedSuccess", "" + permissionRequestArray[i]);
            } else {
                permissionRequiredArray.add(permissionRequestArray[i]);
            }
        }
        if (permissionRequiredArray.size() == 0) {
            return;
        }

        final String[] permissionsToRequest = (String[]) permissionRequiredArray.toArray(new String[permissionRequiredArray.size()]);
        System.out.println("ShowPermissionExplanation: " + messageTitle + " msg=" + messageInfo);
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SoftLiuNative.m_activity);
                        builder.setTitle(messageTitle);
                        builder.setMessage(messageInfo);
                        builder.setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        SoftLiuNative.m_activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                        ActivityCompat.requestPermissions(SoftLiuNative.m_activity, permissionsToRequest, SoftLiuNative.PermissionRequestID)
                                        ;
                                    }
                                });
                        builder.show();
                    } catch (Exception e) {
                        System.out.println("ShowMessageBoxWithButtons.run: " + e.toString());
                    }
                }
            };
            m_activity.runOnUiThread(runnable);
        } catch (Exception e) {
            System.out.println("ShowMessageBoxWithButtons: " + e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static String GetConnectionType() {
        ConnectivityManager cm = (ConnectivityManager) m_activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if ((info == null) || (!info.isConnected()))
            return "-";
        if (info.getType() == 1)
            return "WIFI";
        if (info.getType() == 0) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case 1:
                case 2:
                case 4:
                case 7:
                case 11:
                    return "2G";
                case 3:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 14:
                case 15:
                    return "3G";
                case 13:
                    return "4G";
            }
            return "Unknown";
        }

        return "Unknown";
    }

    public static String GetGameLanguageISO() {
        return Locale.getDefault().getLanguage();
    }

    public static String GetUserCountryISO() {
        String countryCode = null;
        try {
            countryCode = m_activity.getResources().getConfiguration().locale.getCountry();
        } catch (Exception e) {
            System.out.println("getUserLocation: Locale " + e.toString());
        }

        if ((countryCode == null) || (countryCode.equals(""))) {
            countryCode = "XX";
        }
        return countryCode;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static String GetExternalStorageLocation() {
        String result = "";

        try {
            result = m_activity.getExternalFilesDir(null).getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Cannot retrieve external files directory" + e.toString());
        }

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static String GetExpansionFileLocation() {
        String result = "";

        try {
            result = m_activity.getObbDir().getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Cannot retrieve obb files directory" + e.toString());
        }

        return result;
    }

    private RandomAccessFile randomAccessFile = null;
    private RotateAnimation m_rotateAnimation;
    private LinearLayout m_spinnerLayout;

    public int GetMemoryUsage() {
        try {
            if (this.randomAccessFile == null) {
                this.randomAccessFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/stat", "r");
            }

            if (this.randomAccessFile != null) {
                this.randomAccessFile.seek(0L);
                return Integer.parseInt(this.randomAccessFile.readLine().split(" ")[23]);
            }
        } catch (Exception e) {
            System.out.println("Memory Usage Exception" + e.toString());
        }

        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public int GetTotalMemoryPSS() {
        int result = -1;

        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) m_activity.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(memoryInfo);

            int processId = android.os.Process.myPid();
            Debug.MemoryInfo[] mi = activityManager.getProcessMemoryInfo(new int[]{processId});
            result = mi[0].getTotalPss();
        } catch (Exception e) {
            System.out.println("Memory Usage PSS Exception" + e.toString());
        }
        return result;
    }


    public long GetMaxHeapMemory() {
        return Runtime.getRuntime().maxMemory();
    }


    public long GetUsedHeapMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public long GetMaxDeviceMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) m_activity.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }


    public long GetAvailableDeviceMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) m_activity.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }


    public long GetDeviceMemoryThreshold() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) m_activity.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.threshold;
    }

    private final int REFERENCE_SCREEN_HEIGHT = 1080;
    private final float SPINNER_SCALE = 2.0F;
    private final int ROTATION_DURATION = 1500;

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    public void ToggleSpinner(final boolean enable, final float x, final float y) {
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    DisplayMetrics screenMetrics = new DisplayMetrics();

                    if (Build.VERSION.SDK_INT >= 17) {
                        SoftLiuNative.m_activity.getWindowManager().getDefaultDisplay().getRealMetrics(screenMetrics);
                    } else {
                        SoftLiuNative.m_activity.getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);
                    }

                    if (SoftLiuNative.this.m_spinnerLayout == null) {
                        ImageView spinnerImage = new ImageView(SoftLiuNative.m_activity);
                        spinnerImage.setImageResource(R.drawable.spinner);

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inJustDecodeBounds = true;
                        BitmapFactory.decodeResource(SoftLiuNative.m_activity.getResources(), R.drawable.spinner, opt);
                        int spinnerSize = (int) (2.0F * opt.outHeight * screenMetrics.heightPixels / 1080.0F);
                        System.out.println("Spinner Width = " + spinnerSize + " Spinner Height = " + spinnerSize);
                        spinnerImage.setLayoutParams(new LinearLayout.LayoutParams(spinnerSize, spinnerSize));

                        SoftLiuNative.this.m_rotateAnimation = new RotateAnimation(0.0F, 360.0F, 1, 0.5F, 1, 0.5F);
                        SoftLiuNative.this.m_rotateAnimation.setInterpolator(new LinearInterpolator());
                        SoftLiuNative.this.m_rotateAnimation.setRepeatCount(-1);

                        SoftLiuNative.this.m_rotateAnimation.setDuration(1500L);

                        SoftLiuNative.this.m_spinnerLayout = new LinearLayout(SoftLiuNative.m_activity);
                        SoftLiuNative.this.m_spinnerLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
                        SoftLiuNative.this.m_spinnerLayout.addView(spinnerImage, 0);
                    }

                    ImageView spinner = (ImageView) SoftLiuNative.this.m_spinnerLayout.getChildAt(0);

                    int marginLeft = (int) (x * (screenMetrics.widthPixels - spinner.getLayoutParams().height));
                    int marginTop = (int) (y * (screenMetrics.heightPixels - spinner.getLayoutParams().height));

                    System.out.println("Spinner Input X = " + x + " Spinner Input Y = " + y);
                    System.out.println("Spinner Margin Left = " + marginLeft + " Spinner Margin Top = " + marginTop);
                    ((LinearLayout.LayoutParams) spinner.getLayoutParams()).setMargins(marginLeft, marginTop, 0, 0);

                    if (enable) {
                        ViewGroup vg = (ViewGroup) SoftLiuNative.this.m_spinnerLayout.getParent();
                        if (vg == null) {
                            SoftLiuNative.this.m_spinnerLayout.getChildAt(0).setAnimation(SoftLiuNative.this.m_rotateAnimation);

                            SoftLiuNative.m_activity.addContentView(SoftLiuNative.this.m_spinnerLayout, SoftLiuNative.this.m_spinnerLayout.getLayoutParams());
                        }

                    } else {
                        ViewGroup vg = (ViewGroup) SoftLiuNative.this.m_spinnerLayout.getParent();
                        if (vg != null) {
                            vg.removeView(SoftLiuNative.this.m_spinnerLayout);
                        }
                    }
                }
            };
            m_activity.runOnUiThread(runnable);
        } catch (Exception e) {
            System.out.println("Exception toggling loading spinner :: Exception = " + e.toString());
        }
    }

    @SuppressLint({"NewApi"})
    public String GetDocumentsDirectory() {
        try {
            File docPath = new File(Environment.getExternalStorageDirectory() + "/Documents");
            if (docPath != null) {
                boolean exists = true;
                if (!docPath.exists()) {
                    exists = docPath.mkdir();
                }
                if (exists) {
                    return docPath.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while fetching doc folder: " + e.toString());
        }
        return null;
    }

    public long GetAvailableDiskSpace() {
        try {
            StatFs stat = null;
            boolean hasExternalStorage = Environment.getExternalStorageState().equals("mounted");

            if (hasExternalStorage) {
                stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            } else {
                stat = new StatFs(Environment.getDataDirectory().getPath());
            }

            return stat.getBlockSize() * stat.getAvailableBlocks();
        } catch (Exception e) {
            System.out.println("Exception while trying to read available disk space. " + e.toString());
        }
        return 0L;
    }

    public String GetInstalledAppVersion(String appID) {
        String toReturn = null;
        try {
            Context appContext = m_activity.getApplicationContext();
            PackageInfo pinfo = appContext.getPackageManager().getPackageInfo(appID, 0);
            int verCode = pinfo.versionCode;
            String verName = pinfo.versionName;

            System.out.println("FGOLNative: Got verCode: " + verCode + " verName: " + verName);

            return verName;
        } catch (Exception e) {
            System.out.println("FGOLNative: Exception while trying to get installed app version: " + e.toString());
        }
        return toReturn;
    }

    @SuppressLint("WrongConstant")
    @TargetApi(Build.VERSION_CODES.Q)
    public void ShowApplicationSettings() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            Uri uri = Uri.fromParts("package", m_activity.getPackageName(), null);
            intent.setData(uri);//268435456
            intent.setFlags(Intent.FILL_IN_CLIP_DATA);

            m_activity.startActivity(intent);
            m_activity.finish();

            System.out.println("FGOLNative: Showing application settings");
        } catch (Exception e) {
            System.out.println("FGOLNative: Can't open the application settings");
        }
    }

    public static String GetCellularConnectionType() {
        TelephonyManager manager = (TelephonyManager) m_activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            int networkType = manager.getNetworkType();
            System.out.println("FGOLNative: Cellular connection type is : " + networkType);

            switch (networkType) {
                case 1:
                    return "NETWORK_TYPE_GPRS";
                case 2:
                    return "NETWORK_TYPE_EDGE";
                case 4:
                    return "NETWORK_TYPE_CDMA";
                case 7:
                    return "NETWORK_TYPE_1xRTT";
                case 11:
                    return "NETWORK_TYPE_IDEN";
                case 3:
                    return "NETWORK_TYPE_UMTS";
                case 5:
                    return "NETWORK_TYPE_EVDO_0";
                case 6:
                    return "NETWORK_TYPE_EVDO_A";
                case 8:
                    return "NETWORK_TYPE_HSDPA";
                case 9:
                    return "NETWORK_TYPE_HSUPA";
                case 10:
                    return "NETWORK_TYPE_HSPA";
                case 12:
                    return "NETWORK_TYPE_EVDO_B";
                case 14:
                    return "NETWORK_TYPE_EHRPD";
                case 15:
                    return "NETWORK_TYPE_HSPAP";
                case 13:
                    return "NETWORK_TYPE_LTE";
                case 16:
                    return "NETWORK_TYPE_GSM";
                case 18:
                    return "NETWORK_TYPE_IWLAN";
                case 17:
                    return "NETWORK_TYPE_TD_SCDMA";
            }

        }
        return null;
    }

    public static String GetCellularCarrierName() {
        TelephonyManager manager = (TelephonyManager) m_activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            String carrierName = manager.getNetworkOperatorName();
            return carrierName;
        }
        return null;
    }

    public static String GetCellularCountryCode() {
        TelephonyManager manager = (TelephonyManager) m_activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            String carrierCountry = manager.getNetworkCountryIso();
            return carrierCountry;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean IsAudioMuted() {
        AudioManager am = (AudioManager) m_activity.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            return am.isStreamMute(3);
        }
        return false;
    }
}
