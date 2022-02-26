package com.mag.cospend.expenses

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.mag.cospend.MainActivity
import com.mag.cospend.R

class DatePickerFragmentNewExpense : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity as MainActivity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        if(month<10)
            activity!!.findViewById<EditText>(R.id.new_expense_date).text = SpannableStringBuilder(year.toString()+"/0"+(month+1).toString()+"/"+day.toString())
        else
            activity!!.findViewById<EditText>(R.id.new_expense_date).text = SpannableStringBuilder(year.toString()+"/"+(month+1).toString()+"/"+day.toString())
    }
}