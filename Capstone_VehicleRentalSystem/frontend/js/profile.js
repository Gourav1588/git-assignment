/* =========================================================================
   DriveEasy - User Dashboard & Profile Management
   ========================================================================= */

/**
 * 1. INITIALIZATION
 * Executes upon DOM load. Validates the user session and initializes the dashboard.
 */
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');

    // Redirect unauthenticated users to the login portal.
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    // Extract user details from the JWT payload to populate the welcome message.
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('userNameDisplay').textContent = payload.sub.split('@')[0];
    } catch (e) {
        document.getElementById('userNameDisplay').textContent = "User";
    }

    // Initialize dashboard data
    loadUserBookings();
});


/* =========================================================================
   2. DATA FETCHING (API INTEGRATION)
   ========================================================================= */

/**
 * Fetches the authenticated user's booking history from the backend.
 * Requests paginated data and delegates to the rendering function upon success.
 */
async function loadUserBookings() {
    try {
        const response = await apiFetch('/bookings/my?page=0&size=10');

        if (response.ok) {
            const pageData = await response.json();
            const bookings = pageData.content || [];
            renderBookings(bookings);
        } else {
            console.error("Fetch Error:", response.status);
            showToast("Failed to load booking history.");
        }
    } catch (error) {
        console.error("Dashboard Connection Error:", error);
        showToast("Unable to connect to the server.");
    }
}


/* =========================================================================
   3. UI RENDERING
   ========================================================================= */

/**
 * Dynamically generates HTML for the user's booking history.
 * Evaluates temporal business rules to manage UI states (e.g., cancellation locks).
 * @param {Array} bookings - The list of booking objects retrieved from the API.
 */
function renderBookings(bookings) {
    const container = document.getElementById('bookingsContainer');

    if (bookings.length === 0) {
        container.innerHTML = `
            <div style="text-align:center; padding: 40px; color: var(--muted);">
                <p>You haven't made any bookings yet.</p>
                <a href="vehicles.html" class="btn-accent" style="display:inline-block; margin-top:15px; text-decoration:none;">Explore Vehicles</a>
            </div>`;
        return;
    }

    // Capture the current precise time for real-time business logic
    const now = new Date();

    container.innerHTML = bookings.map(b => {
        const statusClass = b.status ? b.status.toLowerCase() : 'pending';

        // Parse exact timestamps for comparison
        const startTime = new Date(b.startTime);
        const isTripStarted = startTime <= now;
        const isAlreadyCancelled = b.status === 'CANCELLED' || b.status === 'COMPLETED';

        let actionButton = '';

        if (!isAlreadyCancelled) {
            if (isTripStarted) {
                actionButton = `<button disabled style="background: transparent; color: #64748b; border: 1px solid #334155; padding: 6px 12px; border-radius: 4px; cursor: not-allowed; margin-top: 10px; font-weight: 600; width: 100%; opacity: 0.7;" title="The rental period has already begun">Locked (Trip Started)</button>`;
            } else {
                actionButton = `<button onclick="cancelBooking(${b.id})" style="background: transparent; color: #dc3545; border: 1px solid #dc3545; padding: 6px 12px; border-radius: 4px; cursor: pointer; margin-top: 10px; font-weight: 600; width: 100%;">Cancel Booking</button>`;
            }
        }

        return `
            <div class="booking-card">
                <div class="booking-details">
                    <div class="booking-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-1.4 3.1A1.5 1.5 0 0 0 2 12v4h3"/>
                            <circle cx="6.5" cy="16.5" r="2.5"/><circle cx="16.5" cy="16.5" r="2.5"/>
                        </svg>
                    </div>
                    <div class="booking-info">
                        <h4>${b.vehicleName}</h4>
                        <p>Booking ID: #BK-${b.id}</p>
                        <div class="dates">${b.startTime} ➔ ${b.endTime}</div>
                    </div>
                </div>
                <div class="booking-status" style="display: flex; flex-direction: column; align-items: flex-end;">
                    <div class="status-badge status-${statusClass}">${b.status}</div>
                    <div class="booking-price" style="margin-bottom: 5px;">₹${b.totalCost.toLocaleString()}</div>
                    ${actionButton}
                </div>
            </div>
        `;
    }).join('');
}


/* =========================================================================
   4. USER ACTIONS & NAVIGATION
   ========================================================================= */

/**
 * Manages tab navigation within the dashboard interface.
 */
function switchTab(tabId) {
    document.querySelectorAll('.dash-link').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));

    document.getElementById('btn-' + tabId).classList.add('active');
    document.getElementById(tabId).classList.add('active');
}

/**
 * Transmits a cancellation request to the backend for a specific booking.
 */
async function cancelBooking(bookingId) {
    if (!confirm("Are you sure you want to cancel this booking?")) return;

    try {
        const response = await apiFetch(`/bookings/${bookingId}/cancel`, { method: 'PUT' });

        if (response.ok) {
            alert("Booking successfully cancelled!");
            loadUserBookings();
        } else {
            const error = await response.json();
            alert(error.message || "Failed to cancel the booking.");
        }
    } catch (err) {
        console.error("Cancel Error:", err);
        alert("Server connection failed.");
    }
}


/* =========================================================================
   5. SESSION MANAGEMENT
   ========================================================================= */

function handleLogout() {
    localStorage.removeItem('token');
    showToast("Logging out...");
    setTimeout(() => window.location.href = 'index.html', 1000);
}

/* =========================================================================
   6. ACCOUNT SETTINGS (PROFILE MANAGEMENT)
   ========================================================================= */

async function updateProfile(event) {
    event.preventDefault();

    const btn = document.getElementById('saveProfileBtn');
    btn.textContent = 'Saving...';
    btn.disabled = true;

    const newName = document.getElementById('settingName').value;
    const newPassword = document.getElementById('settingPassword').value;

    const updatePayload = { name: newName };
    if (newPassword.trim() !== "") {
        updatePayload.password = newPassword;
    }

    try {
        const response = await apiFetch('/users/profile', {
            method: 'PUT',
            body: JSON.stringify(updatePayload)
        });

        if (response.ok) {
            showToast("Profile updated successfully!");
            document.getElementById('settingPassword').value = '';
            document.getElementById('userNameDisplay').textContent = newName.split(' ')[0];
        } else {
            const errorData = await response.json();
            showToast(errorData.message || "Failed to update profile.");
        }
    } catch (error) {
        console.error("Profile Update Error:", error);
        showToast("Network error while saving profile.");
    } finally {
        btn.textContent = 'Save Changes';
        btn.disabled = false;
    }
}