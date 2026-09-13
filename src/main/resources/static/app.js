// State
let currentUser = null;
let userGraphs = [];

// Base URL for API (served from same origin, so empty base path is fine for relative)
const API_BASE = '/workflow-engine';

// Correlation ID Generator (27 digits matching backend Schema: yyyyMMddHHmmssSSS + 10 digits sequence)
function generateCorrelationId() {
    const now = new Date();
    const yyyy = now.getFullYear().toString();
    const MM = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const HH = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    const SSS = String(now.getMilliseconds()).padStart(3, '0');
    const timestamp = yyyy + MM + dd + HH + mm + ss + SSS; // 17 digits
    const randomSeq = String(Math.floor(Math.random() * 10000000000)).padStart(10, '0'); // 10 digits
    return timestamp + randomSeq; // exactly 27 digits
}

// Global fetch wrapper to append correlationId to URL query string and request header
const originalFetch = window.fetch;
window.fetch = function(url, options = {}) {
    if (typeof url === 'string' && (url.startsWith('/workflow-engine') || url.includes('/workflow-engine/'))) {
        const correlationId = generateCorrelationId();
        const sep = url.includes('?') ? '&' : '?';
        url = `${url}${sep}correlationId=${correlationId}`;

        options = options || {};
        options.headers = options.headers || {};
        if (options.headers instanceof Headers) {
            options.headers.set('X-Correlation-ID', correlationId);
        } else {
            options.headers['X-Correlation-ID'] = correlationId;
        }

        console.log(`[DAG-API] ${options.method || 'GET'} ${url} | Correlation-ID: ${correlationId}`);
    }
    return originalFetch(url, options);
};

// DOM Elements
const loginView = document.getElementById('landing-view');
const dashboardView = document.getElementById('dashboard-view');
const graphView = document.getElementById('graph-view');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const authTitle = document.getElementById('auth-title');
const authSubtitle = document.getElementById('auth-subtitle');
const loginError = document.getElementById('login-error');
const loginSuccess = document.getElementById('login-success');
const currentUsernameSpan = document.getElementById('current-username');
const graphsGrid = document.getElementById('graphs-grid');
const graphsLoading = document.getElementById('graphs-loading');
const graphsEmpty = document.getElementById('graphs-empty');
const deleteSelectedBtn = document.getElementById('delete-selected-btn');
const createGraphBtn = document.getElementById('create-graph-btn');
const createAiGraphBtn = document.getElementById('create-ai-graph-btn');

// AI Terminal Elements
const aiChatTerminal = document.getElementById('ai-chat-terminal');
const closeAiTerminalBtn = document.getElementById('close-ai-terminal-btn');
const aiPromptInput = document.getElementById('ai-prompt-input');
const aiGenerateBtn = document.getElementById('ai-generate-btn');
const aiGenerateBtnText = document.getElementById('ai-generate-btn-text');
const aiErrorModal = document.getElementById('ai-error-modal');
const aiErrorMessage = document.getElementById('ai-error-message');
const aiErrorCloseBtn = document.getElementById('ai-error-close-btn');

// Graph View DOM Elements
const backToDashboardBtn = document.getElementById('back-to-dashboard-btn');
const gvGraphName = document.getElementById('gv-graph-name');
const gvGraphId = document.getElementById('gv-graph-id');
const gvNodeCount = document.getElementById('gv-node-count');
const gvEdgeCount = document.getElementById('gv-edge-count');
const gvCostPill = document.getElementById('gv-cost-pill');
const gvCostDimensions = document.getElementById('gv-cost-dimensions');
const zoomInBtn = document.getElementById('zoom-in-btn');
const zoomOutBtn = document.getElementById('zoom-out-btn');
const zoomFitBtn = document.getElementById('zoom-fit-btn');
const resetLayoutBtn = document.getElementById('reset-layout-btn');
const zoomLevelBadge = document.getElementById('zoom-level-badge');
const graphSvg = document.getElementById('graph-svg');
const graphViewLoading = document.getElementById('graph-view-loading');
const viewportContainer = document.getElementById('graph-viewport-container');

// Side Inspector Panel DOM Elements
const graphSidePanel = document.getElementById('graph-side-panel');
const closeSidePanelBtn = document.getElementById('close-side-panel-btn');
const reopenSidePanelBtn = document.getElementById('reopen-side-panel-btn');
const spEmptyState = document.getElementById('sp-empty-state');
const spDetails = document.getElementById('sp-details');
const spTypeBadge = document.getElementById('sp-type-badge');
const spIdBadge = document.getElementById('sp-id-badge');
const spName = document.getElementById('sp-name');
const spDesc = document.getElementById('sp-desc');
const spConnectionSection = document.getElementById('sp-connection-section');
const spConnectionText = document.getElementById('sp-connection-text');
const spCostList = document.getElementById('sp-cost-list');

// Edit Mode DOM Elements
const editModeToggle = document.getElementById('edit-mode-toggle');
const saveGraphBtn = document.getElementById('save-graph-btn');
const spEditSection = document.getElementById('sp-edit-section');
const costCataloguePanel = document.getElementById('cost-catalogue-panel');
const addCostDimBtn = document.getElementById('add-cost-dim-btn');
const globalCostList = document.getElementById('global-cost-list');
const addNodeBtn = document.getElementById('add-node-btn');
const cancelNodeBtn = document.getElementById('cancel-node-btn');
const nodeEmptyPrompt = document.getElementById('node-empty-prompt');
const addEdgeBtn = document.getElementById('add-edge-btn');
const cancelEdgeBtn = document.getElementById('cancel-edge-btn');
const cancelAddEdgeBtn = document.getElementById('cancel-add-edge-btn');
const edgeAddInstruction = document.getElementById('edge-add-instruction');
const edgeEmptyPrompt = document.getElementById('edge-empty-prompt');
const editNodeForm = document.getElementById('edit-node-form');
const editEdgeForm = document.getElementById('edit-edge-form');
const editNodeId = document.getElementById('edit-node-id');
const editNodeName = document.getElementById('edit-node-name');
const editNodeDesc = document.getElementById('edit-node-desc');
const editNodeCosts = document.getElementById('edit-node-costs');
const deleteNodeBtn = document.getElementById('delete-node-btn');
const editEdgeId = document.getElementById('edit-edge-id');
const editEdgeName = document.getElementById('edit-edge-name');
const editEdgeDesc = document.getElementById('edit-edge-desc');
const editEdgeCosts = document.getElementById('edit-edge-costs');
const deleteEdgeBtn = document.getElementById('delete-edge-btn');

// Graph Details Modal DOM Elements
const graphTitleGroup = document.getElementById('graph-title-group');
const graphDetailsModal = document.getElementById('graph-details-modal');
const graphDetailsForm = document.getElementById('graph-details-form');
const modalGraphName = document.getElementById('modal-graph-name');
const modalGraphDesc = document.getElementById('modal-graph-desc');
const cancelGraphDetailsBtn = document.getElementById('cancel-graph-details-btn');
const closeDetailsModalBtn = document.getElementById('close-details-modal-btn');
const saveGraphDetailsBtn = document.getElementById('save-graph-details-btn');

// Add Cost Parameter Modal DOM Elements
const addCostModal = document.getElementById('add-cost-parameter-modal');
const addCostForm = document.getElementById('add-cost-parameter-form');
const newCostParamName = document.getElementById('new-cost-param-name');
const newCostParamDefault = document.getElementById('new-cost-param-default');
const cancelCostModalBtn = document.getElementById('cancel-cost-modal-btn');
const closeCostModalBtn = document.getElementById('close-cost-modal-btn');

// Path Calculation DOM Elements
const calcPathsBtn = document.getElementById('calc-paths-btn');
const spViewTabs = document.getElementById('sp-view-tabs');
const spTabInspector = document.getElementById('sp-tab-inspector');
const spTabPaths = document.getElementById('sp-tab-paths');
const spPathsCountBadge = document.getElementById('sp-paths-count-badge');
const spInspectorContainer = document.getElementById('sp-inspector-container');
const spPathsContainer = document.getElementById('sp-paths-container');
const recalcPathsBtn = document.getElementById('recalc-paths-btn');
const pathsLoading = document.getElementById('paths-loading');
const pathsEmptyPrompt = document.getElementById('paths-empty-prompt');
const pathsPromptCalcBtn = document.getElementById('paths-prompt-calc-btn');
const pathsCardsList = document.getElementById('paths-cards-list');
const spPanelTitle = document.getElementById('sp-panel-title');
const spHeaderIcon = document.getElementById('sp-header-icon');

// Graph Visualization State
let activeSimulation = null;
let activeD3Zoom = null;
let currentSvgSelection = null;
let activeGraphData = null;
let currentGraphId = null;
let zoomLimits = { min: 0.15, max: 4.5 };
let selectedElement = null;

// Graph Edit State
let isEditMode = false;
let hasStructuralChanges = false;
let hasMetadataChanges = false;
let isEdgeAddMode = false;
let edgeSourceNode = null;
let globalCostNames = [];

// Path Calculation State
let cachedPaths = null;
let activeSidePanelTab = 'inspector';


// Initialization
document.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();
    checkExistingSession();
    setupAuthTabs();
});

// Setup Auth Tabs
function setupAuthTabs() {
    tabLogin.addEventListener('click', () => switchToAuthTab('login'));
    tabRegister.addEventListener('click', () => switchToAuthTab('register'));
    document.getElementById('switch-to-register').addEventListener('click', () => switchToAuthTab('register'));
    document.getElementById('switch-to-login').addEventListener('click', () => switchToAuthTab('login'));
}

function switchToAuthTab(tab) {
    loginError.classList.add('hidden');
    loginSuccess.classList.add('hidden');
    
    if (tab === 'login') {
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        authTitle.textContent = 'DAG Engine';
        authSubtitle.textContent = 'Login with your credentials';
    } else {
        tabRegister.classList.add('active');
        tabLogin.classList.remove('active');
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
        authTitle.textContent = 'Create Account';
        authSubtitle.textContent = 'Register a new user in DAG Engine';
    }
    lucide.createIcons();
}

// Event Listeners
loginForm.addEventListener('submit', handleLogin);
registerForm.addEventListener('submit', handleRegister);
document.getElementById('logout-btn').addEventListener('click', handleLogout);
document.getElementById('create-graph-btn').addEventListener('click', createNewGraph);
document.getElementById('empty-create-btn').addEventListener('click', createNewGraph);
document.getElementById('delete-selected-btn').addEventListener('click', deleteSelectedGraphs);

// Graph View Listeners
backToDashboardBtn.addEventListener('click', backToDashboard);
zoomInBtn.addEventListener('click', zoomIn);
zoomOutBtn.addEventListener('click', zoomOut);
zoomFitBtn.addEventListener('click', fitGraphToScreen);
if (resetLayoutBtn) resetLayoutBtn.addEventListener('click', resetGraphLayout);
closeSidePanelBtn.addEventListener('click', closeSidePanel);
reopenSidePanelBtn.addEventListener('click', openSidePanel);

// Edit Mode Listeners
editModeToggle.addEventListener('change', toggleEditMode);
saveGraphBtn.addEventListener('click', saveGraphChanges);
if (addCostDimBtn) addCostDimBtn.addEventListener('click', addGlobalCostDimension);

// Cost Catalogue Expandable Card Listeners
const costCatalogueToggle = document.getElementById('cost-catalogue-toggle');
if (costCatalogueToggle) costCatalogueToggle.addEventListener('click', toggleCostCatalogue);

