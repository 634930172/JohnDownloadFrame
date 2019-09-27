package com.john.johndownloadframe.network.converter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.john.johndownloadframe.network.HttpResult;
import com.john.johndownloadframe.network.ResponseParams;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;


/**
 * Author: ${John}
 * E-mail: 634930172@qq.com
 * Date: 2017/12/6 0006
 * <p/>
 * Description:最外层模型类
 */

public class RxResponseBodyConverter<T> implements Converter<ResponseBody, HttpResult<T>> {
    private final Gson gson;
    private final Type type;
    private static final String TAG="RxResponseBodyConverter";
    RxResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public HttpResult<T> convert(@NonNull ResponseBody value) throws IOException {
        String responseStr = value.string();
        HttpResult<T> httpResult;
        Log.e(TAG,"response: "+responseStr);
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            int code = jsonObject.getInt(ResponseParams.RES_CODE);
            String msg = jsonObject.getString(ResponseParams.RES_MSG);
            if (code == ResponseParams.RES_SUCCEED) {
                httpResult = gson.fromJson(responseStr, type);
            } else {
                httpResult = new HttpResult<>();
                httpResult.setCode(code);
                httpResult.setMsg(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            httpResult = new HttpResult<>();
            httpResult.setCode(401);
            httpResult.setMsg("解析异常");
        } finally {
            value.close();
        }
        return httpResult;
    }
}
