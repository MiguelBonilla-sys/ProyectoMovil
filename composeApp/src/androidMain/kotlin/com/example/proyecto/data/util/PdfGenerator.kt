package com.example.proyecto.data.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.proyecto.data.model.TipoPlantilla
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 60f
    private const val LINE_HEIGHT = 20f
    private const val TITLE_SIZE = 18f
    private const val SUBTITLE_SIZE = 14f
    private const val BODY_SIZE = 11f
    private const val FOOTER_SIZE = 9f

    actual fun generatePdf(tipo: TipoPlantilla, datos: Map<String, String>, nombreDocumento: String): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = TITLE_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = SUBTITLE_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = BODY_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = BODY_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = FOOTER_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "CO"))
        val fechaActual = dateFormat.format(Date())

        when (tipo) {
            TipoPlantilla.PODER_NOTARIAL -> generarPoderNotarial(canvas, titlePaint, subtitlePaint, bodyPaint, labelPaint, linePaint, footerPaint, MARGIN, datos, fechaActual)
            TipoPlantilla.CONTRATO_ARRENDAMIENTO -> generarContratoArrendamiento(canvas, titlePaint, subtitlePaint, bodyPaint, labelPaint, linePaint, footerPaint, MARGIN, datos, fechaActual)
            TipoPlantilla.CARTA_AUTORIZACION -> generarCartaAutorizacion(canvas, titlePaint, subtitlePaint, bodyPaint, labelPaint, linePaint, footerPaint, MARGIN, datos, fechaActual)
            TipoPlantilla.NDA -> generarNDA(canvas, titlePaint, subtitlePaint, bodyPaint, labelPaint, linePaint, footerPaint, MARGIN, datos, fechaActual)
            TipoPlantilla.DEMANDA_CIVIL -> generarDemandaCivil(canvas, titlePaint, subtitlePaint, bodyPaint, labelPaint, linePaint, footerPaint, MARGIN, datos, fechaActual)
        }

        document.finishPage(page)

        val outputStream = java.io.ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()
        return outputStream.toByteArray()
    }

    private fun generarPoderNotarial(
        canvas: Canvas, titlePaint: Paint, subtitlePaint: Paint, bodyPaint: Paint,
        labelPaint: Paint, linePaint: Paint, footerPaint: Paint, margin: Float,
        datos: Map<String, String>, fechaActual: String
    ) {
        var y = margin
        val centerX = PAGE_WIDTH / 2f
        val maxWidth = PAGE_WIDTH - 2 * margin

        canvas.drawText("PODER NOTARIAL GENERAL", centerX - titlePaint.measureText("PODER NOTARIAL GENERAL") / 2, y, titlePaint)
        y += LINE_HEIGHT * 3

        val ciudad = datos["ciudad"] ?: ""
        val fecha = datos["fecha"] ?: fechaActual

        canvas.drawText("Yo, ${datos["nombre_poderdante"] ?: ""}, mayor de edad, identificado(a) con ${datos["documento_poderdante"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("domiciliado(a) en ${datos["direccion_poderdante"] ?: ""}, teléfono ${datos["telefono_poderdante"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("correo electrónico ${datos["email_poderdante"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f
        canvas.drawText("POR MEDIO DEL PRESENTE documento otorgo PODER ILIMITADO a:", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawText("${datos["nombre_apoderado"] ?: ""}, mayor de edad, identificado(a) con ${datos["documento_apoderado"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("portador(a) de la Tarjeta Profesional N° ${datos["tarjeta_profesional"] ?: ""}, para que:", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        val facultades = (datos["facultades"] ?: "").split("\n")
        facultades.forEach { line ->
            val words = line.split(" ")
            var lineText = ""
            words.forEach { word ->
                val testLine = if (lineText.isEmpty()) word else "$lineText $word"
                if (bodyPaint.measureText(testLine) <= maxWidth) {
                    lineText = testLine
                } else {
                    canvas.drawText("• $lineText", margin + 20, y, bodyPaint)
                    y += LINE_HEIGHT
                    lineText = word
                }
            }
            if (lineText.isNotEmpty()) {
                canvas.drawText("• $lineText", margin + 20, y, bodyPaint)
                y += LINE_HEIGHT
            }
        }

        y += LINE_HEIGHT * 1.5f
        canvas.drawText("El presente poder se otorga en la ciudad de $ciudad, a los $fecha.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 3

        canvas.drawLine(margin, y, margin + 150, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma del Poderdante", margin, y, footerPaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawLine(margin + 200, margin, margin + 200, y, linePaint)

        canvas.drawLine(margin + 350, y - LINE_HEIGHT * 2, margin + 500, y - LINE_HEIGHT * 2, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Huella", margin + 350, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("NOMBRE: ${datos["nombre_poderdante"] ?: ""}", margin + 350, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("C.C.: ${datos["documento_poderdante"] ?: ""}", margin + 350, y, footerPaint)

        y += LINE_HEIGHT * 2
        canvas.drawLine(margin, y, margin + 150, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma del Apoderado", margin, y, footerPaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawLine(margin + 200, y - LINE_HEIGHT * 2, margin + 350, y - LINE_HEIGHT * 2, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Huella", margin + 200, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("NOMBRE: ${datos["nombre_apoderado"] ?: ""}", margin + 200, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("C.C.: ${datos["documento_apoderado"] ?: ""}", margin + 200, y, footerPaint)

        y = PAGE_HEIGHT - 60f
        canvas.drawText("Documento generado por LexSign - $fechaActual", centerX - footerPaint.measureText("Documento generado por LexSign - $fechaActual") / 2, y, footerPaint)
    }

    private fun generarContratoArrendamiento(
        canvas: Canvas, titlePaint: Paint, subtitlePaint: Paint, bodyPaint: Paint,
        labelPaint: Paint, linePaint: Paint, footerPaint: Paint, margin: Float,
        datos: Map<String, String>, fechaActual: String
    ) {
        var y = margin
        val centerX = PAGE_WIDTH / 2f

        canvas.drawText("CONTRATO DE ARRENDAMIENTO", centerX - titlePaint.measureText("CONTRATO DE ARRENDAMIENTO") / 2, y, titlePaint)
        y += LINE_HEIGHT
        canvas.drawText("DE INMUEBLE", centerX - subtitlePaint.measureText("DE INMUEBLE") / 2, y, subtitlePaint)
        y += LINE_HEIGHT * 2

        val ciudad = datos["direccion_inmueble"] ?: ""
        val fecha = datos["fecha_inicio"] ?: fechaActual

        canvas.drawText("Entre los abajo firmantes, por una parte el(la) ARRENDADOR(A):", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawText("Nombre: ${datos["nombre_arrendador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC/NIT: ${datos["documento_arrendador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_arrendador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Teléfono: ${datos["telefono_arrendador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("Y por otra parte el(la) ARRENDATARIO(A):", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawText("Nombre: ${datos["nombre_arrendatario"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC/NIT: ${datos["documento_arrendatario"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_arrendatario"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Teléfono: ${datos["telefono_arrendatario"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Email: ${datos["email_arrendatario"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("CLAUSULAS", centerX - subtitlePaint.measureText("CLAUSULAS") / 2, y, subtitlePaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawText("PRIMERA - OBJETO: El ARRENDADOR da en arriendo al ARRENDATARIO el inmueble", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("ubicado en: ${datos["direccion_inmueble"] ?: ""}, destino: ${datos["destino"] ?: "Residencial"}.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("SEGUNDA - VALOR: El canon mensual de arriendo se establece en", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("$${datos["valorCanon"] ?: "0"} (pesos colombianos) mensuales.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("TERCERA - PLAZO: El presente contrato tiene una duración de", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["plazo"] ?: "0"} meses, contados a partir del $fecha.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("CUARTA - OBLIGACIONES DEL ARRENDATARIO:", margin, y, bodyPaint)
        y += LINE_HEIGHT
        val obligaciones = listOf(
            "Pagar el canon de arrendamiento los primeros 5 días de cada mes.",
            "Conservar el inmueble en buen estado.",
            "No realizar mejoras sin autorización escrita.",
            "Permitir inspecciones con previo aviso.",
            "No subarrendar sin consentimiento del arrendador."
        )
        obligaciones.forEach { obs ->
            canvas.drawText("• $obs", margin + 20, y, bodyPaint)
            y += LINE_HEIGHT
        }

        y += LINE_HEIGHT * 2
        canvas.drawText("Para constancia se firma el presente contrato en $ciudad, $fecha.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 4

        canvas.drawLine(margin, y, margin + 200, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma del Arrendador", margin, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC: ${datos["documento_arrendador"] ?: ""}", margin, y, footerPaint)

        canvas.drawLine(centerX, y - LINE_HEIGHT, centerX + 200, y - LINE_HEIGHT, linePaint)
        canvas.drawText("Firma del Arrendatario", centerX, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC: ${datos["documento_arrendatario"] ?: ""}", centerX, y, footerPaint)

        y = PAGE_HEIGHT - 60f
        canvas.drawText("Documento generado por LexSign - $fechaActual", centerX - footerPaint.measureText("Documento generado por LexSign - $fechaActual") / 2, y, footerPaint)
    }

    private fun generarCartaAutorizacion(
        canvas: Canvas, titlePaint: Paint, subtitlePaint: Paint, bodyPaint: Paint,
        labelPaint: Paint, linePaint: Paint, footerPaint: Paint, margin: Float,
        datos: Map<String, String>, fechaActual: String
    ) {
        var y = margin
        val centerX = PAGE_WIDTH / 2f
        val maxWidth = PAGE_WIDTH - 2 * margin

        canvas.drawText("CARTA DE AUTORIZACIÓN", centerX - titlePaint.measureText("CARTA DE AUTORIZACIÓN") / 2, y, titlePaint)
        y += LINE_HEIGHT * 2

        val ciudad = datos["ciudad"] ?: ""
        val fecha = datos["fecha"] ?: fechaActual
        val entidad = datos["entidad"] ?: ""

        canvas.drawText("Ciudad de $ciudad, $fecha", PAGE_WIDTH - margin - bodyPaint.measureText("Ciudad de $ciudad, $fecha"), y, bodyPaint)
        y += LINE_HEIGHT * 3

        canvas.drawText("A quien corresponda:", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("Yo, ${datos["nombre_autorizante"] ?: ""}, mayor de edad, identificado(a) con", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["documento_autorizante"] ?: ""}, domiciliado(a) en ${datos["direccion_autorizante"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("AUTORIZO a:", margin, y, subtitlePaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["nombre_autorizado"] ?: ""}, identificado(a) con ${datos["documento_autorizado"] ?: ""},", margin, y, bodyPaint)
        y += LINE_HEIGHT
        if ((datos["parentesco"] ?: "").isNotEmpty()) {
            canvas.drawText("relación: ${datos["parentesco"] ?: ""}", margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        canvas.drawText("Para realizar el siguiente procedimiento/gestión ante $entidad:", margin, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        val procedimiento = datos["procedimiento"] ?: ""
        val palabras = procedimiento.split(" ")
        var lineTexto = ""
        palabras.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }

        y += LINE_HEIGHT * 2
        canvas.drawText("La presente autorización tiene plena validez jurídica y me comprometo a", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("responder por cualquier anomalía que se derive de los actos aquí autorizados.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 3

        canvas.drawLine(margin, y, margin + 150, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma del Autorizante", margin, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC: ${datos["documento_autorizante"] ?: ""}", margin, y, footerPaint)

        canvas.drawLine(margin + 300, y - LINE_HEIGHT * 2, margin + 450, y - LINE_HEIGHT * 2, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma del Autorizado", margin + 300, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("CC: ${datos["documento_autorizado"] ?: ""}", margin + 300, y, footerPaint)

        y = PAGE_HEIGHT - 60f
        canvas.drawText("Documento generado por LexSign - $fechaActual", centerX - footerPaint.measureText("Documento generado por LexSign - $fechaActual") / 2, y, footerPaint)
    }

    private fun generarNDA(
        canvas: Canvas, titlePaint: Paint, subtitlePaint: Paint, bodyPaint: Paint,
        labelPaint: Paint, linePaint: Paint, footerPaint: Paint, margin: Float,
        datos: Map<String, String>, fechaActual: String
    ) {
        var y = margin
        val centerX = PAGE_WIDTH / 2f
        val maxWidth = PAGE_WIDTH - 2 * margin

        canvas.drawText("ACUERDO DE CONFIDENCIALIDAD (NDA)", centerX - titlePaint.measureText("ACUERDO DE CONFIDENCIALIDAD (NDA)") / 2, y, titlePaint)
        y += LINE_HEIGHT * 2

        val ciudad = datos["ciudad"] ?: ""
        val fecha = datos["fecha"] ?: fechaActual

        canvas.drawText("PARTES:", margin, y, subtitlePaint)
        y += LINE_HEIGHT * 1.5f

        canvas.drawText("PARTE REVELADORA:", margin, y, labelPaint)
        y += LINE_HEIGHT
        canvas.drawText("Nombre: ${datos["nombre_revelador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Documento/NIT: ${datos["documento_revelador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_revelador"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("PARTE RECEPTORA:", margin, y, labelPaint)
        y += LINE_HEIGHT
        canvas.drawText("Nombre: ${datos["nombre_receptor"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Documento/NIT: ${datos["documento_receptor"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_receptor"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("CLÁUSULA PRIMERA - INFORMACIÓN CONFIDENCIAL:", margin, y, labelPaint)
        y += LINE_HEIGHT
        val infoConf = datos["informacion_confidencial"] ?: ""
        val palabras = infoConf.split(" ")
        var lineTexto = ""
        palabras.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        canvas.drawText("CLÁUSULA SEGUNDA - OBLIGACIONES:", margin, y, labelPaint)
        y += LINE_HEIGHT
        val obligaciones = datos["obligaciones"] ?: ""
        val palabras2 = obligaciones.split(" ")
        lineTexto = ""
        palabras2.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        val duracion = datos["duracion"] ?: "1"
        canvas.drawText("CLÁUSULA TERCERA - DURACIÓN: El presente acuerdo tendrá una duración de", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("$duracion años contados a partir de la firma del presente documento.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        if ((datos["penalidad"] ?: "").isNotEmpty()) {
            canvas.drawText("CLÁUSULA CUARTA - PENALIDAD:", margin, y, labelPaint)
            y += LINE_HEIGHT
            canvas.drawText(datos["penalidad"] ?: "", margin, y, bodyPaint)
            y += LINE_HEIGHT * 2
        }

        canvas.drawText("Para constancia de lo anterior, firman el presente acuerdo en $ciudad, $fecha.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 3

        canvas.drawLine(margin, y, margin + 200, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Firma Parte Reveladora", margin, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["nombre_revelador"] ?: ""} - ${datos["documento_revelador"] ?: ""}", margin, y, footerPaint)

        canvas.drawLine(centerX, y - LINE_HEIGHT, centerX + 200, y - LINE_HEIGHT, linePaint)
        canvas.drawText("Firma Parte Receptora", centerX, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["nombre_receptor"] ?: ""} - ${datos["documento_receptor"] ?: ""}", centerX, y, footerPaint)

        y = PAGE_HEIGHT - 60f
        canvas.drawText("Documento generado por LexSign - $fechaActual", centerX - footerPaint.measureText("Documento generado por LexSign - $fechaActual") / 2, y, footerPaint)
    }

    private fun generarDemandaCivil(
        canvas: Canvas, titlePaint: Paint, subtitlePaint: Paint, bodyPaint: Paint,
        labelPaint: Paint, linePaint: Paint, footerPaint: Paint, margin: Float,
        datos: Map<String, String>, fechaActual: String
    ) {
        var y = margin
        val centerX = PAGE_WIDTH / 2f
        val maxWidth = PAGE_WIDTH - 2 * margin

        canvas.drawText("DEMANDA", centerX - titlePaint.measureText("DEMANDA") / 2, y, titlePaint)
        y += LINE_HEIGHT
        canvas.drawText("PROCESO ORDINARIO", centerX - subtitlePaint.measureText("PROCESO ORDINARIO") / 2, y, subtitlePaint)
        y += LINE_HEIGHT * 2

        canvas.drawText(datos["ciudad_juzgado"] ?: "", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("SEÑOR(A) JUEZ ${datos["ciudad_juzgado"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("DEMANDANTE:", margin, y, labelPaint)
        y += LINE_HEIGHT
        canvas.drawText("Nombre: ${datos["nombre_demandante"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("C.C.: ${datos["documento_demandante"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_demandante"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Teléfono: ${datos["telefono_demandante"] ?: ""}", margin, y, bodyPaint)
        if ((datos["email_demandante"] ?: "").isNotEmpty()) {
            y += LINE_HEIGHT
            canvas.drawText("Email: ${datos["email_demandante"] ?: ""}", margin, y, bodyPaint)
        }
        y += LINE_HEIGHT * 2

        canvas.drawText("DEMANDADO:", margin, y, labelPaint)
        y += LINE_HEIGHT
        canvas.drawText("Nombre: ${datos["nombre_demandado"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("C.C./NIT: ${datos["documento_demandado"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT
        canvas.drawText("Dirección: ${datos["direccion_demandado"] ?: ""}", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("HECHOS:", margin, y, subtitlePaint)
        y += LINE_HEIGHT
        val hechos = datos["hechos"] ?: ""
        val palabrasH = hechos.split(" ")
        var lineTexto = ""
        palabrasH.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        canvas.drawText("PRETENSIONES:", margin, y, subtitlePaint)
        y += LINE_HEIGHT
        val pretensiones = datos["pretensiones"] ?: ""
        val palabrasP = pretensiones.split(" ")
        lineTexto = ""
        palabrasP.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        canvas.drawText("FUNDAMENTOS DE DERECHO:", margin, y, subtitlePaint)
        y += LINE_HEIGHT
        val fundamentos = datos["fundamentos_derecho"] ?: ""
        val palabrasF = fundamentos.split(" ")
        lineTexto = ""
        palabrasF.forEach { palabra ->
            val prueba = if (lineTexto.isEmpty()) palabra else "$lineTexto $palabra"
            if (bodyPaint.measureText(prueba) <= maxWidth) {
                lineTexto = prueba
            } else {
                canvas.drawText(lineTexto, margin, y, bodyPaint)
                y += LINE_HEIGHT
                lineTexto = palabra
            }
        }
        if (lineTexto.isNotEmpty()) {
            canvas.drawText(lineTexto, margin, y, bodyPaint)
            y += LINE_HEIGHT
        }
        y += LINE_HEIGHT

        canvas.drawText("PRUEBAS:", margin, y, subtitlePaint)
        y += LINE_HEIGHT
        canvas.drawText(datos["pruebas"] ?: "", margin, y, bodyPaint)
        y += LINE_HEIGHT * 2

        canvas.drawText("CUANTÍA: $${datos["valor_cuantia"] ?: "0"} pesos colombianos.", margin, y, bodyPaint)
        y += LINE_HEIGHT * 3

        canvas.drawLine(margin, y, margin + 200, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("${datos["notificador"] ?: ""}", margin, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("T.P. N° ${datos["tarjeta_profesional"] ?: ""}", margin, y, footerPaint)
        y += LINE_HEIGHT
        canvas.drawText("Abogado - LexSign", margin, y, footerPaint)

        y = PAGE_HEIGHT - 60f
        canvas.drawText("Documento generado por LexSign - $fechaActual", centerX - footerPaint.measureText("Documento generado por LexSign - $fechaActual") / 2, y, footerPaint)
    }
}