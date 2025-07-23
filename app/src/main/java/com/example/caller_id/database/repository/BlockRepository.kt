package com.example.caller_id.database.repository

import android.content.Context
import com.example.caller_id.database.dao.BlockedCalledDao
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.DNDCalledDao
import com.example.caller_id.database.dao.SpamNumberSmsDao
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.DoNotDisturbNumber
import com.example.caller_id.database.entity.SpamNumberSms
import com.example.caller_id.model.DndType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockRepository @Inject constructor(
  private val blockedNumberDao: BlockedNumberDao,
  private val spamNumberSmsDao: SpamNumberSmsDao,
  private val blockedCalledDao: BlockedCalledDao,
  private val dndCalledDao: DNDCalledDao
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


    //do not disturb
    suspend fun insertDndCalled(
        number: String,
        type: DndType,
        durationMillis: Long = 0L,
        remainingCount: Int = 0
    ) {
        val count = if (type == DndType.COUNTER) remainingCount else 0

        val item = DoNotDisturbNumber(
            number = number,
            type = type,
            endTimeMillis = durationMillis,
            remainingCount = count
        )
        dndCalledDao.insert(item)
    }

    suspend fun deleteDndCalled(id: Long) = dndCalledDao.deleteById(id)
    suspend fun deleteExpired(id: Long) = dndCalledDao.deleteExpired(id)
    suspend fun decreaseCounter(number: String) = dndCalledDao.decreaseCounter(number)
    suspend fun deleteIfCounterReached(number: String) = dndCalledDao.deleteIfCounterReached(number)

  fun getAllDndCalledFlow(): Flow<List<DoNotDisturbNumber>> = dndCalledDao.getAll()

}
