package com.liquandong.anpm.utils;

import java.io.IOException;
import java.net.ServerSocket;

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

    public static boolean isPortValid(int port) {
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
}
