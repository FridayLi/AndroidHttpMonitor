package com.liquandong.anpm.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;

/**
 * Created by liquandong on 2018/3/19.
 */

public class ProxyUtils {

    private static final int PORT_START = 8888;

    private static final int PORT_END = 9000;

    public static int findValidPort() {
        for (int port = PORT_START; port <= PORT_END; port++) {
            if (isPortValid(port)) {
                return port;
            }
        }
        return 0;
    }

    private static boolean isPortValid(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            return false;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignore) {

                }
            }
        }
        return true;
    }

    /**
     * 检查代理是否已配置
     */
    public static boolean isProxyConfigured(String host, int port) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        try {
            List<Proxy> list = ProxySelector.getDefault().select(new URI("http://www.baidu.com"));
            if (list.isEmpty()) {
                return false;
            }
            for (Proxy proxy : list) {
                InetSocketAddress address = (InetSocketAddress) proxy.address();
                if (address == null) {
                    continue;
                }
                Log.d("liqd", "proxy hostName = " + address.getHostName()
                        + ", port = " + address.getPort());
                if (host.equals(address.getHostName()) && port == address.getPort()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
