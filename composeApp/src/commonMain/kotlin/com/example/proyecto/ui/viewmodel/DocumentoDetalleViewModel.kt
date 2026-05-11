package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoProcesoFirma
import com.example.proyecto.data.model.ProcesoFirma
import com.example.proyecto.data.repository.DocumentoRepository
import com.example.proyecto.data.repository.FirmaElectronicaRepository
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado UI para la pantalla de detalle de documento y firma.
 */
data class DocumentoDetalleUiState(
    val documento: Documento? = null,
    val procesoFirma: ProcesoFirma? = null,
    
    // Estados de carga
    val isLoadingDocumento: Boolean = false,
    val isInitializingFirma: Boolean = false,
    val isVerifyingFirma: Boolean = false,
    
    // Errores
    val error: String? = null,
    val errorFirma: String? = null,
    
    // Estados de éxito
    val firmaIniciadaExitosamente: Boolean = false,
    val urlCAMERFIRMA: String? = null, // URL para redirigir al usuario
    
    // Información del proceso
    val porcentajeFirma: Int = 0, // 0-100% de firmas completadas
    val firmantesRestantes: List<String> = emptyList(), // IDs de quienes aún no firmaron
    
    // Permisos del usuario actual
    val usuarioActualPuedeVerFirma: Boolean = false,
    val usuarioActualPuedeIniciarFirma: Boolean = false,
    val usuarioActualDebeSerFirmante: Boolean = false
)

