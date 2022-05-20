package com.starostinvlad.tsdapp.api

import com.starostinvlad.tsdapp.data.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ThingsBoardApi {

    @GET("")
    suspend fun checkAccessable(
    ): Response<String>

    @POST("/api/auth/login")
    suspend fun getAccessToken(
        @Body authData: AuthData,
    ): Response<JsonToken>

    @POST("/api/entitiesQuery/find")
    suspend fun findEntityDataByQuery(
        @Body body: EntityDataQuery
    ): EntityData

    @GET("/api/plugins/telemetry/ASSET/{id}/values/attributes/SERVER_SCOPE")
    suspend fun getAssetAttributes(
        @Path("id") id: String
    ): List<CheckBoxListItem>

    @GET("/api/relations/info")
    suspend fun getAssetRelationByForm(
        @Query("fromId") fromId: String,
        @Query("fromType") fromType: String
    ): List<FromTo>

    @GET("/api/asset/info/{id}")
    suspend fun getAssetInfo(
        @Path("id") id: String
    ): AssetInfo

    @GET("/api/plugins/telemetry/ASSET/{id}/values/attributes/SERVER_SCOPE")
    suspend fun getCheckListItems(
        @Path("id") id: String
    ): List<CheckBoxListItem>

    @POST("/api/rule-engine/")
    suspend fun uploadCheckListItemsTo1c(
        @Body body: CheckListResult
    ): Response<ResponseBody>

    @POST("/api/plugins/telemetry/ASSET/{id}/attributes/SERVER_SCOPE")
    suspend fun uploadCheckListItemsToTB(
        @Path("id") entityId: String,
        @Body body: @JvmSuppressWildcards Map<String, Int>
    ): Response<ResponseBody>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<Void>
}