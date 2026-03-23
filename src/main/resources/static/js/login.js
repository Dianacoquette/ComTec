// ── DOM Referencias ──────────────────────────────────
const btnLogin      = document.getElementById('btnLogin');
const btnText       = document.getElementById('btnText');
const btnSpinner    = document.getElementById('btnSpinner');
const alertError    = document.getElementById('alertError');
const alertMessage  = document.getElementById('alertMessage');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const togglePass    = document.getElementById('togglePassword');
const eyeIcon       = document.getElementById('eyeIcon');

// ── Mostrar/ocultar contraseña ───────────────────────
togglePass.addEventListener('click', () => {
    const isPassword = passwordInput.type === 'password';
    passwordInput.type = isPassword ? 'text' : 'password';
    eyeIcon.classList.toggle('fa-eye', !isPassword);
    eyeIcon.classList.toggle('fa-eye-slash', isPassword);
});

// ── Mostrar error ────────────────────────────────────
function showError(msg) {
    alertMessage.textContent = msg;
    alertError.classList.remove('d-none');
}

function hideError() {
    alertError.classList.add('d-none');
}

// ── Estado del botón ─────────────────────────────────
function setLoading(loading) {
    btnLogin.disabled = loading;
    btnText.classList.toggle('d-none', loading);
    btnSpinner.classList.toggle('d-none', !loading);
}

// ── Login con Fetch API ──────────────────────────────
btnLogin.addEventListener('click', async () => {
    hideError();

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    // Validaciones DOM
    if (!username) {
        showError('Ingresa tu nombre de usuario.');
        usernameInput.focus();
        return;
    }
    if (!password) {
        showError('Ingresa tu contraseña.');
        passwordInput.focus();
        return;
    }

    // Obtener token reCAPTCHA
    const captchaToken = grecaptcha.getResponse();
    if (!captchaToken) {
        showError('Por favor completa el captcha.');
        return;
    }

    setLoading(true);

    try {
        // Fetch API → POST /auth/login
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, captchaToken })
        });

        const result = await response.json();

        if (result.success) {
            // Redirigir al dashboard
            window.location.href = '/dashboard';
        } else {
            showError(result.message);
            grecaptcha.reset();
        }
    } catch (error) {
        showError('Error de conexión. Intenta de nuevo.');
        grecaptcha.reset();
    } finally {
        setLoading(false);
    }
});

// ── Enter para hacer login ───────────────────────────
document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') btnLogin.click();
});