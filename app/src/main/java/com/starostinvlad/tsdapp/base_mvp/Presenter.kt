package com.starostinvlad.tsdapp.base_mvp

interface Presenter<V> {
    fun attachView(mvpView: V)
    fun detachView()
}