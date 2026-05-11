package com.example.proyecto.data.util

import com.example.proyecto.data.model.CampoPlantilla
import com.example.proyecto.data.model.TipoPlantilla

object TemplateDefinitions {

    fun getCampos(tipo: TipoPlantilla): List<CampoPlantilla> = when (tipo) {
        TipoPlantilla.PODER_NOTARIAL -> listOf(
            CampoPlantilla("nombre_poderdante", "Nombre del Poderdante", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_poderdante", "Documento de Identidad", "texto", true, placeholder = "C.C. o NIT"),
            CampoPlantilla("direccion_poderdante", "Dirección del Poderdante", "texto", true, placeholder = "Dirección completa"),
            CampoPlantilla("telefono_poderdante", "Teléfono", "texto", true, placeholder = "Teléfono de contacto"),
            CampoPlantilla("email_poderdante", "Correo Electrónico", "texto", true, placeholder = "email@ejemplo.com"),
            CampoPlantilla("nombre_apoderado", "Nombre del Apoderado", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_apoderado", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("tarjeta_profesional", "Tarjeta Profesional", "texto", true, placeholder = "Número de tarjeta"),
            CampoPlantilla("facultades", "Facultades otorgadas", "textarea", true, placeholder = "Describa las facultades..."),
            CampoPlantilla("ciudad", "Ciudad de otorgamiento", "texto", true, placeholder = "Ciudad"),
            CampoPlantilla("fecha", "Fecha de otorgamiento", "fecha", true)
        )

        TipoPlantilla.CONTRATO_ARRENDAMIENTO -> listOf(
            CampoPlantilla("nombre_arrendador", "Nombre del Arrendador", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_arrendador", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("direccion_arrendador", "Dirección", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("telefono_arrendador", "Teléfono", "texto", true, placeholder = "Teléfono"),
            CampoPlantilla("nombre_arrendatario", "Nombre del Arrendatario", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_arrendatario", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("direccion_arrendatario", "Dirección", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("telefono_arrendatario", "Teléfono", "texto", true, placeholder = "Teléfono"),
            CampoPlantilla("email_arrendatario", "Correo Electrónico", "texto", true, placeholder = "email@ejemplo.com"),
            CampoPlantilla("direccion_inmueble", "Dirección del Inmueble", "texto", true, placeholder = "Dirección completa"),
            CampoPlantilla("valorCanon", "Valor del Canon Mensual", "texto", true, placeholder = "En pesos colombianos"),
            CampoPlantilla("fecha_inicio", "Fecha de Inicio", "fecha", true),
            CampoPlantilla("fecha_fin", "Fecha de Terminación", "fecha", true),
            CampoPlantilla("plazo", "Plazo en meses", "numero", true, placeholder = "Ej: 12"),
            CampoPlantilla("destino", "Destino del inmueble", "texto", true, placeholder = "Residencial / Comercial / Industrial")
        )

        TipoPlantilla.CARTA_AUTORIZACION -> listOf(
            CampoPlantilla("nombre_autorizante", "Nombre de quien autoriza", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_autorizante", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("direccion_autorizante", "Dirección", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("nombre_autorizado", "Nombre del autorizado", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_autorizado", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("parentesco", "Parentesco o relación", "texto", false, placeholder = "Familiar / Amigo / Otro"),
            CampoPlantilla("procedimiento", "Procedimiento o gestión", "textarea", true, placeholder = "Describa el procedimiento..."),
            CampoPlantilla("entidad", "Entidad ante la cual se autoriza", "texto", true, placeholder = "Nombre de la entidad"),
            CampoPlantilla("ciudad", "Ciudad", "texto", true, placeholder = "Ciudad"),
            CampoPlantilla("fecha", "Fecha", "fecha", true)
        )

        TipoPlantilla.NDA -> listOf(
            CampoPlantilla("nombre_revelador", "Parte Reveladora", "texto", true, placeholder = "Nombre o empresa"),
            CampoPlantilla("documento_revelador", "Documento/NIT", "texto", true, placeholder = "C.C. o NIT"),
            CampoPlantilla("direccion_revelador", "Dirección", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("nombre_receptor", "Parte Receptora", "texto", true, placeholder = "Nombre o empresa"),
            CampoPlantilla("documento_receptor", "Documento/NIT", "texto", true, placeholder = "C.C. o NIT"),
            CampoPlantilla("direccion_receptor", "Dirección", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("informacion_confidencial", "Definición de información confidencial", "textarea", true, placeholder = "Describa qué se considera información confidencial..."),
            CampoPlantilla("obligaciones", "Obligaciones del receptor", "textarea", true, placeholder = "Describa las obligaciones..."),
            CampoPlantilla("duracion", "Duración del acuerdo (años)", "numero", true, placeholder = "Ej: 2"),
            CampoPlantilla("penalidad", "Cláusula penal", "textarea", false, placeholder = "Indique penalidades por incumplimiento..."),
            CampoPlantilla("ciudad", "Ciudad", "texto", true, placeholder = "Ciudad"),
            CampoPlantilla("fecha", "Fecha", "fecha", true)
        )

        TipoPlantilla.DEMANDA_CIVIL -> listOf(
            CampoPlantilla("ciudad_juzgado", "Ciudad y Juzgado", "texto", true, placeholder = "Ciudad y nombre del juzgado"),
            CampoPlantilla("nombre_demandante", "Nombre del Demandante", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_demandante", "Documento de Identidad", "texto", true, placeholder = "C.C."),
            CampoPlantilla("direccion_demandante", "Dirección de notificación", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("telefono_demandante", "Teléfono", "texto", true, placeholder = "Teléfono"),
            CampoPlantilla("email_demandante", "Correo electrónico", "texto", false, placeholder = "email@ejemplo.com"),
            CampoPlantilla("nombre_demandado", "Nombre del Demandado", "texto", true, placeholder = "Nombre completo"),
            CampoPlantilla("documento_demandado", "Documento de Identidad", "texto", true, placeholder = "C.C. o NIT"),
            CampoPlantilla("direccion_demandado", "Dirección de notificación", "texto", true, placeholder = "Dirección"),
            CampoPlantilla("hechos", "Hechos", "textarea", true, placeholder = "Narración clara y precisa de los hechos..."),
            CampoPlantilla("pretensiones", "Pretensiones", "textarea", true, placeholder = "Qué solicita al juez..."),
            CampoPlantilla("fundamentos_derecho", "Fundamentos de derecho", "textarea", true, placeholder = "Normas aplicables..."),
            CampoPlantilla("pruebas", "Pruebas", "textarea", true, placeholder = "Indique qué pruebas ofrece..."),
            CampoPlantilla("valor_cuantia", "Valor de la cuantía", "texto", true, placeholder = "En pesos colombianos"),
            CampoPlantilla("notificador", "Notificador", "texto", true, placeholder = "Nombre del abogado oappelido"),
            CampoPlantilla("tarjeta_profesional", "Tarjeta Profesional", "texto", true, placeholder = "Número de tarjeta")
        )
    }
}