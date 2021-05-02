package com.example.pti_minautore

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView


class ItemAdapter(val context: Context, val items: ArrayList<AnimalClass>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    /**
     * Inflates the item views which is designed in the XML layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.items_row,
                        parent,
                        false
                )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items.get(position)

        holder.tvCode.text = item.id
        holder.tvSexe.text = item.sex
        holder.tvMere.text = item.mom
        holder.tvPere.text = item.dad

        // Updating the background color according to the odd/even positions in list.
        if (position % 2 == 0) {
            holder.llMain.setBackgroundColor(
                    ContextCompat.getColor(
                            context,
                            R.color.colorLightGray
                    )
            )
        } else {
            holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLighterGray))
        }

        //holder.ivEdit.setOnClickListener {
            //view -> context.updateRecordDialog(AnimalClass(item.id, item.sex, item.mom, item.dad))


        //}
        //holder.ivDelete.setOnClickListener { view ->

           //context.deleteRecordDialog(AnimalClass(item.id, item.sex, item.mom, item.dad))


        //}
    }

    fun updateAdapter( lst : ArrayList<AnimalClass>){
        items.clear()
        items.addAll(lst)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each item to
        val llMain = view.findViewById<LinearLayout>(R.id.llMain)
        val tvCode = view.findViewById<TextView>(R.id.tvCode)
        val tvSexe = view.findViewById<TextView>(R.id.tvSexe)
        val tvMere = view.findViewById<TextView>(R.id.tvMere)
        val tvPere = view.findViewById<TextView>(R.id.tvPere)
        //val ivEdit = view.findViewById<ImageView>(R.id.ivEdit)
        //val ivDelete = view.findViewById<ImageView>(R.id.ivDelete)

    }


}