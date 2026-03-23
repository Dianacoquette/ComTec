// ╔══════════════════════════════════════════════════╗
// ║       MÓDULO FORM - Fetch API + DOM              ║
// ╚══════════════════════════════════════════════════╝

const toastEl    = new bootstrap.Toast(document.getElementById('toast'));
const btnGuardar = document.getElementById('btnGuardar');

// ══════════════════════════════════════════════════════
// DOM - Configurar página según modo
// ══════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', async () => {
    if (MODO === 'editar' && MODULO_ID) {
        document.getElementById('cardTitulo').innerHTML =
            '<i class="fas fa-edit me-2"></i>Editar Módulo';
        document.getElementById('breadcrumbAccion').textContent = 'Editar';
        document.title = 'ComTec - Editar Módulo';
        await cargarDatosModulo();
    }
});

// ══════════════════════════════════════════════════════
// FETCH API - Cargar datos para editar
// ══════════════════════════════════════════════════════
async function cargarDatosModulo() {
    try {
        const response = await fetch(`/seguridad/modulo/api/${MODULO_ID}`);
        const result   = await response.json();
        if (!result.success) { mostrarAlerta('No se pudo cargar el módulo'); return; }
        document.getElementById('moduloNombre').value = result.data.strNombreModulo;
    } catch {
        mostrarAlerta('Error de conexión al cargar el módulo');
    }
}

// ══════════════════════════════════════════════════════
// FETCH API - Guardar
// ══════════════════════════════════════════════════════
btnGuardar.addEventListener('click', async () => {
    const nombre = document.getElementById('moduloNombre').value.trim();

    limpiarValidacion();
    if (!nombre) {
        document.getElementById('moduloNombre').classList.add('is-invalid');
        document.getElementById('errorNombre').textContent = 'El nombre del módulo es requerido';
        return;
    }

    const esEditar = MODO === 'editar';
    const url      = esEditar ? `/seguridad/modulo/api/${MODULO_ID}` : '/seguridad/modulo/api';
    const method   = esEditar ? 'PUT' : 'POST';

    btnGuardar.disabled = true;
    btnGuardar.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';

    try {
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ strNombreModulo: nombre })
        });
        const result = await response.json();

        if (result.success) {
            mostrarToast(result.message, 'success');
            setTimeout(() => window.location.href = '/seguridad/modulo', 1000);
        } else {
            mostrarAlerta(result.message);
        }
    } catch {
        mostrarAlerta('Error de conexión');
    } finally {
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
    document.getElementById('moduloNombre').classList.remove('is-invalid');
    document.getElementById('errorNombre').textContent = '';
    document.getElementById('alertError').classList.add('d-none');
}