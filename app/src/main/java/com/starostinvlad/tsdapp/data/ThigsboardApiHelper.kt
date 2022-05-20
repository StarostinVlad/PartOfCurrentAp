package com.starostinvlad.tsdapp.data

import android.util.Log
import com.starostinvlad.tsdapp.api.RequestInterceptor
import com.starostinvlad.tsdapp.api.ThingsBoardApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ThingsboardApiHelper @Inject constructor(
    private val api: ThingsBoardApi,
    private val interceptor: RequestInterceptor
) {
    var taskChildren: EntityData? = null
    var vehicleChildren: EntityData? = null
    var task: EntityData? = null
    var vehicle: EntityData? = null
    var rfidTag: EntityData? = null
    var additionalInfo: AdditionalInfo? = null
    var userId: EntityId? = null

    var site: Entity? = null
    var row: Entity? = null

    private var _defects: MutableList<DefectData> = mutableListOf()
    var defects: List<DefectData> = _defects


    fun containsDefect(entity: DefectData) = _defects.contains(entity)

    fun removeDefect(defect: DefectData) {
        _defects.remove(defect)
    }

    fun addDefects(defects: List<DefectData>) {
        _defects.clear()
        _defects.addAll(defects)
    }

    fun addDefect(defect: DefectData) {
        _defects.add(defect)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        api.logout()
        interceptor.setToken("")
        return@withContext true
    }


    suspend fun findTaskByVehicle(entityId: EntityId) =
        withContext(Dispatchers.IO) {
            val query = EntityDataQuery(
                entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
                latest = listOf(
                    EntityKey("SERVER_ATTRIBUTE", "Номер шасси"),
                    EntityKey("SHARED_ATTRIBUTE", "aggregatesKeys"),
                    EntityKey("SHARED_ATTRIBUTE", "specificitiesKeys"),
                    EntityKey("SHARED_ATTRIBUTE", "clarificationsKeys"),
                ),
                entityFields = listOf(
                    EntityKey("ENTITY_FIELD", "name"),
                    EntityKey("ENTITY_FIELD", "type"),
                ),
                entityFilter = AssetSearchQueryFilter(
                    type = "relationsQuery",
                    rootEntity = entityId,
                    direction = EntitySearchDirection.TO,
                    maxLevel = 1,
                    fetchLastLevelOnly = false,
                    filters = listOf(
                        Filter(relationType = "TaskVehicle", entityTypes = listOf("ASSET"))
                    )
                )
            )
            task = api.findEntityDataByQuery(query)
            return@withContext task
        }

    suspend fun findVehicleByChassisNumber(chassisNumber: String) =
        withContext(Dispatchers.IO) {
            val query = EntityDataQuery(
                entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
                latest = listOf(
                    EntityKey("SERVER_ATTRIBUTE", "Номер шасси"),
                    EntityKey("SERVER_ATTRIBUTE", "latitude"),
                    EntityKey("SERVER_ATTRIBUTE", "longitude"),
                ),
                entityFields = listOf(
                    EntityKey("ENTITY_FIELD", "name"),
                    EntityKey("ENTITY_FIELD", "type")
                ),
                keyFilters = listOf(
                    KeyFilters(
                        entityKey = EntityKey("SERVER_ATTRIBUTE", "Номер шасси"),
                        predicate = Predicate(
                            operation = "EQUAL",
                            value = mapOf("defaultValue" to chassisNumber, "dynamicValue" to null),
                            type = "STRING",
                        ),
                        valueType = "STRING"
                    )
                ),
                entityFilter = AssetSearchQueryFilter(
                    type = "assetType",
                    assetType = "Vehicle"
                )
            )
            vehicle = api.findEntityDataByQuery(query)
            return@withContext vehicle
        }

    suspend fun findRfidTagByEPC(rfidTagEpc: String) =
        withContext(Dispatchers.IO) {
            val query = EntityDataQuery(
                entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
                latest = listOf(
                    EntityKey("SERVER_ATTRIBUTE", "Epc"),
                ),
                entityFields = listOf(
                    EntityKey("ENTITY_FIELD", "name"),
                    EntityKey("ENTITY_FIELD", "type")
                ),
                keyFilters = listOf(
                    KeyFilters(
                        entityKey = EntityKey("SERVER_ATTRIBUTE", "Epc"),
                        predicate = Predicate(
                            operation = "EQUAL",
                            value = mapOf("defaultValue" to rfidTagEpc, "dynamicValue" to null),
                            type = "STRING",
                        ),
                        valueType = "STRING"
                    )
                ),
                entityFilter = AssetSearchQueryFilter(
                    type = "assetType",
                    assetType = "RfidTag"
                )
            )
            rfidTag = api.findEntityDataByQuery(query)
            return@withContext rfidTag
        }

    suspend fun findVehicleByRfidTag(rfidTagId: EntityId) =
        withContext(Dispatchers.IO) {
            val query = EntityDataQuery(
                entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
                latest = listOf(
                    EntityKey("SERVER_ATTRIBUTE", "Номер шасси"),
                ),
                entityFields = listOf(
                    EntityKey("ENTITY_FIELD", "name"),
                    EntityKey("ENTITY_FIELD", "type")
                ),
                entityFilter = AssetSearchQueryFilter(
                    type = "relationsQuery",
                    rootEntity = rfidTagId,
                    direction = EntitySearchDirection.TO,
                    maxLevel = 1,
                    fetchLastLevelOnly = false,
                    filters = listOf(
                        Filter(
                            relationType = "VehicleRfidTag",
                            entityTypes = listOf("ASSET")
                        )
                    )
                )
            )
            vehicle = api.findEntityDataByQuery(query)
            return@withContext vehicle
        }

    suspend fun findVehicleChildren(entityId: EntityId) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
            latest = listOf(
                EntityKey("SERVER_ATTRIBUTE", "Epc"),
            ),
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type")
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "relationsQuery",
                rootEntity = entityId,
                direction = EntitySearchDirection.FROM,
                maxLevel = 1,
                fetchLastLevelOnly = false,
                filters = listOf(
                    Filter(
                        relationType = "VehicleRfidTag",
                        entityTypes = listOf("ASSET")
                    ),
                    Filter(
                        relationType = "VehicleDefect",
                        entityTypes = listOf("ASSET")
                    ),
                )
            )
        )
        vehicleChildren = api.findEntityDataByQuery(query)
        return@withContext vehicleChildren
    }

    suspend fun findVehicleSiteRow(entityId: EntityId) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10),
            latest = listOf(
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "latitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "longitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "perimeter"
                )
            ),
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type")
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "relationsQuery",
                rootEntity = entityId,
                direction = EntitySearchDirection.TO,
                maxLevel = 1,
                fetchLastLevelOnly = false,
                filters = listOf(
                    Filter(
                        relationType = "SiteRow",
                        entityTypes = listOf("ASSET")
                    ),
                )
            )
        )
        row = api.findEntityDataByQuery(query).data.firstOrNull()
        return@withContext row
    }

    suspend fun findIncompleteTask(userId: EntityId) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type"),
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "relationsQuery",
                rootEntity = userId,
                direction = EntitySearchDirection.FROM,
                filters = listOf(
                    Filter(
                        relationType = "CurrentTask",
                        entityTypes = listOf("ASSET")
                    ),
                    Filter(
                        relationType = "DestinationSiteRow", entityTypes = listOf("ASSET")
                    )
                ),
                maxLevel = 2,
                fetchLastLevelOnly = false
            ),
            latest = listOf(
                EntityKey(key = "status", type = "TIME_SERIES"), EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "latitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "longitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "perimeter"
                )
            ),
            entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10)
        )
        Log.e("QUERY", query.toString())
        task = api.findEntityDataByQuery(query)
        return@withContext task
    }

    //TODO Если TaskAcceptance taskChildren содержит DestinationSiteRow то необходимо сразу переходить к карте
    suspend fun findTaskChildren(entityId: EntityId) =
        withContext(Dispatchers.IO) {
            val query = EntityDataQuery(
                entityFields = listOf(
                    EntityKey("ENTITY_FIELD", "name"),
                    EntityKey("ENTITY_FIELD", "type")
                ),
                entityFilter = AssetSearchQueryFilter(
                    type = "relationsQuery",
                    entityType = "ASSET",
                    rootEntity = entityId,
                    direction = EntitySearchDirection.FROM,
                    maxLevel = 2,
                    fetchLastLevelOnly = false,
                    filters = listOf(
                        Filter(relationType = "TaskCheckList", entityTypes = listOf("ASSET")),
                        Filter(relationType = "TaskVehicle", entityTypes = listOf("ASSET")),
                        Filter(relationType = "VehicleRfidTag", entityTypes = listOf("ASSET")),
                        Filter(
                            relationType = "DestinationSiteRow", entityTypes = listOf("ASSET")
                        )
                    ),
                ),
                keyFilters = emptyList(),
                latest = listOf(
                    EntityKey(
                        "SERVER_ATTRIBUTE", "items"
                    ),
                    EntityKey(
                        "SERVER_ATTRIBUTE",
                        "checkItems"
                    ),
                    EntityKey(
                        "TIME_SERIES",
                        "condition"
                    )
                ),
                entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10)
            )
            Log.d(
                "QUERY", query.toString()
            )
            taskChildren = api.findEntityDataByQuery(query)
            return@withContext taskChildren
        }

    suspend fun searchDefects(text: String) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type")
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "assetType",
                assetType = "Defect"
            ),
            latest = listOf(
                EntityKey(
                    "SERVER_ATTRIBUTE", "Significance"
                ),
                EntityKey(
                    "SERVER_ATTRIBUTE",
                    "SearchNumber"
                ),
                EntityKey(
                    "SERVER_ATTRIBUTE",
                    "ReturnAvz"
                )
            ),
            entityDataPageLink = EntityDataPageLink(
                page = 0,
                pageSize = 10,
                sortOrder = SortOrder(
                    direction = "ASC",
                    key = EntityKey(key = "name", type = "ENTITY_FIELD")
                ), textSearch = text
            )
        )
        Log.d(
            "QUERY", query.toString()
        )
        return@withContext api.findEntityDataByQuery(query)
    }

    suspend fun getSites() = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type"),
                EntityKey("ENTITY_FIELD", "additionalInfo"),
                EntityKey("ENTITY_FIELD", "label"),
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "assetType",
                assetType = "Site"
            ),
            latest = null,
            entityDataPageLink = EntityDataPageLink(
                page = 0,
                pageSize = 10
            )
        )
        Log.d(
            "QUERY", query.toString()
        )
        return@withContext api.findEntityDataByQuery(query)
    }

    suspend fun login(username: String, password: String): Response<JsonToken> =
        withContext(Dispatchers.IO) {
            return@withContext api.getAccessToken(AuthData(username, password))
        }

    suspend fun getLocationSites(locationId: String) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type"),
                EntityKey("ENTITY_FIELD", "additionalInfo"),
                EntityKey("ENTITY_FIELD", "label"),
            ),
            entityFilter = AssetSearchQueryFilter(
                rootEntity = EntityId("ASSET", locationId),
                direction = EntitySearchDirection.FROM,
                type = "relationsQuery",
                filters = listOf(
                    Filter(
                        relationType = "LocationSite",
                        entityTypes = listOf("ASSET")
                    )
                )
            ),
            latest = null,
            entityDataPageLink = EntityDataPageLink(
                page = 0,
                pageSize = 10
            )
        )
        Log.d(
            "QUERY", query.toString()
        )
        return@withContext api.findEntityDataByQuery(query)
    }

    suspend fun getSiteRows(siteId: EntityId) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(
                EntityKey("ENTITY_FIELD", "name"),
                EntityKey("ENTITY_FIELD", "type"),
                EntityKey("ENTITY_FIELD", "additionalInfo"),
                EntityKey("ENTITY_FIELD", "label"),
            ),
            entityFilter = AssetSearchQueryFilter(
                type = "relationsQuery",
                rootEntity = siteId,
                direction = EntitySearchDirection.FROM,
                maxLevel = 1,
                fetchLastLevelOnly = false,
                filters = listOf(
                    Filter(
                        relationType = "SiteRow",
                        entityType = "ASSET"
                    )
                )
            ),
            latest = listOf(
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "latitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "longitude"
                ),
                EntityKey(
                    type = "SERVER_ATTRIBUTE",
                    key = "perimeter"
                )
            ),
            entityDataPageLink = EntityDataPageLink(
                page = 0,
                pageSize = 10,
                sortOrder = SortOrder(
                    direction = "ASC",
                    key = EntityKey(key = "name", type = "ENTITY_FIELD")
                ),
            )
        )
        Log.d(
            "QUERY getRows:", query.toString()
        )
        return@withContext api.findEntityDataByQuery(query)
    }
}