package com.example.caller_id.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.DoNotDisturbNumber
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

@Dao
interface DNDCalledDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(dnd: DoNotDisturbNumber)

  // Lấy tất cả các số đang được thiết lập "Không làm phiền"
  @Query("SELECT * FROM do_not_disturb")
  fun getAll(): Flow<List<DoNotDisturbNumber>>

  // Xoá số theo ID
  @Query("DELETE FROM do_not_disturb WHERE id = :id")
  suspend fun deleteById(id: Long)

  // Xoá các số hết thời gian "Không làm phiền"
  @Query("DELETE FROM do_not_disturb WHERE endTimeMillis > 0 AND endTimeMillis < :currentTime")
  suspend fun deleteExpired(currentTime: Long)

  // Giảm số lần còn lại của loại "đếm số lần gọi"
  @Query("UPDATE do_not_disturb SET remainingCount = remainingCount - 1 WHERE number = :number AND type = 'COUNTER'")
  suspend fun decreaseCounter(number: String)

  // Xoá số nếu đã hết lượt gọi còn lại (dành cho loại 'COUNTER')
  @Query("DELETE FROM do_not_disturb WHERE number = :number AND remainingCount <= 0 AND type = 'COUNTER'")
  suspend fun deleteIfCounterReached(number: String)
}