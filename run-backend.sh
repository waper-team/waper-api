#!/bin/bash
export MONGODB_URI="mongodb+srv://cartuperez_db_user:Cartuperez19@wapercluster.u0wqisa.mongodb.net/waperdb?retryWrites=true&w=majority&appName=wapercluster"
export MONGODB_DATABASE="waperdb"
export API_USER="admin"
export API_PASSWORD="1234"
export JWT_SECRET="waper-api-token-secret-super-safe-key-2026-najdorf"
export JWT_EXPIRATION_SECONDS=3600
export CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000,http://localhost:8080"

echo "Variables de entorno de Atlas y JWT cargadas con éxito."
echo "Levantando backend con Maven..."
./mvnw spring-boot:run
