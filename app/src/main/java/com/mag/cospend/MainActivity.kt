package com.mag.cospend

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.ListView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.mag.cospend.expenses.DatePickerFragmentNewExpense
import org.json.JSONArray
import org.json.JSONObject
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mag.cospend.expenses.PartnerExpenseFragment
import com.mag.cospend.expenses.YourExpensesFragment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    // Global Constant for the Google Script API
    companion object{

        const val API_URL = "https://script.google.com/macros/s/AKfycbxCqkClPmejjC4lg8JRNaP55sKiy3ta4_XC7Psx48OJizfWyb2o/exec"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = this.findNavController(R.id.nav_host_fragment)

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)
        navView.setNavigationItemSelectedListener(this)
    }
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_login -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.connectMainScreen)
            }
            R.id.nav_tasks -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.tasksMainScreen)
            }
            R.id.nav_expenses -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.expensesMainScreen)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun showDatePickerDialogNewExpense(v: View) {
        val newFragment = DatePickerFragmentNewExpense()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    /**
     * Gets the expenses and fills the results in the ListViews
     */
    fun getExpenseList(listView : ListView, view : View, owner : String, progressBar : View, fragment : Fragment) {

        val stringRequest = object : StringRequest(
            Method.POST, MainActivity.API_URL,
            object : Response.Listener<String> {
                override fun onResponse(response: String) {
                    val jsonResponse = JSONObject(response).getJSONArray("expenses")
                    try {
                        parseResponse(listView,owner,jsonResponse,fragment)
                        progressBar.visibility = GONE
                    }

                    catch(e : Exception){
                        Log.println(Log.ERROR,"GET EXPENSES","VOLLEY ERROR: "+e)
                        progressBar.visibility = GONE
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Log.println(Log.ERROR,"NEW EXPENSE","Volley Error: " + error.toString())
                    Snackbar.make(view,"Erreur de connexion", Snackbar.LENGTH_SHORT).show()
                    progressBar.visibility = GONE
                }
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String,String>()

                params.put("action", "getExpenseList")

                return params
            }
        }

        val socketTimeOut = 50000 // 50 secs timeout

        val retryPolicy = DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = retryPolicy

        val queue = Volley.newRequestQueue(this)

        queue.add(stringRequest)


    }

    private fun parseResponse(listView : ListView, owner : String, jsonResponse: JSONArray, fragment : Fragment){
        val list: ArrayList<HashMap<String, String>> = ArrayList()
        // Sets a max of 2 decimals for all numbers
        val df  = DecimalFormat("0.##")

        for (i in jsonResponse.length() - 1 downTo 0) {
            if (!jsonResponse.getJSONObject(i).isNull(owner)) {
                if (jsonResponse.getJSONObject(i)[owner] != "") {


                    val amount = castJsonIntToDouble(jsonResponse.getJSONObject(i),owner)

                    val expense : HashMap<String, String> = HashMap()
                    expense.put("owner",owner)
                    expense.put("desc",(jsonResponse.getJSONObject(i)["desc"] as String))
                    expense.put("date",(jsonResponse.getJSONObject(i)["date"] as String).substring(0,10))
                    expense.put("amount",df.format(amount).toString()+" $")

                    list.add(expense)
                }
            }
        }

        val adapter = SimpleAdapter(
            this,
            list,
            R.layout.expense_list_item,
            arrayOf("desc", "date", "amount"),
            intArrayOf(R.id.expense_list_desc, R.id.expense_list_date, R.id.expense_list_amount)
        )

        listView.adapter = adapter

        if(fragment is YourExpensesFragment)
        {
            val total = df.format(
                castJsonIntToDouble(
                    jsonResponse.getJSONObject(0),"total_mag")).toString()

            fragment.totalSpend = total
            findViewById<TextView>(R.id.tv_expenses_you).text = total
        }
        if(fragment is PartnerExpenseFragment)
        {
            val total = df.format(
                castJsonIntToDouble(
                    jsonResponse.getJSONObject(0),"total_cam")).toString()
            fragment.totalSpend = total
            findViewById<TextView>(R.id.tv_expenses_other).text = total
        }

    }

    private fun castJsonIntToDouble(json:JSONObject, key:String):Double {
        if (json[key] is Double) {
            return json[key] as Double
        } else {
            return (json[key] as Int).toDouble()
        }
    }
}
