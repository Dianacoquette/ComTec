// ╔══════════════════════════════════════════════════╗
// ║  PERMISOS-PERFIL LISTA - Fetch API + DOM         ║
// ╚══════════════════════════════════════════════════╝

const tablaPermisos = document.getElementById('tablaPermisos');
const paginacion = document.getElementById('paginacion');
const infoRegistros = document.getElementById('infoRegistros');
const infoFiltro = document.getElementById('infoFiltro');
const filtroPerfil = document.getElementById('filtroPerfil');
const btnLimpiarFiltro = document.getElementById('btnLimpiarFiltro');
const btnConfirmarEliminar = document.getElementById('btnConfirmarEliminar');
const modalDetalle = new bootstrap.Modal(document.getElementById('modalDetalle'));
const modalEliminar = new bootstrap.Modal(document.getElementById('modalEliminar'));
const toastEl = new bootstrap.Toast(document.getElementById('toast'));

let paginaActual = 0;
let perfilFiltro = 0;
let idEliminar = null;

// ══════════════════════════════════════════════════════
// FETCH API - Cargar perfiles en select
// ══════════════════════════════════════════════════════
async function cargarFiltroPerfiles() {
    try {
        const response = await fetch('/seguridad/permisos-perfil/api/perfiles');
        const result = await response.json();
        if (!result.success) return;
        result.data.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.id;
            opt.textContent = p.nombre;
            filtroPerfil.appendChild(opt);
        });
    } catch { console.error('Error al cargar perfiles'); }
}

// ══════════════════════════════════════════════════════
// FETCH API - Cargar tabla
// ══════════════════════════════════════════════════════
async function cargarPermisos(page = 0, filtro = 0) {
    paginaActual = page;
    perfilFiltro = filtro;

    // Si no hay perfil seleccionado mostrar mensaje
    if (filtro === 0) {
        tablaPermisos.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-5 text-muted">
                    <i class="fas fa-hand-point-up fa-2x d-block mb-2"></i>
                    Selecciona un perfil para ver y editar sus permisos
                </td>
            </tr>`;
        infoRegistros.textContent = '';
        paginacion.innerHTML = '';
        return;
    }

    tablaPermisos.innerHTML = `
        <tr>
            <td colspan="9" class="text-center py-4">
                <div class="spinner-border text-primary"></div>
            </td>
        </tr>`;

    try {
        const url = `/seguridad/permisos-perfil/api?page=${page}&perfilFiltro=${filtro}`;
        const response = await fetch(url);
        const result = await response.json();

        if (!result.success) { mostrarToast('Error al cargar', 'danger'); return; }

        const { content, totalPages, totalElements, currentPage } = result.data;

        // Nombre del perfil seleccionado (DOM)
        const nombrePerfil = filtroPerfil.options[filtroPerfil.selectedIndex]?.text || '';
        infoFiltro.textContent = `Perfil: ${nombrePerfil}`;

        if (content.length === 0) {
            tablaPermisos.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center py-4 text-muted">
                        <i class="fas fa-inbox fa-2x d-block mb-2"></i>Sin módulos
                    </td>
                </tr>`;
        } else {
            tablaPermisos.innerHTML = content.map((p, i) => {
                const chkAgregar = checkboxHtml(p.idPermiso, p.idPerfil, p.idModulo, 'bitAgregar', p.bitAgregar);
                const chkEditar = checkboxHtml(p.idPermiso, p.idPerfil, p.idModulo, 'bitEditar', p.bitEditar);
                const chkConsulta = checkboxHtml(p.idPermiso, p.idPerfil, p.idModulo, 'bitConsulta', p.bitConsulta);
                const chkEliminar = checkboxHtml(p.idPermiso, p.idPerfil, p.idModulo, 'bitEliminar', p.bitEliminar);
                const chkDetalle = checkboxHtml(p.idPermiso, p.idPerfil, p.idModulo, 'bitDetalle', p.bitDetalle);

                const btnDetalle = PERM.detalle && p.idPermiso > 0
                    ? `<button class="btn btn-info btn-sm btn-action"
                   onclick="verDetalle(${p.idPermiso})" title="Detalle">
               <i class="fas fa-eye"></i>
           </button>` : '';

                const btnEliminar = PERM.eliminar && p.idPermiso > 0
                    ? `<button class="btn btn-danger btn-sm btn-action"
                   onclick="confirmarEliminar(${p.idPermiso},
                   '${escapeHtml(nombrePerfil)} - ${escapeHtml(p.strNombreModulo)}')"
                   title="Eliminar">
               <i class="fas fa-trash"></i>
           </button>` : '';

                const colAcciones = (PERM.detalle || PERM.eliminar)
                    ? `<td class="text-center">${btnDetalle}${btnEliminar}</td>`
                    : '';

                return `
        <tr id="fila-${p.idModulo}">
            <td>${currentPage * 5 + i + 1}</td>
            <td>${escapeHtml(p.strNombreModulo)}</td>
            <td class="text-center">${chkAgregar}</td>
            <td class="text-center">${chkEditar}</td>
            <td class="text-center">${chkConsulta}</td>
            <td class="text-center">${chkEliminar}</td>
            <td class="text-center">${chkDetalle}</td>
            ${colAcciones}
        </tr>`;
            }).join('');

            // Agregar listeners a checkboxes (DOM)
            document.querySelectorAll('.chk-permiso').forEach(chk => {
                chk.addEventListener('change', onTogglePermiso);
            });
        }

        infoRegistros.textContent =
            `Mostrando ${content.length} de ${totalElements} módulo(s)`;
        renderPaginacion(currentPage, totalPages);

    } catch {
        tablaPermisos.innerHTML = `
            <tr>
                <td colspan="9" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-triangle me-2"></i>Error de conexión
                </td>
            </tr>`;
    }
}

