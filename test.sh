#!/bin/bash

# Script para ejecutar TODOS los tests del proyecto

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo ""
echo "╔═══════════════════════════════════════════════════════════════════════════╗"
echo "║                   ✅ EJECUTANDO SUITE COMPLETA DE TESTS                  ║"
echo "║                                                                           ║"
echo "║  • 9 Tests Legacy (TDA, ABB, Cliente, etc)                               ║"
echo "║  • 38 Casos de Uso (CU)                                                  ║"
echo "║  • 4 Edge Case Tests                                                     ║"
echo "║  • 6 Performance Tests                                                   ║"
echo "║  • TOTAL: 57 Tests                                                       ║"
echo "║                                                                           ║"
echo "╚═══════════════════════════════════════════════════════════════════════════╝"
echo ""

echo "📦 Compilando código fuente..."
javac -cp "lib/gson-2.10.1.jar" -d out src/**/*.java 2>&1 | grep -i "error" || echo "✓ Código fuente compilado"

echo ""
echo "🧪 Compilando tests legacy (sin JUnit)..."
javac -cp "out:lib/gson-2.10.1.jar" -d out \
  test/TDATest.java \
  test/ABBTest.java \
  test/ClienteTest.java \
  test/GestorClientesTest.java \
  test/JsonLoaderTest.java \
  test/ColaSolicitudesTest.java \
  test/DistanciaTest.java \
  test/AmistadTest.java \
  test/Iteracion2Test.java 2>&1 | grep -i "error" || echo "✓ Tests legacy compilados"

echo ""
echo "📋 Compilando tests de casos de uso, edge cases y performance..."
javac -cp "out:lib/gson-2.10.1.jar" -d out \
  test/casos_de_uso/*.java \
  test/casos_de_uso/edge_cases/*.java \
  test/casos_de_uso/performance/*.java 2>&1 | grep -i "error" || echo "✓ Tests adicionales compilados"

echo ""
FAILED=0

# ═══════════════════════════════════════════════════════════════════════════
echo "═════════════════════════════════════════════════════════════════════════════"
echo "  EJECUTANDO TESTS LEGACY"
echo "═════════════════════════════════════════════════════════════════════════════"
echo ""

# 1. TDATest
echo "[1/9] Ejecutando TDATest..."
java -ea -cp out:"lib/gson-2.10.1.jar" TDATest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 2. ABBTest
echo "[2/9] Ejecutando ABBTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" ABBTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 3. ClienteTest
echo "[3/9] Ejecutando ClienteTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" ClienteTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 4. GestorClientesTest
echo "[4/9] Ejecutando GestorClientesTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" GestorClientesTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 5. JsonLoaderTest
echo "[5/9] Ejecutando JsonLoaderTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" JsonLoaderTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 6. ColaSolicitudesTest
echo "[6/9] Ejecutando ColaSolicitudesTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" ColaSolicitudesTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 7. DistanciaTest
echo "[7/9] Ejecutando DistanciaTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" DistanciaTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 8. AmistadTest
echo "[8/9] Ejecutando AmistadTest..."
java -ea -cp out:"lib/gson-2.10.1.jar" AmistadTest
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# 9. Iteracion2Test
echo "[9/9] Ejecutando Iteracion2Test..."
java -ea -cp out:"lib/gson-2.10.1.jar" Iteracion2Test
if [ $? -ne 0 ]; then FAILED=1; fi
echo ""

# ═══════════════════════════════════════════════════════════════════════════
echo "═════════════════════════════════════════════════════════════════════════════"
echo "  EJECUTANDO CASOS DE USO"
echo "═════════════════════════════════════════════════════════════════════════════"
echo ""

java -ea -cp out:"lib/gson-2.10.1.jar" CasosDeUsoTests
if [ $? -ne 0 ]; then FAILED=1; fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════════"
echo "  EJECUTANDO EDGE CASE TESTS"
echo "═════════════════════════════════════════════════════════════════════════════"
echo ""

java -ea -cp out:"lib/gson-2.10.1.jar" EdgeCaseTests
if [ $? -ne 0 ]; then FAILED=1; fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════════"
echo "  EJECUTANDO PERFORMANCE TESTS"
echo "═════════════════════════════════════════════════════════════════════════════"
echo ""

java -ea -cp out:"lib/gson-2.10.1.jar" PerformanceTests
if [ $? -ne 0 ]; then FAILED=1; fi

# ═════════════════════════════════════════════════════════════════════════════
echo ""
echo "╔═══════════════════════════════════════════════════════════════════════════╗"
if [ $FAILED -ne 0 ]; then
    echo "║                    ❌ ALGUNOS TESTS FALLARON                              ║"
    echo "╚═══════════════════════════════════════════════════════════════════════════╝"
    echo ""
    exit 1
else
    echo "║              ✅ SUITE COMPLETA EJECUTADA EXITOSAMENTE                   ║"
    echo "╚═══════════════════════════════════════════════════════════════════════════╝"
    echo ""
    exit 0
fi
