package com.liquandong.anpm.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class NetworkUtils {

    public static final String NETWORK_CLASS_WIFI = "w";
    public static final String NETWORK_CLASS_2_G = "2g";
    public static final String NETWORK_CLASS_3_G = "3g";
    public static final String NETWORK_CLASS_4_G = "4g";
    public static final String NETWORK_CLASS_UNKNOWN = "x";
    /**
     * 中国移动
     */
    public static final String CHINA_MOBILE = "ChinaMobile";
    /**
     * 中国联通
     */
    public static final String CHINA_UNICOM = "ChinaUnicom";
    /**
     * 中国电信
     */
    public static final String CHINA_TELECOM = "ChinaTelecom";
    /**
     * 未知运营商
     */
    public static final String CHINA_UNKNOWN = "ChinaUnknown";

    public static String getNetworkType(final Context context) {
        if (context == null) {
            return NETWORK_CLASS_UNKNOWN;
        }
        try {
            final PackageManager pManager = context.getPackageManager();
            if (pManager.checkPermission(
                    "android.permission.ACCESS_NETWORK_STATE",
                    context.getPackageName()) != PERMISSION_GRANTED) {
                return NETWORK_CLASS_UNKNOWN;
            }
            final ConnectivityManager cManager =
                    (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            final NetworkInfo nInfo = cManager.getActiveNetworkInfo();
            return getNetworkType(nInfo);
        } catch (Throwable ignore) {
        }
        return NETWORK_CLASS_UNKNOWN;
    }

    public static String getNetworkType(NetworkInfo nInfo) {
        if (nInfo == null) {
            return NETWORK_CLASS_UNKNOWN;
        }
        try {
            if (nInfo.isConnectedOrConnecting()) {
                if (nInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return NETWORK_CLASS_WIFI;
                }
                if (nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return getNetworkClass(nInfo.getSubtype());
                }
            }
        } catch (Throwable ignore) {
        }
        return NETWORK_CLASS_UNKNOWN;
    }

    public static boolean is4G(Context context) {
        return NETWORK_CLASS_4_G.equals(getNetworkType(context));
    }

    /**
     * 是否是中国电信4G网络
     * @param context
     * @return
     */
    public static boolean isChinaTelecom4G(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return isChinaTelecom4G(networkInfo);
    }

    /**
     * 是否是中国电信4G网络
     */
    public static boolean isChinaTelecom4G(NetworkInfo networkInfo) {
        String networkType = getNetworkType(networkInfo);
        String operator = getNetworkOperator(networkInfo);
        if (NETWORK_CLASS_4_G.equals(networkType) || NETWORK_CLASS_UNKNOWN.equals(networkType)) {
            if (CHINA_TELECOM.equals(operator) || CHINA_UNKNOWN.equals(operator)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobileNetwork(NetworkInfo networkInfo) {
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 获取网络运营商
     * @return
     */
    public static String getNetworkOperator(NetworkInfo nInfo) {
        if (nInfo == null) {
            return CHINA_UNKNOWN;
        }
        String extraInfo = nInfo.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo)) {
            return CHINA_UNKNOWN;
        }
        if ("cmnet".equalsIgnoreCase(extraInfo)) {
            return CHINA_MOBILE;
        } else if ("cmwap".equalsIgnoreCase(extraInfo)) {
            return CHINA_MOBILE;
        } else if ("ctnet".equalsIgnoreCase(extraInfo)) {
            return CHINA_TELECOM;
        } else if ("ctwap".equalsIgnoreCase(extraInfo)) {
            return CHINA_TELECOM;
        } else if ("3gnet".equalsIgnoreCase(extraInfo)) {
            return CHINA_UNICOM;
        } else if ("3gwap".equalsIgnoreCase(extraInfo)) {
            return CHINA_UNICOM;
        } else {
            return CHINA_UNKNOWN;
        }
    }


    private static String getNetworkClass(final int networkType) {
        switch (networkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11: {
                return NETWORK_CLASS_2_G;
            }
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15: {
                return NETWORK_CLASS_3_G;
            }
            case 13:
            case 19: {
                return NETWORK_CLASS_4_G;
            }
            default: {
                return NETWORK_CLASS_UNKNOWN;
            }
        }
    }

    public static boolean hasNetwork(final Context pContext) {
        if (pContext != null) {
            try {
                final ConnectivityManager conManager =
                        (ConnectivityManager) pContext.getSystemService(CONNECTIVITY_SERVICE);
                if (conManager != null) {
                    PackageManager localPackageManager = pContext.getPackageManager();
                    if (localPackageManager.checkPermission(
                            "android.permission.ACCESS_NETWORK_STATE",
                            pContext.getPackageName()) == PERMISSION_GRANTED) {
                        final NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
                        return networkInfo != null && networkInfo.isConnectedOrConnecting();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        NetworkInfo networkInfo = null;
        try {
            final ConnectivityManager conManager =
                    (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            if (conManager != null) {
                PackageManager packageManager = context.getPackageManager();
                if (packageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE",
                        context.getPackageName()) == PERMISSION_GRANTED) {
                    networkInfo = conManager.getActiveNetworkInfo();
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
        return networkInfo;
    }
}
