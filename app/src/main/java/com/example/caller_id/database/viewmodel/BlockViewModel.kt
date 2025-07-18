package com.example.caller_id.database.viewmodel

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.repository.BlockRepository
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.utils.SmsUtils.getGroupedSmsInbox
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class BlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BlockRepository
) : ViewModel() {

    // Trigger cho reload inbox
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)  // load lần đầu khi init ViewModel
    }

    // Shared inboxFlow: đọc SMS mỗi khi có trigger
    private val inboxFlow: SharedFlow<List<SmsConversation>> =
        refreshTrigger
            .flatMapLatest {
                flow {
                    emit(getGroupedSmsInbox(context))
                }.flowOn(Dispatchers.IO)
            }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    // Flows số block & spam
    private val blockedFlow = repo.getBlockedNumbersFlow()
    private val spamFlow = repo.getSpamSmsNumbersFlow()
    val blockedNumbers: LiveData<Set<String>> = blockedFlow.asLiveData()
    val spamNumbers: LiveData<Set<String>> = spamFlow.asLiveData()

    // Tìm kiếm theo query
    private val _listSearch = MutableLiveData<String>()
    val listSearch: LiveData<String> get() = _listSearch

    fun search(query: String) {
        _listSearch.value = query
    }

    // Tổng hợp bộ lọc chung
    private val allFilterFlow = blockedFlow.combine(spamFlow) { b, s -> b + s }

    // LiveData sau filter
    val inboxFiltered: LiveData<List<SmsConversation>> = inboxFlow
        .combine(allFilterFlow) { inbox, filters ->
            inbox.filter { it.address !in filters }
        }
        .asLiveData()

    val blockFiltered: LiveData<List<SmsConversation>> = blockedFlow
        .flatMapLatest { blocked ->
            inboxFlow.map { it.filter { c -> c.address in blocked } }
        }
        .asLiveData()

    val spamFiltered: LiveData<List<SmsConversation>> = spamFlow
        .flatMapLatest { spam ->
            inboxFlow.map { it.filter { c -> c.address in spam } }
        }
        .asLiveData()

    // Reload dữ liệu SMS khi có trigger
    fun refreshInbox() {
        viewModelScope.launch { refreshTrigger.emit(Unit) }
    }

    // Chặn, spam SMS
    fun block(num: String) = viewModelScope.launch { repo.block(num) }
    fun unblock(num: String) = viewModelScope.launch { repo.unblock(num) }
    fun spamSms(num: String) = viewModelScope.launch { repo.spamSms(num) }
    fun unSpamSms(num: String) = viewModelScope.launch { repo.unSpamSms(num) }

    // Chặn, spam cuộc gọi
    fun insertCallBlock(num: String, type: String, isSpam: Boolean) = viewModelScope.launch {
        repo.insertCalled(num, type, isSpam)
    }
    fun deleteCallById(id: Long) = viewModelScope.launch {
        repo.deleteCalled(id)
    }
    val callBlockedList: LiveData<List<BlockedCalled>> = repo.getAllBlockedCalledFlow().asLiveData()
    val callSpamList: LiveData<List<BlockedCalled>> = repo.getAllSpamCalledFlow().asLiveData()
}
