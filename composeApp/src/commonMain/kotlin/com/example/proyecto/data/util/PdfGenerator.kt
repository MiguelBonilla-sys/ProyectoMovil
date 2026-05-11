package com.example.proyecto.data.util

import com.example.proyecto.data.model.TipoPlantilla

expect object PdfGenerator {
    fun generatePdf(tipo: TipoPlantilla, datos: Map<String, String>, nombreDocumento: String): ByteArray
}