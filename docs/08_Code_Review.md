# Auditoría de Calidad de Código Software

**Identificación del Proyecto**: Sistema de Gestión de Red Social (TPO)  
**Fecha de Revisión**: 05 de Febrero de 2026  
**Alcance**: Análisis estático de 41 unidades de compilación (Java).

---

## 1. Resumen de Auditoría

La evaluación técnica arroja un índice de calidad de **8.0/10**, destacando la robustez arquitectónica y la eficiencia algorítmica.

### 1.1. Fortalezas Identificadas
*   **Arquitectura**: Implementación estricta de capas con desacoplamiento efectivo.
*   **Diseño**: Aplicación canónica de patrones Singleton y Facade.
*   **Eficiencia**: Implementación de TDAs con complejidad temporal constante O(1).
*   **Documentación**: Invariantes de representación formalmente definidos.

### 1.2. Acciones Correctivas Implementadas
1.  **Integridad de Datos**: Actualización de invariantes en entidad `Cliente`.
2.  **Robustez**: Incorporación de validación de nulidad en función de hashing (`Diccionario`).
3.  **Mantenibilidad**: Eliminación de redundancias en comentarios y código muerto.

---

## 2. Hallazgos Críticos y Resoluciones

### 2.1. Inconsistencia en Invariante (Resuelto)
*   **Ubicación**: `src/modelo/Cliente.java`.
*   **Descripción**: La documentación de invariantes referenciaba restricciones de capacidad obsoletas (arrays fijos) no concordantes con la implementación actual (Diccionario dinámico).
*   **Resolución**: Redefinición formal de los invariantes en la documentación técnica (`docs/07_Invariantes.md`).

### 2.2. Manejo de Nulidad en Hashing (Resuelto)
*   **Ubicación**: `src/tda/Diccionario.java`.
*   **Descripción**: La función `hash(K key)` carecía de validación de precondición, exponiendo el sistema a `NullPointerException`.
*   **Resolución**: Implementación de cláusula de guarda con lanzamiento explícito de `IllegalArgumentException`.

### 2.3. Código Muerto (Resuelto)
*   **Ubicación**: Múltiples archivos.
*   **Descripción**: Presencia de bloques de código comentado y constantes obsoletas.
*   **Resolución**: Depuración completa del código fuente.

---

## 3. Deuda Técnica y Oportunidades de Mejora

Se identifican las siguientes áreas para optimización futura, clasificadas por prioridad.

### 3.1. Prioridad Media: Optimización de Tipos Genéricos
*   **Observación**: El método `getSiguiendo()` realiza conversiones de tipo (`String` a `int`) que podrían evitarse mediante el uso estricto de genéricos en la interfaz `IDiccionario`.
*   **Recomendación**: Refactorizar firma de `obtenerClaves()` para retornar `K[]` en lugar de `Object[]` o `String[]`.

### 3.2. Prioridad Baja: Segregación de Interfaces
*   **Observación**: La clase `GestorClientes` asume múltiples responsabilidades (CRUD, Relaciones, Historial), mostrando signos de alta cohesión pero potencial acoplamiento.
*   **Recomendación**: Evaluar la segregación en `RepositorioClientes` (Acceso a Datos) y `ServicioRelaciones` (Lógica de Dominio).

---

## 4. Métricas de Calidad

| Indicador | Valor | Evaluación |
| :--- | :--- | :--- |
| Unidades de Compilación | 41 | ✅ Adecuado |
| Complejidad Ciclomática Promedio | 2.5 | ✅ Baja |
| Cobertura de Pruebas Unitarias | 22% | ⚠️ Mejorable |
| Adherencia a Convenciones (Lint) | 95% | ✅ Alta |

---

## 5. Conclusión

El sistema demuestra un alto nivel de madurez técnica para la etapa actual del ciclo de vida. La arquitectura base es sólida y escalable. Las correcciones aplicadas han mitigado los riesgos de estabilidad más significativos. Se recomienda focalizar la siguiente iteración en el incremento de la cobertura de pruebas automatizadas.