if (gvCostPill) {
    gvCostPill.addEventListener('click', () => {
        if (costCataloguePanel) {
            costCataloguePanel.classList.remove('hidden');
            costCataloguePanel.classList.remove('collapsed');
        }
    });
}
addNodeBtn.addEventListener('click', handleAddNode);
if (cancelNodeBtn) cancelNodeBtn.addEventListener('click', deselectNode);
addEdgeBtn.addEventListener('click', enterEdgeAddMode);
if (cancelEdgeBtn) cancelEdgeBtn.addEventListener('click', deselectEdge);
cancelAddEdgeBtn.addEventListener('click', exitEdgeAddMode);
deleteNodeBtn.addEventListener('click', handleDeleteNode);
deleteEdgeBtn.addEventListener('click', handleDeleteEdge);

// Input auto-save listeners
editNodeName.addEventListener('input', () => { hasMetadataChanges = true; updateActiveElement(); });
editNodeDesc.addEventListener('input', () => { hasMetadataChanges = true; updateActiveElement(); });
editEdgeName.addEventListener('input', () => { hasMetadataChanges = true; updateActiveElement(); });
editEdgeDesc.addEventListener('input', () => { hasMetadataChanges = true; updateActiveElement(); });

// Graph Details Modal Listeners
if (graphTitleGroup) graphTitleGroup.addEventListener('click', openGraphDetailsModal);
if (cancelGraphDetailsBtn) cancelGraphDetailsBtn.addEventListener('click', closeGraphDetailsModal);
if (closeDetailsModalBtn) closeDetailsModalBtn.addEventListener('click', closeGraphDetailsModal);
if (graphDetailsForm) graphDetailsForm.addEventListener('submit', handleSaveGraphDetails);
if (graphDetailsModal) {
    graphDetailsModal.addEventListener('click', (e) => {
        if (e.target === graphDetailsModal) closeGraphDetailsModal();
    });
}
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && graphDetailsModal && !graphDetailsModal.classList.contains('hidden')) {
        closeGraphDetailsModal();
    }
});

// Add Cost Parameter Triggers & Modal Listeners
document.querySelectorAll('.add-cost-parameter-trigger').forEach(btn => {
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        openAddCostModal();
    });
});
if (cancelCostModalBtn) cancelCostModalBtn.addEventListener('click', closeAddCostModal);
if (closeCostModalBtn) closeCostModalBtn.addEventListener('click', closeAddCostModal);
if (addCostForm) addCostForm.addEventListener('submit', handleAddCostSubmit);
if (addCostModal) {
    addCostModal.addEventListener('click', (e) => {
        if (e.target === addCostModal) closeAddCostModal();
    });
}
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && addCostModal && !addCostModal.classList.contains('hidden')) {
        closeAddCostModal();
    }
});

// Path Calculation Listeners
if (calcPathsBtn) calcPathsBtn.addEventListener('click', () => calculateAndRenderPaths());
if (spTabInspector) spTabInspector.addEventListener('click', () => switchSidePanelTab('inspector'));
if (spTabPaths) spTabPaths.addEventListener('click', () => {
    switchSidePanelTab('paths');
    if (!cachedPaths || cachedPaths.graphId !== currentGraphId) {
        calculateAndRenderPaths();
    }
});
if (recalcPathsBtn) recalcPathsBtn.addEventListener('click', () => calculateAndRenderPaths(true));
if (pathsPromptCalcBtn) pathsPromptCalcBtn.addEventListener('click', () => calculateAndRenderPaths(true));

// Utility: Show Toast
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMsg = document.getElementById('toast-message');
    toastMsg.textContent = message;
    
    // reset classes
    toast.className = 'glass-toast show';
    if (type === 'error') {
        toast.style.borderColor = 'rgba(239, 68, 68, 0.5)';
        toast.style.background = 'rgba(239, 68, 68, 0.1)';
    } else {
        toast.style.borderColor = 'rgba(16, 185, 129, 0.5)';
        toast.style.background = 'rgba(16, 185, 129, 0.1)';
    }

    setTimeout(() => {
        toast.className = 'glass-toast hidden';
    }, 3000);
}

// Authentication
function checkExistingSession() {
    const storedUser = localStorage.getItem('dagUser');
    if (storedUser) {
        currentUser = JSON.parse(storedUser);
        showDashboard();
    }
}

// Login Handler: Only validates credentials, does NOT auto-create
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const submitBtn = document.getElementById('login-btn');

    if (!username || !password) return;

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Validating...';
    lucide.createIcons();
    loginError.classList.add('hidden');
    loginSuccess.classList.add('hidden');

    try {
        const response = await fetch(`${API_BASE}/user/validate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userName: username, userPassword: password })
        });
        
        const data = await response.json();
        
        // If validation succeeds (errorCode === "0")
        if (data.objErrorDetails && data.objErrorDetails.errorCode === "0") {
            currentUser = {
                userId: data.objUserRequest ? data.objUserRequest.userId : null,
                userName: username,
                userPassword: password
            };
            localStorage.setItem('dagUser', JSON.stringify(currentUser));
            showDashboard();
            showToast('Welcome back, ' + currentUser.userName + '!');
            return;
        }

        // If user not available
        if (data.objErrorDetails && data.objErrorDetails.errorCode === "10") {
            throw new Error("User '" + username + "' not found. Please click 'Create User' to register.");
        }

        // Other validation failure
        throw new Error(data.objErrorDetails?.errorMessage || "Invalid username or password.");

    } catch (err) {
        console.error("Login error:", err);
        loginError.textContent = err.message;
        loginError.classList.remove('hidden');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Login';
        lucide.createIcons();
    }
}

// Registration Handler: Dedicated Create User flow
async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    const submitBtn = document.getElementById('register-btn');

    if (!username || !password) return;

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Creating...';
    lucide.createIcons();
    loginError.classList.add('hidden');
    loginSuccess.classList.add('hidden');

    try {
        const response = await fetch(`${API_BASE}/user/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userName: username, userPassword: password })
        });
        
        const data = await response.json();

        // Check if user creation failed because user exists
        if (data.objErrorDetails && data.objErrorDetails.errorCode !== "0") {
            const errMsg = data.objErrorDetails.errorMessage || "";
            if (errMsg.toLowerCase().includes("already exists")) {
                throw new Error("Username '" + username + "' is already taken. Please choose a different name.");
            }
            throw new Error(errMsg || "Could not create user.");
        }

        // Creation succeeded
        const createdUserId = data.objUserRequest ? data.objUserRequest.userId : null;
        currentUser = {
            userId: createdUserId,
            userName: username,
            userPassword: password
        };
        localStorage.setItem('dagUser', JSON.stringify(currentUser));
        showToast("User created successfully!");
        showDashboard();

    } catch (err) {
        console.error("Register error:", err);
        loginError.textContent = err.message;
        loginError.classList.remove('hidden');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Create User';
        lucide.createIcons();
    }
}

function handleLogout() {
    currentUser = null;
    localStorage.removeItem('dagUser');
    if (activeSimulation) {
        activeSimulation.stop();
        activeSimulation = null;
    }
    graphView.classList.add('hidden');
    graphView.classList.remove('active');
    dashboardView.classList.add('hidden');
    dashboardView.classList.remove('active');
    loginView.classList.add('active');
    loginView.classList.remove('hidden');
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
}

// Dashboard
function showDashboard() {
    loginView.classList.remove('active');
    loginView.classList.add('hidden');
    dashboardView.classList.remove('hidden');
    dashboardView.classList.add('active');
    currentUsernameSpan.textContent = currentUser.userName;
    
    fetchUserGraphs();
}

async function fetchUserGraphs() {
    graphsGrid.innerHTML = '';
    graphsEmpty.classList.add('hidden');
    graphsLoading.classList.remove('hidden');

    try {
        // Fetch all graph details using user/get with the fetched userId
        const response = await fetch(`${API_BASE}/user/get`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                userId: currentUser.userId,
                userName: currentUser.userName,
                userPassword: currentUser.userPassword
            })
        });
        
        const data = await response.json();
        userGraphs = data.userGraphList || [];
        
        renderGraphs();
    } catch (err) {
        console.error("Error fetching graphs:", err);
        showToast("Failed to load graphs", "error");
        graphsLoading.classList.add('hidden');
    }
}

function renderGraphs() {
    graphsLoading.classList.add('hidden');
    graphsGrid.innerHTML = '';

    if (userGraphs.length === 0) {
        graphsEmpty.classList.remove('hidden');
        return;
    }

    userGraphs.forEach(graph => {
        const card = document.createElement('div');
        card.className = 'glass-card graph-card';
        card.style.cursor = 'pointer';
        card.innerHTML = `
            <div class="checkbox-container">
                <input type="checkbox" class="graph-checkbox" data-id="${graph.graphId}">
            </div>
            <div class="graph-card-header">
                <h4 class="graph-card-title">${graph.graphName}</h4>
            </div>
            <p class="graph-card-desc">${graph.graphDescription || 'No description provided.'}</p>
            <div class="graph-stats">
                <div class="stat-item">
                    <span class="stat-value">${graph.node ? graph.node.length : 0}</span>
                    <span class="stat-label">Nodes</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${graph.edge ? graph.edge.length : 0}</span>
                    <span class="stat-label">Edges</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value" id="paths-${graph.graphId}">-</span>
                    <span class="stat-label">Paths</span>
                </div>
            </div>
        `;
        graphsGrid.appendChild(card);

        // Click card to open Graph View (unless clicking checkbox)
        card.addEventListener('click', (e) => {
            if (e.target.closest('.checkbox-container') || e.target.type === 'checkbox') {
                return;
            }
            openGraphView(graph.graphId);
        });

        // Fetch paths asynchronously to update paths count
        fetchGraphPaths(graph.graphId);
    });

    // Attach listeners for checkboxes
    document.querySelectorAll('.graph-checkbox').forEach(cb => {
        cb.addEventListener('click', (e) => {
            e.stopPropagation();
        });
        cb.addEventListener('change', (e) => {
            const card = e.target.closest('.graph-card');
            if (e.target.checked) card.classList.add('selected');
            else card.classList.remove('selected');
            
            const anyChecked = document.querySelectorAll('.graph-checkbox:checked').length > 0;
            deleteSelectedBtn.disabled = !anyChecked;
        });
    });
}

async function fetchGraphPaths(graphId) {
    try {
        const response = await fetch(`${API_BASE}/graphs/${graphId}/paths`);
        const data = await response.json();
        const pathsSpan = document.getElementById(`paths-${graphId}`);
        if (pathsSpan) {
            pathsSpan.textContent = data.paths ? data.paths.length : (data.totalPaths || 0);
        }
    } catch (err) {
        console.error(`Failed to fetch paths for graph ${graphId}`, err);
    }
}

// Graph Operations
async function createNewGraph() {
    const btn1 = document.getElementById('create-graph-btn');
    const btn2 = document.getElementById('empty-create-btn');
    const ogHtml = btn1.innerHTML;
    
    btn1.disabled = true;
    btn2.disabled = true;
    btn1.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Creating...';
    lucide.createIcons();

    const timestamp = Date.now();
    const newGraphRequest = {
        graphName: `New Graph ${timestamp}`,
        graphDescription: 'A basic singleton graph created from UI',
        userId: currentUser.userId,
        startNodeId: 1,
        costNames: ['time'],
        node: [
            { id: 1, name: 'Start Node', description: 'Starting point', nodeCost: { costVector: { time: 0 } } },
            { id: 2, name: 'End Node', description: 'Ending point', nodeCost: { costVector: { time: 0 } } }
        ],
        edge: [
            { id: 1, sourceNodeId: 1, targetNodeId: 2, edgeName: 'Main Edge', edgeDescription: 'Direct path', edgeCost: { costVector: { time: 10 } } }
        ]
    };

    try {
        const response = await fetch(`${API_BASE}/graphs/upload`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newGraphRequest)
        });
        
        const data = await response.json();
        
        if (data.objErrorDetails && (data.objErrorDetails.errorCode === "0" || data.objErrorDetails.errorCode === "200")) {
            showToast('Graph created successfully!');
            fetchUserGraphs();
        } else {
            throw new Error(data.objErrorDetails?.errorMessage || "Failed to create graph");
        }
    } catch (err) {
        console.error(err);
        showToast(err.message, 'error');
    } finally {
        btn1.disabled = false;
        btn2.disabled = false;
        btn1.innerHTML = ogHtml;
        lucide.createIcons();
    }
}

