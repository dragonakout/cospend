package com.mag.cospend.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mag.cospend.R
import com.android.volley.toolbox.Volley
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController

import com.android.volley.toolbox.StringRequest
import com.android.volley.*
import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.mag.cospend.MainActivity


/**
 * Huge thanks to https://www.youtube.com/watch?v=GdJ_se9wiv4 for the backend
 * https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard for keyboard hiding
*/

class NewExpenseFragment : Fragment() {
    private val EXPENSE_SO = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.expenses_new_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.findViewById<View>(R.id.btn_add_expense).setOnClickListener {
            addExpense()

        }
    }
    private fun addExpense() {

        val amount = activity!!.findViewById<EditText>(R.id.new_expense_montant).text.toString()
        val desc = activity!!.findViewById<EditText>(R.id.new_expense_desc).text.toString()
        val date = activity!!.findViewById<EditText>(R.id.new_expense_date).text.toString()

        // if there are blanks left
        if(amount == "" || desc == "" || date == "") {
            return
        }

        // Creates the loading dialog
        val loadingDialog = NewExpenseLoadingDialogFragment()
        loadingDialog.show(activity!!.supportFragmentManager, "loadingDialog")

        // Prevents user interacting with the app
        (activity as MainActivity).window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val stringRequest = object : StringRequest(Method.POST, MainActivity.API_URL,
            object : Response.Listener<String> {
                override fun onResponse(response: String) {

                    Snackbar.make(view!!,"Dépense ajoutée avec succès!",Snackbar.LENGTH_SHORT).show()
                    hideKeyboardFrom(activity as MainActivity,view!!)
                    loadingDialog.dismiss()
                    (activity as MainActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    findNavController().popBackStack()

                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {

                    Log.println(Log.ERROR,"NEW EXPENSE","Volley Error: " + error.toString())
                    Snackbar.make(view!!,"Erreur de connexion",Snackbar.LENGTH_SHORT).show()
                    hideKeyboardFrom(activity as MainActivity,view!!)
                    (activity as MainActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    loadingDialog.dismiss()
                    findNavController().popBackStack()
                }
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String,String>()
                val date = activity!!.findViewById<EditText>(R.id.new_expense_date).text.toString()
                val mag_expenses = activity!!.findViewById<EditText>(R.id.new_expense_montant).text.toString().replace('.',',')
                val desc = activity!!.findViewById<EditText>(R.id.new_expense_desc).text.toString()

                //here we pass params
                params.put("action", "addExpense")
                params.put("date", date)
                params.put("mag", mag_expenses)
                params.put("cam", EXPENSE_SO)
                params.put("desc", desc)

                return params
            }
        }

        val socketTimeOut = 50000 // 50 secs timeout

        val retryPolicy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = retryPolicy

        val queue = Volley.newRequestQueue(this.activity)

        queue.add(stringRequest)


    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}