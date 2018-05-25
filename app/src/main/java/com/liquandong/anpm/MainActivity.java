package com.liquandong.anpm;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.liquandong.anpm.proxy.ProxyService;
import com.liquandong.anpm.utils.NetworkUtils;
import com.liquandong.anpm.utils.ProxyUtils;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private boolean mIsProxyServerConnected;

    private boolean mIsProxyConfigured;

    private int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Switch monitorSwitch = navigationView.getHeaderView(0).findViewById(R.id.sh_monitor);
        monitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startProxyServer();
                } else {
                    stopProxyServer();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsProxyConfigured = ProxyUtils.isProxyConfigured("127.0.0.1", mPort);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_cert_install) {
            installCert();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private ServiceConnection mProxyConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName component) {
            mIsProxyServerConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder) {
            IProxyCallback callbackService = IProxyCallback.Stub.asInterface(binder);
            if (callbackService != null) {
                try {
                    callbackService.getProxyPort(new IProxyPortListener.Stub() {
                        @Override
                        public void setProxyPort(final int port) throws RemoteException {
                            Log.d(TAG, "setProxyPort = " + port);
                            mPort = port;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mIsProxyConfigured = ProxyUtils.isProxyConfigured("127.0.0.1", mPort);
                                    if (!mIsProxyConfigured) {
                                        showProxySettingDialog();
                                    } else {
                                        Toast.makeText(MainActivity.this, "抓包功能已开启", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mIsProxyServerConnected = true;
            Log.d(TAG, "proxy service start");
        }
    };

    private void startProxyServer() {
        if (!NetworkUtils.NETWORK_CLASS_WIFI.equals(NetworkUtils.getNetworkType(this))) {
            showWifiSettingDialog();
            return;
        }
        if (mIsProxyServerConnected) {
            mIsProxyConfigured = ProxyUtils.isProxyConfigured("127.0.0.1", mPort);
            if (!mIsProxyConfigured) {
                showProxySettingDialog();
            } else {
                Toast.makeText(this, "抓包功能已开启", Toast.LENGTH_LONG).show();
            }
        } else {
            Intent intent = new Intent(this, ProxyService.class);
            bindService(intent, mProxyConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void stopProxyServer() {
        if (mIsProxyServerConnected) {
            unbindService(mProxyConnection);
            mIsProxyServerConnected = false;
            Toast.makeText(this, "抓包功能已关闭", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "抓包功能已关闭", Toast.LENGTH_LONG).show();
        }
    }


    private void showWifiSettingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog dialog = dialogBuilder.setTitle("提示")
                .setMessage("需要开启wifi网络才能进行网络抓包哦")
                .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.show();
    }

    private void showProxySettingDialog() {
        String msg = String.format(Locale.getDefault(),
                "需要设置wifi代理才能进行网络抓包哦\r\nhost: 127.0.0.1\r\nport: %d", mPort);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog dialog = dialogBuilder.setTitle("提示")
                .setMessage(msg)
                .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.show();
    }

    private void installCert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog dialog = dialogBuilder.setTitle("提示")
                .setMessage("需要安装证书才能实现https抓包哦")
                .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.show();
    }

}