async function deleteSelectedGraphs() {
    const checkedBoxes = Array.from(document.querySelectorAll('.graph-checkbox:checked'));
    if (checkedBoxes.length === 0) return;

    if (!confirm(`Are you sure you want to delete ${checkedBoxes.length} graph(s)?`)) return;

    deleteSelectedBtn.disabled = true;
    deleteSelectedBtn.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Deleting...';
    lucide.createIcons();

    let successCount = 0;
    let errors = [];

    for (let cb of checkedBoxes) {
        const graphIdStr = cb.getAttribute('data-id');
        const graphId = parseInt(graphIdStr);

        if (isNaN(graphId)) {
            console.error("Invalid graphId for deletion:", graphIdStr);
            errors.push("Invalid graph ID: " + graphIdStr);
            continue;
        }

        try {
            // Step 1: Close the graph first to unload from memory
            try {
                const closeRes = await fetch(`${API_BASE}/graphs/close`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ graphId: graphId, userId: currentUser ? currentUser.userId : null })
                });
                const closeData = await closeRes.json();
                console.log(`Close response for graph ${graphId}:`, closeData);
            } catch (closeErr) {
                console.warn(`Close call failed for graph ${graphId}, proceeding with delete:`, closeErr);
            }

            // Step 2: Delete the graph from DB and disk
            const deleteRes = await fetch(`${API_BASE}/graphs/delete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ graphId: graphId, userId: currentUser ? currentUser.userId : null })
            });

            const deleteData = await deleteRes.json();

            if (deleteData.objErrorDetails && (deleteData.objErrorDetails.errorCode === "0" || deleteData.objErrorDetails.errorCode === "200")) {
                successCount++;
            } else {
                const msg = deleteData.objErrorDetails?.errorMessage || `Failed to delete graph ${graphId}`;
                errors.push(msg);
                console.error(`Delete failed for graph ${graphId}:`, deleteData);
            }
        } catch (err) {
            console.error(`Failed to delete graph ${graphId}:`, err);
            errors.push(err.message);
        }
    }

    if (successCount > 0) {
        showToast(`Successfully deleted ${successCount} graph(s)`);
    }

    if (errors.length > 0) {
        showToast(`Error: ${errors.join(', ')}`, 'error');
    }

    deleteSelectedBtn.disabled = true;
    deleteSelectedBtn.innerHTML = '<i data-lucide="trash-2"></i> Delete Selected';
    lucide.createIcons();
    
    // Refresh the list of graphs
    fetchUserGraphs();
}

// ==========================================================================
// Graph View Engine
// ==========================================================================

function closeSidePanel() {
    graphSidePanel.classList.add('collapsed');
    reopenSidePanelBtn.classList.remove('hidden');
    lucide.createIcons();
}

function openSidePanel() {
    graphSidePanel.classList.remove('collapsed');
    reopenSidePanelBtn.classList.add('hidden');
    lucide.createIcons();
}

async function openGraphView(graphId) {
    if (!graphId) return;
    currentGraphId = Number(graphId);

    // Switch views
    dashboardView.classList.add('hidden');
    dashboardView.classList.remove('active');
    graphView.classList.remove('hidden');
    graphView.classList.add('active');
    graphViewLoading.classList.remove('hidden');

    // Reset Edit Mode toggle to false upon opening a graph
    if (editModeToggle) editModeToggle.checked = false;
    isEditMode = false;

    // Reset Side Panel State
    openSidePanel();
    spEmptyState.classList.remove('hidden');
    spDetails.classList.add('hidden');
    selectedElement = null;

    // Reset Path Calculation State
    cachedPaths = null;
    if (spPathsCountBadge) spPathsCountBadge.textContent = '0';
    if (pathsCardsList) pathsCardsList.innerHTML = '';
    if (pathsEmptyPrompt) {
        pathsEmptyPrompt.classList.remove('hidden');
        const promptP = pathsEmptyPrompt.querySelector('p');
        if (promptP) promptP.textContent = 'No paths calculated yet.';
    }
    if (pathsLoading) pathsLoading.classList.add('hidden');
    switchSidePanelTab('inspector');
    lucide.createIcons();

    try {
        const response = await fetch(`${API_BASE}/graphs/get`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ graphId: Number(graphId) })
        });
        const data = await response.json();

        if (data.objErrorDetails && data.objErrorDetails.errorCode !== "0") {
            throw new Error(data.objErrorDetails.errorMessage || "Failed to load graph details");
        }

        const graphUploadRequest = data.objGraphUploadRequest || {};
        graphUploadRequest.graphId = Number(graphId);
        renderGraphVisualization(graphUploadRequest, data, false);
    } catch (err) {
        console.error("Error opening graph view:", err);
        showToast(err.message || "Failed to load graph", "error");
        backToDashboard();
    } finally {
        graphViewLoading.classList.add('hidden');
    }
}

function backToDashboard() {
    if (activeSimulation) {
        activeSimulation.stop();
        activeSimulation = null;
    }
    clearPathHighlightOnCanvas();
    selectedElement = null;
    if (editModeToggle) editModeToggle.checked = false;
    isEditMode = false;
    graphView.classList.add('hidden');
    graphView.classList.remove('active');
    dashboardView.classList.remove('hidden');
    dashboardView.classList.add('active');
    lucide.createIcons();

    // Immediately refresh graphs on dashboard to update node/edge counts
    fetchUserGraphs();
}

function renderGraphVisualization(graphUploadRequest, graphResponse, preserveEditState = false) {
    // 1. Update Header Metadata
    const graphId = Number(graphUploadRequest.graphId || (graphResponse && graphResponse.graphId) || currentGraphId);
    if (graphId) currentGraphId = graphId;

    const graphName = graphUploadRequest.graphName || (graphResponse && graphResponse.graphName) || 'Unnamed Graph';
    const graphDescription = graphUploadRequest.graphDescription || (graphResponse && graphResponse.graphDescription) || '';
    const rawNodes = graphUploadRequest.nodes || graphUploadRequest.node || [];
    const rawEdges = graphUploadRequest.edges || graphUploadRequest.edge || [];
    
    // Discover all active cost dimensions from request or elements
    let costNames = graphUploadRequest.costNames || [];
    if (!costNames || costNames.length === 0) {
        const discovered = new Set();
        rawNodes.forEach(n => {
            const cv = (n.nodeCost && n.nodeCost.costVector) ? n.nodeCost.costVector : (n.cost || {});
            Object.keys(cv).forEach(k => discovered.add(k));
        });
        rawEdges.forEach(e => {
            const cv = (e.edgeCost && e.edgeCost.costVector) ? e.edgeCost.costVector : (e.cost || {});
            Object.keys(cv).forEach(k => discovered.add(k));
        });
        costNames = Array.from(discovered);
    }
    globalCostNames = costNames.length > 0 ? [...costNames] : [...(globalCostNames || [])];

    // Respect user's Edit Mode toggle switch state
    isEditMode = editModeToggle ? editModeToggle.checked : false;
    renderGlobalCostCatalogue();

    if (isEditMode) {
        saveGraphBtn.classList.remove("hidden");
        costCataloguePanel.classList.remove("hidden");
        costCataloguePanel.classList.remove("collapsed");
        spEditSection.classList.remove("hidden");
        spDetails.classList.add("hidden");
        spEmptyState.classList.add("hidden");
        if (calcPathsBtn) calcPathsBtn.classList.add("hidden");
        if (spViewTabs) spViewTabs.classList.add("hidden");
        switchSidePanelTab('inspector');
    } else {
        saveGraphBtn.classList.add("hidden");
        spEditSection.classList.add("hidden");
        if (calcPathsBtn) calcPathsBtn.classList.remove("hidden");
        if (spViewTabs) spViewTabs.classList.remove("hidden");
        // In view mode, show cost catalogue card if cost dimensions exist
        if (costNames.length > 0) {
            costCataloguePanel.classList.remove("hidden");
        } else {
            costCataloguePanel.classList.add("hidden");
        }
    }

    gvGraphName.textContent = graphName;
    gvGraphId.textContent = `ID: ${graphId || '-'}`;
    gvNodeCount.textContent = rawNodes.length;
    gvEdgeCount.textContent = rawEdges.length;

    if (costNames.length > 0) {
        gvCostDimensions.textContent = `${costNames.length} Parameter${costNames.length > 1 ? 's' : ''}`;
        gvCostPill.classList.remove('hidden');
    } else {
        gvCostPill.classList.add('hidden');
    }

    // 2. Parse Graph DTO to UI Graph Model
    const nodes = rawNodes.map(n => ({
        id: n.id,
        name: n.name || `Node ${n.id}`,
        description: n.description || '',
        cost: (n.nodeCost && n.nodeCost.costVector) ? { ...n.nodeCost.costVector } : { ...(n.cost || {}) }
    }));

    const nodeMap = new Map(nodes.map(n => [n.id, n]));

    const edges = rawEdges
        .map(e => {
            const sId = (typeof e.source === 'object' && e.source !== null) ? e.source.id : (e.sourceNodeId !== undefined ? e.sourceNodeId : e.source);
            const tId = (typeof e.target === 'object' && e.target !== null) ? e.target.id : (e.targetNodeId !== undefined ? e.targetNodeId : e.target);
            return {
                id: e.id,
                source: sId,
                target: tId,
                name: e.edgeName || e.name || `Edge ${e.id || ''}`,
                description: e.edgeDescription || e.description || '',
                cost: (e.edgeCost && e.edgeCost.costVector) ? { ...e.edgeCost.costVector } : { ...(e.cost || {}) }
            };
        })
        .filter(e => nodeMap.has(e.source) && nodeMap.has(e.target));

    activeGraphData = {
        ...graphUploadRequest,
        graphId: graphId,
        graphName: graphName,
        graphDescription: graphDescription,
        userId: graphUploadRequest.userId || (currentUser ? currentUser.userId : null),
        startNodeId: graphUploadRequest.startNodeId || (nodes.length > 0 ? nodes[0].id : 1),
        nodes: nodes,
        edges: edges,
        costNames: globalCostNames
    };

    // 3. Setup SVG Canvas & Dimensions
    const width = viewportContainer.clientWidth || window.innerWidth;
    const height = viewportContainer.clientHeight || (window.innerHeight - 72);
    const centerX = width / 2;
    const centerY = height / 2;

    const svg = d3.select(graphSvg);
    svg.selectAll('*').remove();
    svg.attr('viewBox', `0 0 ${width} ${height}`);

    // Click on canvas background deselects items
    svg.on('click', (event) => {
        if (event.target.tagName === 'svg' || event.target.id === 'bg-grid-rect') {
            clearSelection();
        }
    });

    // 4. SVG Definitions (White Arrow Markers & Grid Pattern)
    const defs = svg.append('defs');

    // Dot grid background
    defs.append('pattern')
        .attr('id', 'dot-grid')
        .attr('width', 28)
        .attr('height', 28)
        .attr('patternUnits', 'userSpaceOnUse')
        .append('circle')
        .attr('cx', 2)
        .attr('cy', 2)
        .attr('r', 1.2)
        .attr('fill', 'rgba(255, 255, 255, 0.08)');

    // Arrow marker normal (White)
    defs.append('marker')
        .attr('id', 'arrow')
        .attr('viewBox', '0 -5 10 10')
        .attr('refX', 30) // Positioned at edge of circle (radius 24)
        .attr('refY', 0)
        .attr('markerWidth', 7.5)
        .attr('markerHeight', 7.5)
        .attr('orient', 'auto')
        .append('path')
        .attr('d', 'M0,-4L8,0L0,4Z')
        .attr('fill', 'rgba(255, 255, 255, 0.85)');

    // Arrow marker hover (Bright White with full opacity)
    defs.append('marker')
        .attr('id', 'arrow-hover')
        .attr('viewBox', '0 -5 10 10')
        .attr('refX', 30)
        .attr('refY', 0)
        .attr('markerWidth', 8.5)
        .attr('markerHeight', 8.5)
        .attr('orient', 'auto')
        .append('path')
        .attr('d', 'M0,-4L8,0L0,4Z')
        .attr('fill', '#ffffff');

    // Grid Background Rectangle
    svg.append('rect')
        .attr('id', 'bg-grid-rect')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('fill', 'url(#dot-grid)');

    // Main Zoomable Container
    const g = svg.append('g').attr('class', 'graph-root-g');
    const edgeLayer = g.append('g').attr('class', 'edge-layer');
    const nodeLayer = g.append('g').attr('class', 'node-layer');

    // 5. Anti-Crowding Topological Layering & Initial Coordinate Calculation
    const inDegree = new Map();
    nodes.forEach(n => inDegree.set(n.id, 0));
    edges.forEach(e => {
        inDegree.set(e.target, (inDegree.get(e.target) || 0) + 1);
    });

    const rank = new Map();
    nodes.forEach(n => {
        if (inDegree.get(n.id) === 0) rank.set(n.id, 0);
    });
    if (rank.size === 0 && nodes.length > 0) rank.set(nodes[0].id, 0);

    // Iterative longest path relaxation
    for (let i = 0; i < nodes.length; i++) {
        let changed = false;
        edges.forEach(e => {
            const uRank = rank.get(e.source);
            if (uRank !== undefined) {
                const vRank = rank.get(e.target);
                if (vRank === undefined || uRank + 1 > vRank) {
                    rank.set(e.target, uRank + 1);
                    changed = true;
                }
            }
        });
        if (!changed) break;
    }

    let maxRank = 0;
    nodes.forEach(n => {
        n.rank = rank.has(n.id) ? rank.get(n.id) : 0;
        if (n.rank > maxRank) maxRank = n.rank;
    });

    const rankGroups = new Map();
    nodes.forEach(n => {
        if (!rankGroups.has(n.rank)) rankGroups.set(n.rank, []);
        rankGroups.get(n.rank).push(n);
    });

    // Space layers along X (left to right) and spread along Y
    const layerSpacingX = Math.max(220, Math.min(360, (width * 0.7) / (maxRank + 1 || 1)));
    let maxNodesInRank = 1;
    rankGroups.forEach(group => {
        if (group.length > maxNodesInRank) maxNodesInRank = group.length;
    });
    const nodeSpacingY = Math.max(150, Math.min(240, (height * 0.65) / maxNodesInRank));

    // Sort nodes in each rank r >= 1 by average targetY of their incoming parents to minimize edge crossings (Barycenter heuristic)
    for (let r = 1; r <= maxRank; r++) {
        const group = rankGroups.get(r);
        if (!group || group.length <= 1) continue;
        group.sort((a, b) => {
            const aParents = edges.filter(e => {
                const tid = typeof e.target === 'object' ? e.target.id : e.target;
                return tid === a.id;
            });
            const bParents = edges.filter(e => {
                const tid = typeof e.target === 'object' ? e.target.id : e.target;
                return tid === b.id;
            });
            const aAvgY = aParents.length > 0
                ? aParents.reduce((sum, e) => {
                    const sid = typeof e.source === 'object' ? e.source.id : e.source;
                    const src = nodeMap.get(sid);
                    return sum + (src && src.targetY != null ? src.targetY : centerY);
                }, 0) / aParents.length
                : centerY;
            const bAvgY = bParents.length > 0
                ? bParents.reduce((sum, e) => {
                    const sid = typeof e.source === 'object' ? e.source.id : e.source;
                    const src = nodeMap.get(sid);
                    return sum + (src && src.targetY != null ? src.targetY : centerY);
                }, 0) / bParents.length
                : centerY;
            return aAvgY - bAvgY;
        });
    }

    rankGroups.forEach((group, r) => {
        const x = centerX + (r - maxRank / 2) * layerSpacingX;
        if (group.length === 1) {
            const prevGroup = rankGroups.get(r - 1);
            const nextGroup = rankGroups.get(r + 1);
            const hasAdjacentMulti = (prevGroup && prevGroup.length > 1) || (nextGroup && nextGroup.length > 1);
            // Stagger single-node ranks along a gentle wave to prevent 1D collinearity
            const staggerY = (r % 2 === 0) ? -45 : 45;
            const y = hasAdjacentMulti ? centerY : (centerY + staggerY);
            const node = group[0];
            node.x = node.fx != null ? node.fx : x;
            node.y = node.fy != null ? node.fy : y;
            node.targetX = x;
            node.targetY = y;
        } else {
            group.forEach((node, idx) => {
                const y = centerY + (idx - (group.length - 1) / 2) * nodeSpacingY;
                node.x = node.fx != null ? node.fx : x;
                node.y = node.fy != null ? node.fy : y;
                node.targetX = x;
                node.targetY = y;
            });
        }
    });

    // 6. Dynamic Zoom Constraints Calculation
    let maxDist = 0;
    if (nodes.length <= 1) {
        maxDist = 52;
    } else {
        for (let i = 0; i < nodes.length; i++) {
            for (let j = i + 1; j < nodes.length; j++) {
                const dx = nodes[i].x - nodes[j].x;
                const dy = nodes[i].y - nodes[j].y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > maxDist) maxDist = dist;
            }
        }
    }
    if (maxDist < 50) maxDist = 50;

    const screenSize = Math.min(width, height);
    const halfScreen = 0.5 * screenSize;
    let minZoom = halfScreen / maxDist;
    minZoom = Math.max(0.1, Math.min(0.85, minZoom));
    const maxZoom = 4.5;
    zoomLimits = { min: minZoom, max: maxZoom };

    // 7. Setup D3 Zoom & Pan Behavior
    const zoom = d3.zoom()
        .scaleExtent([zoomLimits.min, zoomLimits.max])
        .on('start', () => {
            viewportContainer.classList.add('panning');
        })
        .on('zoom', (event) => {
            g.attr('transform', event.transform);
            const pct = Math.round(event.transform.k * 100);
            zoomLevelBadge.textContent = `${pct}%`;
        })
        .on('end', () => {
            viewportContainer.classList.remove('panning');
        });

    svg.call(zoom);
    activeD3Zoom = zoom;
    currentSvgSelection = svg;

    // Compute edge multiplicity (for parallel and multi-edges) and pair index
    const pairGroups = new Map();
    edges.forEach(e => {
        const u = typeof e.source === 'object' ? e.source.id : e.source;
        const v = typeof e.target === 'object' ? e.target.id : e.target;
        const key = u < v ? `${u}_${v}` : `${v}_${u}`;
        if (!pairGroups.has(key)) pairGroups.set(key, []);
        pairGroups.get(key).push(e);
    });

    pairGroups.forEach(group => {
        group.forEach((edge, idx) => {
            edge.pairIndex = idx;
            edge.pairCount = group.length;
        });
    });

    // 8. Render Edges (Transparent with White Outline/Arrows)
    const edgeGroups = edgeLayer.selectAll('.edge-group')
        .data(edges)
        .enter()
        .append('g')
        .attr('class', 'edge-group');

    // Invisible wide hitbox for easy hover and click
    const edgeHitboxes = edgeGroups.append('path')
        .attr('class', 'edge-hitbox');

    // Visible styled white path with arrow
    const edgePaths = edgeGroups.append('path')
        .attr('class', 'edge-path')
        .attr('marker-end', 'url(#arrow)');

    edgeGroups
        .on('mouseenter', function(event, d) {
            d3.select(this).select('.edge-path').attr('marker-end', 'url(#arrow-hover)');
            if (!selectedElement) {
                inspectEdge(d);
            }
        })
        .on('mouseleave', function(event, d) {
            if (selectedElement !== d) {
                d3.select(this).select('.edge-path').attr('marker-end', 'url(#arrow)');
            }
            if (!selectedElement) {
                clearSelection();
            }
        })
        .on('click', function(event, d) {
            event.stopPropagation();
            d3.selectAll('.node-group').classed('selected', false);
            d3.selectAll('.edge-group').classed('selected', false);
            d3.select(this).classed('selected', true);
            d3.select(this).select('.edge-path').attr('marker-end', 'url(#arrow-hover)');
            selectedElement = d;
            inspectEdge(d);
        });

    // 9. Render Nodes (Transparent with White Outlines)
    const nodeGroups = nodeLayer.selectAll('.node-group')
        .data(nodes)
        .enter()
        .append('g')
        .attr('class', 'node-group')
        .call(d3.drag()
            .on('start', dragStarted)
            .on('drag', dragged)
            .on('end', dragEnded));

    // Outer glow halo
    nodeGroups.append('circle')
        .attr('r', 32)
        .attr('class', 'node-halo');

    // Core transparent circle with white border (radius 24)
    nodeGroups.append('circle')
        .attr('r', 24)
        .attr('class', d => {
            if (d.id === graphUploadRequest.startNodeId) return 'node-circle start-node';
            if (d.rank === maxRank && maxRank > 0) return 'node-circle end-node';
            return 'node-circle';
        });

    // Crisp White Node Label
    nodeGroups.append('text')
        .attr('class', 'node-label')
        .text(d => d.name.length > 7 ? d.name.substring(0, 6) + '..' : d.name);

    nodeGroups
        .on('mouseenter', function(event, d) {
            if (!selectedElement) {
                inspectNode(d);
            }
        })
        .on('mouseleave', function(event, d) {
            if (!selectedElement) {
                clearSelection();
            }
        })
        .on('click', function(event, d) {
            event.stopPropagation();
            d3.selectAll('.node-group').classed('selected', false);
            d3.selectAll('.edge-group').classed('selected', false);
            d3.select(this).classed('selected', true);
            selectedElement = d;
            inspectNode(d);
        });

    // 10. Force Simulation with Anti-Collinear Stability & Anti-Overlap Anchoring
    if (activeSimulation) activeSimulation.stop();

    activeSimulation = d3.forceSimulation(nodes)
        .force('link', d3.forceLink(edges).id(d => d.id).distance(layerSpacingX * 0.95).strength(0.2))
        .force('charge', d3.forceManyBody().strength(-150))
        .force('collide', d3.forceCollide().radius(62).iterations(4))
        .force('x', d3.forceX(d => d.targetX).strength(0.75))
        .force('y', d3.forceY(d => d.targetY).strength(0.70))
        .alphaDecay(0.06) // Smooth, stable settle preventing oscillation or snap-back
        .on('tick', () => {
            edgeHitboxes.attr('d', edgePathGenerator);
            edgePaths.attr('d', edgePathGenerator);
            nodeGroups.attr('transform', d => `translate(${d.x},${d.y})`);
        });

    // Pre-warm 40 ticks synchronously for instant clean layout
    for (let i = 0; i < 40; i++) activeSimulation.tick();

    // Initial centering and fit
    fitGraphToScreen();

    // 11. Helper Generators & Handlers
    function edgePathGenerator(d) {
        const sx = d.source.x != null ? d.source.x : d.source.targetX;
        const sy = d.source.y != null ? d.source.y : d.source.targetY;
        const tx = d.target.x != null ? d.target.x : d.target.targetX;
        const ty = d.target.y != null ? d.target.y : d.target.targetY;
        const dx = tx - sx;
        const dy = ty - sy;
        const dist = Math.sqrt(dx * dx + dy * dy);

        // 1. Self Loop
        if (d.source.id === d.target.id || dist < 2) {
            return `M ${sx - 10},${sy - 20} C ${sx - 35},${sy - 65} ${sx + 35},${sy - 65} ${sx + 10},${sy - 20}`;
        }

        const sRank = d.source.rank != null ? d.source.rank : 0;
        const tRank = d.target.rank != null ? d.target.rank : 0;
        const rankDiff = tRank - sRank;

        const pairIndex = d.pairIndex != null ? d.pairIndex : 0;
        const pairCount = d.pairCount != null ? d.pairCount : 1;
        const midX = (sx + tx) / 2;
        const midY = (sy + ty) / 2;

        // 2. Reverse / Feedback Edges (Target is behind source or cycle)
        if (dx < -25 || rankDiff < 0) {
            const archDrop = Math.max(70, Math.abs(dx) * 0.22) + pairIndex * 26;
            const cy = Math.max(sy, ty) + archDrop;
            return `M ${sx},${sy} Q ${midX},${cy} ${tx},${ty}`;
        }

        // 3. Skip-Rank Forward Edges (Edge hops over 1 or more intermediate columns)
        if (rankDiff > 1 && dx > 80) {
            // Arch cleanly above intermediate nodes so it never cuts through them or overlaps intermediate edges
            const archHeight = 55 + (rankDiff - 1) * 32 + (pairIndex * 24);
            const cy = Math.min(sy, ty) - archHeight;
            return `M ${sx},${sy} Q ${midX},${cy} ${tx},${ty}`;
        }

        // 4. Parallel Edges (Multiple edges between the same pair of nodes)
        if (pairCount > 1) {
            const nx = -dy / (dist || 1);
            const ny = dx / (dist || 1);
            const spread = (pairIndex - (pairCount - 1) / 2) * 32;
            const cx = midX + nx * spread;
            const cy = midY + ny * spread;
            return `M ${sx},${sy} Q ${cx},${cy} ${tx},${ty}`;
        }

        // 5. Standard Forward Adjacent Edge: Smooth Horizontal S-Curve
        const curvature = 0.45;
        const cx1 = sx + dx * curvature;
        const cy1 = sy;
        const cx2 = tx - dx * curvature;
        const cy2 = ty;
        return `M ${sx},${sy} C ${cx1},${cy1} ${cx2},${cy2} ${tx},${ty}`;
    }

    function dragStarted(event, d) {
        if (!event.active) activeSimulation.alphaTarget(0.2).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragEnded(event, d) {
        if (!event.active) activeSimulation.alphaTarget(0);
        d.fx = event.x;
        d.fy = event.y;
        d.targetX = event.x;
        d.targetY = event.y;
    }
}

// Side Inspector Helpers
function inspectNode(node) {
    if (isEdgeAddMode) {
        handleAddEdgeClick(node);
        return;
    }

    selectedElement = node;
    d3.selectAll('.node-group').classed('selected', d => d.id === node.id);
    d3.selectAll('.edge-group').classed('selected', false);
    d3.selectAll('.edge-path').attr('marker-end', 'url(#arrow)');

    openSidePanel();
    spEmptyState.classList.add('hidden');

    if (isEditMode) {
        spDetails.classList.add('hidden');
        spEditSection.classList.remove('hidden');

        // Upper Section: Node Form
        editNodeForm.classList.remove('hidden');
        nodeEmptyPrompt.classList.add('hidden');
        cancelNodeBtn.classList.remove('hidden');

        editNodeId.textContent = `ID: ${node.id}`;
        editNodeName.value = node.name || '';
        editNodeDesc.value = node.description || '';
        renderEditCostGrid(node.cost, editNodeCosts, 'node');

        // Lower Section: Deselect Edge
        editEdgeForm.classList.add('hidden');
        edgeEmptyPrompt.classList.remove('hidden');
        cancelEdgeBtn.classList.add('hidden');
    } else {
        switchSidePanelTab('inspector');
        spEditSection.classList.add('hidden');
        spDetails.classList.remove('hidden');

        spTypeBadge.textContent = 'NODE';
        spTypeBadge.className = 'sp-badge node-badge';
        spIdBadge.textContent = `ID: ${node.id}`;
        spName.textContent = node.name;
        spDesc.textContent = node.description || 'No description provided for this node.';

        spConnectionSection.classList.remove('hidden');
        const edges = activeGraphData ? activeGraphData.edges : [];
        const inCount = edges.filter(e => (e.target.id || e.target) === node.id).length;
        const outCount = edges.filter(e => (e.source.id || e.source) === node.id).length;
        spConnectionText.textContent = `${inCount} Incoming, ${outCount} Outgoing`;

        renderCostList(node.cost);
    }
    lucide.createIcons();
}

function inspectEdge(edge) {
    if (isEdgeAddMode) return; // Don't inspect edge while adding edge

    selectedElement = edge;
    d3.selectAll('.node-group').classed('selected', false);
    d3.selectAll('.edge-group').classed('selected', d => d.id === edge.id);
    d3.selectAll('.edge-path').attr('marker-end', d => (d.id === edge.id) ? 'url(#arrow-selected)' : 'url(#arrow)');

    openSidePanel();
    spEmptyState.classList.add('hidden');

    if (isEditMode) {
        spDetails.classList.add('hidden');
        spEditSection.classList.remove('hidden');

        // Lower Section: Edge Form
        editEdgeForm.classList.remove('hidden');
        edgeEmptyPrompt.classList.add('hidden');
        cancelEdgeBtn.classList.remove('hidden');

        editEdgeId.textContent = `ID: ${edge.id || '-'}`;
        editEdgeName.value = edge.name || '';
        editEdgeDesc.value = edge.description || '';
        renderEditCostGrid(edge.cost, editEdgeCosts, 'edge');

        // Upper Section: Deselect Node
        editNodeForm.classList.add('hidden');
        nodeEmptyPrompt.classList.remove('hidden');
        cancelNodeBtn.classList.add('hidden');
    } else {
        switchSidePanelTab('inspector');
        spEditSection.classList.add('hidden');
        spDetails.classList.remove('hidden');

        spTypeBadge.textContent = 'DIRECTED EDGE';
        spTypeBadge.className = 'sp-badge edge-badge';
        spIdBadge.textContent = `ID: ${edge.id || '-'}`;
        spName.textContent = edge.name || 'Directed Edge';
        spDesc.textContent = edge.description || 'No description provided for this edge.';

        spConnectionSection.classList.remove('hidden');
        const srcName = edge.source.name || edge.source;
        const tgtName = edge.target.name || edge.target;
        spConnectionText.textContent = `${srcName} → ${tgtName}`;

        renderCostList(edge.cost);
    }
    lucide.createIcons();
}

function renderCostList(costObj) {
    if (!spCostList) return;
    spCostList.innerHTML = '';

    const costMap = costObj || {};
    // Check globalCostNames first, fallback to costMap keys
    const costKeys = (globalCostNames && globalCostNames.length > 0)
        ? globalCostNames
        : Object.keys(costMap);

    if (costKeys.length === 0) {
        spCostList.innerHTML = `
            <div class="sp-cost-card">
                <span class="sp-cost-dim">No cost dimensions</span>
                <span class="sp-cost-val">0</span>
            </div>`;
        return;
    }

    costKeys.forEach(key => {
        const val = costMap[key] != null ? costMap[key] : 0;
        const card = document.createElement('div');
        card.className = 'sp-cost-card';
        card.innerHTML = `
            <span class="sp-cost-dim">${key}</span>
            <span class="sp-cost-val">${val}</span>`;
        spCostList.appendChild(card);
    });
}

function deselectNode() {
    editNodeForm.classList.add('hidden');
    nodeEmptyPrompt.classList.remove('hidden');
    cancelNodeBtn.classList.add('hidden');
    if (selectedElement && !selectedElement.source) {
        selectedElement = null;
        d3.selectAll('.node-group').classed('selected', false);
    }
}

function deselectEdge() {
    editEdgeForm.classList.add('hidden');
    edgeEmptyPrompt.classList.remove('hidden');
    cancelEdgeBtn.classList.add('hidden');
    if (selectedElement && selectedElement.source) {
        selectedElement = null;
        d3.selectAll('.edge-group').classed('selected', false);
        d3.selectAll('.edge-path').attr('marker-end', 'url(#arrow)');
    }
}

function clearSelection() {
    if (isEdgeAddMode) return; // Cannot clear selection during edge add mode
    selectedElement = null;
    d3.selectAll('.node-group').classed('selected', false);
    d3.selectAll('.edge-group').classed('selected', false);
    d3.selectAll('.edge-path').attr('marker-end', 'url(#arrow)');
    if (isEditMode) {
        spEmptyState.classList.add('hidden');
        spDetails.classList.add('hidden');
        spEditSection.classList.remove('hidden');
        deselectNode();
        deselectEdge();
    } else {
        spEmptyState.classList.remove('hidden');
        spDetails.classList.add('hidden');
        spEditSection.classList.add('hidden');
    }
}

// Zoom Button Controls
function zoomIn() {
    if (!currentSvgSelection || !activeD3Zoom) return;
    currentSvgSelection.transition().duration(250).call(activeD3Zoom.scaleBy, 1.3);
}

function zoomOut() {
    if (!currentSvgSelection || !activeD3Zoom) return;
    currentSvgSelection.transition().duration(250).call(activeD3Zoom.scaleBy, 0.77);
}

function fitGraphToScreen() {
    if (!currentSvgSelection || !activeD3Zoom || !activeGraphData) return;
    const nodes = activeGraphData.nodes;
    if (!nodes || nodes.length === 0) return;

    const width = viewportContainer.clientWidth || window.innerWidth;
    const height = viewportContainer.clientHeight || (window.innerHeight - 72);

    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    nodes.forEach(n => {
        const x = n.x != null ? n.x : n.targetX;
        const y = n.y != null ? n.y : n.targetY;
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
    });

    const padding = 120;
    const graphW = Math.max(1, (maxX - minX) + padding * 2);
    const graphH = Math.max(1, (maxY - minY) + padding * 2);

    let scale = Math.min(width / graphW, height / graphH, 1.15);
    scale = Math.max(zoomLimits.min, Math.min(zoomLimits.max, scale));

    const midX = (minX + maxX) / 2;
    const midY = (minY + maxY) / 2;
    const tx = width / 2 - midX * scale;
    const ty = height / 2 - midY * scale;

    currentSvgSelection.transition().duration(400).call(
        activeD3Zoom.transform,
        d3.zoomIdentity.translate(tx, ty).scale(scale)
    );
}

function resetGraphLayout() {
    if (!activeGraphData) return;
    (activeGraphData.nodes || []).forEach(n => {
        n.fx = null;
        n.fy = null;
    });
    reconstructGraphUploadRequest();
    renderGraphVisualization(activeGraphData, { graphId: currentGraphId, graphName: activeGraphData.graphName }, true);
    showToast("Layout re-aligned", "info");
}

// ==========================================================================
// Graph Edit Mode Logic
// ==========================================================================

function toggleEditMode() {
    isEditMode = editModeToggle.checked;
    exitEdgeAddMode();
    clearPathHighlightOnCanvas();
    if (isEditMode) {
        saveGraphBtn.classList.remove("hidden");
        costCataloguePanel.classList.remove("hidden");
        costCataloguePanel.classList.remove("collapsed");
        spDetails.classList.add("hidden");
        spEmptyState.classList.add("hidden");
        spEditSection.classList.remove("hidden");
        if (calcPathsBtn) calcPathsBtn.classList.add("hidden");
        if (spViewTabs) spViewTabs.classList.add("hidden");
        switchSidePanelTab('inspector');
        if (selectedElement) {
            if (selectedElement.source) inspectEdge(selectedElement);
            else inspectNode(selectedElement);
        } else {
            deselectNode();
            deselectEdge();
        }
    } else {
        saveGraphBtn.classList.add("hidden");
        // In view mode, keep cost catalogue visible if cost parameters exist
        if (globalCostNames && globalCostNames.length > 0) {
            costCataloguePanel.classList.remove("hidden");
        } else {
            costCataloguePanel.classList.add("hidden");
        }
        spEditSection.classList.add("hidden");
        if (calcPathsBtn) calcPathsBtn.classList.remove("hidden");
        if (spViewTabs) spViewTabs.classList.remove("hidden");
        if (selectedElement) {
            if (selectedElement.source) inspectEdge(selectedElement);
            else inspectNode(selectedElement);
        } else {
            spEmptyState.classList.remove("hidden");
            spDetails.classList.add("hidden");
        }
    }
    // Re-render cost catalogue to immediately toggle delete buttons & add button
    renderGlobalCostCatalogue();
    lucide.createIcons();
}

function renderGlobalCostCatalogue() {
    // 1. Floating Cost Catalogue Card on Canvas
    if (globalCostList) {
        globalCostList.innerHTML = "";
        
        const countBadge = document.getElementById('cost-catalogue-count');
        if (countBadge) {
            countBadge.textContent = globalCostNames ? globalCostNames.length : 0;
        }

        const addBtn = document.getElementById('add-cost-dim-btn');
        if (addBtn) {
            if (isEditMode) addBtn.classList.remove('hidden');
            else addBtn.classList.add('hidden');
        }

        const addBottomBtn = document.getElementById('add-cost-dim-bottom-btn');
        if (addBottomBtn) {
            if (isEditMode) addBottomBtn.classList.remove('hidden');
            else addBottomBtn.classList.add('hidden');
        }

        if (!globalCostNames || globalCostNames.length === 0) {
            globalCostList.innerHTML = "<span class=\"text-sm text-gray-400\" style=\"padding: 0.5rem;\">No cost parameters added.</span>";
        } else {
            globalCostNames.forEach((cost, idx) => {
                const div = document.createElement("div");
                div.className = "cost-item";
                div.innerHTML = `
                    <span>${cost}</span>
                    <button type="button" class="cost-delete-btn ${isEditMode ? '' : 'hidden'}" title="Remove parameter '${cost}' globally" onclick="event.preventDefault(); event.stopPropagation(); window.removeGlobalCostDimension(${idx})">
                        <i data-lucide="trash-2"></i>
                    </button>
                `;
                globalCostList.appendChild(div);
            });
        }
    }

    // 2. Side Panel Inspector Cost Parameters Section (Edit Mode)
    const spCostList = document.getElementById('sp-cost-parameters-list');
    const spCostCountBadge = document.getElementById('sp-cost-count-badge');
    if (spCostCountBadge) {
        spCostCountBadge.textContent = globalCostNames ? globalCostNames.length : 0;
    }
    if (spCostList) {
        spCostList.innerHTML = "";
        if (!globalCostNames || globalCostNames.length === 0) {
            spCostList.innerHTML = "<span class=\"text-sm text-gray-400\" style=\"padding: 0.5rem;\">No cost parameters defined. Click '+ Add' to create one.</span>";
        } else {
            globalCostNames.forEach((cost, idx) => {
                const div = document.createElement("div");
                div.className = "sp-cost-manage-item";
                div.innerHTML = `
                    <span class="sp-cost-manage-name">${cost}</span>
                    <button type="button" class="cost-delete-btn" title="Delete parameter '${cost}' globally" onclick="event.preventDefault(); event.stopPropagation(); window.removeGlobalCostDimension(${idx})">
                        <i data-lucide="trash-2"></i>
                    </button>
                `;
                spCostList.appendChild(div);
            });
        }
    }

    lucide.createIcons();
}

function toggleCostCatalogue(e) {
    if (e && e.target && e.target.closest('#add-cost-dim-btn')) return;
    if (e && e.target && e.target.closest('.cost-delete-btn')) return;
    if (e && e.target && e.target.closest('.danger-outline')) return;
    if (!costCataloguePanel) return;
    costCataloguePanel.classList.toggle('collapsed');
}

function addGlobalCostDimension() {
    const costName = prompt("Enter new cost dimension name:");
    if (!costName || costName.trim() === "") return;
    const trimmed = costName.trim();
    if (globalCostNames.includes(trimmed)) {
        showToast("Cost dimension already exists", "error");
        return;
    }
    globalCostNames.push(trimmed);
    hasStructuralChanges = true;
    renderGlobalCostCatalogue();
    if (selectedElement) {
        if (selectedElement.source) inspectEdge(selectedElement);
        else inspectNode(selectedElement);
    }
}

window.removeGlobalCostDimension = function(idx) {
    if (idx < 0 || idx >= globalCostNames.length) return;
    const costNameToRemove = globalCostNames[idx];
    if (confirm(`Remove cost parameter "${costNameToRemove}"? This will delete it from all nodes and edges across the graph.`)) {
        globalCostNames.splice(idx, 1);
        hasStructuralChanges = true;
        
        // Remove from all active nodes in activeGraphData
        if (activeGraphData && activeGraphData.nodes) {
            activeGraphData.nodes.forEach(n => {
                if (n.cost) delete n.cost[costNameToRemove];
                if (n.nodeCost && n.nodeCost.costVector) delete n.nodeCost.costVector[costNameToRemove];
            });
        }
        // Remove from all active edges in activeGraphData
        if (activeGraphData && activeGraphData.edges) {
            activeGraphData.edges.forEach(e => {
                if (e.cost) delete e.cost[costNameToRemove];
                if (e.edgeCost && e.edgeCost.costVector) delete e.edgeCost.costVector[costNameToRemove];
            });
        }
        
        if (activeGraphData) {
            activeGraphData.costNames = [...globalCostNames];
        }
        
        // Update header pill
        if (gvCostDimensions && gvCostPill) {
            if (globalCostNames.length > 0) {
                gvCostDimensions.textContent = `${globalCostNames.length} Parameter${globalCostNames.length > 1 ? 's' : ''}`;
                gvCostPill.classList.remove('hidden');
            } else {
                gvCostPill.classList.add('hidden');
            }
        }
        
        renderGlobalCostCatalogue();
        
        if (selectedElement) {
            if (selectedElement.source) inspectEdge(selectedElement);
            else inspectNode(selectedElement);
        }
        
        showToast(`Removed parameter "${costNameToRemove}". Click "Save Changes" to persist.`, "info");
    }
};

window.removeGlobalCostDimensionByName = function(name) {
    const idx = globalCostNames.indexOf(name);
    if (idx !== -1) {
        window.removeGlobalCostDimension(idx);
    }
};

function renderEditCostGrid(costObj, container, type) {
    container.innerHTML = "";
    if (!globalCostNames || globalCostNames.length === 0) {
        container.innerHTML = "<span class='text-sm text-gray-400'>No cost parameters defined.</span>";
        return;
    }
    globalCostNames.forEach(costName => {
        const val = (costObj && costObj[costName] !== undefined) ? costObj[costName] : 0;
        const div = document.createElement("div");
        div.className = "cost-input-wrap";
        div.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem;">
                <label style="margin: 0; font-weight: 500;">${costName}</label>
                <button type="button" class="cost-delete-btn" title="Delete parameter '${costName}' globally" onclick="event.preventDefault(); event.stopPropagation(); window.removeGlobalCostDimensionByName('${costName}')">
                    <i data-lucide="trash-2"></i>
                </button>
            </div>
            <input type="number" step="any" class="glass-input-sm" value="${val}" data-cost="${costName}">
        `;
        const input = div.querySelector("input");
        input.addEventListener("input", (e) => {
            costObj[costName] = parseFloat(e.target.value) || 0;
            hasStructuralChanges = true;
        });
        container.appendChild(div);
    });
    lucide.createIcons();
}

