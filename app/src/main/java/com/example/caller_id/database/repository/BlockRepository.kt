package com.example.caller_id.database.repository

import android.content.Context
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.BlockedSmsDao
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.BlockedSms
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.utils.SmsUtils.getGroupedSmsInbox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockRepository @Inject constructor(
  private val blockedNumberDao: BlockedNumberDao,
  private val blockedSmsDao: BlockedSmsDao
) {


  suspend fun block(num: String) = blockedNumberDao.insert(BlockedNumber(num))
  suspend fun unblock(num: String) = blockedNumberDao.delete(BlockedNumber(num))
  fun getBlockedSmsFlow(): Flow<List<BlockedSms>> = blockedSmsDao.getAllBlockedSms()
  fun getBlockedNumbersFlow(): Flow<Set<String>> =
    blockedNumberDao.getAllBlockedNumber()
      .map { list -> list.map { it.number }.toSet() }

  fun getAllBlockedSmsFlow(): Flow<List<BlockedSms>> = blockedSmsDao.getAllBlockedSms()
  fun getSmsByAddressFlow(address: String): Flow<List<BlockedSms>> =
    blockedSmsDao.getSmsByAddress(address)

}
