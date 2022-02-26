package com.mag.cospend.expenses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mag.cospend.R



class ExpenseListAdapter : ArrayAdapter<String> {
    private var  assetList : ArrayList<String>
    var  dataListArray : ArrayList<ExpenseDataClass>


    private class ViewHolder {
        lateinit var desc: TextView
        lateinit var date : TextView
        lateinit var amount : TextView

    }


    constructor(context: Context, list : ArrayList<String>, dataList: ArrayList<ExpenseDataClass>) : super(context, R.layout.expense_list_item, list)
    {
        this.assetList = list
        this.dataListArray = dataList

    }


     override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Get the asset at position
        val viewHolder = ViewHolder()
        val result : View

        val inflater : LayoutInflater = LayoutInflater.from(context)
        result = inflater.inflate(R.layout.expense_list_item,parent,false)
        viewHolder.desc = result.findViewById(R.id.expense_list_desc) as TextView
        viewHolder.date = result.findViewById(R.id.expense_list_date)
        viewHolder.amount = result.findViewById(R.id.expense_list_amount)

        convertView?.tag = viewHolder

        viewHolder.desc.text = dataListArray[position].Description
        viewHolder.date.text = dataListArray[position].Date
        viewHolder.amount.text = dataListArray[position].Amount.toString()

        return result
    }

}