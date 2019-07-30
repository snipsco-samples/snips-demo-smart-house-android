package ai.snips.smarthousedemo

import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import java.util.EnumMap


enum class RoomType(@StringRes val nameRes: Int, @DrawableRes val imageRes: Int) {
    KITCHEN(R.string.room_name_kitchen, R.drawable.bg_kitchen),
    BEDROOM(R.string.room_name_bedroom, R.drawable.bg_bedroom),
    KIDS_BEDROOM(R.string.room_name_kids_bedroom, R.drawable.kb_bg),
    LIVING_ROOM(R.string.room_name_living_room, R.drawable.lr_bg)
}

enum class ItemType(@StringRes val nameRes: Int) {
    LIGHTS(R.string.room_item_lights), HEATER(R.string.room_item_heater), WINDOWS(R.string.room_item_window)
}

data class RoomModel(
        val type: RoomType,
        val items: EnumMap<ItemType, RoomItemModel>
)

data class RoomItemModel(
        val type: ItemType,
        @DrawableRes val imageResId: Int,
        val value: RoomItemValue,
        @ColorInt val color: Int = 0xff696e7d.toInt()
)

class RoomItemValue private constructor(@StringRes val res: Int?, val value: String?, val numeric: Double) {
    companion object {
        fun res(@StringRes res: Int, numeric: Double) = RoomItemValue(res, null, numeric)
        fun value(value: String, numeric: Double) = RoomItemValue(null, value, numeric)
    }

    override fun toString() = "RoomItemValue[$numeric]"
}

data class RoomUpdateModel(
        val updatedRooms: List<RoomType>,
        val beforeRoomItem: RoomItemModel,
        val afterRoomItem: RoomItemModel
)