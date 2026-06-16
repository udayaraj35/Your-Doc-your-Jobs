package com.example.data

import kotlinx.coroutines.flow.Flow

class ResumeRepository(private val resumeDao: ResumeDao) {
    val allResumes: Flow<List<Resume>> = resumeDao.getAllResumes()

    fun getResumeById(id: Int): Flow<Resume?> = resumeDao.getResumeById(id)

    suspend fun getResumeByIdSingle(id: Int): Resume? = resumeDao.getResumeByIdSingle(id)

    suspend fun insertResume(resume: Resume): Long = resumeDao.insertResume(resume)

    suspend fun updateResume(resume: Resume) = resumeDao.updateResume(resume)

    suspend fun deleteResumeById(id: Int) = resumeDao.deleteResumeById(id)

    suspend fun duplicateResume(id: Int) {
        val original = resumeDao.getResumeByIdSingle(id) ?: return
        val copied = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        resumeDao.insertResume(copied)
    }
}
