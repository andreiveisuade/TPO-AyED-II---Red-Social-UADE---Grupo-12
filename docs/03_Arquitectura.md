# Arquitectura del Sistema

Este documento detalla la arquitectura de software implementada, justificando las decisiones de diseño, los patrones aplicados y el cumplimiento de principios de ingeniería de software.

## 1. Estructura y Capas

El sistema adopta una arquitectura en capas estricta para garantizar la **separación de responsabilidades** (SoC) y facilitar la mantenibilidad. El flujo de dependencia es unidireccional descendente.

### Diagrama de Dependencias
```mermaid
VISTA --> SERVICIO --> MODELO --> TDA
```

1.  **Capa Vista (Presentación)**: Responsable de la interacción con el usuario a través de la consola. No contiene lógica de negocio.
2.  **Capa Servicio (Lógica de Negocio)**: Gestiona casos de uso complejos (`GestorClientes`). Actúa como fachada para el modelo de dominio.
3.  **Capa Modelo (Dominio)**: Representa las entidades del negocio (`Cliente`, `Sesion`) y sus reglas fundamentales.
4.  **Capa TDA (Infraestructura de Datos)**: Provee las estructuras de datos fundamentales (`Diccionario`, `Pila`, `Cola`) optimizadas para el problema.

## 2. Patrones de Diseño

Se han seleccionado patrones específicos para resolver problemas recurrentes de manera estándar y eficiente.

### 2.1. Singleton (Creacional)
*   **Componente**: `Sesion`
*   **Propósito**: Garantizar la existencia de una única instancia de sesión activa en todo el ciclo de vida de la aplicación.
*   **Justificación**: Centraliza el control de acceso y el historial de acciones del usuario autenticado, evitando inconsistencias de estado.

### 2.2. Facade (Estructural)
*   **Componente**: `GestorClientes`
*   **Propósito**: Proporcionar una interfaz unificada y simplificada para subsistemas complejos.
*   **Justificación**: Desacopla la capa de presentación de la complejidad de gestión de memoria, validaciones y acceso a datos.

### 2.3. Value Object (Estructural)
*   **Componentes**: `SolicitudSeguimiento`, `ResultadoValidacion`
*   **Propósito**: Encapsular un conjunto de valores inmutables que no requieren identidad propia.
*   **Justificación**: Aumenta la seguridad del código al prevenir modificaciones laterales no deseadas en objetos de transferencia de datos.

### 2.4. Static Utility
*   **Componentes**: `Validador`, `Terminal`
*   **Propósito**: Agrupar funciones de propósito general sin estado.

## 3. Principios de Ingeniería

### 3.1. SOLID
*   **SRP (Single Responsibility)**: Cada clase posee una única razón de cambio (e.g., `GestorClientes` no maneja I/O de consola).
*   **OCP (Open/Closed)**: Uso de enums (`TipoAccion`) y genéricos para permitir extensiones sin modificar código base.
*   **DIP (Dependency Inversion)**: Utilización de interfaces abstractas para definir contratos en los TDAs.

### 3.2. GRASP
*   **Information Expert**: La lógica (como `seguir()` o `encolar()`) reside en la clase que posee los datos (`Cliente`).
*   **Creator**: `GestorClientes` es responsable de instanciar nuevos objetos `Cliente` y `Accion`, ya que gestiona su ciclo de vida.
*   **Low Coupling / High Cohesion**: Las capas minimizan sus dependencias y las clases internas maximizan su enfoque funcional.

## 4. Características Técnicas Destacadas

### 4.1. Persistencia y Carga
Se optó por una estrategia híbrida "Memory-First" para cumplir con los requerimientos de persistencia sin sacrificar rendimiento:
*   **Carga Bootstrap**: Lectura secuencial O(N) al inicio.
*   **Operación In-Memory**: Todas las interacciones ocurren en RAM con estructuras de acceso O(1).
*   **Dureza al Cierre**: Volcado completo de estado O(N) al finalizar sesión.

### 4.2. Command Pattern (Historial)
El sistema de deshacer/rehacer se implementa mediante dos pilas complementarias (`historial` y `pilaRehacer`), permitiendo transiciones de estado reversibles con complejidad constante O(1).

## 5. Conclusión
La arquitectura elegida equilibra la simplicidad técnica requerida para un entorno educativo con prácticas profesionales de desarrollo (patrones de diseño, separación de capas), resultando en un sistema robusto, testeable y eficiente.
