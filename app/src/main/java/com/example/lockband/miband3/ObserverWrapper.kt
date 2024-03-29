package com.example.lockband.miband3

import io.reactivex.Emitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Wraps [Observer] with [Emitter]
 */
internal class ObserverWrapper<T>(private val emitter: Emitter<T>) : Observer<T> {

    override fun onSubscribe(d: Disposable) {
        // do nothing
    }

    override fun onNext(value: T) {
        emitter.onNext(value)
    }

    override fun onError(e: Throwable) {
        emitter.onError(e)
    }

    override fun onComplete() {
        emitter.onComplete()
    }
}
