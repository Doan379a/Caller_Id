package com.example.caller_id.database.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.caller_id.database.entity.BlockedSms
import com.example.caller_id.database.repository.BlockRepository
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.utils.SmsUtils.getGroupedSmsInbox
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BlockRepository
) : ViewModel() {
    private val _refreshTrigger = MutableLiveData(Unit)
    private val _blockedTrigger = MutableLiveData(Unit)
    fun block(num: String) = viewModelScope.launch { repo.block(num) }
    fun unblock(num: String) = viewModelScope.launch { repo.unblock(num) }


    private val blockedNumbersFlow: Flow<Set<String>> = repo.getBlockedNumbersFlow()
    val blockedNumbers: LiveData<Set<String>> = blockedNumbersFlow.asLiveData()

    val inboxFiltered: LiveData<List<SmsConversation>> = _refreshTrigger.switchMap {
        blockedNumbers.map { blockedSet ->
            getGroupedSmsInbox(context)
                .filter { it.address !in blockedSet }
        }
    }

    fun refreshInbox() {
        _refreshTrigger.value = Unit
    }

    val blockFiltered: LiveData<List<SmsConversation>> = _blockedTrigger.switchMap {
        blockedNumbers.map { blockedSet ->
            getGroupedSmsInbox(context)
                .filter { it.address in blockedSet }
        }
    }
    fun refreshBlocked() {
        _blockedTrigger.value = Unit
    }

    fun getBlockedChat(address: String): LiveData<List<SmsMessage>> =
        repo.getSmsByAddressFlow(address).map { list ->
            list.map { bs ->
                SmsMessage(
                    address = bs.address,
                    body = bs.body,
                    date = bs.date,
                    read = bs.read,
                    isSentByMe = bs.isSentByMe,
                    status = SmsSendStatus.values().getOrNull(bs.status) ?: SmsSendStatus.SENT
                )
            }
        }.asLiveData()
}