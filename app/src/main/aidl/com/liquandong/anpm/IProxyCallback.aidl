// IProxyCallback.aidl
package com.liquandong.anpm;
// Declare any non-default types here with import statements

interface IProxyCallback
{
    oneway void getProxyPort(IBinder callback);
}