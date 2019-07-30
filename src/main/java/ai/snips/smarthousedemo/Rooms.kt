package ai.snips.smarthousedemo

import ai.snips.hermes.IntentMessage
import ai.snips.nlu.ontology.SlotValue.CustomValue
import ai.snips.nlu.ontology.SlotValue.NumberValue
import ai.snips.nlu.ontology.SlotValue.PercentageValue
import ai.snips.nlu.ontology.SlotValue.TemperatureValue
import ai.snips.smarthousedemo.ItemType.HEATER
import ai.snips.smarthousedemo.ItemType.LIGHTS
import ai.snips.smarthousedemo.ItemType.WINDOWS
import ai.snips.smarthousedemo.RoomType.BEDROOM
import ai.snips.smarthousedemo.RoomType.KIDS_BEDROOM
import ai.snips.smarthousedemo.RoomType.KITCHEN
import ai.snips.smarthousedemo.RoomType.LIVING_ROOM
import ai.snips.smarthousedemo.Rooms.UpDown.DOWN
import ai.snips.smarthousedemo.Rooms.UpDown.UP
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.widget.Toast
import java.util.*

object Rooms {
    private val rooms = EnumMap(mapOf(
            KITCHEN to RoomModel(KITCHEN, defaultAppliances()),
            BEDROOM to RoomModel(BEDROOM, defaultAppliances()),
            KIDS_BEDROOM to RoomModel(KIDS_BEDROOM, defaultAppliances()),
            LIVING_ROOM to RoomModel(LIVING_ROOM, defaultAppliances())
    ))

    fun asList() = rooms.values.toList()

    private fun defaultAppliances() = EnumMap(
            mapOf(LIGHTS to RoomItemModel(type = LIGHTS,
                    imageResId = R.drawable.light_off,
                    value = RoomItemValue.res(R.string.room_item_value_off, 0.0)),
                    HEATER to RoomItemModel(type = HEATER,
                            imageResId = R.drawable.heater_off,
                            value = RoomItemValue.res(R.string.room_item_value_off, 20.0)),
                    WINDOWS to RoomItemModel(type = WINDOWS,
                            imageResId = R.drawable.blinds_closed,
                            value = RoomItemValue.res(R.string.room_item_value_closed, 0.0))
            ))

    fun applyIntent(intent: IntentMessage): List<RoomModel> {
        val roomsTypes = extractRoomTypes(intent)

        roomsTypes.forEach {
            update(rooms[it]!!, intent)
        }
        println(asList())

        return asList()

    }

    fun getUpdateDiff(intent: IntentMessage) = getUpdateDiffs(intent).firstOrNull()

    private fun getUpdateDiffs(intent: IntentMessage): List<RoomUpdateModel> {

        val roomTypes = extractRoomTypes(intent)

        val selectedRoomItemModels = roomTypes.map {
            rooms[it]!!.items[extractRoomItemType(intent)]!!
        }

        val afterRoomItemModels = selectedRoomItemModels.map { update(it, intent) }

        return (selectedRoomItemModels zip afterRoomItemModels).map { RoomUpdateModel(roomTypes, it.first, it.second) }

    }

    private fun update(room: RoomModel, intent: IntentMessage) {
        val matchedItemType = extractRoomItemType(intent)
        room.items[matchedItemType] = update(room.items[matchedItemType]!!, intent)
    }

    private fun RoomItemModel.percentOrOff(value: Double,
                                           @StringRes offRes: Int,
                                           @DrawableRes onImg: Int,
                                           @DrawableRes offImg: Int): RoomItemModel {
        val newValue = Math.min(Math.max(0.0, value), 100.0)
        return if (newValue < 0.0001)
            copy(value = RoomItemValue.res(offRes, numeric = 0.0), imageResId = offImg)
        else
            copy(value = RoomItemValue.value(String.format("%.0f %%", newValue), newValue), imageResId = onImg)
    }

    private fun RoomItemModel.temperature(value: Double): RoomItemModel {
        val newValue = Math.min(Math.max(1.0, value), 40.0)
        return copy(value = RoomItemValue.value(String.format("%.1f °", newValue), newValue), imageResId = R.drawable.heater_on)
    }

