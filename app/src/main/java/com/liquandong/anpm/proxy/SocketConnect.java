package com.liquandong.anpm.proxy;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @hide
 */
public class SocketConnect extends Thread {

    private static final int TYPE_CLIENT = 1;

    private static final int TYPE_SERVER = 2;

    private InputStream from;
    private OutputStream to;
    private int type;

    public SocketConnect(Socket from, Socket to, int type) throws IOException {
        this.from = from.getInputStream();
        this.to = to.getOutputStream();
        this.type = type;
        start();
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[512];
        ByteArrayOutputStream result = null;
        if (type == TYPE_SERVER) {
            result = new ByteArrayOutputStream();
        }
        try {
            while (true) {
                int r = from.read(buffer);
                if (result != null) {
                    result.write(buffer);
                }
                if (r < 0) {
                    break;
                }
                to.write(buffer, 0, r);
            }
            from.close();
            to.close();
            if (result != null) {
                result.flush();
                Log.d("liqd", "SocketConnect result = " + result.toString());
            }
        } catch (IOException io) {

        }
    }

    public static void connect(Socket first, Socket second) {
        try {
            SocketConnect sc1 = new SocketConnect(first, second, TYPE_CLIENT);
            SocketConnect sc2 = new SocketConnect(second, first, TYPE_SERVER);
            try {
                sc1.join();
            } catch (InterruptedException e) {
            }
            try {
                sc2.join();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