class DocumentoDetalleViewModel(
    private val documentoRepository: DocumentoRepository = DocumentoRepository(),
    private val firmaRepository: FirmaElectronicaRepository = FirmaElectronicaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentoDetalleUiState())
    val uiState: StateFlow<DocumentoDetalleUiState> = _uiState.asStateFlow()

    /**
     * Carga un documento específico.
     */
    fun cargarDocumento(documentoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingDocumento = true,
                error = null
            )
            try {
                val doc = documentoRepository.obtenerPorId(documentoId)
                if (doc == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingDocumento = false,
                        error = "Documento no encontrado"
                    )
                    return@launch
                }

                // Cargar proceso de firma si existe
                var procesoFirma: ProcesoFirma? = null
                if (doc.procesoFirmaId != null) {
                    procesoFirma = firmaRepository.verificarEstadoFirma(doc.procesoFirmaId!!)
                }

                // Determinar permisos del usuario actual
                val currentUser = SessionManager.currentUser
                val usuarioPuedeVerFirma = currentUser != null && (
                    doc.subidoPor == currentUser.id || // El que subió el doc
                    (procesoFirma?.firmantesRequeridos?.contains(currentUser.id) == true) // Es firmante
                )

                val usuarioPuedeIniciar = currentUser != null &&
                    doc.subidoPor == currentUser.id && // Solo quien subió puede iniciar
                    doc.estadoFirma.name == "PENDIENTE" && // Solo documentos sin procesar
                    procesoFirma == null // Sin proceso iniciado

                val usuarioDebeSerFirmante = currentUser != null && currentUser.id != null &&
                    doc.subidoPor != currentUser.id && // No es quien subió
                    procesoFirma?.firmantesRequeridos?.contains(currentUser.id) == true &&
                    !procesoFirma.yaFirmo(currentUser.id) // Aún no ha firmado

                val firmantesRestantes = procesoFirma?.let { proc ->
                    proc.firmantesRequeridos.filter { !proc.yaFirmo(it) }
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoadingDocumento = false,
                    documento = doc,
                    procesoFirma = procesoFirma,
                    porcentajeFirma = procesoFirma?.porcentajeCompletitud ?: 0,
                    firmantesRestantes = firmantesRestantes,
                    usuarioActualPuedeVerFirma = usuarioPuedeVerFirma,
                    usuarioActualPuedeIniciarFirma = usuarioPuedeIniciar,
                    usuarioActualDebeSerFirmante = usuarioDebeSerFirmante
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDocumento = false,
                    error = e.message ?: "Error al cargar documento"
                )
            }
        }
    }

    /**
     * Inicia el proceso de firma electrónica en CAMERFIRMA.
     * Solo el usuario que subió el documento puede iniciar la firma.
     */
    fun iniciarFirmaElectronica(firmantesIds: List<String>) {
        val currentUser = SessionManager.currentUser ?: return
        val doc = _uiState.value.documento ?: return

        if (!_uiState.value.usuarioActualPuedeIniciarFirma) {
            _uiState.value = _uiState.value.copy(
                errorFirma = "No tienes permisos para iniciar la firma de este documento"
            )
            return
        }

        if (firmantesIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorFirma = "Debes seleccionar al menos un firmante"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isInitializingFirma = true,
                errorFirma = null
            )
            try {
                // Iniciar proceso en CAMERFIRMA
                val proceso = firmaRepository.iniciarProcesoFirma(
                    documentoId = doc.id!!,
                    firmantesIds = firmantesIds,
                    usuarioActualId = currentUser.id!!
                )

                // Actualizar documento con referencia al proceso
                documentoRepository.actualizarProcesoFirma(
                    documentoId = doc.id!!,
                    procesoFirmaId = proceso.id
                )

                _uiState.value = _uiState.value.copy(
                    isInitializingFirma = false,
                    procesoFirma = proceso,
                    urlCAMERFIRMA = proceso.camerfirmaUrl,
                    firmaIniciadaExitosamente = true,
                    porcentajeFirma = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInitializingFirma = false,
                    errorFirma = e.message ?: "Error al iniciar proceso de firma"
                )
            }
        }
    }

    /**
     * Verifica el estado del proceso de firma.
     * Se llama después del callback de CAMERFIRMA.
     */
    fun verificarEstadoFirma(procesoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isVerifyingFirma = true,
                error = null
            )
            try {
                val proceso = firmaRepository.verificarEstadoFirma(procesoId)
                if (proceso == null) {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingFirma = false,
                        error = "Proceso no encontrado"
                    )
                    return@launch
                }

                val firmantesRestantes = proceso.firmantesRequeridos.filter { !proceso.yaFirmo(it) }

                _uiState.value = _uiState.value.copy(
                    isVerifyingFirma = false,
                    procesoFirma = proceso,
                    porcentajeFirma = proceso.porcentajeCompletitud,
                    firmantesRestantes = firmantesRestantes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingFirma = false,
                    error = e.message ?: "Error al verificar estado de firma"
                )
            }
        }
    }

    /**
     * Rechaza la firma (el usuario no quiso firmar).
     */
    fun rechazarFirma(procesoId: String, motivo: String = "Rechazado por el usuario") {
        viewModelScope.launch {
            try {
                val currentUser = SessionManager.currentUser ?: return@launch
                firmaRepository.rechazarFirma(
                    procesoId = procesoId,
                    firmanteId = currentUser.id!!,
                    motivo = motivo,
                    ipOrigen = "0.0.0.0" // TODO: Obtener IP real
                )

                _uiState.value = _uiState.value.copy(
                    error = "Firma rechazada correctamente"
                )
                verificarEstadoFirma(procesoId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorFirma = e.message ?: "Error al rechazar firma"
                )
            }
        }
    }

    /**
     * Cancela el proceso de firma (solo el creador).
     */
    fun cancelarProcesoFirma(procesoId: String) {
        viewModelScope.launch {
            try {
                firmaRepository.cancelarProcesoFirma(
                    procesoId = procesoId,
                    razon = "Cancelado por el usuario"
                )

                _uiState.value = _uiState.value.copy(
                    procesoFirma = null,
                    porcentajeFirma = 0,
                    error = "Proceso de firma cancelado"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorFirma = e.message ?: "Error al cancelar proceso"
                )
            }
        }
    }

    /**
     * Descargar el documento firmado (disponible cuando proceso está completo).
     */
    fun descargarDocumentoFirmado(procesoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingFirma = true)
            try {
                val pdfBytes = firmaRepository.descargarDocumentoFirmado(procesoId)
                if (pdfBytes != null) {
                    // TODO: Guardar/compartir el PDF
                    _uiState.value = _uiState.value.copy(
                        isVerifyingFirma = false,
                        error = "Documento descargado"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingFirma = false,
                        error = "No se pudo descargar el documento"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingFirma = false,
                    error = e.message ?: "Error al descargar"
                )
            }
        }
    }

    /**
     * Limpia mensajes de error/éxito.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            errorFirma = null,
            firmaIniciadaExitosamente = false
        )
    }
}
