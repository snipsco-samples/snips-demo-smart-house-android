package ai.snips.smarthousedemo

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class RoomCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val roomImage: AppCompatImageView by lazy { itemView.findViewById<AppCompatImageView>(R.id.room_img) }
    private val roomName: AppCompatTextView  by lazy { itemView.findViewById<AppCompatTextView>(R.id.room_name) }
    private val items = listOf(RoomItemHolder(itemView, R.id.room_item_1_img, R.id.room_item_1_name, R.id.room_item_1_value),
                               RoomItemHolder(itemView, R.id.room_item_2_img, R.id.room_item_2_name, R.id.room_item_2_value),
                               RoomItemHolder(itemView, R.id.room_item_3_img, R.id.room_item_3_name, R.id.room_item_3_value))

    fun bind(model: RoomModel) {
        roomImage.setImageResource(model.type.imageRes)
        roomName.setText(model.type.nameRes)
        items.zip(model.items.values).forEach { (view, model) -> view.bind(model)}
    }
}

class RoomItemHolder(private val itemView: View,
                     private val imageId: Int,
                     private val nameId: Int,
                     private val valueId: Int) {
    private val image by lazy { itemView.findViewById<AppCompatImageView>(imageId) }
    private val name by lazy { itemView.findViewById<AppCompatTextView>(nameId) }
    private val value by lazy { itemView.findViewById<AppCompatTextView>(valueId) }

    fun bind(model: RoomItemModel) {
        image.setImageResource(model.imageResId)
        name.setText(model.type.nameRes)
        model.value.res?.let { value.setText(it) }
        model.value.value?.let { value.text = it }
        value.setTextColor(model.color)
    }

}


class RoomsAdapter(private var data: List<RoomModel>) : Adapter<RoomCardHolder>() {


    fun update(newData : List<RoomModel>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RoomCardHolder(LayoutInflater.from(parent.context).inflate(R.layout.room_card, parent, false))


    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RoomCardHolder, position: Int) {
        holder.bind(data[position])
    }

}
