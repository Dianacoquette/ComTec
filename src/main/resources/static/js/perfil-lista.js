// ╔══════════════════════════════════════════════════╗
// ║       PERFIL LISTA - Fetch API + DOM             ║
// ╚══════════════════════════════════════════════════╝

// ── Referencias DOM ──────────────────────────────────
const tablaPerfil          = document.getElementById('tablaPerfil');
const paginacion           = document.getElementById('paginacion');
const infoRegistros        = document.getElementById('infoRegistros');
const inputBuscar          = document.getElementById('inputBuscar');
const btnBuscar            = document.getElementById('btnBuscar');
const btnLimpiar           = document.getElementById('btnLimpiar');
const btnConfirmarEliminar = document.getElementById('btnConfirmarEliminar');
const modalDetalle         = new bootstrap.Modal(document.getElementById('modalDetalle'));
const modalEliminar        = new bootstrap.Modal(document.getElementById('modalEliminar'));
const toastEl              = new bootstrap.Toast(document.getElementById('toast'));

// ── Estado ────────────────────────────────────────────
let paginaActual    = 0;
let terminoBusqueda = '';
let idEliminar      = null;

// ══════════════════════════════════════════════════════
// FETCH API - Cargar tabla
// ══════════════════════════════════════════════════════
async function cargarPerfiles(page = 0, buscar = '') {
    paginaActual    = page;
    terminoBusqueda = buscar;

    // Spinner (DOM)
    tablaPerfil.innerHTML = `
        <tr>
            <td colspan="4" class="text-center py-4">
                <div class="spinner-border text-primary"></div>
            </td>
        </tr>`;

    try {
        const url      = `/seguridad/perfil/api?page=${page}&buscar=${encodeURIComponent(buscar)}`;
        const response = await fetch(url);
        const result   = await response.json();

        if (!result.success) { mostrarToast('Error al cargar', 'danger'); return; }

        const { content, totalPages, totalElements, currentPage } = result.data;

        // ── Filas (DOM) ─────────────────────────────
        tablaPerfil.innerHTML = content.length === 0
            ? `<tr><td colspan="4" class="text-center py-4 text-muted">
                   <i class="fas fa-inbox fa-2x d-block mb-2"></i>Sin registros
               </td></tr>`
            : content.map((p, i) => `
                <tr>
                    <td>${currentPage * 5 + i + 1}</td>
                    <td>${escapeHtml(p.strNombrePerfil)}</td>
                    <td>
                        <span class="badge ${p.bitAdministrador ? 'bg-success' : 'bg-secondary'}">
                            ${p.bitAdministrador ? 'Sí' : 'No'}
                        </span>
                    </td>
                    <td class="text-center">
                        <button class="btn btn-info btn-sm btn-action"
                                onclick="verDetalle(${p.id})" title="Detalle">
                            <i class="fas fa-eye"></i>
                        </button>
                        <a href="/seguridad/perfil/editar/${p.id}"
                           class="btn btn-warning btn-sm btn-action" title="Editar">
                            <i class="fas fa-edit"></i>
                        </a>
                        <button class="btn btn-danger btn-sm btn-action"
                                onclick="confirmarEliminar(${p.id},'${escapeHtml(p.strNombrePerfil)}')"
                                title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`).join('');

        // ── Info y paginación (DOM) ─────────────────
        infoRegistros.textContent =
            `Mostrando ${content.length} de ${totalElements} registro(s)`;
        renderPaginacion(currentPage, totalPages);

    } catch {
        tablaPerfil.innerHTML = `
            <tr><td colspan="4" class="text-center text-danger py-4">
                <i class="fas fa-exclamation-triangle me-2"></i>Error de conexión
            </td></tr>`;
    }
}

