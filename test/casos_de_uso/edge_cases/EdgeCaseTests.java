/*
EDGE CASE TESTS - Validar comportamientos límite y casos excepcionales

Prueba:
- Valores extremos (máximos, mínimos)
- Valores inválidos (null, vacío, negativo)
- Estados inconsistentes
- Límites del sistema
*/

public class EdgeCaseTests {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("         EDGE CASE TESTS - VALIDACIÓN DE LÍMITES");
        System.out.println("═".repeat(70) + "\n");

        ejecutarTest("EC_001_ValoresNull");
        ejecutarTest("EC_002_ValoresVacios");
        ejecutarTest("EC_003_ValoresNegativos");
        ejecutarTest("EC_004_LimitesScoring");

        // Resumen final
        System.out.println("\n" + "═".repeat(70));
        System.out.printf("EDGE CASES RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═".repeat(70) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void ejecutarTest(String nombreClase) {
        try {
            Class<?> clazz = Class.forName(nombreClase);
            java.lang.reflect.Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            testsPasados++;
            System.out.println("[OK] " + nombreClase);
        } catch (Exception e) {
            testsFallados++;
            System.err.println("[FAIL] " + nombreClase + ": " + e.getMessage());
        }
    }
}
