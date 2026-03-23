// ╔══════════════════════════════════════════════════╗
// ║       PERFIL FORM - Fetch API + DOM              ║
// ╚══════════════════════════════════════════════════╝

const toastEl   = new bootstrap.Toast(document.getElementById('toast'));
const btnGuardar = document.getElementById('btnGuardar');

// ══════════════════════════════════════════════════════
// DOM - Configurar página según modo
// ══════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', async () => {
    if (MODO === 'editar' && PERFIL_ID) {
        // Cambiar títulos (DOM)
        document.getElementById('cardTitulo').innerHTML =
            '<i class="fas fa-edit me-2"></i>Editar Perfil';
        document.getElementById('breadcrumbAccion').textContent = 'Editar';
        document.title = 'ComTec - Editar Perfil';

        // FETCH API - Cargar datos del perfil
        await cargarDatosPerfil();
    }
});

// ══════════════════════════════════════════════════════
// FETCH API - Cargar datos para editar
// ══════════════════════════════════════════════════════
async function cargarDatosPerfil() {
    try {
        const response = await fetch(`/seguridad/perfil/api/${PERFIL_ID}`);
        const result   = await response.json();
        if (!result.success) {
            mostrarAlerta('No se pudo cargar el perfil');
            return;
        }
        const p = result.data;
        // Llenar DOM con los datos
        document.getElementById('perfilNombre').value  = p.strNombrePerfil;
        document.getElementById('perfilAdmin').checked = p.bitAdministrador;
    } catch {
        mostrarAlerta('Error de conexión al cargar el perfil');
    }
}

// ══════════════════════════════════════════════════════
// FETCH API - Guardar (crear o editar)
// ══════════════════════════════════════════════════════
btnGuardar.addEventListener('click', async () => {
    // Leer valores del DOM
    const nombre = document.getElementById('perfilNombre').value.trim();
    const admin  = document.getElementById('perfilAdmin').checked;

    // Validación DOM
    limpiarValidacion();
    if (!nombre) {
        document.getElementById('perfilNombre').classList.add('is-invalid');
        document.getElementById('errorNombre').textContent = 'El nombre es requerido';
        return;
    }

    const esEditar = MODO === 'editar';
    const url      = esEditar
        ? `/seguridad/perfil/api/${PERFIL_ID}`
        : '/seguridad/perfil/api';
    const method   = esEditar ? 'PUT' : 'POST';

    // Estado cargando (DOM)
    btnGuardar.disabled = true;
    btnGuardar.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';

    try {
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ strNombrePerfil: nombre, bitAdministrador: admin })
        });
        const result = await response.json();

        if (result.success) {
            mostrarToast(result.message, 'success');
            // Redirigir a lista después de 1 segundo
            setTimeout(() => window.location.href = '/seguridad/perfil', 1000);
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
    document.getElementById('perfilNombre').classList.remove('is-invalid');
    document.getElementById('errorNombre').textContent = '';
    document.getElementById('alertError').classList.add('d-none');
}