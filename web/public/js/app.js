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
  
  // Bulk Request Cart Elements
  const labActivityTitle = document.getElementById('lab-activity-title');
  const requestCartContainer = document.getElementById('request-cart-container');
  const requestCartItems = document.getElementById('request-cart-items');
  const clearCartBtn = document.getElementById('clear-cart-btn');
  const submitBulkBtn = document.getElementById('submit-bulk-request-btn');

  // Modal Elements
  const membersModal = document.getElementById('members-modal');
  const closeModalBtn = document.getElementById('close-modal-btn');
  const addMemberForm = document.getElementById('add-member-form');
  const newMemberName = document.getElementById('new-member-name');
  const membersCountText = document.getElementById('members-count');
  const membersListContainer = document.getElementById('members-list');

  // Receipt Modal Elements
  const receiptModal = document.getElementById('receipt-modal');
  const closeReceiptModalBtn = document.getElementById('close-receipt-modal-btn');
  const printReceiptBtn = document.getElementById('print-receipt-btn');
  const receiptItemsBody = document.getElementById('receipt-items-body');
  const receiptGroupName = document.getElementById('receipt-group-name');
  const receiptDate = document.getElementById('receipt-date');
  const receiptActivityTitle = document.getElementById('receipt-activity-title');
  const receiptQuickSelect = document.getElementById('receipt-quick-select');

  // Dashboard Stats Elements
  const statPendingCount = document.getElementById('stat-pending-count');
  const statBorrowedCount = document.getElementById('stat-borrowed-count');
  const statInventoryHealth = document.getElementById('stat-inventory-health');
  const statUtilBar = document.getElementById('util-bar-fill');
  const activityTimeline = document.getElementById('activity-timeline');

  // Application State
  let currentGroup = null;
  let requestCart = [];
  let currentGroupHistoryGroups = {};


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
    groupWelcomeBadge.textContent = groupData.groupName;
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
    
    let svgPath = '';
    let iconClass = 'icon-sm';

    if (type === 'success') {
      svgPath = '<path d="M20 6 9 17l-5-5"/>';
      iconClass += ' icon-primary';
    } else if (type === 'error') {
      svgPath = '<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>';
      iconClass += ' icon-danger';
    } else if (type === 'info') {
      svgPath = '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>';
      iconClass += ' icon-accent';
    } else {
      svgPath = '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>';
    }

    toast.innerHTML = `
      <svg class="svg-icon ${iconClass}" viewBox="0 0 24 24">${svgPath}</svg>
      <span></span>
    `;
    toast.querySelector('span').textContent = message;

    container.appendChild(toast);

    // Auto-remove after 4 seconds
    setTimeout(() => {
      const removeToast = () => toast.remove();
      toast.addEventListener('transitionend', removeToast, { once: true });
      toast.classList.add('toast-exit');
      setTimeout(removeToast, 350);
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
    loginBtn.classList.add('is-loading');
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
      loginBtn.classList.remove('is-loading');
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

      // Populate Request History (Grouped by Lab Activity)
      if (data.length > 0) {
        requestsHistoryList.innerHTML = '';
        
        // Grouping logic
        const groups = {};
        data.forEach(r => {
          const act = r.lab_activity || 'General Laboratory Activity';
          if (!groups[act]) groups[act] = [];
          groups[act].push(r);
        });
        currentGroupHistoryGroups = groups;

        // Populate Quick Select
        receiptQuickSelect.innerHTML = '<option value="">Quick Select Activity...</option>';
        Object.keys(groups).sort().forEach(actTitle => {
          const opt = document.createElement('option');
          opt.value = actTitle;
          opt.textContent = actTitle;
          receiptQuickSelect.appendChild(opt);
        });

        Object.keys(groups).forEach(actTitle => {
          const items = groups[actTitle];
          
          // Header for the group
          const header = document.createElement('div');
          header.className = 'history-group-header';
          header.innerHTML = `
            <h4>${escapeHtml(actTitle)}</h4>
            <button class="btn-receipt-small" data-activity="${escapeHtml(actTitle)}">
              View Receipt
            </button>
          `;
          requestsHistoryList.appendChild(header);

          items.forEach(r => {
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
        });

        // Update Stats & Timeline
        updateDashboardStats(data);
        renderTimeline(data);

        // Bind Receipt Buttons
        document.querySelectorAll('.btn-receipt-small').forEach(btn => {
          btn.addEventListener('click', (e) => {
            const actTitle = e.target.getAttribute('data-activity');
            const itemsToPopulate = groups[actTitle];
            openReceipt(actTitle, itemsToPopulate);
          });
        });

      } else {
        requestsHistoryList.innerHTML = '<p class="empty-text">No requests submitted yet.</p>';
      }
    } catch (err) {
      console.error('Fetch requests history error:', err);
    }
  }

  function openReceipt(title, items) {
    receiptActivityTitle.textContent = title;
    receiptGroupName.textContent = currentGroup ? currentGroup.groupName : 'Student Group';
    receiptDate.textContent = new Date().toLocaleDateString(undefined, {
      year: 'numeric', month: 'long', day: 'numeric'
    });

    receiptItemsBody.innerHTML = '';
    items.forEach(item => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${escapeHtml(item.item_name)}</td>
        <td style="text-align: center;">${item.qty}</td>
      `;
      receiptItemsBody.appendChild(tr);
    });

    receiptModal.classList.add('active');
  }

  closeReceiptModalBtn.addEventListener('click', () => receiptModal.classList.remove('active'));
  
  // Quick Select Activity Event
  receiptQuickSelect.addEventListener('change', (e) => {
    const actTitle = e.target.value;
    if (actTitle && currentGroupHistoryGroups && currentGroupHistoryGroups[actTitle]) {
      openReceipt(actTitle, currentGroupHistoryGroups[actTitle]);
      receiptQuickSelect.value = ''; // Reset select
    }
  });

  printReceiptBtn.addEventListener('click', () => {
    const originalTitle = document.title;
    const activityName = receiptActivityTitle.textContent.replace(/[^a-z0-9]/gi, '_').toLowerCase();
    document.title = `Receipt_${activityName}`;
    
    window.print();
    
    // Restore title after a short delay (printing is usually synchronous in blocking the UI, 
    // but some browsers might need a moment)
    setTimeout(() => {
      document.title = originalTitle;
    }, 1000);
  });

  // 6. Stats & Timeline Logic
  function updateDashboardStats(data) {
    const pending = data.filter(r => r.status === 'Pending').length;
    const borrowed = data.filter(r => r.status === 'Approved').length;
    const total = data.length;

    statPendingCount.textContent = pending;
    statBorrowedCount.textContent = borrowed;
    
    // Utilization Bar logic (Example: Pending vs Total)
    const utilProgress = total > 0 ? Math.min(100, (pending / 10) * 100) : 0;
    statUtilBar.style.width = `${utilProgress}%`;

    // Health logic (Example: based on rejection rate)
    const rejected = data.filter(r => r.status === 'Rejected').length;
    const health = (rejected / (total || 1)) > 0.3 ? 'Warning' : 'Good';
    statInventoryHealth.textContent = health;
    statInventoryHealth.style.color = health === 'Good' ? 'var(--green)' : 'var(--amber)';
  }

  function renderTimeline(data) {
    if (!activityTimeline) return;
    
    // Take the 6 most recent unique status changes or requests
    const recent = data.slice(0, 6);
    
    if (recent.length === 0) {
      // Keep existing empty state
      return;
    }

    activityTimeline.innerHTML = '';
    recent.forEach(r => {
      const event = document.createElement('div');
      event.className = 'timeline-event';
      
      let dotClass = 'active';
      if (r.status === 'Approved') dotClass = 'success';
      if (r.status === 'Rejected') dotClass = 'warning';
      
      const timeAgo = formatTimeAgo(new Date(r.created_at));

      event.innerHTML = `
        <div class="timeline-dot ${dotClass}"></div>
        <div class="timeline-content">
          <span class="timeline-text">${escapeHtml(r.item_name)}: ${r.status}</span>
          <span class="timeline-time">${timeAgo}</span>
        </div>
      `;
      activityTimeline.appendChild(event);
    });
  }

  function formatTimeAgo(date) {
    const seconds = Math.floor((new Date() - date) / 1000);
    if (seconds < 60) return 'just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    return date.toLocaleDateString();
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
            <span class="status-badge Pending">In Queue</span>
          `;
          othersPendingList.appendChild(div);
        });
      } else {
        othersPendingList.innerHTML = `
          <div class="empty-state">
            <svg class="svg-illustration" viewBox="0 0 64 64">
              <rect x="12" y="10" width="40" height="44" rx="2" fill="none" stroke="var(--line-strong)" stroke-width="3"/>
              <path d="M22 24h20M22 32h20M22 40h10" stroke="var(--line-strong)" stroke-width="3" stroke-linecap="round"/>
            </svg>
            <p>Borrowing queue is clear.<br><span style="font-size: 0.75rem; opacity: 0.7;">No pending requests from other groups.</span></p>
          </div>
        `;
      }
    } catch (err) {
      console.error('Fetch other pending requests error:', err);
    }
  }

  // 7. Bulk Request Cart Logic
  borrowForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const aid = apparatusSelect.value;
    const qty = parseInt(borrowQty.value);
    
    if (!aid || isNaN(qty) || qty < 1) return;

    // Get item name for the cart UI
    const selectedOption = apparatusSelect.options[apparatusSelect.selectedIndex];
    const appName = selectedOption.textContent.split(' (')[0];

    // Check if already in cart
    const existing = requestCart.find(item => item.apparatus_id === aid);
    if (existing) {
      existing.qty += qty;
    } else {
      requestCart.push({ apparatus_id: aid, qty, name: appName });
    }

    renderCart();
    borrowForm.reset();
  });

  function renderCart() {
    if (requestCart.length === 0) {
      requestCartContainer.classList.add('hidden');
      return;
    }

    requestCartContainer.classList.remove('hidden');
    requestCartItems.innerHTML = '';

    requestCart.forEach((item, index) => {
      const div = document.createElement('div');
      div.className = 'cart-item';
      div.innerHTML = `
        <div class="cart-item-info">
          <span class="cart-item-name">${escapeHtml(item.name)}</span>
          <span class="cart-item-qty">Quantity: ${item.qty}</span>
        </div>
        <button class="remove-item-btn" data-index="${index}">
          <svg class="svg-icon icon-sm" viewBox="0 0 24 24">
            <path d="M18 6 6 18M6 6l12 12"/>
          </svg>
        </button>
      `;
      requestCartItems.appendChild(div);
    });

    // Bind remove buttons
    document.querySelectorAll('.remove-item-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const index = parseInt(e.currentTarget.getAttribute('data-index'));
        requestCart.splice(index, 1);
        renderCart();
      });
    });
  }

  clearCartBtn.addEventListener('click', () => {
    requestCart = [];
    renderCart();
  });

  submitBulkBtn.addEventListener('click', async () => {
    if (requestCart.length === 0) return;
    
    submitBulkBtn.disabled = true;
    const activityTitle = labActivityTitle.value.trim();

    try {
      const res = await fetch('/api/requests/borrow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          lab_activity: activityTitle,
          items: requestCart 
        })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast(data.message, 'success');
        requestCart = [];
        labActivityTitle.value = '';
        renderCart();
        loadDashboardData();
      } else {
        showToast(data.error || 'Failed to submit batch requests', 'error');
      }
    } catch (err) {
      showToast('Network error, please try again.', 'error');
    } finally {
      submitBulkBtn.disabled = false;
    }
  });

  // 8. Submit Request for Unlisted Apparatus (Updated for lab_activity)
  unlistedForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = unlistedForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    const apparatus_name = unlistedName.value;
    const reason = unlistedReason.value;
    const lab_activity = labActivityTitle.value.trim(); // Shared field

    try {
      const res = await fetch('/api/requests/unlisted', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ apparatus_name, reason, lab_activity })
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
    unlistedToggle.querySelector('span').textContent = isHidden ? 'Request Unlisted Apparatus' : 'Close Unlisted Apparatus';
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
            <button class="btn-remove-member" data-id="${m.member_id}">Remove</button>
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
