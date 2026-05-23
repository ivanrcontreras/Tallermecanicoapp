package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.Client
import com.example.data.ComponentCheck
import com.example.data.Maintenance
import com.example.data.VehicleCheck
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateVehicleCheckPdf(
        context: Context,
        client: Client,
        check: VehicleCheck,
        results: List<ComponentCheck>,
        maintenance: Maintenance?
    ): File? {
        val pdfDocument = PdfDocument()
        
        // Page 1: Client Card, Vehicle Info & Maintenance Forecast
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 dimensions
        val page1 = pdfDocument.startPage(pageInfo1)
        var canvas = page1.canvas
        var paint = Paint()

        // Background / Theme colors
        val primaryColor = Color.rgb(33, 150, 243) // Modern Light Blue
        val accentColor = Color.rgb(25, 118, 210)
        val darkGray = Color.rgb(33, 33, 33)
        val lightGray = Color.rgb(245, 245, 245)
        val borderGray = Color.rgb(224, 224, 224)

        // Draw header accent bar
        paint.color = primaryColor
        canvas.drawRect(RectF(30f, 30f, 565f, 45f), paint)

        // Header Title
        paint.color = darkGray
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("TALLER MECÁNICO", 35f, 75f, paint)
        
        paint.textSize = 14f
        paint.color = Color.GRAY
        paint.isFakeBoldText = false
        canvas.drawText("REPORTE DE DIAGNÓSTICO Y CONTROL VEHICULAR", 35f, 95f, paint)

        // Date & Document Number
        paint.color = darkGray
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Fecha de Inspección: ${check.datePerformed}", 400f, 75f, paint)
        canvas.drawText("Patente: ${client.plate}", 400f, 95f, paint)

        // Divider
        paint.color = borderGray
        canvas.drawLine(30f, 115f, 565f, 115f, paint)

        // CLIENT & VEHICLE INFORMATION BOX
        paint.color = primaryColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("1. DATOS DEL CLIENTE Y VEHÍCULO", 35f, 140f, paint)

        // Card background
        paint.color = lightGray
        canvas.drawRoundRect(RectF(30f, 150f, 565f, 250f), 8f, 8f, paint)

        // Card borders
        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(30f, 150f, 565f, 250f), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = darkGray
        paint.textSize = 10f
        paint.isFakeBoldText = false

        // Left Column (Client Info)
        canvas.drawText("Nombre de Cliente: ${client.name}", 45f, 175f, paint)
        canvas.drawText("Teléfono / WhatsApp: ${client.phone}", 45f, 195f, paint)
        canvas.drawText("Correo Electrónico: ${client.email}", 45f, 215f, paint)

        // Right Column (Vehicle Info)
        canvas.drawText("Modelo de Vehículo: ${client.vehicle}", 310f, 175f, paint)
        canvas.drawText("Nro. de Patente / Chapa: ${client.plate}", 310f, 195f, paint)
        canvas.drawText("Estado del Reporte: Completado", 310f, 215f, paint)

        // MAINTENANCE FORECAST & MILEAGE RECORD
        paint.color = primaryColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("2. MANTENIMIENTOS Y NOTIFICACIÓN DE SERVICIO", 35f, 280f, paint)

        if (maintenance != null) {
            // Maintenance details box
            paint.color = lightGray
            canvas.drawRoundRect(RectF(30f, 290f, 565f, 410f), 8f, 8f, paint)
            paint.color = borderGray
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(30f, 290f, 565f, 410f), 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.color = darkGray
            paint.textSize = 10f

            canvas.drawText("Kilometraje al Ingresar: ${maintenance.currentKm} KM", 45f, 315f, paint)
            canvas.drawText("Kilometraje Próximo Servicio: ${maintenance.nextServiceKm} KM", 45f, 335f, paint)
            canvas.drawText("Fecha del Servicio Guardado: ${maintenance.datePerformed}", 45f, 355f, paint)
            
            // Draw visual kilometers countdown
            val remaining = maintenance.nextServiceKm - maintenance.currentKm
            val forecastText = if (remaining > 0) {
                "Siguiente control en $remaining KM o en 6 meses."
            } else {
                "¡Atención! Servicio vencido hace ${Math.abs(remaining)} KM."
            }
            paint.color = accentColor
            paint.isFakeBoldText = true
            canvas.drawText("Próximo Control Sugerido: $forecastText", 45f, 380f, paint)

            // Right side of maintenance card (Observation)
            paint.color = darkGray
            paint.isFakeBoldText = false
            canvas.drawText("Observaciones del Servicio:", 310f, 315f, paint)
            
            // Handle multiline observations for maintenance
            val words = maintenance.observation.split(" ")
            var line = ""
            var textY = 335f
            for (word in words) {
                if (paint.measureText("$line $word") < 230f) {
                    line = "$line $word"
                } else {
                    canvas.drawText(line, 310f, textY, paint)
                    line = word
                    textY += 15f
                    if (textY > 395f) break
                }
            }
            if (line.isNotEmpty() && textY <= 395f) {
                canvas.drawText(line, 310f, textY, paint)
            }
        } else {
            // No custom service loaded yet context
            paint.color = lightGray
            canvas.drawRoundRect(RectF(30f, 290f, 565f, 350f), 8f, 8f, paint)
            paint.color = borderGray
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(30f, 290f, 565f, 350f), 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.GRAY
            canvas.drawText("No se registró un mantenimiento de kilometraje específico en este chequeo.", 45f, 325f, paint)
        }

        // GLOBAL RECOMMENDATIONS / OBSERVATIONS AT THE BOTTOM
        paint.color = primaryColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("3. OBSERVACIONES GENERALES DEL MECÁNICO", 35f, 440f, paint)

        paint.color = lightGray
        canvas.drawRoundRect(RectF(30f, 450f, 565f, 580f), 8f, 8f, paint)
        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(RectF(30f, 450f, 565f, 580f), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = darkGray
        paint.textSize = 10f
        paint.isFakeBoldText = false

        val obsWords = check.mechanicObservation.ifEmpty { "Sin observaciones adicionales generales." }.split(" ")
        var obsLine = ""
        var obsY = 475f
        for (word in obsWords) {
            if (paint.measureText("$obsLine $word") < 500f) {
                obsLine = "$obsLine $word"
            } else {
                canvas.drawText(obsLine, 45f, obsY, paint)
                obsLine = word
                obsY += 15f
                if (obsY > 565f) break
            }
        }
        if (obsLine.isNotEmpty() && obsY <= 565f) {
            canvas.drawText(obsLine, 45f, obsY, paint)
        }

        // Footer signatures
        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        canvas.drawLine(100f, 750f, 230f, 750f, paint)
        canvas.drawLine(365f, 750f, 495f, 750f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("Firma del Mecánico Responsable", 108f, 765f, paint)
        canvas.drawText("Conformidad del Cliente", 390f, 765f, paint)

        canvas.drawText("Taller Mecánico Oficial - Reporte Autogenerado", 35f, 810f, paint)
        canvas.drawText("Página 1 de 2", 490f, 810f, paint)

        pdfDocument.finishPage(page1)


        // Page 2: Detailed Component Checklist Results
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        canvas = page2.canvas
        paint = Paint()

        paint.color = primaryColor
        canvas.drawRect(RectF(30f, 30f, 565f, 45f), paint)

        paint.color = darkGray
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("TABLA DETALLADA DE COMPONENTES", 35f, 75f, paint)

        paint.textSize = 10f
        paint.color = Color.GRAY
        paint.isFakeBoldText = false
        canvas.drawText("Revisión de Seguridad e Inspección Técnica Estructural", 35f, 92f, paint)

        // Draw Table Header
        val tableYStart = 110f
        paint.color = Color.rgb(230, 240, 250)
        paint.style = Paint.Style.FILL
        canvas.drawRect(RectF(30f, tableYStart, 565f, tableYStart + 22f), paint)

        paint.color = darkGray
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(RectF(30f, tableYStart, 565f, tableYStart + 22f), paint)

        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true
        paint.textSize = 9f
        canvas.drawText("COMPONENTE", 38f, tableYStart + 15f, paint)
        canvas.drawText("TIPO DE INSPECCIÓN", 220f, tableYStart + 15f, paint)
        canvas.drawText("ESTADO / NIVEL", 350f, tableYStart + 15f, paint)
        canvas.drawText("OBSERVACIONES MECÁNICAS", 435f, tableYStart + 15f, paint)

        // Populate items
        paint.isFakeBoldText = false
        var currentY = tableYStart + 22f
        val rowHeight = 22f
        val maxAvailableY = 770f

        for (result in results) {
            if (currentY + rowHeight > maxAvailableY) {
                // If it exceeds page 2, we stop drawing to avoid layout spill
                paint.color = Color.RED
                canvas.drawText("[+ Algunos componentes omitidos, verifique en la app]", 35f, maxAvailableY + 15f, paint)
                break
            }

            // Draw alternate white/gray row backgrounds
            paint.style = Paint.Style.FILL
            paint.color = if (results.indexOf(result) % 2 == 0) Color.WHITE else Color.rgb(248, 248, 248)
            canvas.drawRect(RectF(30f, currentY, 565f, currentY + rowHeight), paint)

            // Draw line border
            paint.style = Paint.Style.STROKE
            paint.color = borderGray
            paint.strokeWidth = 0.5f
            canvas.drawLine(30f, currentY + rowHeight, 565f, currentY + rowHeight, paint)
            canvas.drawLine(30f, currentY, 30f, currentY + rowHeight, paint)
            canvas.drawLine(565f, currentY, 565f, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = darkGray
            paint.textSize = 8.5f

            // Component name
            var compName = result.componentName
            if (paint.measureText(compName) > 170f) {
                compName = compName.take(30) + "..."
            }
            canvas.drawText(compName, 35f, currentY + 14f, paint)

            // Component Type
            canvas.drawText(
                if (result.isLevelCheck) "Revisión Nivel" else "Estado General",
                222f,
                currentY + 14f,
                paint
            )

            // Status Badge Column
            val statusText = result.status
            if (result.isLevelCheck) {
                paint.textSize = 8.5f
                paint.isFakeBoldText = true
                if (statusText.uppercase().contains("A NIVEL") || statusText.lowercase().contains("si") || statusText.lowercase().contains("bueno")) {
                    paint.color = Color.rgb(46, 125, 50) // Dark Green
                    canvas.drawText("✔ A NIVEL", 350f, currentY + 14f, paint)
                } else {
                    paint.color = Color.rgb(198, 40, 40) // Dark Red
                    canvas.drawText("✘ BAJO NIVEL", 350f, currentY + 14f, paint)
                }
            } else {
                paint.textSize = 8.5f
                paint.isFakeBoldText = true
                when (statusText.uppercase()) {
                    "BUENO" -> {
                        paint.color = Color.rgb(46, 125, 50) // Green
                        canvas.drawText("● BUENO", 350f, currentY + 14f, paint)
                    }
                    "REGULAR" -> {
                        paint.color = Color.rgb(249, 168, 37) // Yellow (Darker)
                        canvas.drawText("● REGULAR", 350f, currentY + 14f, paint)
                    }
                    else -> {
                        paint.color = Color.rgb(198, 40, 40) // Red
                        canvas.drawText("● MALO", 350f, currentY + 14f, paint)
                    }
                }
            }

            paint.color = darkGray
            paint.isFakeBoldText = false
            paint.textSize = 8f

            // Obs with truncation
            var obsText = result.observation.ifEmpty { "Sin observaciones" }
            if (paint.measureText(obsText) > 120f) {
                obsText = obsText.take(22) + "..."
            }
            canvas.drawText(obsText, 435f, currentY + 14f, paint)

            currentY += rowHeight
        }

        // Clean closure for page outline
        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        canvas.drawLine(30f, tableYStart, 30f, currentY, paint)
        canvas.drawLine(565f, tableYStart, 565f, currentY, paint)

        // Footer Page 2
        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        paint.textSize = 8f
        canvas.drawText("Taller Mecánico - Inspección Completa de Componentes", 35f, 810f, paint)
        canvas.drawText("Página 2 de 2", 490f, 810f, paint)

        pdfDocument.finishPage(page2)

        // Save PDF to cache
        val fileName = "Diagnostico_Taller_${client.plate.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File, clientName: String, plate: String) {
        try {
            val authority = "com.example.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Técnico Vehicular - $plate")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hola $clientName, este es el diagnóstico y reporte técnico en PDF para su vehículo con patente $plate. Generado por Taller Mecánico."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Enviar Reporte con:")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
