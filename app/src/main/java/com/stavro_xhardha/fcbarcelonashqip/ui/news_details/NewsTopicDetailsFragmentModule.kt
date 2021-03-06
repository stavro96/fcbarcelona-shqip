package com.stavro_xhardha.fcbarcelonashqip.ui.news_details

import com.stavro_xhardha.fcbarcelonashqip.ui.news_details.interactor.NewsTopicDetailsMVPInteractor
import com.stavro_xhardha.fcbarcelonashqip.ui.news_details.interactor.NewsTopicInteractor
import com.stavro_xhardha.fcbarcelonashqip.ui.news_details.presenter.NewsDetailsTopicPresenter
import com.stavro_xhardha.fcbarcelonashqip.ui.news_details.presenter.NewsTopicDetailsMVPPresenter
import com.stavro_xhardha.fcbarcelonashqip.ui.news_details.view.NewsTopicDetailsMVPView
import dagger.Module
import dagger.Provides

@Module
class NewsTopicDetailsFragmentModule {
    @Provides
    internal fun provideNewsTopicInteractor(interactor: NewsTopicInteractor): NewsTopicDetailsMVPInteractor = interactor

    @Provides
    internal fun provideNewsDetailsPresenter(presenter: NewsDetailsTopicPresenter<NewsTopicDetailsMVPView, NewsTopicDetailsMVPInteractor>)
            : NewsTopicDetailsMVPPresenter<NewsTopicDetailsMVPView, NewsTopicDetailsMVPInteractor> = presenter
}