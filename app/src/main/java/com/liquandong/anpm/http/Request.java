package com.liquandong.anpm.http;

import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by liquandong on 2018/4/20.
 */
public final class Request {
    public String url;
    public String method;
    public Headers headers;
    public @Nullable RequestBody body;
}
