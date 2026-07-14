const API_ORDERS = "http://localhost:8080/api/orders";
const API_CUSTOMERS = "http://localhost:8080/api/customers";
const API_PRODUCTS = "http://localhost:8080/api/products";

// Autoload data on page start
document.addEventListener("DOMContentLoaded", () => {
    fetchOrders();
    loadCheckoutFormOptions();
});

// 1. Fetch Orders and render table
async function fetchOrders() {
    try {
        const response = await fetch(API_ORDERS);
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
            fetch(API_CUSTOMERS),
            fetch(API_PRODUCTS)
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

// 3. Render Dashboard Data
function renderDashboard(orders) {
    const tbody = document.getElementById("orders-table-body");
    tbody.innerHTML = "";

    let totalRevenue = 0;
    let processingCount = 0;

    orders.forEach(order => {
        totalRevenue += order.totalAmount;
        if (order.status === "PROCESSING") processingCount++;

        const orderDate = new Date(order.orderDate).toLocaleString();
        const statusBadgeClass = order.status === "PROCESSING"
            ? "bg-amber-50 text-amber-700 border-amber-200"
            : "bg-emerald-50 text-emerald-700 border-emerald-200";

        const row = `
            <tr class="hover:bg-slate-50/70 transition-colors">
                <td class="p-4 font-mono text-sm font-bold text-slate-900">${order.orderNumber}</td>
                <td class="p-4 text-sm text-slate-600">${order.customer ? order.customer.name : 'Guest User'}</td>
                <td class="p-4 text-sm text-slate-500">${orderDate}</td>
                <td class="p-4 text-sm font-medium text-slate-900">$${order.totalAmount.toFixed(2)}</td>
                <td class="p-4 text-xs">
                    <span class="px-2.5 py-1 rounded-full border font-semibold ${statusBadgeClass}">
                        ${order.status}
                    </span>
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
    loadCheckoutFormOptions(); // Refresh items and live stock values
    document.getElementById("checkout-modal").classList.remove("hidden");
}

function closeCheckoutModal() {
    document.getElementById("checkout-modal").classList.add("hidden");
    document.getElementById("checkout-form").reset();
}

// 5. Handle Form Submit - POST to Checkout API
document.getElementById("checkout-form").addEventListener("submit", async function(e) {
    e.preventDefault();

    const customerId = parseInt(document.getElementById("checkout-customer").value);
    const productId = parseInt(document.getElementById("checkout-product").value);
    const quantity = parseInt(document.getElementById("checkout-quantity").value);

    // Build standard OrderRequest DTO structure expected by Spring
    const checkoutData = {
        customerId: customerId,
        items: [
            {
                productId: productId,
                quantity: quantity
            }
        ]
    };

    try {
        const response = await fetch(`${API_ORDERS}/checkout`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(checkoutData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Insufficient inventory stock!");
        }

        alert("Order placed successfully! Check your Spring Boot terminal console for low-stock event logs!");
        closeCheckoutModal();
        fetchOrders(); // Reload table data live
    } catch (error) {
        alert("Transaction Failed: " + error.message);
    }
});