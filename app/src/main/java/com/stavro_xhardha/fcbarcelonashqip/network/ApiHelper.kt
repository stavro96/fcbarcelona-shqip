package com.stavro_xhardha.fcbarcelonashqip.network

import com.stavro_xhardha.fcbarcelonashqip.model.NewsBody
import com.stavro_xhardha.fcbarcelonashqip.model.Standing
import com.stavro_xhardha.fcbarcelonashqip.model.TableItem
import com.stavro_xhardha.fcbarcelonashqip.model.Topic
import io.reactivex.Observable

interface ApiHelper {
    fun makeTopicApiCall(): Observable<List<Topic>>
    fun makeNewsBodyApiCall(): Observable<NewsBody>
    fun makeRankingApiCall(): Observable<Standing>
}