package com.example.sg360.models

data class App(
    val name: String,
    val packageName: String,
    val source: String
)

data class ClusterRequest(
    val clusterName: String,
    val apps: List<App>
)

data class ClusterResponse(
    val Cluster: String,
    val Round: Int,
    val Epoch: Int,
    val modelData: String
)
