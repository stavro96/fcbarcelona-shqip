package com.stavro_xhardha.fcbarcelonashqip

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.stavro_xhardha.fcbarcelonashqip.adapters.HistoryMatchAdapter
import com.stavro_xhardha.fcbarcelonashqip.brain.Brain
import com.stavro_xhardha.fcbarcelonashqip.events.CheckNetworkEvent
import com.stavro_xhardha.fcbarcelonashqip.events.ConfirmEmptyMatchHistorySetEvent
import com.stavro_xhardha.fcbarcelonashqip.events.RefreshDataEvent
import com.stavro_xhardha.fcbarcelonashqip.events.SetFragmenTagEvent
import com.stavro_xhardha.fcbarcelonashqip.model.MatchDetails
import com.stavro_xhardha.fcbarcelonashqip.model.ResultResponse

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Collections

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import com.stavro_xhardha.fcbarcelonashqip.brain.Brain.Companion.HISTORY_MATCH_FRAGMENT_TAG


class MatchHistoryFragment : Fragment() {
    private var details: ArrayList<MatchDetails>? = ArrayList()
    private var adapter: HistoryMatchAdapter? = null
    private var rvMatchDetails: RecyclerView? = null
    private var historyRefresh: SwipeRefreshLayout? = null
    private var emptyListContainer: LinearLayout? = null

    private var isSetEmpty: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents(view)
        afterInitialize()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(SetFragmenTagEvent(INSTANCE.getHISTORY_MATCH_FRAGMENT_TAG()))
        EventBus.getDefault().post(CheckNetworkEvent())
        getApiData()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshDataEvent?) {
        if (event != null) {
            getApiData()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ConfirmEmptyMatchHistorySetEvent?) {
        if (event != null) {
            isSetEmpty = event.isSetEmpty
            if (isSetEmpty) {
                emptyListContainer!!.visibility = View.VISIBLE
            } else {
                emptyListContainer!!.visibility = View.GONE
            }
        }
    }

    private fun initializeComponents(view: View) {
        rvMatchDetails = view.findViewById(R.id.history_rv)
        historyRefresh = view.findViewById(R.id.history_refresh)
        emptyListContainer = view.findViewById(R.id.empty_list_content_history)
    }

    private fun afterInitialize() {
        prepareRecyclerView()
        getApiData()
        historyRefresh!!.setOnRefreshListener { getApiData() }
    }

    private fun prepareRecyclerView() {
        val manageer = LinearLayoutManager(activity)
        rvMatchDetails!!.layoutManager = manageer
        adapter = HistoryMatchAdapter(details)
        rvMatchDetails!!.adapter = adapter
    }

    private fun getApiData() {
        val url = "http://api.football-data.org/v1/teams/81/fixtures"
        if (Brain.INSTANCE.isNetworkAvailable(context)) {
            GetMatchesAsync().execute(url)
        }
    }

    internal inner class GetMatchesAsync : AsyncTask<String, String, String>() {
        private var mApiResponse: ResultResponse<*>? = null
        private var code: Int = 0

        override fun onPreExecute() {
            super.onPreExecute()
            if (activity != null) {
                historyRefresh!!.isRefreshing = true
            }
        }

        override fun doInBackground(vararg strings: String): String? {
            try {
                val client = OkHttpClient()
                val gsonBuilder = GsonBuilder()
                val gson = gsonBuilder.create()
                val request = Request.Builder()
                        .addHeader(Brain.INSTANCE.getHEADER_RESPONSE_CONTROL(), Brain.INSTANCE.getRESPONSE_HEADER_VALUE())
                        .addHeader(Brain.INSTANCE.getAUTHORIZATION(), Brain.INSTANCE.getTOKEN())
                        .url(strings[0])
                        .build()
                var mInputStream: InputStream? = null
                var response: Response? = null

                response = client.newCall(request).execute()
                if (response != null) {
                    if (response.isSuccessful) {
                        mInputStream = response.body()!!.byteStream()
                    }
                }
                if (mInputStream != null) {
                    val reader = InputStreamReader(mInputStream)
                    val responseType = object : TypeToken<ResultResponse<MatchDetails>>() {

                    }.type
                    mApiResponse = gson.fromJson<ResultResponse>(reader, responseType)
                    code = response!!.code()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            historyRefresh!!.isRefreshing = false
            if (mApiResponse != null) {
                if (code == 200) {
                    details = mApiResponse!!.fixtures
                    if (details!!.size == 0) {
                        emptyListContainer!!.visibility = View.VISIBLE
                    } else {
                        adapter!!.setItemList(details!!)
                        Collections.reverse(details!!)
                    }
                } else {
                    Snackbar.make(view!!, resources.getString(R.string.can_not_get_data), Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view!!, resources.getString(R.string.can_not_get_data), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        fun newInstance(): MatchHistoryFragment {
            return MatchHistoryFragment()
        }
    }
}