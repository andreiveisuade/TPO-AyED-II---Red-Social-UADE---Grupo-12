#!/bin/bash

# Script para compilar y ejecutar todos los tests

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

# Fuentes comunes
SRC_FILES="src/tda/*.java src/modelo/*.java src/servicio/*.java src/util/*.java src/interfaces/*.java"
CP="lib/gson-2.10.1.jar"
FAILED=0

echo "═══════════════════════════════════════════"
echo "   COMPILACIÓN Y EJECUCIÓN DE TESTS        "
echo "═══════════════════════════════════════════"
echo ""

# ─── 1. TDATest ───
echo "Compilando TDATest..."
javac -cp "$CP" -d out test/TDATest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" TDATest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando TDATest"
    FAILED=1
fi

echo ""

# ─── 2. ABBTest ───
echo "Compilando ABBTest..."
javac -cp "$CP" -d out test/ABBTest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" ABBTest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando ABBTest"
    FAILED=1
fi

echo ""

# ─── 3. ClienteTest ───
echo "Compilando ClienteTest..."
javac -cp "$CP" -d out test/ClienteTest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" ClienteTest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando ClienteTest"
    FAILED=1
fi

echo ""

# ─── 4. GestorClientesTest ───
echo "Compilando GestorClientesTest..."
javac -cp "$CP" -d out test/GestorClientesTest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" GestorClientesTest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando GestorClientesTest"
    FAILED=1
fi

echo ""

# ─── 5. JsonLoaderTest ───
echo "Compilando JsonLoaderTest..."
javac -cp "$CP" -d out test/JsonLoaderTest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" JsonLoaderTest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando JsonLoaderTest"
    FAILED=1
fi

echo ""

# ─── 6. ColaSolicitudesTest ───
echo "Compilando ColaSolicitudesTest..."
javac -cp "$CP" -d out test/ColaSolicitudesTest.java $SRC_FILES
if [ $? -eq 0 ]; then
    java -ea -cp out:"$CP" ColaSolicitudesTest
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "❌ Error compilando ColaSolicitudesTest"
    FAILED=1
fi

echo ""
echo "═══════════════════════════════════════════"
if [ $FAILED -ne 0 ]; then
    echo "❌ Algunos tests fallaron"
    exit 1
else
    echo "✅ Todos los tests pasaron exitosamente"
    exit 0
fi
