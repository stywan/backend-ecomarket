# 📦 Backend Ecomarket — Arquitectura de Microservicios

Este repositorio contiene la **arquitectura backend** de **Ecomarket**, desarrollada con **Java 17**, **Spring Boot** y un modelo **multi-módulo Maven** para facilitar la gestión y el despliegue de microservicios independientes.

---

## 📂 Estructura del proyecto

```txt
ecomarket-backend/           # Proyecto padre (multi-módulo)
├── pom.xml                
├── auth-service/     
├── catalog-product-service/ 
├── cart-order-service/    
├── payment-service/     
├── shipping-service/      
└── .gitignore
```
## 🧩 Descripción de los microservicios
```txt
•Auth-service: Gestión de usuarios, roles y autenticación.
•Catalog-product-service: Administración de productos y categorías.
•Cart-order-service: Lógica del carrito de compras y flujo de pedidos.
•Payment-service: Procesamiento de pagos.
•Shipping-service: Gestión de envíos.
```
## ⚙️ Tecnologías
```txt
•Lenguaje: Java 17
•Framework: Spring Boot 3.5.x
•Build tool: Maven (multi-módulo)
•Control de versiones: Git + GitHub
```