function updateActiveElement() {
    if (!selectedElement) return;
    if (selectedElement.source) {
        selectedElement.name = editEdgeName.value;
        selectedElement.description = editEdgeDesc.value;
    } else {
        selectedElement.name = editNodeName.value;
        selectedElement.description = editNodeDesc.value;
        // SVG label update without full re-render
        d3.selectAll(".node-group").filter(d => d.id === selectedElement.id)
          .select(".node-label").text(selectedElement.name.length > 7 ? selectedElement.name.substring(0, 6) + ".." : selectedElement.name);
    }
}

function handleAddNode() {
    let maxId = 0;
    activeGraphData.nodes.forEach(n => { if (n.id > maxId) maxId = n.id; });
    const newNode = {
        id: maxId + 1,
        name: `Node_${maxId + 1}`,
        description: "",
        cost: {}
    };
    globalCostNames.forEach(cn => newNode.cost[cn] = 0);
    activeGraphData.nodes.push(newNode);
    hasStructuralChanges = true;
    
    // Re-render D3 graph while keeping activeGraphData structure intact
    reconstructGraphUploadRequest();
    renderGraphVisualization(activeGraphData, { graphId: currentGraphId, graphName: activeGraphData.graphName });
    inspectNode(newNode);
    showToast(`Added ${newNode.name}`, "success");
}

