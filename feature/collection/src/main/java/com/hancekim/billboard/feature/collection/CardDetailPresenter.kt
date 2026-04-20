package com.hancekim.billboard.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.data.model.CollectedCard
import com.hancekim.billboard.core.domain.GetCollectionFlowUseCase
import com.hancekim.billboard.core.domain.RemoveFromCollectionUseCase
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CardDetailPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: BillboardScreen.CardDetail,
    private val getCollectionFlowUseCase: GetCollectionFlowUseCase,
    private val removeFromCollectionUseCase: RemoveFromCollectionUseCase,
) : Presenter<CardDetailState> {

    @Composable
    override fun present(): CardDetailState {
        val scope = rememberCoroutineScope()

        val card by produceRetainedState<CollectedCard?>(null) {
            getCollectionFlowUseCase()
                .map { list -> list.find { it.key == screen.cardKey } }
                .collect { value = it }
        }

        return CardDetailState(card = card) { event ->
            when (event) {
                CardDetailEvent.OnCloseClick -> navigator.pop()
                CardDetailEvent.OnRemoveClick -> {
                    scope.launch {
                        removeFromCollectionUseCase(screen.cardKey)
                        navigator.pop()
                    }
                }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(BillboardScreen.CardDetail::class, ActivityRetainedComponent::class)
    fun interface CardDetailPresenterFactory {
        fun create(navigator: Navigator, screen: BillboardScreen.CardDetail): CardDetailPresenter
    }
}
