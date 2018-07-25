package com.example.minato.minastore.utils;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.minato.minastore.RxRetrofitApplication;
import com.example.minato.minastore.cookie.CookieResult;
import com.example.minato.minastore.cookie.CookieResultDao;
import com.example.minato.minastore.cookie.DaoMaster;
import com.example.minato.minastore.cookie.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;
import java.util.List;


/**
 * Created by minato on 2018/7/20.
 * 缓存数据库的工具类
 */

public class CookieDbUtil {
    private static volatile CookieDbUtil mCookieDbUtil;
    private static String mDbName = "cookie_db";
    private Context mContext;
    private DaoMaster.DevOpenHelper mOpenHelper;

    private CookieDbUtil() {
        mContext = RxRetrofitApplication.getApplication();
        mOpenHelper = new DaoMaster.DevOpenHelper(mContext, mDbName);
    }

    public static CookieDbUtil getInstance() {
        if (mCookieDbUtil==null){
            synchronized (CookieDbUtil.class){
                mCookieDbUtil=new CookieDbUtil();
            }
        }
        return mCookieDbUtil;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase(){
        if (mOpenHelper==null){
            mOpenHelper=new DaoMaster.DevOpenHelper(mContext,mDbName);
        }
        SQLiteDatabase readableDatabase = mOpenHelper.getReadableDatabase();
        return readableDatabase;
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase(){
        if (mOpenHelper==null){
            mOpenHelper=new DaoMaster.DevOpenHelper(mContext,mDbName);
        }
        SQLiteDatabase writableDatabase = mOpenHelper.getWritableDatabase();
        return writableDatabase;
    }

    private CookieResultDao getCookieResultDao(){
        DaoMaster daoMaster=new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CookieResultDao cookieResultDao = daoSession.getCookieResultDao();
        return cookieResultDao;
    }

    /**
     * 保存缓存至数据库
     */
    public void saveCookie(CookieResult cookieResult){
        CookieResultDao cookieResultDao = getCookieResultDao();
        cookieResultDao.insert(cookieResult);

    }

    /**
     * 修改数据库
     */
    public void updateCookie(CookieResult cookieResult){
        CookieResultDao downInfoDao = getCookieResultDao();
        downInfoDao.update(cookieResult);
    }


    /**
     * 删除缓存
     */
    public void deleteCookie(CookieResult info){
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CookieResultDao downInfoDao = daoSession.getCookieResultDao();
        downInfoDao.delete(info);
    }


    /**
     * 根据url获取缓存实例
     */
    public CookieResult queryCookieBy(String  url) {
        CookieResultDao downInfoDao = getCookieResultDao();
        QueryBuilder<CookieResult> qb = downInfoDao.queryBuilder();
        qb.where(CookieResultDao.Properties.MUrl.eq(url));
        List<CookieResult> list = qb.list();
        if(list.isEmpty()){
            return null;
        }else{
            return list.get(0);
        }
    }

    /**
     * 获取缓存实例
     */
    public List<CookieResult> queryCookieAll() {
        CookieResultDao downInfoDao = getCookieResultDao();
        QueryBuilder<CookieResult> qb = downInfoDao.queryBuilder();
        return qb.list();
    }
}
