
// ║       MÓDULO LISTA - Fetch API + DOM            

// ── Referencias DOM ──────────────────────────────────
const tablaModulo          = document.getElementById('tablaModulo');
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


// FETCH API - Cargar tabla
async function cargarModulos(page = 0, buscar = '') {
    paginaActual    = page;
    terminoBusqueda = buscar;

    tablaModulo.innerHTML = `
        <tr>
            <td colspan="3" class="text-center py-4">
                <div class="spinner-border text-primary"></div>
            </td>
        </tr>`;

    try {
        const url      = `/seguridad/modulo/api?page=${page}&buscar=${encodeURIComponent(buscar)}`;
        const response = await fetch(url);
        const result   = await response.json();

        if (!result.success) { mostrarToast('Error al cargar', 'danger'); return; }

        const { content, totalPages, totalElements, currentPage } = result.data;

        // ── Filas (DOM) ─────────────────────────────
        tablaModulo.innerHTML = content.length === 0
    ? `<tr><td colspan="3" class="text-center py-4 text-muted">
           <i class="fas fa-inbox fa-2x d-block mb-2"></i>Sin registros
       </td></tr>`
    : content.map((m, i) => {

        const btnDetalle = PERM.detalle
            ? `<button class="btn btn-info btn-sm btn-action"
                       onclick="verDetalle(${m.id})" title="Detalle">
                   <i class="fas fa-eye"></i>
               </button>` : '';

        const btnEditar = PERM.editar
            ? `<a href="/seguridad/modulo/editar/${m.id}"
                  class="btn btn-warning btn-sm btn-action" title="Editar">
                   <i class="fas fa-edit"></i>
               </a>` : '';

        const btnEliminar = PERM.eliminar
            ? `<button class="btn btn-danger btn-sm btn-action"
                       onclick="confirmarEliminar(${m.id},'${escapeHtml(m.strNombreModulo)}')"
                       title="Eliminar">
                   <i class="fas fa-trash"></i>
               </button>` : '';

        return `
            <tr>
                <td>${currentPage * 5 + i + 1}</td>
                <td>${escapeHtml(m.strNombreModulo)}</td>
                <td class="text-center">
                    ${btnDetalle}
                    ${btnEditar}
                    ${btnEliminar}
                </td>
            </tr>`;
    }).join('');
        infoRegistros.textContent =
            `Mostrando ${content.length} de ${totalElements} registro(s)`;
        renderPaginacion(currentPage, totalPages);

    } catch {
        tablaModulo.innerHTML = `
            <tr><td colspan="3" class="text-center text-danger py-4">
                <i class="fas fa-exclamation-triangle me-2"></i>Error de conexión
            </td></tr>`;
    }
}


// DOM - Paginación « ‹ 1 2 3 › »

function renderPaginacion(currentPage, totalPages) {
    if (totalPages <= 1) { paginacion.innerHTML = ''; return; }

    const esPrimera = currentPage === 0;
    const esUltima  = currentPage === totalPages - 1;

    paginacion.innerHTML = `
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarModulos(0,'${terminoBusqueda}')" title="Primera">«</a>
        </li>
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarModulos(${currentPage - 1},'${terminoBusqueda}')" title="Anterior">‹</a>
        </li>
        ${Array.from({ length: totalPages }, (_, i) => `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#"
                   onclick="cargarModulos(${i},'${terminoBusqueda}')">${i + 1}</a>
            </li>`).join('')}
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarModulos(${currentPage + 1},'${terminoBusqueda}')" title="Siguiente">›</a>
        </li>
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarModulos(${totalPages - 1},'${terminoBusqueda}')" title="Última">»</a>
        </li>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Ver detalle
// ══════════════════════════════════════════════════════
async function verDetalle(id) {
    try {
        const response = await fetch(`/seguridad/modulo/api/${id}`);
        const result   = await response.json();
        if (!result.success) return;
        const m = result.data;
        document.getElementById('detalleId').textContent     = m.id;
        document.getElementById('detalleNombre').textContent = m.strNombreModulo;
        modalDetalle.show();
    } catch { mostrarToast('Error al obtener detalle', 'danger'); }
}

// ══════════════════════════════════════════════════════
// DOM - Confirmar / FETCH API - Eliminar
// ══════════════════════════════════════════════════════
function confirmarEliminar(id, nombre) {
    idEliminar = id;
    document.getElementById('eliminarNombre').textContent = nombre;
    modalEliminar.show();
}

btnConfirmarEliminar.addEventListener('click', async () => {
    if (!idEliminar) return;
    btnConfirmarEliminar.disabled = true;
    try {
        const response = await fetch(`/seguridad/modulo/api/${idEliminar}`, { method: 'DELETE' });
        const result   = await response.json();
        modalEliminar.hide();
        mostrarToast(result.message, result.success ? 'success' : 'danger');
        if (result.success) cargarModulos(paginaActual, terminoBusqueda);
    } catch { mostrarToast('Error de conexión', 'danger'); }
    finally  { btnConfirmarEliminar.disabled = false; idEliminar = null; }
});

// ── Búsqueda ──────────────────────────────────────────
btnBuscar.addEventListener('click', () =>
    cargarModulos(0, inputBuscar.value.trim()));
btnLimpiar.addEventListener('click', () => {
    inputBuscar.value = '';
    cargarModulos(0, '');
});
inputBuscar.addEventListener('keydown', e => {
    if (e.key === 'Enter') cargarModulos(0, inputBuscar.value.trim());
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
document.addEventListener('DOMContentLoaded', () => cargarModulos());