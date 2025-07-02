package com.alibaba.langengine.docloader.yuque.service;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * 语雀API
 *
 * @author xiaoxuan.lp
 */
public interface YuqueApi {

    @GET("/api/v2/repos/{namespace}/docs")
    Single<YuqueResult<List<YuqueDocInfo>>> getDocs(@Path(value = "namespace", encoded = true) String namespace,
                                                    @Query("offset") Integer offset,
                                                    @Query("limit") Integer limit,
                                                    @Query("optional_properties") String optionalProperties);

    @GET("/api/v2/repos/{namespace}/docs/{slug}")
    Single<YuqueResult<YuqueDocInfo>> getDocDetail(@Path(value = "namespace", encoded = true) String namespace,
                                                   @Path("slug") String slug);
}
