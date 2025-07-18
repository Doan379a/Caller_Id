package com.example.caller_id.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.SpamNumberSms

import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
  @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE number = :num)")
  suspend fun isBlocked(num: String): Boolean

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(num: BlockedNumber)

  @Delete
  suspend fun delete(num: BlockedNumber)

  @Query("SELECT * FROM blocked_numbers")
  fun getAllBlockedNumber(): Flow<List<BlockedNumber>>
}

@Dao
interface SpamNumberSmsDao {
  @Query("SELECT EXISTS(SELECT 1 FROM spam_numbers_sms WHERE number = :num)")
  suspend fun isBlocked(num: String): Boolean

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(num: SpamNumberSms)

  @Delete
  suspend fun delete(num: SpamNumberSms)

  @Query("SELECT * FROM spam_numbers_sms")
  fun getAllSpamNumber(): Flow<List<SpamNumberSms>>
}

@Dao
interface BlockedCalledDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(blocked: BlockedCalled)

  @Query("DELETE FROM blocked_called WHERE id = :id")
  suspend fun deleteById(id: Long)


  @Query("SELECT * FROM blocked_called WHERE isSpam = 1")
  fun getAllSpamCalled(): Flow<List<BlockedCalled>>

  @Query("SELECT * FROM blocked_called WHERE isSpam = 0")
  fun getAllBlockCalled(): Flow<List<BlockedCalled>>
}