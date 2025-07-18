package com.example.caller_id.database.repository

import android.content.Context
import com.example.caller_id.database.dao.BlockedCalledDao
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.SpamNumberSmsDao
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.SpamNumberSms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockRepository @Inject constructor(
  private val blockedNumberDao: BlockedNumberDao,
  private val spamNumberSmsDao: SpamNumberSmsDao,
  private val blockedCalledDao: BlockedCalledDao
) {


  suspend fun block(num: String) = blockedNumberDao.insert(BlockedNumber(num))
  suspend fun unblock(num: String) = blockedNumberDao.delete(BlockedNumber(num))

  fun getBlockedNumbersFlow(): Flow<Set<String>> =
    blockedNumberDao.getAllBlockedNumber()
      .map { list -> list.map { it.number }.toSet() }

  suspend fun spamSms(num: String) = spamNumberSmsDao.insert(SpamNumberSms(num))
  suspend fun unSpamSms(num: String) = spamNumberSmsDao.delete(SpamNumberSms(num))

  fun getSpamSmsNumbersFlow(): Flow<Set<String>> =
    spamNumberSmsDao.getAllSpamNumber()
      .map { list -> list.map { it.number }.toSet() }

  suspend fun insertCalled(num: String, type: String, isSpam: Boolean) = blockedCalledDao.insert(BlockedCalled(number = num, type = type, isSpam = isSpam))
  suspend fun deleteCalled(id: Long) = blockedCalledDao.deleteById(id)
  fun getAllBlockedCalledFlow(): Flow<List<BlockedCalled>> = blockedCalledDao.getAllBlockCalled()
  fun getAllSpamCalledFlow(): Flow<List<BlockedCalled>> = blockedCalledDao.getAllSpamCalled()
}
