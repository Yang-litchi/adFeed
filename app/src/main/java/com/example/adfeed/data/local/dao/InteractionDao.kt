package com.example.adfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adfeed.data.local.entity.InteractionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 广告交互状态的 Room 数据库访问接口 (DAO)
 */
@Dao
interface InteractionDao {
    
    /**
     * 根据广告 ID 获取单条交互状态实体
     */
    @Query("SELECT * FROM interactions WHERE adId = :adId")
    suspend fun getInteraction(adId: String): InteractionEntity?

    /**
     * 以 Flow 形式响应式观察所有交互状态的变更
     */
    @Query("SELECT * FROM interactions")
    fun getAllInteractionsFlow(): Flow<List<InteractionEntity>>

    /**
     * 以 Flow 形式响应式观察特定广告交互状态的变更
     */
    @Query("SELECT * FROM interactions WHERE adId = :adId")
    fun getInteractionFlow(adId: String): Flow<InteractionEntity?>

    /**
     * 插入或更新单条交互状态，若主键冲突则替换旧数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(interaction: InteractionEntity)
}
