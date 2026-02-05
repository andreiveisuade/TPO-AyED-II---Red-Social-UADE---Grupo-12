#!/bin/bash

# Script para compilar y ejecutar tests

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo "Compilando tests..."
javac -cp lib/gson-2.10.1.jar -d out test/TDATest.java src/tda/*.java src/modelo/*.java src/servicio/*.java src/util/*.java src/interfaces/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    echo ""
    echo "Ejecutando tests con assertions habilitadas..."
    echo ""
    java -ea -cp out:lib/gson-2.10.1.jar TDATest
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Todos los tests pasaron exitosamente"
        exit 0
    else
        echo ""
        echo "❌ Algunos tests fallaron"
        exit 1
    fi
else
    echo "❌ Error de compilación"
    exit 1
fi
