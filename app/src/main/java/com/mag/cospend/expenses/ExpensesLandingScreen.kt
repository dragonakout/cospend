package com.mag.cospend.expenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mag.cospend.MainActivity
import com.mag.cospend.R
import com.mag.cospend.databinding.ExpensesYouBinding
import kotlinx.android.synthetic.main.app_bar_main.*
import org.json.JSONObject
import java.util.ArrayList

class ExpensesLandingScreen : Fragment() {
    // When requested, this adapter returns a YourExpensesFragment,
    // representing an object in the collection.
    private lateinit var expensesPagerAdapter: ExpensesPagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.expenses_landing_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity!!.toolbar.title = "Dépenses"
        expensesPagerAdapter = ExpensesPagerAdapter(childFragmentManager)
        viewPager = view.findViewById(R.id.expenses_pager)
        viewPager.adapter = expensesPagerAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)
    }
}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class ExpensesPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int  = 2

    override fun getItem(i: Int): Fragment {
        val fragment : Fragment
        if(i==0)
        {
            fragment = YourExpensesFragment()
        }
        else
        {
            fragment = PartnerExpenseFragment()
        }

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        if(position==0)
            return "Vous"
        if(position==1)
            return "Votre partenaire"
        else
            return "Votre "+position+"e partenaire"
    }

}


// Instances of this class are fragments representing a single
// object in our collection.
class YourExpensesFragment : Fragment() {

    lateinit var listView :  ListView
    private val owner : String = "mag"
    lateinit var totalSpend : String
    private lateinit var viewModel: NewExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalSpend = "0"

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = ExpensesYouBinding.inflate(inflater)
        viewModel = ViewModelProviders.of(this).get(NewExpenseViewModel::class.java)

        binding.viewModel = viewModel

        // Add observer for new expense fab
        viewModel.navigateToSearch.observe(viewLifecycleOwner,
            Observer<Boolean> { navigate ->
                if(navigate) {
                    val navController = findNavController()
                    navController.navigate(R.id.action_expensesMainScreen_to_newExpenseFragment)
                    viewModel.onNavigatedToSearch()
                }
            })

        listView = binding.expenseListYou
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets progressbar visibility
        val progressbar = activity!!.findViewById<View>(R.id.progressBar_you)
        progressbar.visibility = VISIBLE

        // Sends the http request for the data
        (activity as MainActivity).getExpenseList(listView,view,owner,progressbar, this)

        activity!!.findViewById<TextView>(R.id.tv_expenses_you).text = totalSpend
    }
}
class PartnerExpenseFragment : Fragment() {

    lateinit var listView :  ListView
    private val owner : String = "cam"
    lateinit var totalSpend : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalSpend = "0"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.expenses_other, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets progressbar visibility
        val progressbar = activity!!.findViewById<View>(R.id.progressBar_other)
        progressbar.visibility = VISIBLE

        // Sends the http request for the data
        listView = (activity as MainActivity).findViewById(R.id.expense_list_other)
        (activity as MainActivity).getExpenseList(listView,view,owner,progressbar,this)

        activity!!.findViewById<TextView>(R.id.tv_expenses_other).text = totalSpend
    }
}