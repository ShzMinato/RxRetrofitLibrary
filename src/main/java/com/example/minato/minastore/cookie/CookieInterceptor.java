package com.example.minato.minastore.cookie;

import com.example.minato.minastore.utils.CookieDbUtil;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by minato on 2018/7/21.
 * Cookie拦截器 实现缓存的功能  将联网数据缓存之数据库
 *  缓存的是根据Response构造的CookieResult（时间和string），键是url（baseUrl+method）
 */

public class CookieInterceptor implements Interceptor {
    private CookieDbUtil mCookieDbUtil;
    //是否缓存标识
    private boolean mCache;
    //url
    private String mUrl;

    public CookieInterceptor(boolean cache, String url) {
        mCookieDbUtil =CookieDbUtil.getInstance();
        this.mUrl=url;
        this.mCache =cache;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if(mCache){
            ResponseBody body = response.body();
            BufferedSource source = body.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            Charset charset = Charset.defaultCharset();
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            String bodyString = buffer.clone().readString(charset);
            CookieResult result= mCookieDbUtil.queryCookieBy(mUrl);
            long time=System.currentTimeMillis();
            /*保存和更新本地数据*/
            if(result==null){
                result  =new CookieResult(mUrl,bodyString,time);
                mCookieDbUtil.saveCookie(result);
            }else{
                result.setResult(bodyString);
                result.setTime(time);
                mCookieDbUtil.updateCookie(result);
            }
        }
        return response;
    }
}