    private fun update(item: RoomItemModel, intent: IntentMessage): RoomItemModel {
        return when (intent.intent.intentName) {
            "lightsSet" -> {
                val value = (intent.numericSlot("intensity_number")
                        ?: intent.percentSlot("intensity_percent") ?: 100.0)

                item.percentOrOff(value, R.string.room_item_value_off, R.drawable.light_on, R.drawable.light_off)
            }
            "lightsTurnOff" -> {
                item.copy(value = RoomItemValue.res(R.string.room_item_value_off, 0.0), imageResId = R.drawable.light_off)
            }
            "lightsShift" -> {
                val value = item.value.numeric + (intent.numericSlot("intensity_number")
                        ?: intent.percentSlot("intensity_percent")
                        ?: 20.0) * (intent.upDownSlot("up_down")
                        ?: UP).numeric
                item.percentOrOff(value, R.string.room_item_value_off, R.drawable.light_on, R.drawable.light_off)

            }
            "windowDevicesOpen" -> {
                val value = intent.numericSlot("number") ?: 100.0
                item.percentOrOff(value, R.string.room_item_value_closed, R.drawable.blinds_opened, R.drawable.blinds_closed)
            }
            "windowDevicesClose" -> {
                val value = intent.numericSlot("number") ?: 0.0
                item.percentOrOff(value, R.string.room_item_value_closed, R.drawable.blinds_opened, R.drawable.blinds_closed)
            }
            "windowDevicesPause" -> {
                item
            }
            "thermostatTurnOff" -> {
                item.copy(value = RoomItemValue.res(R.string.room_item_value_off, item.value.numeric), imageResId = R.drawable.heater_off)
            }
            "thermostatShift" -> {
                val value = item.value.numeric + (intent.temperature("temperature")
                        ?: 2.0f) * (intent.upDownSlot("up_down") ?: UP).numeric.toFloat()
                item.temperature(value)
            }
            "thermostatSet" -> {
                val value = intent.temperature("temperature")?.toDouble() ?: 20.0
                item.temperature(value)
            }
            else -> throw RuntimeException("unknown intent ${intent.intent.intentName}")
        }
    }

    private fun extractRoomItemType(intent: IntentMessage): ItemType {
        val intentClassifierResult: String = intent.intent.intentName
        return when {
            intentClassifierResult.startsWith("lights") ->
                LIGHTS
            intentClassifierResult.startsWith("window") ->
                WINDOWS
            intentClassifierResult.startsWith("thermostat") ->
                HEATER
            else -> throw RuntimeException("unknown intent ${intent.intent.intentName}")
        }
    }

    private fun extractRoomTypes(intent: IntentMessage): List<RoomType> {
        return intent.slots.filter { it.slotName == "house_room" }.flatMap {
            when ((it.value as CustomValue).value) {
                "kitchen" -> listOf(KITCHEN)
                "bedroom", "bedroom s" -> listOf(BEDROOM)
                "danis bedroom", "danis room", "Dani's room" -> listOf(KIDS_BEDROOM)
                "kids bedroom" -> listOf(KIDS_BEDROOM)
                "living room" -> listOf(LIVING_ROOM)
                "everywhere", "house", "flat", "appartment" -> listOf(LIVING_ROOM, BEDROOM, KITCHEN, KIDS_BEDROOM)
                else -> {
                    Toast.makeText(SmartHouseApplication.appContext, "I cannot control the ${(it.value as CustomValue).value} !", Toast.LENGTH_LONG).show()
                    listOf(null)
                }
            }
        }.let { if (it.isEmpty()) listOf(KITCHEN, BEDROOM, KIDS_BEDROOM, LIVING_ROOM) else it }.filterNotNull()
    }

    private fun IntentMessage.numericSlot(name: String): Double? {
        return this.slots.filter { it.slotName == name }.map { (it.value as NumberValue).value }.firstOrNull()
    }

    private fun IntentMessage.percentSlot(name: String): Double? {
        return this.slots.filter { it.slotName == name }.map { (it.value as PercentageValue).value }.firstOrNull()
    }

    private fun IntentMessage.temperature(name: String): Float? {
        return this.slots.filter { it.slotName == name }.map { (it.value as TemperatureValue).value }.firstOrNull()
    }

    enum class UpDown(val numeric: Double) {
        UP(1.0), DOWN(-1.0)
    }

    private fun IntentMessage.upDownSlot(name: String): UpDown? {
        return this.slots.filter { it.slotName == name }.map {
            when ((it.value as CustomValue).value) {
                "up" -> UP
                "down" -> DOWN
                else -> throw RuntimeException("unknown value for updown : $it")
            }
        }.firstOrNull()
    }

}
