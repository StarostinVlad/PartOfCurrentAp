package com.starostinvlad.tsdapp.base_mvp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter<T : BaseView?> : Presenter<T>, CoroutineScope {
    protected var view: T? = null

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun attachView(mvpView: T) {
        view = mvpView
    }

    override fun detachView() {
        view = null
        job.cancelChildren()
    }
}