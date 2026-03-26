// ╔══════════════════════════════════════════════════╗
// ║      USUARIO FORM - Fetch API + DOM              ║
// ╚══════════════════════════════════════════════════╝

const toastEl    = new bootstrap.Toast(document.getElementById('toast'));
const btnGuardar = document.getElementById('btnGuardar');

// ── Estado imagen ─────────────────────────────────────
let imagenPendiente = null; // archivo seleccionado aún no subido

// ══════════════════════════════════════════════════════
// DOM - Inicializar página
// ══════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', async () => {
    await cargarSelects();

    if (MODO === 'editar' && USUARIO_ID) {
        // Ajustar títulos DOM
        document.getElementById('cardTitulo').innerHTML =
            '<i class="fas fa-edit me-2"></i>Editar Usuario';
        document.getElementById('breadcrumbAccion').textContent = 'Editar';
        document.getElementById('pwdRequerido').style.display  = 'none';
        document.getElementById('pwdOpcional').style.display   = 'inline';
        document.title = 'ComTec - Editar Usuario';
        await cargarDatosUsuario();
    }

    inicializarUploadImagen();
    inicializarTogglePassword();
});

// ══════════════════════════════════════════════════════
// FETCH API - Cargar selects de Perfil y Estado
// ══════════════════════════════════════════════════════
async function cargarSelects() {
    try {
        // Perfiles
        const rPerfil  = await fetch('/seguridad/usuario/api/perfiles');
        const dPerfil  = await rPerfil.json();
        const selPerfil = document.getElementById('usuarioPerfil');
        if (dPerfil.success) {
            dPerfil.data.forEach(p => {
                const opt = document.createElement('option');
                opt.value       = p.id;
                opt.textContent = p.nombre;
                selPerfil.appendChild(opt);
            });
        }

        // Estados
        const rEstado  = await fetch('/seguridad/usuario/api/estados');
        const dEstado  = await rEstado.json();
        const selEstado = document.getElementById('usuarioEstado');
        if (dEstado.success) {
            dEstado.data.forEach(e => {
                const opt = document.createElement('option');
                opt.value       = e.id;
                opt.textContent = e.nombre;
                selEstado.appendChild(opt);
            });
        }
    } catch { mostrarAlerta('Error al cargar datos del formulario'); }
}

// ══════════════════════════════════════════════════════
// FETCH API - Cargar datos del usuario para editar
// ══════════════════════════════════════════════════════
async function cargarDatosUsuario() {
    try {
        const response = await fetch(`/seguridad/usuario/api/${USUARIO_ID}`);
        const result   = await response.json();
        if (!result.success) { mostrarAlerta('No se pudo cargar el usuario'); return; }
        const u = result.data;

        // Llenar DOM con datos
        document.getElementById('usuarioNombre').value  = u.strNombreUsuario;
        document.getElementById('usuarioPerfil').value  = u.idPerfil;
        document.getElementById('usuarioEstado').value  = u.idEstadoUsuario;
        document.getElementById('usuarioCorreo').value  = u.strCorreo  || '';
        document.getElementById('usuarioCelular').value = u.strNumeroCelular || '';

        // Mostrar imagen actual (DOM)
        if (u.strImagen) {
            document.getElementById('imgPreview').src = u.strImagen;
        }
    } catch { mostrarAlerta('Error de conexión al cargar el usuario'); }
}

// ══════════════════════════════════════════════════════
// DOM - Upload de imagen con drag & drop + preview
// ══════════════════════════════════════════════════════
function inicializarUploadImagen() {
    const uploadArea  = document.getElementById('uploadArea');
    const inputImagen = document.getElementById('inputImagen');
    const imgPreview  = document.getElementById('imgPreview');
    const estadoImg   = document.getElementById('estadoImagen');

    // Clic en área de upload
    uploadArea.addEventListener('click', () => inputImagen.click());

    // Drag & Drop (DOM eventos)
    uploadArea.addEventListener('dragover', e => {
        e.preventDefault();
        uploadArea.style.background = '#e8f0fe';
    });
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.style.background = '';
    });
    uploadArea.addEventListener('drop', e => {
        e.preventDefault();
        uploadArea.style.background = '';
        const file = e.dataTransfer.files[0];
        if (file) procesarImagen(file);
    });

    // Selección por input
    inputImagen.addEventListener('change', () => {
        if (inputImagen.files[0]) procesarImagen(inputImagen.files[0]);
    });

    function procesarImagen(file) {
        const ext = file.name.split('.').pop().toLowerCase();
        if (!['jpg','jpeg','png','gif','webp'].includes(ext)) {
            estadoImg.innerHTML =
                '<span class="text-danger"><i class="fas fa-times-circle me-1"></i>Formato no válido</span>';
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            estadoImg.innerHTML =
                '<span class="text-danger"><i class="fas fa-times-circle me-1"></i>Imagen mayor a 5MB</span>';
            return;
        }

        // Preview con FileReader (DOM)
        const reader = new FileReader();
        reader.onload = e => { imgPreview.src = e.target.result; };
        reader.readAsDataURL(file);

        imagenPendiente = file;
        estadoImg.innerHTML =
            `<span class="text-success">
                <i class="fas fa-check-circle me-1"></i>${file.name}
             </span>`;
    }
}

