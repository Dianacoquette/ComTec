// ╔══════════════════════════════════════════════════╗
// ║      USUARIO LISTA - Fetch API + DOM             ║
// ╚══════════════════════════════════════════════════╝

const tablaUsuario         = document.getElementById('tablaUsuario');
const paginacion           = document.getElementById('paginacion');
const infoRegistros        = document.getElementById('infoRegistros');
const inputBuscar          = document.getElementById('inputBuscar');
const btnBuscar            = document.getElementById('btnBuscar');
const btnLimpiar           = document.getElementById('btnLimpiar');
const btnConfirmarEliminar = document.getElementById('btnConfirmarEliminar');
const modalDetalle         = new bootstrap.Modal(document.getElementById('modalDetalle'));
const modalEliminar        = new bootstrap.Modal(document.getElementById('modalEliminar'));
const toastEl              = new bootstrap.Toast(document.getElementById('toast'));

let paginaActual    = 0;
let terminoBusqueda = '';
let idEliminar      = null;

// ══════════════════════════════════════════════════════
// FETCH API - Cargar tabla
// ══════════════════════════════════════════════════════
async function cargarUsuarios(page = 0, buscar = '') {
    paginaActual    = page;
    terminoBusqueda = buscar;

    tablaUsuario.innerHTML = `
        <tr>
            <td colspan="7" class="text-center py-4">
                <div class="spinner-border text-primary"></div>
            </td>
        </tr>`;

    try {
        const url      = `/seguridad/usuario/api?page=${page}&buscar=${encodeURIComponent(buscar)}`;
        const response = await fetch(url);
        const result   = await response.json();

        if (!result.success) { mostrarToast('Error al cargar', 'danger'); return; }

        const { content, totalPages, totalElements, currentPage } = result.data;

        // ── Filas (DOM) ─────────────────────────────
        tablaUsuario.innerHTML = content.length === 0
            ? `<tr><td colspan="7" class="text-center py-4 text-muted">
                   <i class="fas fa-inbox fa-2x d-block mb-2"></i>Sin registros
               </td></tr>`
            : content.map((u, i) => `
                <tr>
                    <td>${currentPage * 5 + i + 1}</td>
                    <td>
                        <img src="${u.strImagen
                            ? '/uploads/usuarios/' + u.strImagen
                            : 'https://cdn-icons-png.flaticon.com/512/149/149071.png'}"
                             alt="foto"
                             class="rounded-circle"
                             style="width:40px;height:40px;object-fit:cover;">
                    </td>
                    <td>${escapeHtml(u.strNombreUsuario)}</td>
                    <td>${escapeHtml(u.strNombrePerfil)}</td>
                    <td>${u.strCorreo ? escapeHtml(u.strCorreo) : '<span class="text-muted">—</span>'}</td>
                    <td>
                        <span class="badge ${u.strEstado === 'Activo' ? 'bg-success' : 'bg-danger'}">
                            ${escapeHtml(u.strEstado)}
                        </span>
                    </td>
                    <td class="text-center">
                        <button class="btn btn-info btn-sm btn-action"
                                onclick="verDetalle(${u.id})" title="Detalle">
                            <i class="fas fa-eye"></i>
                        </button>
                        <a href="/seguridad/usuario/editar/${u.id}"
                           class="btn btn-warning btn-sm btn-action" title="Editar">
                            <i class="fas fa-edit"></i>
                        </a>
                        <button class="btn btn-danger btn-sm btn-action"
                                onclick="confirmarEliminar(${u.id},'${escapeHtml(u.strNombreUsuario)}')"
                                title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`).join('');

        infoRegistros.textContent =
            `Mostrando ${content.length} de ${totalElements} registro(s)`;
        renderPaginacion(currentPage, totalPages);

    } catch {
        tablaUsuario.innerHTML = `
            <tr><td colspan="7" class="text-center text-danger py-4">
                <i class="fas fa-exclamation-triangle me-2"></i>Error de conexión
            </td></tr>`;
    }
}

// ══════════════════════════════════════════════════════
// DOM - Paginación « ‹ 1 2 › »
// ══════════════════════════════════════════════════════
function renderPaginacion(currentPage, totalPages) {
    if (totalPages <= 1) { paginacion.innerHTML = ''; return; }
    const esPrimera = currentPage === 0;
    const esUltima  = currentPage === totalPages - 1;
    paginacion.innerHTML = `
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarUsuarios(0,'${terminoBusqueda}')" title="Primera">«</a>
        </li>
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarUsuarios(${currentPage - 1},'${terminoBusqueda}')" title="Anterior">‹</a>
        </li>
        ${Array.from({ length: totalPages }, (_, i) => `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#"
                   onclick="cargarUsuarios(${i},'${terminoBusqueda}')">${i + 1}</a>
            </li>`).join('')}
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarUsuarios(${currentPage + 1},'${terminoBusqueda}')" title="Siguiente">›</a>
        </li>
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarUsuarios(${totalPages - 1},'${terminoBusqueda}')" title="Última">»</a>
        </li>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Ver detalle
// ══════════════════════════════════════════════════════
async function verDetalle(id) {
    try {
        const response = await fetch(`/seguridad/usuario/api/${id}`);
        const result   = await response.json();
        if (!result.success) return;
        const u = result.data;

        // Llenar DOM del modal
        document.getElementById('detalleId').textContent      = u.id;
        document.getElementById('detalleUsuario').textContent = u.strNombreUsuario;
        document.getElementById('detallePerfil').textContent  = u.strNombrePerfil;
        document.getElementById('detalleCorreo').textContent  = u.strCorreo || '—';
        document.getElementById('detalleCelular').textContent = u.strNumeroCelular || '—';
        document.getElementById('detalleEstado').innerHTML    =
            `<span class="badge ${u.strEstado === 'Activo' ? 'bg-success' : 'bg-danger'}">
                ${u.strEstado}
            </span>`;
        document.getElementById('detalleImagen').src = u.strImagen
            ? `/uploads/usuarios/${u.strImagen}`
            : 'https://cdn-icons-png.flaticon.com/512/149/149071.png';

        modalDetalle.show();
    } catch { mostrarToast('Error al obtener detalle', 'danger'); }
}

// ══════════════════════════════════════════════════════
// DOM + FETCH API - Eliminar
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
        const response = await fetch(`/seguridad/usuario/api/${idEliminar}`, { method: 'DELETE' });
        const result   = await response.json();
        modalEliminar.hide();
        mostrarToast(result.message, result.success ? 'success' : 'danger');
        if (result.success) cargarUsuarios(paginaActual, terminoBusqueda);
    } catch { mostrarToast('Error de conexión', 'danger'); }
    finally  { btnConfirmarEliminar.disabled = false; idEliminar = null; }
});

// ── Búsqueda ──────────────────────────────────────────
btnBuscar.addEventListener('click', () =>
    cargarUsuarios(0, inputBuscar.value.trim()));
btnLimpiar.addEventListener('click', () => {
    inputBuscar.value = '';
    cargarUsuarios(0, '');
});
inputBuscar.addEventListener('keydown', e => {
    if (e.key === 'Enter') cargarUsuarios(0, inputBuscar.value.trim());
});

// ── Logout ────────────────────────────────────────────
document.getElementById('btnLogout').addEventListener('click', async e => {
    e.preventDefault();
    try { await fetch('/auth/logout', { method: 'POST' }); } finally {
        document.cookie = 'jwt_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
        window.location.href = '/login';
    }
});

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

document.addEventListener('DOMContentLoaded', () => cargarUsuarios());