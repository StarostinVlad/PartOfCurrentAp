package com.starostinvlad.tsdapp.data

import com.google.gson.annotations.SerializedName
import com.starostinvlad.tsdapp.adapter.Item
import java.io.Serializable


data class DefectData(var title: String, var subTitle: String, var status: Boolean) : Item

data class JsonToken(
    val token: String,
    val refreshToken: String
)

data class JwtToken(
    var sub: String,
    var scopes: List<String>,
    var userId: String,
    var enabled: Boolean,
    var isPublic: Boolean,
    var tenantId: String,
    var customerId: String,
    var iss: String,
    var iat: Long,
    var exp: Long
)

enum class EntitySearchDirection {
    FROM, TO
}


data class EntityDataQuery(
    var entityFields: List<EntityKey>? = null,
    var entityFilter: AssetSearchQueryFilter? = null,
    var keyFilters: List<KeyFilters>? = null,
    @SerializedName("latestValues") var latest: List<EntityKey>?,
    @SerializedName("pageLink") var entityDataPageLink: EntityDataPageLink?
)

data class Filter(
    var type: String? = null, var entityType: String? = null,
    @SerializedName("relationType") var relationType: String? = null,
    @SerializedName("entityTypes") var entityTypes: List<String>? = null
)

data class AssetSearchQueryFilter(
    var type: String? = null,
    var entityType: String? = null,
    var rootEntity: EntityId? = null,
    var relationType: String? = null,
    var direction: EntitySearchDirection? = null,
    var maxLevel: Int? = null,
    var fetchLastLevelOnly: Boolean? = null,
    var filters: List<Filter>? = null,
    var assetType: String? = null
)

data class KeyFilters(
    @SerializedName("key") var entityKey: EntityKey,
    var predicate: Predicate?,
    var valueType: String
)


data class EntityKey(var type: String, var key: String)


data class Predicate(
    var type: String,
    var operation: String,
    var value: Map<String, String?>,
    var ignoreCase: Boolean? = null
)

data class EntityDataPageLink(
    var page: Int,
    var pageSize: Int,
    var sortOrder: SortOrder? = null,
    var textSearch: String? = null
)

data class SortOrder(var direction: String, var key: EntityKey)

data class EntityData(
    var data: List<Entity>,
    var totalPages: Int,
    var totalElements: Int,
    var hasNext: Boolean
) : Serializable

data class Entity(var entityId: EntityId, @SerializedName("latest") var latest: Latest) : Item,
    Serializable


data class EntityId(var entityType: String, var id: String) : Serializable


data class Latest(
    @SerializedName("ENTITY_FIELD") var entityField: Map<String, Name>?,
    @SerializedName("SERVER_ATTRIBUTE") var serverAttribute: Map<String, Name>?,
    @SerializedName("SHARED_ATTRIBUTE") var sharedAttribute: Map<String, Name>?
) : Serializable

data class Name(var ts: Long, var value: String) : Item, Serializable
data class NameMap(var ts: Long, var value: Map<String, String>) : Item, Serializable

data class FromTo(
    var from: EntityId,
    var to: EntityId,
    var type: String,
    var typeGroup: String,
    var additionalInfo: String,
    var fromName: String,
    var toName: String
) : Item, Serializable


data class AssetInfo(
    var id: EntityId,
    val createdTime: String,
    var additionalInfo: Map<String, String>,
    var tenantId: EntityId,
    var customerId: EntityId,
    var name: String,
    var type: String,
    var label: String,
    var customerTitle: String,
    var customerIsPublic: Boolean
)


data class CheckBoxListItem(
    var lastUpdateTs: Long,
    var key: String,
    var value: String,
    var status: Int
) : Item


data class CheckListResult(
    var check_list_name: String,
    var check_list_items: Map<String, Int>
)

data class AttachResult(var method: MqttRpcCommandMethod, var params: Params)
data class Params(
    var success: Boolean,
    var message: String,
    var additionalInfo: AdditionalInfo?
)

data class AdditionalInfo(
    var locationId: String,
    var locationName: String,
    var locationType: String
)