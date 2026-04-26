package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.RemoveAllFromCollectionUseCase
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import timber.log.Timber

class CollectionPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getCollectionFlowUseCase: GetCollectionFlowUseCase,
    private val removeAllFromCollectionUseCase: RemoveAllFromCollectionUseCase,
) : Presenter<CollectionState> {

    @Composable
    override fun present(): CollectionState {
        val cards by produceRetainedState(emptyList()) {
            getCollectionFlowUseCase().collect { value = it }
        }
        val scope = rememberCoroutineScope()

        return CollectionState(
            cards = cards.toPersistentList(),
        ) { event ->
            when (event) {
                is CollectionEvent.OnCardClick -> navigator.goTo(BillboardScreen.CardDetail(event.cardKey))
                CollectionEvent.OnBackClick -> navigator.pop()
                CollectionEvent.OnRemoveAllClick -> {
                    scope.launch {
                        runCatching { removeAllFromCollectionUseCase() }
                            .onFailure { Timber.e(it, "Failed to remove all cards") }
                    }
                }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.Collection::class, ActivityRetainedComponent::class)
    fun interface CollectionPresenterFactory {
        fun create(navigator: Navigator): CollectionPresenter
    }
}