// ══════════════════════════════════════════════════════
// DOM - Paginación « ‹ 1 2 3 › »
// ══════════════════════════════════════════════════════
function renderPaginacion(currentPage, totalPages) {
    if (totalPages <= 1) { paginacion.innerHTML = ''; return; }

    const esPrimera = currentPage === 0;
    const esUltima  = currentPage === totalPages - 1;

    paginacion.innerHTML = `
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPerfiles(0,'${terminoBusqueda}')"
               title="Primera">«</a>
        </li>
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPerfiles(${currentPage - 1},'${terminoBusqueda}')"
               title="Anterior">‹</a>
        </li>
        ${Array.from({ length: totalPages }, (_, i) => `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#"
                   onclick="cargarPerfiles(${i},'${terminoBusqueda}')">${i + 1}</a>
            </li>`).join('')}
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPerfiles(${currentPage + 1},'${terminoBusqueda}')"
               title="Siguiente">›</a>
        </li>
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPerfiles(${totalPages - 1},'${terminoBusqueda}')"
               title="Última">»</a>
        </li>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Ver detalle
// ══════════════════════════════════════════════════════
async function verDetalle(id) {
    try {
        const response = await fetch(`/seguridad/perfil/api/${id}`);
        const result   = await response.json();
        if (!result.success) return;
        const p = result.data;
        document.getElementById('detalleId').textContent     = p.id;
        document.getElementById('detalleNombre').textContent = p.strNombrePerfil;
        document.getElementById('detalleAdmin').innerHTML    =
            `<span class="badge ${p.bitAdministrador ? 'bg-success' : 'bg-secondary'}">
                ${p.bitAdministrador ? 'Sí' : 'No'}
            </span>`;
        modalDetalle.show();
    } catch { mostrarToast('Error al obtener detalle', 'danger'); }
}

// ══════════════════════════════════════════════════════
// DOM - Confirmar eliminar
// ══════════════════════════════════════════════════════
function confirmarEliminar(id, nombre) {
    idEliminar = id;
    document.getElementById('eliminarNombre').textContent = nombre;
    modalEliminar.show();
}

// ══════════════════════════════════════════════════════
// FETCH API - Eliminar
// ══════════════════════════════════════════════════════
btnConfirmarEliminar.addEventListener('click', async () => {
    if (!idEliminar) return;
    btnConfirmarEliminar.disabled = true;
    try {
        const response = await fetch(`/seguridad/perfil/api/${idEliminar}`, { method: 'DELETE' });
        const result   = await response.json();
        modalEliminar.hide();
        mostrarToast(result.message, result.success ? 'success' : 'danger');
        if (result.success) cargarPerfiles(paginaActual, terminoBusqueda);
    } catch { mostrarToast('Error de conexión', 'danger'); }
    finally  { btnConfirmarEliminar.disabled = false; idEliminar = null; }
});

// ── Búsqueda ──────────────────────────────────────────
btnBuscar.addEventListener('click', () =>
    cargarPerfiles(0, inputBuscar.value.trim()));
btnLimpiar.addEventListener('click', () => {
    inputBuscar.value = '';
    cargarPerfiles(0, '');
});
inputBuscar.addEventListener('keydown', e => {
    if (e.key === 'Enter') cargarPerfiles(0, inputBuscar.value.trim());
});

// ── Logout ────────────────────────────────────────────
document.getElementById('btnLogout').addEventListener('click', async e => {
    e.preventDefault();
    try { await fetch('/auth/logout', { method: 'POST' }); } finally {
        document.cookie = 'jwt_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
        window.location.href = '/login';
    }
});

// ── Utils ─────────────────────────────────────────────
function mostrarToast(mensaje, tipo = 'success') {
    const toast = document.getElementById('toast');
    toast.className = `toast align-items-center text-white border-0 bg-${tipo}`;
    document.getElementById('toastMensaje').textContent = mensaje;
    toastEl.show();
}
function escapeHtml(text) {
    const d = document.createElement('div');
    d.appendChild(document.createTextNode(String(text)));
    return d.innerHTML;
}

// ── Inicio ────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => cargarPerfiles());