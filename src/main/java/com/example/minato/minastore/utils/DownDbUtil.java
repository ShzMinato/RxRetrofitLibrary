package com.example.minato.minastore.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.minato.minastore.RxRetrofitApplication;
import com.example.minato.minastore.cookie.DaoMaster;
import com.example.minato.minastore.cookie.DaoSession;
import com.example.minato.minastore.down.DownInfo;
import com.example.minato.minastore.down.DownInfoDao;

import org.greenrobot.greendao.query.QueryBuilder;
import java.util.List;

/**
 * Created by minato on 2018/7/21.
 * 下载任务的数据库的工具类
 */

public class DownDbUtil {
    private volatile static DownDbUtil mInstance;
    private static String mDbName = "down_db";
    private Context mContext;
    private DaoMaster.DevOpenHelper mDevOpenHelper;


    private DownDbUtil() {
        mContext = RxRetrofitApplication.getApplication();
        mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, mDbName);
    }


    public static DownDbUtil getInstance() {
        if (mInstance == null) {
            synchronized (DownDbUtil.class) {
                if (mInstance == null)
                    mInstance = new DownDbUtil();
            }
        }
        return mInstance;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, mDbName);
        }
        SQLiteDatabase readableDatabase = mDevOpenHelper.getReadableDatabase();
        return readableDatabase;
    }

    private SQLiteDatabase getWritableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, mDbName);
        }
        SQLiteDatabase readableDatabase = mDevOpenHelper.getWritableDatabase();
        return readableDatabase;
    }

    private DownInfoDao getDownInfoDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownInfoDao downInfoDao = daoSession.getDownInfoDao();
        return downInfoDao;
    }

    public void saveDownInfo(DownInfo downInfo) {
        DownInfoDao downInfoDao = getDownInfoDao();
        downInfoDao.insert(downInfo);
    }

    public void updateDownInfo(DownInfo downInfo){
        DownInfoDao downInfoDao = getDownInfoDao();
        downInfoDao.update(downInfo);
    }

    public void deleteDownInfo(DownInfo downInfo){
        DownInfoDao downInfoDao = getDownInfoDao();
        downInfoDao.delete(downInfo);
    }

    public List<DownInfo> queryAll(){
        DownInfoDao downInfoDao = getDownInfoDao();
        QueryBuilder<DownInfo> downInfoQueryBuilder = downInfoDao.queryBuilder();
        List<DownInfo> list = downInfoQueryBuilder.list();
        return list;
    }

    public DownInfo queryDownById(long id){
        DownInfoDao downInfoDao = getDownInfoDao();
        QueryBuilder<DownInfo> downInfoQueryBuilder = downInfoDao.queryBuilder();
        List<DownInfo> list = downInfoQueryBuilder.where(DownInfoDao.Properties.MId.eq(id)).list();
        if (list.isEmpty()){
            return null;
        }else {
            return list.get(0);
        }
    }
}
