// ── Logout ────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnLogout').addEventListener('click', async e => {
        e.preventDefault();
        try { await fetch('/auth/logout', { method: 'POST' }); } finally {
            document.cookie = 'jwt_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
            window.location.href = '/login';
        }
    });
});