// ══════════════════════════════════════════════════════
// DOM - Toggle contraseña visible
// ══════════════════════════════════════════════════════
function inicializarTogglePassword() {
    document.getElementById('togglePwd').addEventListener('click', () => {
        const input   = document.getElementById('usuarioPwd');
        const icon    = document.getElementById('eyeIconPwd');
        const isPass  = input.type === 'password';
        input.type    = isPass ? 'text' : 'password';
        icon.className = isPass ? 'fas fa-eye-slash' : 'fas fa-eye';
    });
}

// ══════════════════════════════════════════════════════
// FETCH API - Guardar usuario
// ══════════════════════════════════════════════════════
btnGuardar.addEventListener('click', async () => {
    limpiarValidacion();

    const nombre  = document.getElementById('usuarioNombre').value.trim();
    const pwd     = document.getElementById('usuarioPwd').value.trim();
    const perfil  = document.getElementById('usuarioPerfil').value;
    const estado  = document.getElementById('usuarioEstado').value;
    const correo  = document.getElementById('usuarioCorreo').value.trim();
    const celular = document.getElementById('usuarioCelular').value.trim();

    // Validaciones DOM
    let valido = true;
    if (!nombre) {
        document.getElementById('usuarioNombre').classList.add('is-invalid');
        document.getElementById('errorNombre').textContent = 'El nombre de usuario es requerido';
        valido = false;
    }
    if (MODO === 'nuevo' && !pwd) {
        document.getElementById('usuarioPwd').classList.add('is-invalid');
        document.getElementById('errorPwd').textContent = 'La contraseña es requerida';
        valido = false;
    }
    if (!perfil) {
        document.getElementById('usuarioPerfil').classList.add('is-invalid');
        document.getElementById('errorPerfil').textContent = 'Selecciona un perfil';
        valido = false;
    }
    if (!estado) {
        document.getElementById('usuarioEstado').classList.add('is-invalid');
        document.getElementById('errorEstado').textContent = 'Selecciona un estado';
        valido = false;
    }
    if (correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
        document.getElementById('usuarioCorreo').classList.add('is-invalid');
        document.getElementById('errorCorreo').textContent = 'Correo no válido';
        valido = false;
    }
    if (!valido) return;

    const esEditar = MODO === 'editar';
    const url      = esEditar ? `/seguridad/usuario/api/${USUARIO_ID}` : '/seguridad/usuario/api';
    const method   = esEditar ? 'PUT' : 'POST';

    btnGuardar.disabled = true;
    btnGuardar.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';

    try {
        // PASO 1: Guardar datos del usuario
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                strNombreUsuario: nombre,
                strPwd:           pwd,
                idPerfil:         parseInt(perfil),
                idEstadoUsuario:  parseInt(estado),
                strCorreo:        correo  || null,
                strNumeroCelular: celular || null
            })
        });
        const result = await response.json();

        if (!result.success) { mostrarAlerta(result.message); return; }

        const idGuardado = result.data.id;

        // PASO 2: Subir imagen si hay una pendiente (Fetch API con FormData)
        if (imagenPendiente) {
            const formData = new FormData();
            formData.append('imagen', imagenPendiente);
            await fetch(`/seguridad/usuario/api/${idGuardado}/imagen`, {
                method: 'POST',
                body: formData
            });
        }

        mostrarToast(result.message, 'success');
        setTimeout(() => window.location.href = '/seguridad/usuario', 1000);

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
    ['usuarioNombre','usuarioPwd','usuarioPerfil','usuarioEstado','usuarioCorreo']
        .forEach(id => document.getElementById(id).classList.remove('is-invalid'));
    ['errorNombre','errorPwd','errorPerfil','errorEstado','errorCorreo']
        .forEach(id => document.getElementById(id).textContent = '');
    document.getElementById('alertError').classList.add('d-none');
}