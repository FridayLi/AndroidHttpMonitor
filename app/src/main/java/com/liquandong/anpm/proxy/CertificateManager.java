package com.liquandong.anpm.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.security.KeyChain;
import android.util.Log;

import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by liquandong on 2018/5/25.
 */
public class CertificateManager {

    private static final String TAG = "CertificateManager";

    private MitmManager mitmManager;

    private Authority authority;

    private static class CertificateHolder {
        private static final CertificateManager INSTANCE = new CertificateManager();
    }

    private CertificateManager() {

    }

    public static CertificateManager getInstance() {
        return CertificateHolder.INSTANCE;
    }

    public synchronized void installCertificate(Activity activity) {
        getMitmManager(activity);
        if (authority == null) {
            Log.e(TAG, "authority is null");
            return;
        }
        File certFile = authority.aliasFile(".pem");
        try {
            byte[] keychainBytes;
            FileInputStream is = null;
            try {
                is = new FileInputStream(certFile);
                keychainBytes = new byte[is.available()];
                is.read(keychainBytes);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            Intent intent = KeyChain.createInstallIntent();
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, keychainBytes);
            intent.putExtra(KeyChain.EXTRA_NAME, "ANPM CA Certificate");
            activity.startActivityForResult(intent, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized MitmManager getMitmManager(Context context) {
        if (mitmManager == null) {
            mitmManager = createMitmManager(context);
        }
        return mitmManager;
    }

    private MitmManager createMitmManager(Context context) {
        File keystoreDir = new File(context.getFilesDir(), "cert");
        if (!keystoreDir.exists()) {
            keystoreDir.mkdir();
        }
        authority = new Authority(keystoreDir);
        MitmManager mitmManager = null;
        try {
            mitmManager = new CertificateSniffingMitmManager(authority);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mitmManager;
    }
}
