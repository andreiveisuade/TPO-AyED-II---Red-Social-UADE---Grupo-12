#!/bin/bash

# Directorio del proyecto
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

# Limpiar .class anteriores
echo "Limpiando build anterior..."
rm -rf out

# Compilar
echo "Compilando..."
cd src
javac -cp ../lib/gson-2.10.1.jar -d ../out Main.java tda/*.java modelo/*.java servicio/*.java vista/*.java interfaces/*.java util/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    echo ""
    cd ..
    java -cp out:lib/gson-2.10.1.jar Main
else
    echo "Error de compilación"
    exit 1
fi
