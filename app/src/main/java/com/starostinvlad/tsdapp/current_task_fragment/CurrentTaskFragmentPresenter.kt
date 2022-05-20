package com.starostinvlad.tsdapp.current_task_fragment

import android.util.Log
import com.starostinvlad.tsdapp.api.ThingsBoardApi
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrentTaskFragmentPresenter @Inject constructor(val thingsBoardApi: ThingsBoardApi) :
    BasePresenter<CurrentTaskFragmentContract>() {

    val TAG = javaClass.simpleName
    fun onLoaded(entityId: EntityId) {
        Log.d(TAG, "onLoaded: ${entityId.id}")
//        TODO("Not yet implemented")
    }

    fun onSelectedItem(it: FromTo) {
//        TODO("Not yet implemented")
    }

    private suspend fun loadCheckList(entityId: EntityId) = withContext(Dispatchers.IO) {
        val query = EntityDataQuery(
            entityFields = listOf(EntityKey("ENTITY_FIELD", "name")),
            entityFilter = AssetSearchQueryFilter(
                type = "assetSearchQuery",
                entityType = "ASSET",
                rootEntity = entityId,
                direction = EntitySearchDirection.FROM,
                maxLevel = 1,
                fetchLastLevelOnly = false,
                relationType = "TaskCheckList",
                assetType = "CheckList",
            ),
            keyFilters = emptyList(),
            latest = emptyList(),
            entityDataPageLink = EntityDataPageLink(page = 0, pageSize = 10)
        )
        Log.d("QUERY", query.toString())
        return@withContext thingsBoardApi.findEntityDataByQuery(query)
    }
}