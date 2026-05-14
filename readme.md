# initial project structure
src/main/java/com/gannah/ecommerce/
│
├── controller/         → handles HTTP requests
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── OrderController.java
│   └── CartController.java
│
├── service/            → business logic
│   ├── AuthService.java
│   ├── ProductService.java
│   ├── OrderService.java
│   └── CartService.java
│
├── repository/         → database queries (JPA)
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── CartRepository.java
│
├── model/              → your database entities
│   ├── User.java
│   ├── Product.java
│   ├── Category.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Payment.java
│   └── Review.java
│
├── dto/                → what you send/receive in requests
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── ProductRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── ProductResponse.java
│
├── security/           → JWT & Spring Security config
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   └── SecurityConfig.java
│
└── exception/          → error handling
├── GlobalExceptionHandler.java
└── ResourceNotFoundException.java