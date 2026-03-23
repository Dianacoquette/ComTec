// ╔══════════════════════════════════════════════════╗
// ║   PERMISOS-PERFIL FORM - Fetch API + DOM         ║
// ╚══════════════════════════════════════════════════╝

const toastEl    = new bootstrap.Toast(document.getElementById('toast'));
const btnGuardar = document.getElementById('btnGuardar');
const checkTodos = document.getElementById('checkTodos');
const checks     = document.querySelectorAll('.permiso-check');

// ══════════════════════════════════════════════════════
// DOM - Inicializar
// ══════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', async () => {
    await cargarSelects();

    if (MODO === 'editar' && PERMISO_ID) {
        document.getElementById('cardTitulo').innerHTML =
            '<i class="fas fa-edit me-2"></i>Editar Permiso';
        document.getElementById('breadcrumbAccion').textContent = 'Editar';
        document.title = 'ComTec - Editar Permiso';
        await cargarDatosPermiso();
    }

    inicializarCheckTodos();
});

// ══════════════════════════════════════════════════════
// FETCH API - Cargar selects
// ══════════════════════════════════════════════════════
async function cargarSelects() {
    try {
        // Perfiles
        const rP = await fetch('/seguridad/permisos-perfil/api/perfiles');
        const dP = await rP.json();
        const selPerfil = document.getElementById('permisoPerfil');
        if (dP.success) {
            dP.data.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.textContent = p.nombre;
                selPerfil.appendChild(opt);
            });
        }

        // Módulos
        const rM = await fetch('/seguridad/permisos-perfil/api/modulos');
        const dM = await rM.json();
        const selModulo = document.getElementById('permisoModulo');
        if (dM.success) {
            dM.data.forEach(m => {
                const opt = document.createElement('option');
                opt.value = m.id;
                opt.textContent = m.nombre;
                selModulo.appendChild(opt);
            });
        }
    } catch { mostrarAlerta('Error al cargar catálogos'); }
}

// ══════════════════════════════════════════════════════
// FETCH API - Cargar datos para editar
// ══════════════════════════════════════════════════════
async function cargarDatosPermiso() {
    try {
        const response = await fetch(`/seguridad/permisos-perfil/api/${PERMISO_ID}`);
        const result   = await response.json();
        if (!result.success) { mostrarAlerta('No se pudo cargar el permiso'); return; }
        const p = result.data;

        // Llenar DOM
        document.getElementById('permisoPerfil').value  = p.idPerfil;
        document.getElementById('permisoModulo').value  = p.idModulo;
        document.getElementById('chkAgregar').checked  = p.bitAgregar;
        document.getElementById('chkEditar').checked   = p.bitEditar;
        document.getElementById('chkConsulta').checked = p.bitConsulta;
        document.getElementById('chkEliminar').checked = p.bitEliminar;
        document.getElementById('chkDetalle').checked  = p.bitDetalle;

        // Actualizar estado del "Seleccionar todos" (DOM)
        actualizarCheckTodos();
    } catch { mostrarAlerta('Error de conexión al cargar el permiso'); }
}

// ══════════════════════════════════════════════════════
// DOM - Lógica de "Seleccionar todos"
// ══════════════════════════════════════════════════════
function inicializarCheckTodos() {
    checkTodos.addEventListener('change', () => {
        checks.forEach(c => c.checked = checkTodos.checked);
    });
    checks.forEach(c => c.addEventListener('change', actualizarCheckTodos));
}

function actualizarCheckTodos() {
    const todos = Array.from(checks).every(c => c.checked);
    const ninguno = Array.from(checks).every(c => !c.checked);
    checkTodos.checked       = todos;
    checkTodos.indeterminate = !todos && !ninguno;
}

// ══════════════════════════════════════════════════════
// FETCH API - Guardar
// ══════════════════════════════════════════════════════
btnGuardar.addEventListener('click', async () => {
    limpiarValidacion();

    const perfil = document.getElementById('permisoPerfil').value;
    const modulo = document.getElementById('permisoModulo').value;

    // Validaciones DOM
    let valido = true;
    if (!perfil) {
        document.getElementById('permisoPerfil').classList.add('is-invalid');
        document.getElementById('errorPerfil').textContent = 'Selecciona un perfil';
        valido = false;
    }
    if (!modulo) {
        document.getElementById('permisoModulo').classList.add('is-invalid');
        document.getElementById('errorModulo').textContent = 'Selecciona un módulo';
        valido = false;
    }
    if (!valido) return;

    const esEditar = MODO === 'editar';
    const url      = esEditar
        ? `/seguridad/permisos-perfil/api/${PERMISO_ID}`
        : '/seguridad/permisos-perfil/api';
    const method   = esEditar ? 'PUT' : 'POST';

    btnGuardar.disabled = true;
    btnGuardar.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';

    try {
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                idPerfil:   parseInt(perfil),
                idModulo:   parseInt(modulo),
                bitAgregar:  document.getElementById('chkAgregar').checked,
                bitEditar:   document.getElementById('chkEditar').checked,
                bitConsulta: document.getElementById('chkConsulta').checked,
                bitEliminar: document.getElementById('chkEliminar').checked,
                bitDetalle:  document.getElementById('chkDetalle').checked
            })
        });
        const result = await response.json();

        if (result.success) {
            mostrarToast(result.message, 'success');
            setTimeout(() => window.location.href = '/seguridad/permisos-perfil', 1000);
        } else {
            mostrarAlerta(result.message);
        }
    } catch { mostrarAlerta('Error de conexión'); }
    finally {
        btnGuardar.disabled = false;
        btnGuardar.innerHTML = '<i class="fas fa-save me-1"></i>Guardar';
    }
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
function mostrarAlerta(msg) {
    document.getElementById('alertError').classList.remove('d-none');
    document.getElementById('alertMensaje').textContent = msg;
}
function limpiarValidacion() {
    ['permisoPerfil', 'permisoModulo'].forEach(id =>
        document.getElementById(id).classList.remove('is-invalid'));
    ['errorPerfil', 'errorModulo'].forEach(id =>
        document.getElementById(id).textContent = '');
    document.getElementById('alertError').classList.add('d-none');
}