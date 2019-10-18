package com.john.breakpoint.network;

import com.google.gson.JsonObject;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


/**
 * Author: John
 * E-mail：634930172@qq.com
 * Date: 2018/3/2 10:11
 * Description:AppService类
 */

public interface AppService {

    //单图上传
    @Multipart
    @POST("testUploadFile")
    Observable<HttpResult<JsonObject>> uploadImg(@PartMap Map<String, RequestBody> map);

    //多图上传
    @Multipart
    @POST("testUploadFiles")
    Observable<HttpResult<JsonObject>> uploadImgs(@PartMap Map<String, RequestBody> map);

    //文件下载
    @Streaming
    @GET
    Observable<ResponseBody> download(@Header ("Range") String start,@Url String url);



}
