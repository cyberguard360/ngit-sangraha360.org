package com.example.sg360.models

import android.app.Activity

data class StatsData(
    // Data class to hold the statistics from the device
    val username: String? = "",
    val APICall: String = "",
    val Activity: String = "",
    val Call: String = "",
    val Feature: String = "",
    val Intent: String = "",
    val MemoryHeapAlloc: Double = 0.0,
    val MemoryHeapFree: Double = 0.0,
    val MemoryHeapSize: Double = 0.0,
    val MemoryParcelMemory: Double = 0.0,
    val MemoryPrivateClean: Double = 0.0,
    val MemoryPrivateDirty: Double = 0.0,
    val MemoryPssClean: Double = 0.0,
    val MemorySharedClean: Double = 0.0,
    val MemorySharedDirty: Double = 0.0,
    val Memory_PssTotal: Double = 0.0,
    val Permission: String,
    val Provider: String,
    val RealPermission: String,
    val ServiceReceiver: String,
    val URL: String,
    val callbacks_nanonymous: Double = 0.0,
    val callbacks_ncallbacks: Double = 0.0,
    val callbacks_ngeneric: Double = 0.0,
    val handles_nfile: Double = 0.0,
    val handles_nthread: Double = 0.0,
    val ldrmodules_not_in_init: Double = 0.0,
    val ldrmodules_not_in_init_avg: Double = 0.0,
    val ldrmodules_not_in_load: Double = 0.0,
    val ldrmodules_not_in_load_avg: Double = 0.0,
    val ldrmodules_not_in_mem: Double = 0.0,
    val ldrmodules_not_in_mem_avg: Double = 0.0,
    val malfind_commitCharge: Double = 0.0,
    val malfind_ninjections: Double = 0.0,
    val malfind_protection: Double = 0.0,
    val malfind_uniqueInjections: Double = 0.0,
    val pslist_avg_threads: Double = 0.0,
    val pslist_nproc: Double = 0.0,
    val pslist_nprocs64bit: Double = 0.0,
    val svcscan_fs_drivers: Double = 0.0,
    val svcscan_interactive_process_services: Double = 0.0,
    val svcscan_kernel_drivers: Double = 0.0,
    val svcscan_nactive: Double = 0.0,
    val svcscan_nservices: Double = 0.0,
    val svcscan_process_services: Double = 0.0,
    val svcscan_shared_process_services: Double = 0.0,
    val totalReceivedBytes: Double = 0.0,
    val totalReceivedPackets: Double = 0.0,
    val totalTransmittedBytes: Double = 0.0,
    val totalTransmittedPackets: Double = 0.0
){
    override fun toString(): String {
        // Returns a string representation of the StatsData object
        return "StatsData(" +
                "username='$username', "+
                "APICall='$APICall', " +
                "Activity='$Activity', " +
                "Call='$Call', " +
                "Feature='$Feature', " +
                "Intent='$Intent'," +
                "MemoryHeapAlloc=$MemoryHeapAlloc, " +
                "MemoryHeapFree=$MemoryHeapFree, " +
                "MemoryHeapSize=$MemoryHeapSize, " +
                "MemoryParcelMemory=$MemoryParcelMemory, " +
                "MemoryPrivateClean=$MemoryPrivateClean, " +
                "MemoryPrivateDirty=$MemoryPrivateDirty, " +
                "MemoryPssClean=$MemoryPssClean, " +
                "MemorySharedClean=$MemorySharedClean, " +
                "MemorySharedDirty=$MemorySharedDirty, " +
                "Memory_PssTotal=$Memory_PssTotal, " +
                "Permission='$Permission', " +
                "Provider='$Provider', " +
                "RealPermission='$RealPermission', " +
                "ServiceReceiver='$ServiceReceiver', " +
                "URL='$URL', " +
                "callbacks_nanonymous=$callbacks_nanonymous, " +
                "callbacks_ncallbacks=$callbacks_ncallbacks, " +
                "callbacks_ngeneric=$callbacks_ngeneric, " +
                "handles_nfile=$handles_nfile, " +
                "handles_nthread=$handles_nthread, " +
                "ldrmodules_not_in_init=$ldrmodules_not_in_init, " +
                "ldrmodules_not_in_init_avg=$ldrmodules_not_in_init_avg, " +
                "ldrmodules_not_in_load=$ldrmodules_not_in_load, " +
                "ldrmodules_not_in_load_avg=$ldrmodules_not_in_load_avg, " +
                "ldrmodules_not_in_mem=$ldrmodules_not_in_mem, " +
                "ldrmodules_not_in_mem_avg=$ldrmodules_not_in_mem_avg, " +
                "malfind_commitCharge=$malfind_commitCharge, " +
                "malfind_ninjections=$malfind_ninjections, " +
                "malfind_protection=$malfind_protection, " +
                "malfind_uniqueInjections=$malfind_uniqueInjections, " +
                "pslist_avg_threads=$pslist_avg_threads, " +
                "pslist_nproc=$pslist_nproc, " +
                "pslist_nprocs64bit=$pslist_nprocs64bit, " +
                "svcscan_fs_drivers=$svcscan_fs_drivers, " +
                "svcscan_interactive_process_services=$svcscan_interactive_process_services, " +
                "svcscan_kernel_drivers=$svcscan_kernel_drivers, " +
                "svcscan_nactive=$svcscan_nactive, " +
                "svcscan_nservices=$svcscan_nservices, " +
                "svcscan_process_services=$svcscan_process_services, " +
                "svcscan_shared_process_services=$svcscan_shared_process_services, " +
                "totalReceivedBytes=$totalReceivedBytes, " +
                "totalReceivedPackets=$totalReceivedPackets, " +
                "totalTransmittedBytes=$totalTransmittedBytes, " +
                "totalTransmittedPackets=$totalTransmittedPackets" +
                ")"
    }
}
