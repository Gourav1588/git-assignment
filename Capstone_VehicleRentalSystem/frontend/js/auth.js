/* =========================================================================
   DriveEasy - Identity & Access Management (Toast Validation)
   ========================================================================= */

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

/* =========================================================================
   VALIDATION UTILITIES
   ========================================================================= */

function isValidEmail(email) {
    const trimmed = email.trim();
    const parts = trimmed.split('@');
    if (parts.length !== 2) return false;
    const local = parts[0];
    const domainFull = parts[1];
    if (!/^[a-zA-Z0-9._%+\-]+$/.test(local) || local.length === 0) return false;
    const domainParts = domainFull.split('.');
    if (domainParts.length < 2) return false;
    const tld = domainParts[domainParts.length - 1];
    if (!/^[a-zA-Z]{2,}$/.test(tld)) return false;
    const domainName = domainParts.slice(0, -1).join('.');
    if (!/[a-zA-Z]/.test(domainName)) return false;
    if (!/^[a-zA-Z0-9.\-]+$/.test(domainName)) return false;
    return true;
}

function validatePassword(password) {
    if (password.length < 8) return { valid: false, message: 'Password must be at least 8 characters long.' };
    if (!/[A-Z]/.test(password)) return { valid: false, message: 'Password must contain at least one uppercase letter.' };
    if (!/[a-z]/.test(password)) return { valid: false, message: 'Password must contain at least one lowercase letter.' };
    if (!/[0-9]/.test(password)) return { valid: false, message: 'Password must contain at least one number.' };
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) return { valid: false, message: 'Password must contain at least one special character.' };
    return { valid: true, message: '' };
}

function validateName(name) {
    const trimmed = name.trim();
    if (trimmed.length < 3) return { valid: false, message: 'Name must be at least 3 characters long.' };
    if (/[0-9]/.test(trimmed)) return { valid: false, message: 'Name cannot contain numbers.' };
    if (/[^a-zA-Z\s]/.test(trimmed)) return { valid: false, message: 'Name can only contain letters and spaces.' };
    return { valid: true, message: '' };
}

/* =========================================================================
   AUTHENTICATION WORKFLOWS (LOGIN)
   ========================================================================= */

function validateLoginForm() {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;

    if (!email) {
        showToast('Email address is required.');
        return false;
    }
    if (!isValidEmail(email)) {
        showToast('Please enter a valid email address.');
        return false;
    }
    if (!password) {
        showToast('Password is required.');
        return false;
    }

    return true; // Form is perfect
}

if (loginForm) {
    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        // Run validation. If it fails (returns false), stop submission.
        if (!validateLoginForm()) return;

        const btn = document.getElementById('loginBtn');
        const credentials = {
            email: document.getElementById('loginEmail').value.trim(),
            password: document.getElementById('loginPassword').value
        };

        if (btn) {
            btn.textContent = 'Authenticating...';
            btn.disabled = true;
        }

        try {
            const response = await apiFetch('/auth/login', {
                method: 'POST',
                body: JSON.stringify(credentials)
            });

            if (response.ok) {
                const data = await response.json();
                const token = data.data || data.token || data.jwt;

                if (window.localStorage) {
                    localStorage.setItem('token', token);
                    localStorage.setItem('userEmail', credentials.email);
                }

                showToast('Authentication successful.');
                const role = getRoleFromToken(token);

                setTimeout(() => {
                    if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
                        window.location.href = 'admin.html';
                    } else {
                        window.location.href = 'vehicles.html';
                    }
                }, 1500);

            } else {
                showToast('Invalid email or password.');
                if (btn) {
                    btn.textContent = 'Sign In';
                    btn.disabled = false;
                }
            }
        } catch (error) {
            console.error("Authentication Service Error:", error);
            showToast('Service currently unavailable.');
            if (btn) {
                btn.textContent = 'Sign In';
                btn.disabled = false;
            }
        }
    });
}

/* =========================================================================
   REGISTRATION WORKFLOW
   ========================================================================= */

function validateRegisterForm() {
    const name = document.getElementById('regName').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword')?.value;

    // Check Name
    if (!name) {
        showToast('Full name is required.');
        return false;
    }
    const nameCheck = validateName(name);
    if (!nameCheck.valid) {
        showToast(nameCheck.message);
        return false;
    }

    // Check Email
    if (!email) {
        showToast('Email address is required.');
        return false;
    }
    if (!isValidEmail(email)) {
        showToast('Please enter a valid email address.');
        return false;
    }

    // Check Password
    if (!password) {
        showToast('Password is required.');
        return false;
    }
    const passwordCheck = validatePassword(password);
    if (!passwordCheck.valid) {
        showToast(passwordCheck.message);
        return false;
    }

    // Check Confirm Password
    if (confirmPassword !== undefined) {
        if (!confirmPassword) {
            showToast('Please confirm your password.');
            return false;
        }
        if (confirmPassword !== password) {
            showToast('Passwords do not match.');
            return false;
        }
    }

    return true; // Form is perfect
}

if (registerForm) {
    registerForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        // Run validation. If it fails (returns false), stop submission.
        if (!validateRegisterForm()) return;

        const btn = document.getElementById('regBtn');
        const registrationPayload = {
            name: document.getElementById('regName').value.trim(),
            email: document.getElementById('regEmail').value.trim(),
            password: document.getElementById('regPassword').value
        };

        if (btn) {
            btn.textContent = 'Provisioning...';
            btn.disabled = true;
        }

        try {
            const response = await apiFetch('/auth/register', {
                method: 'POST',
                body: JSON.stringify(registrationPayload)
            });

            if (response.ok) {
                showToast('Account successfully created.');
                setTimeout(() => window.location.href = 'login.html', 1500);
            } else {
                showToast('Registration failed to process.');
                if (btn) {
                    btn.textContent = 'Create Account';
                    btn.disabled = false;
                }
            }
        } catch (error) {
            console.error("Registration Service Error:", error);
            showToast('Service currently unavailable.');
            if (btn) {
                btn.textContent = 'Create Account';
                btn.disabled = false;
            }
        }
    });
}

/* =========================================================================
   SECURITY UTILITIES
   ========================================================================= */

function getRoleFromToken(token) {
    if (!token) return 'USER';

    try {
        const base64 = token.split('.')[1]
            .replace(/-/g, '+')
            .replace(/_/g, '/');

        const payload = JSON.parse(
            decodeURIComponent(
                atob(base64).split('').map(c =>
                    '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
                ).join('')
            )
        );

        const roleData = payload.role || payload.roles || payload.authorities || 'USER';

        if (Array.isArray(roleData)) {
            if (roleData.includes('ADMIN') || roleData.includes('ROLE_ADMIN')) return 'ADMIN';
            if (roleData.some(r => r.authority === 'ADMIN' || r.authority === 'ROLE_ADMIN')) return 'ADMIN';
        }

        if (typeof roleData === 'string') {
            const normalizedRole = roleData.toUpperCase();
            if (normalizedRole.includes('ADMIN')) return 'ADMIN';
        }

        return 'USER';
    } catch (error) {
        console.error("Token decode sequence failure:", error);
        return 'USER';
    }
}

/* =========================================================================
   REVERSE AUTH GUARD
   Immediately redirects logged-in users away from auth pages.
   ========================================================================= */
(function redirectIfLoggedIn() {
    const token = localStorage.getItem('token');

    if (token) {
        const role = getRoleFromToken(token);

        if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
            window.location.replace('admin.html');
        } else {
            window.location.replace('vehicles.html');
        }
    }
})();