package com.starostinvlad.tsdapp.data

data class MqttRpcCommand(val method: MqttRpcCommandMethod, val params: Map<String, Any?>)
enum class MqttRpcCommandMethod {
    AttachDeviceToUser, DetachDeviceFromUser, StartTask, EndTask, UpdateCheckList, AttachRfidTagToVehicle, AttachDefectToVehicle, DetachDefectFromVehicle, SetCoordinates, SetSite, AllowPassage
}