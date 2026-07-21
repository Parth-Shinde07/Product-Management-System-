const API_ORDERS = "http://localhost:8080/api/orders";
const API_CUSTOMERS = "http://localhost:8080/api/customers";
const API_PRODUCTS = "http://localhost:8080/api/products";
const API_AUTH = "http://localhost:8080/api/auth/login";

// Initial page access validation
document.addEventListener("DOMContentLoaded", () => {
    checkAuthentication();
});

function checkAuthentication() {
    const token = localStorage.getItem("admin_jwt_token");
    const loginModal = document.getElementById("login-modal");
    const dashboardContent = document.getElementById("dashboard-content");

    if (token) {
        loginModal.classList.add("hidden");
        dashboardContent.classList.remove("hidden");
        fetchOrders();
        loadCheckoutFormOptions();
    } else {
        loginModal.classList.remove("hidden");
        dashboardContent.classList.add("hidden");
    }
}

// Global Auth Header Builder Helper
function getAuthHeaders() {
    const token = localStorage.getItem("admin_jwt_token");
    return token ? { "Authorization": `Bearer ${token}` } : {};
}

// 🔐 HANDLE LOGIN FORM SUBMISSION
document.getElementById("login-form").addEventListener("submit", async function(e) {
    e.preventDefault();
    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;
    const errorDiv = document.getElementById("login-error");

    errorDiv.classList.add("hidden");

    try {
        const response = await fetch(API_AUTH, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error("Invalid username or password credentials.");
        }

        const data = await response.json();
        localStorage.setItem("admin_jwt_token", data.token); // Save token securely

        checkAuthentication(); // Toggle display structures
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.classList.remove("hidden");
    }
});

function handleLogout() {
    localStorage.removeItem("admin_jwt_token");
    checkAuthentication();
}

// 1. Fetch Orders and render table
async function fetchOrders() {
    try {
        const response = await fetch(API_ORDERS, {
            headers: { ...getAuthHeaders() }
        });

        if (response.status === 401 || response.status === 403) {
            handleLogout();
            return;
        }

        if (!response.ok) throw new Error("Failed to fetch orders");
        const orders = await response.json();
        renderDashboard(orders);
    } catch (error) {
        console.error("Error communicating with backend:", error);
    }
}

// 2. Load Customers and Products dynamically into Select lists
async function loadCheckoutFormOptions() {
    try {
        const [custRes, prodRes] = await Promise.all([
            fetch(API_CUSTOMERS, { headers: { ...getAuthHeaders() } }),
            fetch(API_PRODUCTS, { headers: { ...getAuthHeaders() } })
        ]);

        if (custRes.ok) {
            const customers = await custRes.json();
            const selectCust = document.getElementById("checkout-customer");
            selectCust.innerHTML = '<option value="">-- Choose a Customer --</option>';
            customers.forEach(cust => {
                selectCust.insertAdjacentHTML("beforeend", `<option value="${cust.id}">${cust.name}</option>`);
            });
        }

        if (prodRes.ok) {
            const products = await prodRes.json();
            const selectProd = document.getElementById("checkout-product");
            selectProd.innerHTML = '<option value="">-- Choose a Product --</option>';
            products.forEach(prod => {
                selectProd.insertAdjacentHTML("beforeend", `<option value="${prod.id}">${prod.name} (Stock: ${prod.stockQuantity})</option>`);
            });
        }
    } catch (err) {
        console.error("Could not load dropdown parameters:", err);
    }
}

