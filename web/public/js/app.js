// ==========================================================================
// ChemLab System - Frontend Client Application JavaScript
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
  // DOM View Elements
  const loginView = document.getElementById('login-view');
  const dashboardView = document.getElementById('dashboard-view');
  
  // Login Form Elements
  const loginForm = document.getElementById('login-form');
  const loginBtn = document.getElementById('login-btn');
  
  // Dashboard Action Elements
  const logoutBtn = document.getElementById('logout-btn');
  const toggleMembersBtn = document.getElementById('toggle-members-btn');
  const groupWelcomeBadge = document.getElementById('group-welcome-badge');
  const groupNameTitle = document.getElementById('group-name-title');
  
  // Forms & Interactions
  const borrowForm = document.getElementById('borrow-form');
  const apparatusSelect = document.getElementById('apparatus-select');
  const borrowQty = document.getElementById('borrow-qty');
  const unlistedToggle = document.getElementById('unlisted-toggle');
  const unlistedForm = document.getElementById('unlisted-form');
  const unlistedName = document.getElementById('unlisted-name');
  const unlistedReason = document.getElementById('unlisted-reason');
  
  // Lists Containers
  const borrowingCard = document.getElementById('borrowing-card');
  const borrowingList = document.getElementById('borrowing-list');
  const othersPendingList = document.getElementById('others-pending-list');
  const requestsHistoryList = document.getElementById('requests-history-list');
  
  // Modal Elements
  const membersModal = document.getElementById('members-modal');
  const closeModalBtn = document.getElementById('close-modal-btn');
  const addMemberForm = document.getElementById('add-member-form');
  const newMemberName = document.getElementById('new-member-name');
  const membersCountText = document.getElementById('members-count');
  const membersListContainer = document.getElementById('members-list');

  // Application State
  let currentGroup = null;

  // Initialize App: check authentication
  checkAuth();

  // ==========================================
  // VIEW TRANSITIONS & STATE MANAGEMENT
  // ==========================================

  function showView(view) {
    // Hide all views first
    document.querySelectorAll('.view-container').forEach(v => {
      v.classList.remove('active');
    });
    // Activate targeted view
    view.classList.add('active');
  }

  function showDashboard(groupData) {
    currentGroup = groupData;
    groupWelcomeBadge.textContent = `👥 ${groupData.groupName}`;
    groupNameTitle.textContent = groupData.groupName;
    
    showView(dashboardView);
    
    // Fetch initial dashboard contents
    loadDashboardData();
  }

  function showLogin() {
    currentGroup = null;
    showView(loginView);
    loginForm.reset();
  }

  function loadDashboardData() {
    fetchApparatus();
    fetchRequestsHistory();
    fetchOthersPendingRequests();
    fetchMembers();
  }

  // ==========================================
  // TOAST NOTIFICATIONS
  // ==========================================

  function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = '🔔';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';
    if (type === 'info') icon = 'ℹ️';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    // Auto-remove after 4 seconds
    setTimeout(() => {
      toast.classList.add('toast-exit');
      toast.addEventListener('transitionend', () => toast.remove());
    }, 4000);
  }

  // ==========================================
  // API INTEGRATIONS
  // ==========================================

  // 1. Check Session Auth
  async function checkAuth() {
    try {
      const res = await fetch('/api/auth/me');
      const data = await res.json();
      if (data.loggedIn) {
        showDashboard(data);
      } else {
        showLogin();
      }
    } catch (err) {
      console.error('Auth check failure:', err);
      showLogin();
    }
  }

  // 2. Handle Login Submission
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginBtn.disabled = true;
    const originalText = loginBtn.innerHTML;
    loginBtn.innerHTML = '<span>Verifying...</span>';

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast('Login Successful!', 'success');
        showDashboard(data);
      } else {
        showToast(data.error || 'Authentication failed', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
      console.error(err);
    } finally {
      loginBtn.disabled = false;
      loginBtn.innerHTML = originalText;
    }
  });

  // 3. Handle Logout Action
  logoutBtn.addEventListener('click', async () => {
    try {
      const res = await fetch('/api/auth/logout', { method: 'POST' });
      if (res.ok) {
        showToast('Logged out successfully.', 'info');
        showLogin();
      }
    } catch (err) {
      showToast('Failed to log out.', 'error');
    }
  });

  // 4. Fetch Available Apparatus
  async function fetchApparatus() {
    try {
      const res = await fetch('/api/apparatus');
      const data = await res.json();
      
      // Save previously selected value
      const prevSelected = apparatusSelect.value;

      apparatusSelect.innerHTML = '<option value="">Select an item...</option>';
      data.forEach(item => {
        const option = document.createElement('option');
        option.value = item.apparatus_id;
        option.disabled = item.real_available <= 0;
        option.textContent = `${item.item_name} (${item.real_available} available)`;
        apparatusSelect.appendChild(option);
      });

      // Restore previous selection if valid
      if (prevSelected) {
        apparatusSelect.value = prevSelected;
      }
    } catch (err) {
      console.error('Fetch apparatus error:', err);
    }
  }

  // 5. Fetch Requests & Borrowing History
  async function fetchRequestsHistory() {
    try {
      const res = await fetch('/api/requests/my');
      const data = await res.json();
      
      // Populate Currently Borrowed (Status: Approved)
      const approvedItems = data.filter(r => r.status === 'Approved');
      if (approvedItems.length > 0) {
        borrowingCard.classList.remove('hidden');
        borrowingList.innerHTML = '';
        approvedItems.forEach(b => {
          const div = document.createElement('div');
          div.className = 'borrow-item';
          div.innerHTML = `<strong>${escapeHtml(b.item_name)}</strong> x${b.qty}`;
          borrowingList.appendChild(div);
        });
      } else {
        borrowingCard.classList.add('hidden');
      }

      // Populate Request History
      if (data.length > 0) {
        requestsHistoryList.innerHTML = '';
        data.forEach(r => {
          const div = document.createElement('div');
          div.className = `list-item ${r.status}`;
          
          const date = new Date(r.created_at).toLocaleDateString(undefined, { 
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
          });

          div.innerHTML = `
            <div class="item-info">
              <span class="item-title">${escapeHtml(r.item_name)} x${r.qty}</span>
              <span class="item-subtitle">Requested on ${date}</span>
            </div>
            <span class="status-badge ${r.status}">${r.status}</span>
          `;
          requestsHistoryList.appendChild(div);
        });
      } else {
        requestsHistoryList.innerHTML = '<p class="empty-text">No requests submitted yet.</p>';
      }
    } catch (err) {
      console.error('Fetch requests history error:', err);
    }
  }

  // 6. Fetch Other Groups' Pending Requests
  async function fetchOthersPendingRequests() {
    try {
      const res = await fetch('/api/requests/others-pending');
      const data = await res.json();
      
      // Get all apparatus to match names
      const appRes = await fetch('/api/apparatus');
      const apparatus = await appRes.json();
      const appNamesMap = {};
      apparatus.forEach(item => {
        appNamesMap[item.apparatus_id] = item.item_name;
      });

      const keys = Object.keys(data);
      if (keys.length > 0) {
        othersPendingList.innerHTML = '';
        keys.forEach(aid => {
          const others = data[aid];
          const appName = appNamesMap[aid] || 'Unknown Apparatus';
          
          const div = document.createElement('div');
          div.className = 'list-item Pending';
          
          let listStr = '';
          others.forEach(o => {
            listStr += `${escapeHtml(o.group_name)} — qty: ${o.qty}<br>`;
          });

          div.innerHTML = `
            <div class="item-info">
              <span class="item-title">${escapeHtml(appName)}</span>
              <div class="other-pending-details">${listStr}</div>
            </div>
            <span class="status-badge Pending">Pending</span>
          `;
          othersPendingList.appendChild(div);
        });
      } else {
        othersPendingList.innerHTML = '<p class="empty-text">No pending requests from other groups.</p>';
      }
    } catch (err) {
      console.error('Fetch other pending requests error:', err);
    }
  }

  // 7. Submit Normal Borrow Request
  borrowForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = borrowForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    const apparatus_id = apparatusSelect.value;
    const qty = borrowQty.value;

    try {
      const res = await fetch('/api/requests/borrow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ apparatus_id, qty })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast(data.message, 'success');
        borrowForm.reset();
        loadDashboardData();
      } else {
        showToast(data.error || 'Failed to submit borrow request', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });

  // 8. Submit Request for Unlisted Apparatus
  unlistedForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = unlistedForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    const apparatus_name = unlistedName.value;
    const reason = unlistedReason.value;

    try {
      const res = await fetch('/api/requests/unlisted', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ apparatus_name, reason })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast(data.message, 'success');
        unlistedForm.reset();
        unlistedForm.classList.add('hidden'); // collapse it
      } else {
        showToast(data.error || 'Failed to submit custom request', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });

  // Toggle Collapse on Unlisted Request Form
  unlistedToggle.addEventListener('click', () => {
    const isHidden = unlistedForm.classList.toggle('hidden');
    unlistedToggle.querySelector('span').textContent = isHidden ? '▶ Request Unlisted Apparatus' : '▼ Request Unlisted Apparatus';
  });

  // ==========================================
  // GROUP MEMBERS MANAGEMENT MODAL
  // ==========================================

  // Open modal & load members
  toggleMembersBtn.addEventListener('click', () => {
    membersModal.classList.add('active');
    fetchMembers();
  });

  // Close modal
  closeModalBtn.addEventListener('click', () => {
    membersModal.classList.remove('active');
  });

  // Click outside to close modal
  membersModal.addEventListener('click', (e) => {
    if (e.target === membersModal) {
      membersModal.classList.remove('active');
    }
  });

  // Fetch current group members
  async function fetchMembers() {
    try {
      const res = await fetch('/api/group/members');
      const data = await res.json();
      
      membersCountText.textContent = `Current Members (${data.length})`;
      
      if (data.length > 0) {
        membersListContainer.innerHTML = '';
        data.forEach(m => {
          const div = document.createElement('div');
          div.className = 'member-row';
          div.innerHTML = `
            <span class="member-row-name">${escapeHtml(m.member_name)}</span>
            <button class="btn-remove-member" data-id="${m.member_id}">✕ Remove</button>
          `;
          membersListContainer.appendChild(div);
        });
        
        // Bind remove actions
        document.querySelectorAll('.btn-remove-member').forEach(btn => {
          btn.addEventListener('click', async (e) => {
            const memberId = e.target.getAttribute('data-id');
            const memberName = e.target.parentElement.querySelector('.member-row-name').textContent;
            if (confirm(`Are you sure you want to remove "${memberName}" from the group?`)) {
              await deleteMember(memberId);
            }
          });
        });

      } else {
        membersListContainer.innerHTML = '<p class="empty-text">No members added yet.</p>';
      }
    } catch (err) {
      console.error('Fetch members error:', err);
    }
  }

  // Add a member
  addMemberForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = addMemberForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    const member_name = newMemberName.value;

    try {
      const res = await fetch('/api/group/members', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ member_name })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast(data.message, 'success');
        addMemberForm.reset();
        fetchMembers();
      } else {
        showToast(data.error || 'Failed to add member', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });

  // Delete member API call
  async function deleteMember(id) {
    try {
      const res = await fetch(`/api/group/members/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (res.ok && data.success) {
        showToast(data.message, 'info');
        fetchMembers();
      } else {
        showToast(data.error || 'Failed to remove member', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
    }
  }

  // ==========================================
  // UTILS
  // ==========================================

  function escapeHtml(str) {
    if (!str) return '';
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

});
