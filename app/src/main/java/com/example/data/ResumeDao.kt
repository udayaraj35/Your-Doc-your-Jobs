package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    fun getAllResumes(): Flow<List<Resume>>

    @Query("SELECT * FROM resumes WHERE id = :id")
    fun getResumeById(id: Int): Flow<Resume?>

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeByIdSingle(id: Int): Resume?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: Resume): Long

    @Update
    suspend fun updateResume(resume: Resume)

    @Delete
    suspend fun deleteResume(resume: Resume)

    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteResumeById(id: Int)
}
