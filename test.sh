#!/bin/bash

# Script para compilar y ejecutar todos los tests

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

# Fuentes comunes
SRC_FILES="src/tda/*.java src/modelo/*.java src/servicio/*.java src/util/*.java src/interfaces/*.java"
CP="lib/gson-2.10.1.jar"
JAVA_OPTS="-ea -Dtest.quiet=true"
FAILED=0

echo "══════════════════════════════════════════════════"
echo "  TESTS - RED SOCIAL UADE"
echo "══════════════════════════════════════════════════"
echo ""

# ─── Función auxiliar ───
run_test() {
    local name=$1
    local src=$2
    local class=$3
    javac -cp "$CP" -d out $src $SRC_FILES 2>&1
    if [ $? -eq 0 ]; then
        java $JAVA_OPTS -cp out:"$CP" $class
        if [ $? -ne 0 ]; then FAILED=1; fi
    else
        echo "  [FAIL] Error compilando $name"
        FAILED=1
    fi
    echo ""
}

# ─── Tests unitarios ───
run_test "TDATest" "test/TDATest.java" "TDATest"
run_test "ABBTest" "test/ABBTest.java" "ABBTest"
run_test "ClienteTest" "test/ClienteTest.java" "ClienteTest"
run_test "GestorClientesTest" "test/GestorClientesTest.java" "GestorClientesTest"
run_test "JsonLoaderTest" "test/JsonLoaderTest.java" "JsonLoaderTest"
run_test "ColaSolicitudesTest" "test/ColaSolicitudesTest.java" "ColaSolicitudesTest"
run_test "DistanciaTest" "test/DistanciaTest.java" "DistanciaTest"
run_test "AmistadTest" "test/AmistadTest.java" "AmistadTest"
run_test "PersistenciaRoundTripTest" "test/PersistenciaRoundTripTest.java" "PersistenciaRoundTripTest"

# ─── Suites ───
javac -cp "$CP" -d out test/casos_de_uso/*.java test/casos_de_uso/edge_cases/*.java test/casos_de_uso/performance/*.java $SRC_FILES 2>&1
if [ $? -eq 0 ]; then
    java $JAVA_OPTS -cp out:"$CP" CasosDeUsoTests
    if [ $? -ne 0 ]; then FAILED=1; fi
    echo ""

    java $JAVA_OPTS -cp out:"$CP" EdgeCaseTests
    if [ $? -ne 0 ]; then FAILED=1; fi
    echo ""

    java $JAVA_OPTS -cp out:"$CP" PerformanceTests
    if [ $? -ne 0 ]; then FAILED=1; fi
else
    echo "  [FAIL] Error compilando suites"
    FAILED=1
fi

echo ""
echo "══════════════════════════════════════════════════"
if [ $FAILED -ne 0 ]; then
    echo "  ALGUNOS TESTS FALLARON"
    echo "══════════════════════════════════════════════════"
    exit 1
else
    echo "  TODOS LOS TESTS PASARON"
    echo "══════════════════════════════════════════════════"
    exit 0
fi