// ══════════════════════════════════════════════════════
// DOM - Generar checkbox con data attributes
// ══════════════════════════════════════════════════════
function checkboxHtml(idPermiso, idPerfil, idModulo, campo, valor) {
    const checked = valor ? 'checked' : '';
    const disabled = !PERM.editar ? 'disabled' : '';
    return `
        <div class="form-check d-flex justify-content-center mb-0">
            <input type="checkbox"
                   class="form-check-input chk-permiso"
                   data-idpermiso="${idPermiso}"
                   data-idperfil="${idPerfil}"
                   data-idmodulo="${idModulo}"
                   data-campo="${campo}"
                   ${checked}
                   ${disabled}>
        </div>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Toggle: actualiza o crea el permiso
// ══════════════════════════════════════════════════════
async function onTogglePermiso(e) {
    const chk = e.target;
    const idPermiso = parseInt(chk.dataset.idpermiso) || 0;
    const idPerfil = parseInt(chk.dataset.idperfil) || 0;
    const idModulo = parseInt(chk.dataset.idmodulo) || 0;
    const campo = chk.dataset.campo;
    const valor = chk.checked;

    // Indicador visual (DOM)
    chk.disabled = true;
    const cell = chk.closest('td');
    const indicator = document.createElement('span');
    indicator.className = 'saving-indicator ms-1';
    cell.appendChild(indicator);

    try {
        const response = await fetch('/seguridad/permisos-perfil/api/toggle', {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idPermiso, idPerfil, idModulo, campo, valor })
        });
        const result = await response.json();

        if (result.success) {
            // Si se creó nuevo permiso, actualizar el data-idpermiso en todos
            // los checkboxes de esa fila (DOM)
            if (idPermiso === 0 && result.data?.idPermiso) {
                const nuevaId = result.data.idPermiso;
                const fila = chk.closest('tr');
                fila.querySelectorAll('.chk-permiso').forEach(c => {
                    c.dataset.idpermiso = nuevaId;
                });

                // Actualizar botones de detalle/eliminar en la fila
                actualizarBotonesAccion(fila, nuevaId, idPerfil, idModulo);
            }
            mostrarToast('Permiso actualizado', 'success');
        } else {
            chk.checked = !valor;
            mostrarToast('Error al actualizar', 'danger');
        }
    } catch {
        chk.checked = !valor;
        mostrarToast('Error de conexión', 'danger');
    } finally {
        chk.disabled = false;
        indicator.remove();
    }
}

// ══════════════════════════════════════════════════════
// DOM - Actualizar botones de acción cuando se crea permiso
// ══════════════════════════════════════════════════════
function actualizarBotonesAccion(fila, idPermiso, idPerfil, idModulo) {
    const colAcciones = fila.querySelector('td:last-child');
    if (!colAcciones) return;

    const nombreModulo = fila.querySelector('td:nth-child(2)')?.textContent || '';
    const nombrePerfil = filtroPerfil.options[filtroPerfil.selectedIndex]?.text || '';

    const btnDetalle = PERM.detalle
        ? `<button class="btn btn-info btn-sm btn-action me-1"
                   onclick="verDetalle(${idPermiso})" title="Detalle">
               <i class="fas fa-eye"></i>
           </button>` : '';

    const btnEliminar = PERM.eliminar
        ? `<button class="btn btn-danger btn-sm btn-action"
                   onclick="confirmarEliminar(${idPermiso},
                   '${escapeHtml(nombrePerfil)} - ${escapeHtml(nombreModulo)}')"
                   title="Eliminar">
               <i class="fas fa-trash"></i>
           </button>` : '';

    if (PERM.detalle || PERM.eliminar) {
        colAcciones.innerHTML = btnDetalle + btnEliminar;
    }
}

// ══════════════════════════════════════════════════════
// DOM - Paginación « ‹ 1 2 › »
// ══════════════════════════════════════════════════════
function renderPaginacion(currentPage, totalPages) {
    if (totalPages <= 1) { paginacion.innerHTML = ''; return; }
    const esPrimera = currentPage === 0;
    const esUltima = currentPage === totalPages - 1;
    paginacion.innerHTML = `
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(0,${perfilFiltro})" title="Primera">«</a>
        </li>
        <li class="page-item ${esPrimera ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${currentPage - 1},${perfilFiltro})"
               title="Anterior">‹</a>
        </li>
        ${Array.from({ length: totalPages }, (_, i) => `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#"
                   onclick="cargarPermisos(${i},${perfilFiltro})">${i + 1}</a>
            </li>`).join('')}
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${currentPage + 1},${perfilFiltro})"
               title="Siguiente">›</a>
        </li>
        <li class="page-item ${esUltima ? 'disabled' : ''}">
            <a class="page-link" href="#"
               onclick="cargarPermisos(${totalPages - 1},${perfilFiltro})"
               title="Última">»</a>
        </li>`;
}

// ══════════════════════════════════════════════════════
// FETCH API - Ver detalle
// ══════════════════════════════════════════════════════
async function verDetalle(id) {
    try {
        const response = await fetch(`/seguridad/permisos-perfil/api/${id}`);
        const result = await response.json();
        if (!result.success) return;
        const p = result.data;
        document.getElementById('detalleId').textContent = p.id;
        document.getElementById('detallePerfil').textContent = p.strNombrePerfil;
        document.getElementById('detalleModulo').textContent = p.strNombreModulo;
        document.getElementById('detalleAgregar').innerHTML = badgeBool(p.bitAgregar);
        document.getElementById('detalleEditar').innerHTML = badgeBool(p.bitEditar);
        document.getElementById('detalleConsulta').innerHTML = badgeBool(p.bitConsulta);
        document.getElementById('detalleEliminar').innerHTML = badgeBool(p.bitEliminar);
        document.getElementById('detalleDetalle').innerHTML = badgeBool(p.bitDetalle);
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
        if (result.success) cargarPermisos(paginaActual, perfilFiltro);
    } catch { mostrarToast('Error de conexión', 'danger'); }
    finally { btnConfirmarEliminar.disabled = false; idEliminar = null; }
});

// ── Filtro ────────────────────────────────────────────
filtroPerfil.addEventListener('change', () => {
    cargarPermisos(0, parseInt(filtroPerfil.value));
});

btnLimpiarFiltro.addEventListener('click', () => {
    filtroPerfil.value = '0';
    cargarPermisos(0, 0);
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

// ── Inicio ────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    await cargarFiltroPerfiles();
    await cargarPermisos(0, 0);
});