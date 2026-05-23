package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val vehicle: String, // Vehicle model/make, e.g. "Toyota Hilux"
    val plate: String   // License plate (Patente), e.g. "AE123XX" or "AAA111"
) : Serializable

@Entity(tableName = "maintenances")
data class Maintenance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientPlate: String, // foreign reference to client's car plate
    val currentKm: Int,
    val nextServiceKm: Int,
    val datePerformed: String, // Date of service
    val observation: String
) : Serializable

@Entity(tableName = "components")
data class Component(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isLevelCheck: Boolean // True for "revisión de nivel", False for standard state (Bueno/Regular/Malo)
) : Serializable

@Entity(tableName = "vehicle_checks")
data class VehicleCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientPlate: String,
    val datePerformed: String,
    val mechanicObservation: String
) : Serializable

@Entity(tableName = "component_checks")
data class ComponentCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleCheckId: Int, // Refers to the VehicleCheck table
    val componentName: String,
    val isLevelCheck: Boolean,
    val status: String, // "Bueno" (Green), "Regular" (Yellow), "Malo" (Red), or "A Nivel" / "No A Nivel"
    val observation: String
) : Serializable
