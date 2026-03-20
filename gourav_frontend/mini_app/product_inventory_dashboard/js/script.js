// Try to get products from localStorage
// If nothing is stored, use default data
let products = JSON.parse(localStorage.getItem("products")) || [
  { id: 1, name: "Laptop", price: 55000, stock: 5, category: "electronics" },
  { id: 2, name: "Shirt", price: 1500, stock: 10, category: "clothing" },
  { id: 3, name: "Book", price: 500, stock: 0, category: "books" },
  { id: 4, name: "Watch", price: 2000, stock: 3, category: "accessories" }
];