// 3. Render Dashboard Data (With Admin Controls!)
function renderDashboard(orders) {
    const tbody = document.getElementById("orders-table-body");
    tbody.innerHTML = "";

    let totalRevenue = 0;
    let processingCount = 0;

    orders.forEach(order => {
        totalRevenue += order.totalAmount;
        if (order.status === "PROCESSING") processingCount++;

        const orderDate = new Date(order.orderDate).toLocaleString();

        let customerDisplayName = 'Guest User';
        if (order.customer) {
            if (typeof order.customer === 'object' && order.customer.name) {
                customerDisplayName = order.customer.name;
            } else if (typeof order.customer === 'string') {
                customerDisplayName = order.customer;
            }
        }

        let statusBadgeClass = "";
        if (order.status === "PROCESSING") statusBadgeClass = "bg-amber-50 text-amber-700 border-amber-200";
        else if (order.status === "SHIPPED") statusBadgeClass = "bg-blue-50 text-blue-700 border-blue-200";
        else if (order.status === "DELIVERED") statusBadgeClass = "bg-emerald-50 text-emerald-700 border-emerald-200";
        else statusBadgeClass = "bg-slate-50 text-slate-700 border-slate-200";

        const row = `
            <tr class="hover:bg-slate-50/70 transition-colors">
                <td class="p-4 font-mono text-sm font-bold text-slate-900">${order.orderNumber}</td>
                <td class="p-4 text-sm text-slate-600 font-medium">${customerDisplayName}</td>
                <td class="p-4 text-sm text-slate-500">${orderDate}</td>
                <td class="p-4 text-sm font-medium text-slate-900">$${order.totalAmount.toFixed(2)}</td>
                <td class="p-4 text-xs">
                    <span class="px-2.5 py-1 rounded-full border font-semibold ${statusBadgeClass}">
                        ${order.status}
                    </span>
                </td>
                <td class="p-4 text-sm text-right">
                    <select onchange="updateStatus(${order.id}, this.value)" class="p-1 text-xs border border-slate-200 rounded-md outline-none bg-white font-medium text-slate-700 cursor-pointer shadow-xs focus:ring-1 focus:ring-indigo-500">
                        <option value="PROCESSING" ${order.status === 'PROCESSING' ? 'selected' : ''}>Processing</option>
                        <option value="SHIPPED" ${order.status === 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                        <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                    </select>
                </td>
            </tr>
        `;
        tbody.insertAdjacentHTML("beforeend", row);
    });

    document.getElementById("total-orders-count").textContent = orders.length;
    document.getElementById("processing-count").textContent = processingCount;
    document.getElementById("total-revenue").textContent = `$${totalRevenue.toFixed(2)}`;
}

// 4. Modal Open/Close Controls
function openCheckoutModal() {
    loadCheckoutFormOptions();
    document.getElementById("checkout-modal").classList.remove("hidden");
}

function closeCheckoutModal() {
    document.getElementById("checkout-modal").classList.add("hidden");
    document.getElementById("checkout-form").reset();
}

// 5. Handle Form Submit - POST to Checkout API
document.getElementById("checkout-form").addEventListener("submit", async function(e) {
    e.preventDefault();

    const submitBtn = document.getElementById("place-order-btn");
    const customerId = parseInt(document.getElementById("checkout-customer").value);
    const productId = parseInt(document.getElementById("checkout-product").value);
    const quantity = parseInt(document.getElementById("checkout-quantity").value);

    if (!customerId || !productId || !quantity) {
        alert("Please make sure all options are selected completely.");
        return;
    }

    const checkoutData = {
        customerId: customerId,
        items: [{ productId: productId, quantity: quantity }]
    };

    try {
        submitBtn.disabled = true;
        submitBtn.innerText = "Processing...";

        const response = await fetch(`${API_ORDERS}/checkout`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getAuthHeaders()
            },
            body: JSON.stringify(checkoutData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Insufficient inventory stock!");
        }

        alert("Order placed successfully!");
        closeCheckoutModal();

        setTimeout(() => { fetchOrders(); }, 200);
    } catch (error) {
        alert("Transaction Failed: " + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = "Place Order";
    }
});

// 🔴 ADMIN STATUS SELECTOR CONTROLLER FUNCTION
async function updateStatus(orderId, newStatus) {
    try {
        const response = await fetch(`${API_ORDERS}/${orderId}/status`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                ...getAuthHeaders()
            },
            body: JSON.stringify({ status: newStatus })
        });

        if (!response.ok) throw new Error("Could not update transaction status state.");
        console.log(`✅ Order #${orderId} set to: ${newStatus}`);
    } catch (error) {
        alert("Action Failed: " + error.message);
        fetchOrders();
    }
}

// Real-Time SSE Event Stream Subscription Management
const eventSource = new EventSource(`${API_ORDERS}/stream`);
eventSource.onmessage = function(event) {
    setTimeout(() => { fetchOrders(); }, 200);
};
eventSource.onerror = function(err) {
    console.warn("SSE connection lost. Reconnecting...");
};