function handleDeleteNode() {
    if (!selectedElement || selectedElement.source) return;
    const nid = selectedElement.id;
    activeGraphData.nodes = activeGraphData.nodes.filter(n => n.id !== nid);
    activeGraphData.edges = activeGraphData.edges.filter(e => e.source !== nid && e.target !== nid && (!e.source || e.source.id !== nid) && (!e.target || e.target.id !== nid));
    hasStructuralChanges = true;
    clearSelection();
    reconstructGraphUploadRequest();
    renderGraphVisualization(activeGraphData, { graphId: currentGraphId, graphName: activeGraphData.graphName }, true);
}

function enterEdgeAddMode() {
    isEdgeAddMode = true;
    edgeSourceNode = null;
    edgeAddInstruction.classList.remove("hidden");
    addEdgeBtn.classList.add("hidden");
    const svg = d3.select(graphSvg);
    svg.classed("svg-edge-add-mode", true);
}

function exitEdgeAddMode() {
    isEdgeAddMode = false;
    edgeSourceNode = null;
    edgeAddInstruction.classList.add("hidden");
    addEdgeBtn.classList.remove("hidden");
    d3.select(graphSvg).classed("svg-edge-add-mode", false);
    d3.selectAll(".node-group").classed("selected", false); // clear any temp selection
}

