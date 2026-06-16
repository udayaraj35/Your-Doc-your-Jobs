package com.example.data

import android.content.Context
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.File
import java.io.FileOutputStream

// 1. Core Models for persistence
@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String = "default_user_profile",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePhotoUri: String = "",
    val professionalSummary: String = "",
    val preferredJobTitle: String = "",
    val country: String = "",
    val city: String = "",
    val education: String = "",
    val experience: String = "",
    val skills: String = "",
    val languages: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Section(
    val id: String = "",
    val name: String = "",
    val isHidden: Boolean = false,
    val isCollapsed: Boolean = false,
    val orderIndex: Int = 0,
    val iconName: String = "Star",
    val customContent: String = ""
)

// 2. Generic Hive Type Adapter System
interface HiveAdapter<T> {
    val typeId: Int
    fun serialize(value: T): String
    fun deserialize(json: String): T?
}

// 3. Concrete Type Adapters
class ResumeHiveAdapter(private val moshi: Moshi) : HiveAdapter<Resume> {
    override val typeId: Int = 0
    override fun serialize(value: Resume): String {
        return moshi.adapter(Resume::class.java).toJson(value)
    }
    override fun deserialize(json: String): Resume? {
        return moshi.adapter(Resume::class.java).fromJson(json)
    }
}

class UserProfileHiveAdapter(private val moshi: Moshi) : HiveAdapter<UserProfile> {
    override val typeId: Int = 1
    override fun serialize(value: UserProfile): String {
        return moshi.adapter(UserProfile::class.java).toJson(value)
    }
    override fun deserialize(json: String): UserProfile? {
        return moshi.adapter(UserProfile::class.java).fromJson(json)
    }
}

class SectionHiveAdapter(private val moshi: Moshi) : HiveAdapter<Section> {
    override val typeId: Int = 2
    override fun serialize(value: Section): String {
        return moshi.adapter(Section::class.java).toJson(value)
    }
    override fun deserialize(json: String): Section? {
        return moshi.adapter(Section::class.java).fromJson(json)
    }
}

// 4. Hive Box implementation supporting dynamic CRUD and direct serialization
class HiveBox<T>(
    val name: String,
    private val directory: File,
    private val adapter: HiveAdapter<T>
) {
    private val boxFile = File(directory, "$name.hive")
    private val cacheMap = mutableMapOf<String, T>()

    init {
        loadFromDisk()
    }

    @Synchronized
    private fun loadFromDisk() {
        if (!boxFile.exists()) return
        try {
            val lines = boxFile.readLines()
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split("||", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0]
                    val serializedValue = parts[1]
                    val value = adapter.deserialize(serializedValue)
                    if (value != null) {
                        cacheMap[key] = value
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun flushToDisk() {
        try {
            FileOutputStream(boxFile).bufferedWriter().use { writer ->
                for ((key, value) in cacheMap) {
                    val serialized = adapter.serialize(value)
                    writer.write("$key||$serialized\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun get(key: String): T? {
        return cacheMap[key]
    }

    fun getOrDefault(key: String, defaultValue: T): T {
        return cacheMap[key] ?: defaultValue
    }

    @Synchronized
    fun put(key: String, value: T) {
        cacheMap[key] = value
        flushToDisk()
    }

    @Synchronized
    fun delete(key: String) {
        cacheMap.remove(key)
        flushToDisk()
    }

    fun values(): List<T> {
        return cacheMap.values.toList()
    }

    fun keys(): List<String> {
        return cacheMap.keys.toList()
    }

    @Synchronized
    fun clear() {
        cacheMap.clear()
        flushToDisk()
    }

    fun size(): Int = cacheMap.size

    fun isEmpty(): Boolean = cacheMap.isEmpty()
}

// 5. Global Hive Singleton Manager
object Hive {
    private var isInitialized = false
    private lateinit var hiveDir: File
    private val adapters = mutableMapOf<Int, HiveAdapter<*>>()
    private val activeBoxes = mutableMapOf<String, HiveBox<*>>()
    private val moshi: Moshi = Moshi.Builder().build()

    fun init(context: Context) {
        if (isInitialized) return
        hiveDir = File(context.filesDir, "hive_db")
        if (!hiveDir.exists()) {
            hiveDir.mkdirs()
        }

        // Auto-register default adapters
        registerAdapter(ResumeHiveAdapter(moshi))
        registerAdapter(UserProfileHiveAdapter(moshi))
        registerAdapter(SectionHiveAdapter(moshi))

        isInitialized = true
    }

    fun <T> registerAdapter(adapter: HiveAdapter<T>) {
        adapters[adapter.typeId] = adapter
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun <T> box(name: String): HiveBox<T> {
        if (!isInitialized) {
            throw IllegalStateException("Hive has not been initialized. Please call Hive.init(context) on startup.")
        }
        return activeBoxes.getOrPut(name) {
            val adapter = retrieveAdapterForBox<T>(name)
            HiveBox(name, hiveDir, adapter)
        } as HiveBox<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> retrieveAdapterForBox(boxName: String): HiveAdapter<T> {
        val adapter = when {
            boxName.contains("resume", ignoreCase = true) -> adapters[0]
            boxName.contains("profile", ignoreCase = true) -> adapters[1]
            boxName.contains("section", ignoreCase = true) -> adapters[2]
            else -> adapters.values.firstOrNull()
        } ?: throw IllegalArgumentException("No Hive adapter registered for box: $boxName")
        return adapter as HiveAdapter<T>
    }
}
