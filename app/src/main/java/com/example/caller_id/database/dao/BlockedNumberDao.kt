package com.example.caller_id.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.BlockedSms

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
interface BlockedSmsDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(sms: BlockedSms)


  @Query("SELECT * FROM blocked_sms WHERE address = :address ORDER BY date ASC")
  fun getSmsByAddress(address: String): Flow<List<BlockedSms>>

  @Query("SELECT * FROM blocked_sms")
  fun getAllBlockedSms(): Flow<List<BlockedSms>>

}