function handleAddEdgeClick(node) {
    if (!edgeSourceNode) {
        edgeSourceNode = node;
        showToast(`Source node ${node.name} selected. Click target node.`, "success");
    } else {
        if (edgeSourceNode.id === node.id) {
            showToast("Cannot connect node to itself", "error");
            exitEdgeAddMode();
            return;
        }
        let maxId = 0;
        activeGraphData.edges.forEach(e => { if (e.id > maxId) maxId = e.id; });
        const newEdge = {
            id: maxId + 1,
            name: `Edge_${maxId + 1}`,
            description: "",
            source: edgeSourceNode.id,
            target: node.id,
            sourceNodeId: edgeSourceNode.id,
            targetNodeId: node.id,
            cost: {}
        };
        globalCostNames.forEach(cn => newEdge.cost[cn] = 0);
        activeGraphData.edges.push(newEdge);
        hasStructuralChanges = true;
        exitEdgeAddMode();
        
        reconstructGraphUploadRequest();
        renderGraphVisualization(activeGraphData, { graphId: currentGraphId, graphName: activeGraphData.graphName });
        inspectEdge(newEdge);
        showToast(`Connected ${edgeSourceNode.name} to ${node.name}`, "success");
    }
}

function handleDeleteEdge() {
    if (!selectedElement || !selectedElement.source) return;
    const eid = selectedElement.id;
    activeGraphData.edges = activeGraphData.edges.filter(e => e.id !== eid);
    hasStructuralChanges = true;
    clearSelection();
    reconstructGraphUploadRequest();
    renderGraphVisualization(activeGraphData, { graphId: currentGraphId, graphName: activeGraphData.graphName }, true);
}

