// Try to get products from localStorage If nothing is stored, we will use default data
let products = JSON.parse(localStorage.getItem("products")) || [
    { id: 1, name: "Pro Laptop", price: 55000, stock: 5, category: "electronics" },
    { id: 2, name: "Cotton Shirt", price: 1500, stock: 10, category: "clothing" },
    { id: 3, name: "JS Guide Book", price: 500, stock: 0, category: "books" },
    { id: 4, name: "Smart Watch", price: 2000, stock: 3, category: "accessories" },
    { id: 5, name: "Bluetooth Buds", price: 3000, stock: 12, category: "electronics" },
    { id: 6, name: "Denim Jeans", price: 2500, stock: 4, category: "clothing" },
    { id: 7, name: "Data Structures", price: 800, stock: 2, category: "books" },
    { id: 8, name: "Leather Belt", price: 1200, stock: 15, category: "accessories" },
    { id: 9, name: "Gaming Mouse", price: 4500, stock: 1, category: "electronics" },
    { id: 10, name: "Travel Backpack", price: 3500, stock: 0, category: "accessories" }
];