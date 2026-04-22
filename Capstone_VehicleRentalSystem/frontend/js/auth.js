// Base URL of your Spring Boot backend
const BASE_URL = 'http://localhost:8080';

// ─── UTILITY FUNCTIONS ───────────────────────────────────────

// Show error message
function showError(elementId, message) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.style.display = 'block';
}

// Hide message
function hideMessage(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = 'none';
}

// Show success message
function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.style.display = 'block';
}

// Save JWT token to localStorage
function saveToken(token) {
    localStorage.setItem('token', token);
}

// Get JWT token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Remove token on logout
function removeToken() {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
}

// Check if user is already logged in
function isLoggedIn() {
    return getToken() !== null;
}

// Redirect if already logged in
if (isLoggedIn() &&
    (window.location.pathname.includes('index.html') ||
     window.location.pathname.includes('register.html'))) {
    window.location.href = 'vehicles.html';
}

// ─── LOGIN ───────────────────────────────────────────────────

const loginForm = document.getElementById('loginForm');

if (loginForm) {
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // hide previous errors
        hideMessage('errorMsg');

        const email    = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const loginBtn = document.getElementById('loginBtn');

        // basic validation
        if (!email || !password) {
            showError('errorMsg', 'Please fill in all fields');
            return;
        }

        // show loading state
        loginBtn.textContent = 'Signing in...';
        loginBtn.disabled = true;

        try {
            const response = await fetch(`${BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                // save token to localStorage
                saveToken(data.data);
                localStorage.setItem('userEmail', email);

                // redirect to vehicles page
                window.location.href = 'vehicles.html';

            } else {
                // show error from backend
                showError('errorMsg', data.message || 'Login failed');
            }

        } catch (error) {
            showError('errorMsg', 'Cannot connect to server. Please try again.');
        } finally {
            loginBtn.textContent = 'Sign In';
            loginBtn.disabled = false;
        }
    });
}

// ─── REGISTER ────────────────────────────────────────────────

const registerForm = document.getElementById('registerForm');

if (registerForm) {
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // hide previous messages
        hideMessage('errorMsg');
        hideMessage('successMsg');

        const name            = document.getElementById('name').value.trim();
        const email           = document.getElementById('email').value.trim();
        const password        = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const registerBtn     = document.getElementById('registerBtn');

        // frontend validation
        if (!name || !email || !password || !confirmPassword) {
            showError('errorMsg', 'Please fill in all fields');
            return;
        }

        if (password.length < 6) {
            showError('errorMsg', 'Password must be at least 6 characters');
            return;
        }

        if (password !== confirmPassword) {
            showError('errorMsg', 'Passwords do not match');
            return;
        }

        // show loading state
        registerBtn.textContent = 'Creating account...';
        registerBtn.disabled = true;

        try {
            const response = await fetch(`${BASE_URL}/api/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, email, password })
            });

            const data = await response.json();

            if (response.ok) {
                // save token
                saveToken(data.data);
                localStorage.setItem('userEmail', email);

                // show success then redirect
                showSuccess('successMsg', 'Account created! Redirecting...');

                setTimeout(() => {
                    window.location.href = 'vehicles.html';
                }, 1500);

            } else {
                showError('errorMsg', data.message || 'Registration failed');
            }

        } catch (error) {
            showError('errorMsg', 'Cannot connect to server. Please try again.');
        } finally {
            registerBtn.textContent = 'Create Account';
            registerBtn.disabled = false;
        }
    });
}