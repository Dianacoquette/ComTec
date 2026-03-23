// ╔══════════════════════════════════════════════════╗
// ║   PERMISOS-PERFIL LISTA - Fetch API + DOM        ║
// ╚══════════════════════════════════════════════════╝

const tablaPermisos        = document.getElementById('tablaPermisos');
const paginacion           = document.getElementById('paginacion');
const infoRegistros        = document.getElementById('infoRegistros');
const btnConfirmarEliminar = document.getElementById('btnConfirmarEliminar');
const modalDetalle         = new bootstrap.Modal(document.getElementById('modalDetalle'));
const modalEliminar        = new bootstrap.Modal(document.getElementById('modalEliminar'));
const toastEl              = new bootstrap.Toast(document.getElementById('toast'));

let paginaActual = 0;
let idEliminar   = null;

// ══════════════════════════════════════════════════════
// FETCH API - Cargar tabla (sin búsqueda)
// ══════════════════════════════════════════════════════
async function cargarPermisos(page = 0) {
    paginaActual = page;

    tablaPermisos.innerHTML = `
        <tr>
            <td colspan="9" class="text-center py-4">
                <div class="spinner-border text-primary"></div>
            </td>
        </tr>`;

    try {
        const response = await fetch(`/seguridad/permisos-perfil/api?page=${page}`);
        const result   = await response.json();

        if (!result.success) { mostrarToast('Error al cargar', 'danger'); return; }

        const { content, totalPages, totalElements, currentPage } = result.data;

        // ── Renderizar filas (DOM) ──────────────────
        tablaPermisos.innerHTML = content.length === 0
            ? `<tr><td colspan="9" class="text-center py-4 text-muted">
                   <i class="fas fa-inbox fa-2x d-block mb-2"></i>Sin registros
               </td></tr>`
            : content.map((p, i) => `
                <tr>
                    <td>${currentPage * 5 + i + 1}</td>
                    <td>${escapeHtml(p.strNombrePerfil)}</td>
                    <td>${escapeHtml(p.strNombreModulo)}</td>
                    <td class="text-center">${badgeBool(p.bitAgregar)}</td>
                    <td class="text-center">${badgeBool(p.bitEditar)}</td>
                    <td class="text-center">${badgeBool(p.bitConsulta)}</td>
                    <td class="text-center">${badgeBool(p.bitEliminar)}</td>
                    <td class="text-center">${badgeBool(p.bitDetalle)}</td>
                    <td class="text-center">
                        <button class="btn btn-info btn-sm btn-action"
                                onclick="verDetalle(${p.id})" title="Detalle">
                            <i class="fas fa-eye"></i>
                        </button>
                        <a href="/seguridad/permisos-perfil/editar/${p.id}"
                           class="btn btn-warning btn-sm btn-action" title="Editar">
                            <i class="fas fa-edit"></i>
                        </a>
                        <button class="btn btn-danger btn-sm btn-action"
                                onclick="confirmarEliminar(${p.id},'${escapeHtml(p.strNombrePerfil)} - ${escapeHtml(p.strNombreModulo)}')"
                                title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`).join('');

        infoRegistros.textContent =
            `Mostrando ${content.length} de ${totalElements} registro(s)`;
        renderPaginacion(currentPage, totalPages);

    } catch {
        tablaPermisos.innerHTML = `
            <tr><td colspan="9" class="text-center text-danger py-4">
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
               onclick="cargarPermisos(0)" title="Primera">«</a>
        </li>
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${currentPage - 1})" title="Anterior">‹</a>
        </li>
        ${Array.from({ length: totalPages }, (_, i) => `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#"
                   onclick="cargarPermisos(${i})">${i + 1}</a>
            </li>`).join('')}
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${currentPage + 1})" title="Siguiente">›</a>
        </li>
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${totalPages - 1})" title="Última">»</a>
        </li>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Ver detalle
// ══════════════════════════════════════════════════════
async function verDetalle(id) {
    try {
        const response = await fetch(`/seguridad/permisos-perfil/api/${id}`);
        const result   = await response.json();
        if (!result.success) return;
        const p = result.data;

        // Llenar DOM del modal
        document.getElementById('detalleId').textContent      = p.id;
        document.getElementById('detallePerfil').textContent  = p.strNombrePerfil;
        document.getElementById('detalleModulo').textContent  = p.strNombreModulo;
        document.getElementById('detalleAgregar').innerHTML   = badgeBool(p.bitAgregar);
        document.getElementById('detalleEditar').innerHTML    = badgeBool(p.bitEditar);
        document.getElementById('detalleConsulta').innerHTML  = badgeBool(p.bitConsulta);
        document.getElementById('detalleEliminar').innerHTML  = badgeBool(p.bitEliminar);
        document.getElementById('detalleDetalle').innerHTML   = badgeBool(p.bitDetalle);

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
        const response = await fetch(
            `/seguridad/permisos-perfil/api/${idEliminar}`, { method: 'DELETE' }
        );
        const result = await response.json();
        modalEliminar.hide();
        mostrarToast(result.message, result.success ? 'success' : 'danger');
        if (result.success) cargarPermisos(paginaActual);
    } catch { mostrarToast('Error de conexión', 'danger'); }
    finally  { btnConfirmarEliminar.disabled = false; idEliminar = null; }
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
function badgeBool(val) {
    return val
        ? '<span class="badge bg-success"><i class="fas fa-check"></i></span>'
        : '<span class="badge bg-secondary"><i class="fas fa-times"></i></span>';
}
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

document.addEventListener('DOMContentLoaded', () => cargarPermisos());