function reconstructGraphUploadRequest() {
    if (!activeGraphData) return;
    activeGraphData.graphId = currentGraphId || activeGraphData.graphId;
    activeGraphData.graphName = gvGraphName.textContent || activeGraphData.graphName || 'Unnamed Graph';
    activeGraphData.userId = (currentUser && currentUser.userId) ? currentUser.userId : activeGraphData.userId;
    activeGraphData.costNames = globalCostNames;

    activeGraphData.node = (activeGraphData.nodes || []).map(n => ({
        id: n.id,
        name: n.name,
        description: n.description || '',
        nodeCost: { costVector: { ...n.cost } }
    }));
    activeGraphData.edge = (activeGraphData.edges || []).map(e => ({
        id: e.id,
        edgeName: e.name || `Edge ${e.id}`,
        edgeDescription: e.description || '',
        sourceNodeId: (typeof e.source === 'object' && e.source !== null) ? e.source.id : (e.sourceNodeId !== undefined ? e.sourceNodeId : e.source),
        targetNodeId: (typeof e.target === 'object' && e.target !== null) ? e.target.id : (e.targetNodeId !== undefined ? e.targetNodeId : e.target),
        edgeCost: { costVector: { ...e.cost } }
    }));
}

async function saveGraphChanges() {
    if (!hasStructuralChanges && !hasMetadataChanges) {
        showToast("No changes to save.", "error");
        return;
    }
    
    const finalGraphId = currentGraphId || (activeGraphData && activeGraphData.graphId);
    if (!finalGraphId) {
        showToast("Graph ID is missing. Cannot save changes.", "error");
        return;
    }

    const updateType = hasStructuralChanges ? "C" : "S";
    reconstructGraphUploadRequest();
    
    // Explicitly construct payload matching GraphUpdateRequest
    const payload = {
        graphId: Number(finalGraphId),
        graphName: activeGraphData.graphName || 'Unnamed Graph',
        graphDescription: activeGraphData.graphDescription || '',
        userId: activeGraphData.userId || (currentUser ? currentUser.userId : null),
        startNodeId: activeGraphData.startNodeId || (activeGraphData.nodes && activeGraphData.nodes.length > 0 ? activeGraphData.nodes[0].id : 1),
        node: activeGraphData.node,
        edge: activeGraphData.edge,
        costNames: activeGraphData.costNames || globalCostNames,
        updateType: updateType
    };
    
    saveGraphBtn.disabled = true;
    saveGraphBtn.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Saving...';
    lucide.createIcons();
    
    try {
        const response = await fetch(`${API_BASE}/graphs/update`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        
        if (data.objErrorDetails && data.objErrorDetails.errorCode !== "1" && data.objErrorDetails.errorCode !== "0") {
            throw new Error(data.objErrorDetails.errorMessage || "Failed to update graph");
        }
        
        showToast("Graph updated successfully!");
        hasStructuralChanges = false;
        hasMetadataChanges = false;
        cachedPaths = null;
        if (spPathsCountBadge) spPathsCountBadge.textContent = '0';
        
        // Re-render with new data
        if (data.objGraphUploadRequest) {
            data.objGraphUploadRequest.graphId = Number(finalGraphId);
            renderGraphVisualization(data.objGraphUploadRequest, data, false);
        } else {
            renderGraphVisualization(activeGraphData, { graphId: Number(finalGraphId), graphName: activeGraphData.graphName }, false);
        }
    } catch (err) {
        console.error("Error updating graph:", err);
        showToast(err.message, "error");
    } finally {
        saveGraphBtn.disabled = false;
        saveGraphBtn.innerHTML = '<i data-lucide="save"></i> Save Changes';
        lucide.createIcons();
    }
}

// Graph Details Modal Handlers
function openGraphDetailsModal() {
    if (!graphDetailsModal) return;
    const currentName = (activeGraphData && activeGraphData.graphName) ? activeGraphData.graphName : (gvGraphName.textContent || '');
    const currentDesc = (activeGraphData && activeGraphData.graphDescription) ? activeGraphData.graphDescription : '';
    
    modalGraphName.value = currentName;
    modalGraphDesc.value = currentDesc;
    
    graphDetailsModal.classList.remove('hidden');
    lucide.createIcons();
    setTimeout(() => {
        modalGraphName.focus();
        modalGraphName.select();
    }, 50);
}

function closeGraphDetailsModal() {
    if (!graphDetailsModal) return;
    graphDetailsModal.classList.add('hidden');
}

async function handleSaveGraphDetails(e) {
    if (e) e.preventDefault();
    
    const newName = modalGraphName.value.trim();
    const newDesc = modalGraphDesc.value.trim();
    
    if (!newName) {
        showToast("Graph name cannot be empty.", "error");
        modalGraphName.focus();
        return;
    }
    
    const finalGraphId = currentGraphId || (activeGraphData && activeGraphData.graphId);
    if (!finalGraphId) {
        showToast("Graph ID is missing. Cannot save details.", "error");
        return;
    }
    
    const ogHtml = saveGraphDetailsBtn.innerHTML;
    saveGraphDetailsBtn.disabled = true;
    saveGraphDetailsBtn.innerHTML = '<i data-lucide="loader-2" class="spin"></i> Saving...';
    lucide.createIcons();
    
    reconstructGraphUploadRequest();
    
    const payload = {
        graphId: Number(finalGraphId),
        graphName: newName,
        graphDescription: newDesc,
        userId: (activeGraphData && activeGraphData.userId) || (currentUser ? currentUser.userId : null),
        startNodeId: (activeGraphData && activeGraphData.startNodeId) || (activeGraphData && activeGraphData.nodes && activeGraphData.nodes.length > 0 ? activeGraphData.nodes[0].id : 1),
        node: activeGraphData ? activeGraphData.node : [],
        edge: activeGraphData ? activeGraphData.edge : [],
        costNames: (activeGraphData && activeGraphData.costNames) || globalCostNames || [],
        updateType: "S"
    };
    
    try {
        const response = await fetch(`${API_BASE}/graphs/update`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        
        if (data.objErrorDetails && data.objErrorDetails.errorCode !== "1" && data.objErrorDetails.errorCode !== "0") {
            throw new Error(data.objErrorDetails.errorMessage || "Failed to update graph details");
        }
        
        if (activeGraphData) {
            activeGraphData.graphName = newName;
            activeGraphData.graphDescription = newDesc;
        }
        gvGraphName.textContent = newName;
        
        closeGraphDetailsModal();
        showToast("Graph details updated successfully!");
        
        // Refresh graphs in catalogue so card title & desc update immediately
        fetchUserGraphs();
    } catch (err) {
        console.error("Error updating graph details:", err);
        showToast(err.message || "Failed to update graph details", "error");
    } finally {
        saveGraphDetailsBtn.disabled = false;
        saveGraphDetailsBtn.innerHTML = ogHtml;
        lucide.createIcons();
    }
}

// Add Cost Parameter Modal Handlers
function openAddCostModal() {
    if (!addCostModal) return;
    newCostParamName.value = '';
    newCostParamDefault.value = '0';
    addCostModal.classList.remove('hidden');
    lucide.createIcons();
    setTimeout(() => {
        newCostParamName.focus();
    }, 50);
}

function closeAddCostModal() {
    if (!addCostModal) return;
    addCostModal.classList.add('hidden');
}

function handleAddCostSubmit(e) {
    if (e) e.preventDefault();
    const name = newCostParamName.value.trim();
    const defaultVal = parseFloat(newCostParamDefault.value) || 0;

    if (!name) {
        showToast("Parameter name cannot be empty.", "error");
        newCostParamName.focus();
        return;
    }

    if (globalCostNames && globalCostNames.some(c => c.toLowerCase() === name.toLowerCase())) {
        showToast(`Cost parameter "${name}" already exists.`, "error");
        newCostParamName.focus();
        return;
    }

    // Add to global dimensions
    if (!globalCostNames) globalCostNames = [];
    globalCostNames.push(name);

    // Initialize across all active nodes
    if (activeGraphData && activeGraphData.nodes) {
        activeGraphData.nodes.forEach(n => {
            n.cost = n.cost || {};
            if (n.cost[name] === undefined) {
                n.cost[name] = defaultVal;
            }
        });
    }

    // Initialize across all active edges
    if (activeGraphData && activeGraphData.edges) {
        activeGraphData.edges.forEach(ed => {
            ed.cost = ed.cost || {};
            if (ed.cost[name] === undefined) {
                ed.cost[name] = defaultVal;
            }
        });
    }

    hasStructuralChanges = true;

    // Refresh UI displays
    renderGlobalCostCatalogue();
    if (costCataloguePanel) {
        costCataloguePanel.classList.remove('hidden');
        costCataloguePanel.classList.remove('collapsed');
    }

    if (gvCostDimensions && gvCostPill) {
        gvCostDimensions.textContent = `${globalCostNames.length} Parameter${globalCostNames.length > 1 ? 's' : ''}`;
        gvCostPill.classList.remove('hidden');
    }

    // Update inspector if element is selected
    if (selectedElement) {
        if (selectedElement.source) {
            inspectEdge(selectedElement);
        } else {
            inspectNode(selectedElement);
        }
    }

    closeAddCostModal();
    showToast(`Added cost parameter "${name}"! Click "Save Changes" to persist.`);
}

// ==========================================================================
// Path Calculation & Presentation Engine
// ==========================================================================

function switchSidePanelTab(tabName) {
    activeSidePanelTab = tabName;
    if (tabName === 'paths') {
        if (spTabPaths) spTabPaths.classList.add('active');
        if (spTabInspector) spTabInspector.classList.remove('active');
        if (spPathsContainer) spPathsContainer.classList.remove('hidden');
        if (spInspectorContainer) spInspectorContainer.classList.add('hidden');
        if (spPanelTitle) spPanelTitle.textContent = 'Paths';
        if (spHeaderIcon) spHeaderIcon.setAttribute('data-lucide', 'route');
    } else {
        if (spTabInspector) spTabInspector.classList.add('active');
        if (spTabPaths) spTabPaths.classList.remove('active');
        if (spInspectorContainer) spInspectorContainer.classList.remove('hidden');
        if (spPathsContainer) spPathsContainer.classList.add('hidden');
        if (spPanelTitle) spPanelTitle.textContent = isEditMode ? 'Graph Editor' : 'Inspector';
        if (spHeaderIcon) spHeaderIcon.setAttribute('data-lucide', isEditMode ? 'edit-3' : 'info');
    }
    lucide.createIcons();
}

function findConnectingEdge(fromId, toId) {
    if (!activeGraphData || !activeGraphData.edges) return null;
    return activeGraphData.edges.find(e => {
        const s = (typeof e.source === 'object' && e.source !== null) ? e.source.id : (e.sourceNodeId !== undefined ? e.sourceNodeId : e.source);
        const t = (typeof e.target === 'object' && e.target !== null) ? e.target.id : (e.targetNodeId !== undefined ? e.targetNodeId : e.target);
        return Number(s) === Number(fromId) && Number(t) === Number(toId);
    });
}

async function calculateAndRenderPaths(forceRefresh = false) {
    if (!currentGraphId) {
        showToast('No active graph selected', 'error');
        return;
    }

    openSidePanel();
    switchSidePanelTab('paths');

    if (!forceRefresh && cachedPaths && cachedPaths.graphId === currentGraphId) {
        renderPathsCards(cachedPaths.paths);
        return;
    }

    if (pathsLoading) pathsLoading.classList.remove('hidden');
    if (pathsEmptyPrompt) pathsEmptyPrompt.classList.add('hidden');
    if (pathsCardsList) pathsCardsList.innerHTML = '';
    lucide.createIcons();

    try {
        const response = await fetch(`${API_BASE}/graphs/${currentGraphId}/paths`);
        const data = await response.json();

        if (data.objErrorDetails && data.objErrorDetails.errorCode !== '0' && data.objErrorDetails.errorCode !== '200') {
            throw new Error(data.objErrorDetails.errorMessage || 'Failed to calculate paths');
        }

        const paths = data.paths || [];
        cachedPaths = {
            graphId: currentGraphId,
            paths: paths
        };

        if (spPathsCountBadge) {
            spPathsCountBadge.textContent = paths.length;
        }

        renderPathsCards(paths);
        if (paths.length > 0) {
            showToast(`Calculated ${paths.length} path${paths.length > 1 ? 's' : ''}!`);
        }
    } catch (err) {
        console.error('Failed to calculate paths:', err);
        showToast(err.message || 'Error calculating paths', 'error');
        if (pathsLoading) pathsLoading.classList.add('hidden');
        if (pathsEmptyPrompt) {
            pathsEmptyPrompt.classList.remove('hidden');
            const promptP = pathsEmptyPrompt.querySelector('p');
            if (promptP) promptP.textContent = 'Error calculating paths: ' + (err.message || 'Unknown error');
        }
    } finally {
        if (pathsLoading) pathsLoading.classList.add('hidden');
        lucide.createIcons();
    }
}

function renderPathsCards(paths) {
    if (!pathsCardsList) return;
    pathsCardsList.innerHTML = '';

    if (!paths || paths.length === 0) {
        if (pathsEmptyPrompt) {
            pathsEmptyPrompt.classList.remove('hidden');
            const promptP = pathsEmptyPrompt.querySelector('p');
            if (promptP) promptP.textContent = 'No reachable paths found for this graph.';
        }
        return;
    }

    if (pathsEmptyPrompt) pathsEmptyPrompt.classList.add('hidden');

    paths.forEach((pathItem, index) => {
        const nodeSeq = pathItem.nodeSequence || [];
        const pathCosts = pathItem.pathCosts || {};
        const costEntries = Object.entries(pathCosts);

        // Header cost summary chips
        let costSummaryHtml = '';
        if (costEntries.length === 0) {
            costSummaryHtml = '<span class="path-cost-badge">No cost data</span>';
        } else {
            costSummaryHtml = costEntries.map(([k, v]) => `
                <span class="path-cost-badge">
                    <strong>${escapeHtml(k)}:</strong> ${v}
                </span>
            `).join('');
        }

        // Expanded detail cost grid
        let costDetailsHtml = '';
        if (costEntries.length === 0) {
            costDetailsHtml = '<span class="text-sm text-gray-400">No cumulative costs recorded.</span>';
        } else {
            costDetailsHtml = costEntries.map(([k, v]) => `
                <div class="path-cost-item">
                    <span class="cost-name">${escapeHtml(k)}</span>
                    <span class="cost-val">${v}</span>
                </div>
            `).join('');
        }

        // Sequential Route: alternating Node and Edge
        let routeFlowHtml = '';
        const connectingEdgeIds = [];
        const pathNodeIds = nodeSeq.map(n => n.id);

        nodeSeq.forEach((node, i) => {
            const isStart = (i === 0);
            const isEnd = (i === nodeSeq.length - 1);
            const dotClass = isStart ? 'start' : (isEnd ? 'end' : '');
            const badgeText = isStart ? 'START' : (isEnd ? 'END' : `HOP ${i + 1}`);

            routeFlowHtml += `
                <div class="flow-node-row">
                    <div class="flow-node-indicator">
                        <div class="flow-dot ${dotClass}"></div>
                    </div>
                    <div class="flow-node-info">
                        <div class="flow-node-header">
                            <span class="flow-node-title">${escapeHtml(node.name || ('Node ' + node.id))}</span>
                            <span class="flow-node-badge ${dotClass}">${badgeText}</span>
                        </div>
                        <span class="flow-node-id">ID: ${node.id}${node.description ? ' • ' + escapeHtml(node.description) : ''}</span>
                    </div>
                </div>
            `;

            if (i < nodeSeq.length - 1) {
                const nextNode = nodeSeq[i + 1];
                const edge = findConnectingEdge(node.id, nextNode.id);
                const edgeName = edge ? (edge.name || ('Edge ' + edge.id)) : `Edge (${node.id} → ${nextNode.id})`;
                const edgeId = edge ? edge.id : null;
                if (edgeId != null) connectingEdgeIds.push(edgeId);

                routeFlowHtml += `
                    <div class="flow-edge-row">
                        <div class="flow-edge-line">
                            <i data-lucide="arrow-down" class="flow-arrow-icon"></i>
                        </div>
                        <div class="flow-edge-info">
                            <i data-lucide="move-down-right" class="flow-edge-icon"></i>
                            <span class="flow-edge-title">${escapeHtml(edgeName)}</span>
                            ${edgeId ? `<span class="flow-edge-id">ID: ${edgeId}</span>` : ''}
                        </div>
                    </div>
                `;
            }
        });

        const card = document.createElement('div');
        card.className = 'path-card';
        card.dataset.pathIdx = index;
        card.innerHTML = `
            <div class="path-card-header" title="Click to expand/collapse path details">
                <div class="path-header-info">
                    <div class="path-title-wrap">
                        <i data-lucide="git-commit"></i>
                        <span class="path-name">Path ${index + 1}</span>
                        <span class="path-hops-count">${nodeSeq.length} Nodes</span>
                    </div>
                    <div class="path-costs-summary">
                        ${costSummaryHtml}
                    </div>
                </div>
                <div class="path-chevron-wrap">
                    <i data-lucide="chevron-down" class="path-chevron"></i>
                </div>
            </div>
            <div class="path-card-body">
                <div class="path-route-flow">
                    ${routeFlowHtml}
                </div>
                <div class="path-costs-detail-section">
                    <span class="path-detail-label">Cumulative Path Costs</span>
                    <div class="path-costs-grid">
                        ${costDetailsHtml}
                    </div>
                </div>
            </div>
        `;

        // Click to expand / collapse (independent expansion for multiple cards)
        const header = card.querySelector('.path-card-header');
        header.addEventListener('click', () => {
            const isExpanded = card.classList.toggle('expanded');
            if (isExpanded) {
                highlightPathOnCanvas(pathNodeIds, connectingEdgeIds);
            } else {
                clearPathHighlightOnCanvas();
            }
        });

        // Hover to highlight on canvas
        card.addEventListener('mouseenter', () => {
            highlightPathOnCanvas(pathNodeIds, connectingEdgeIds);
        });

        card.addEventListener('mouseleave', () => {
            const anyExpanded = document.querySelector('.path-card.expanded');
            if (!anyExpanded) {
                clearPathHighlightOnCanvas();
            } else {
                // If another card is expanded, highlight that one
                const expIdx = anyExpanded.dataset.pathIdx;
                if (paths[expIdx]) {
                    const expNodeIds = (paths[expIdx].nodeSequence || []).map(n => n.id);
                    const expEdgeIds = [];
                    for (let j = 0; j < expNodeIds.length - 1; j++) {
                        const ed = findConnectingEdge(expNodeIds[j], expNodeIds[j + 1]);
                        if (ed && ed.id != null) expEdgeIds.push(ed.id);
                    }
                    highlightPathOnCanvas(expNodeIds, expEdgeIds);
                }
            }
        });

        pathsCardsList.appendChild(card);
    });

    lucide.createIcons();
}

function highlightPathOnCanvas(nodeIds, edgeIds) {
    if (!currentSvgSelection) return;
    const nodeIdSet = new Set(nodeIds.map(Number));
    const edgeIdSet = new Set(edgeIds.map(Number));

    // Dim all elements first
    d3.selectAll('.node-group').classed('path-dimmed', true).classed('path-active-node', false);
    d3.selectAll('.edge-group').classed('path-dimmed', true).classed('path-active-edge', false);

    // Highlight path nodes
    d3.selectAll('.node-group').filter(d => nodeIdSet.has(Number(d.id)))
        .classed('path-dimmed', false)
        .classed('path-active-node', true);

    // Highlight path edges
    d3.selectAll('.edge-group').filter(d => {
        if (d.id != null && edgeIdSet.has(Number(d.id))) return true;
        const s = Number(d.source.id != null ? d.source.id : d.source);
        const t = Number(d.target.id != null ? d.target.id : d.target);
        for (let i = 0; i < nodeIds.length - 1; i++) {
            if (Number(nodeIds[i]) === s && Number(nodeIds[i + 1]) === t) return true;
        }
        return false;
    }).classed('path-dimmed', false).classed('path-active-edge', true);
}

function clearPathHighlightOnCanvas() {
    if (!currentSvgSelection) return;
    d3.selectAll('.node-group').classed('path-dimmed', false).classed('path-active-node', false);
    d3.selectAll('.edge-group').classed('path-dimmed', false).classed('path-active-edge', false);
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ==========================================
// AI Terminal Event Listeners & Logic
// ==========================================

if (createAiGraphBtn) {
    createAiGraphBtn.addEventListener('click', () => {
        // Clear graph data
        currentGraphId = null;
        currentGraph = null;
        activeGraphData = null;
        
        // Transition to Graph View explicitly WITHOUT loading a graph
        dashboardView.classList.add('hidden');
        dashboardView.classList.remove('active');
        graphView.classList.remove('hidden');
        graphView.classList.add('active');
        graphViewLoading.classList.add('hidden');
        
        // Setup empty visual state
        if (gvGraphName) gvGraphName.innerText = 'New AI Graph';
        if (gvGraphId) gvGraphId.innerText = 'ID: -';
        if (gvNodeCount) gvNodeCount.innerText = '0';
        if (gvEdgeCount) gvEdgeCount.innerText = '0';
        if (gvCostPill) gvCostPill.classList.add('hidden');
        
        // Clear the SVG
        d3.select('#graph-svg').selectAll('*').remove();
        
        aiChatTerminal.classList.remove('hidden');
        if (aiPromptInput) {
            aiPromptInput.value = '';
            aiPromptInput.focus();
        }
    });
}

if (closeAiTerminalBtn) {
    closeAiTerminalBtn.addEventListener('click', () => {
        aiChatTerminal.classList.add('hidden');
    });
}

if (aiErrorCloseBtn) {
    aiErrorCloseBtn.addEventListener('click', () => {
        aiErrorModal.classList.add('hidden');
    });
}

if (aiGenerateBtn) {
    aiGenerateBtn.addEventListener('click', async () => {
        const prompt = aiPromptInput.value.trim();
        if (!prompt) return;

        // Set loading state
        aiGenerateBtn.disabled = true;
        const originalText = aiGenerateBtnText.innerText;
        aiGenerateBtnText.innerText = 'Generating...';

        try {
            const payload = {
                prompt: prompt,
                userId: (currentUser && currentUser.userId) ? currentUser.userId : null
            };
            
            // If currentGraph has an ID, it means we are updating
            if (currentGraph && currentGraph.id) {
                payload.graphId = currentGraph.id;
                payload.currentGraphJson = JSON.stringify(currentGraph);
            }

            const response = await fetch('/workflow-engine/ai/createAIGraph', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (data.objErrorDetails && data.objErrorDetails.errorCode !== "0") {
                throw data.objErrorDetails;
            }

            // Success: Load graph and close terminal
            aiChatTerminal.classList.add('hidden');
            currentGraph = data.objGraphUploadRequest; // Set the new graph
            renderGraphVisualization(currentGraph, data, false);
            fetchUserGraphs(); // Refresh dashboard list in background

        } catch (error) {
            console.error('[AI Generation Error]', error);
            const msg = 'Service is currently unavailable or failed to process the request. Please check the network tab for details.';
            if (aiErrorMessage) aiErrorMessage.innerText = msg;
            if (aiErrorModal) aiErrorModal.classList.remove('hidden');
        } finally {
            aiGenerateBtn.disabled = false;
            aiGenerateBtnText.innerText = originalText;
        }
    });
}

