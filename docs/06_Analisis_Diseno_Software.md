# Análisis de Diseño Software (SOLID & GRASP)

Este documento presenta una auditoría arquitectónica basada en los principios de diseño de software orientado a objetos, identificando fortalezas y áreas de oportunidad en la implementación actual.

---

## 1. Evaluación de Principios SOLID

### 1.1. SRP: Single Responsibility Principle
**Estado**: Cumplimiento Alto.
*   **Evidencia**: Segregación estricta entre capas (`Vista`, `Servicio`, `Modelo`).
*   **Hallazgo**: La clase `GestorClientes` tiende a acumular múltiples responsabilidades (gestión de clientes, historial y relaciones).
*   **Acción**: Se mitiga mediante la delegación en clases de soporte como `HistorialAcciones` y `ColaSolicitudes`.

### 1.2. OCP: Open/Closed Principle
**Estado**: Cumplimiento Medio-Alto.
*   **Evidencia**: Uso de Enumeraciones (`TipoAccion`) para definir comandos.
*   **Oportunidad**: La implementación de métodos de búsqueda ad-hoc (`buscarPorScoring`, `buscarPorNombre`) viola OCP. Se sugiere migrar a patrón **Strategy** (`CriterioBusqueda`) en futuras iteraciones.

### 1.3. LSP: Liskov Substitution Principle
**Estado**: Cumplimiento Total.
*   **Evidencia**: No existe herencia de implementación que altere el comportamiento base. El polimorfismo se maneja exclusivamente a través de interfaces.

### 1.4. ISP: Interface Segregation Principle
**Estado**: Cumplimiento Total.
*   **Evidencia**: Interfaces atómicas (`IPila`, `ICola`, `IDiccionario`) sin métodos superfluos forzados a los implementadores.

### 1.5. DIP: Dependency Inversion Principle
**Estado**: Cumplimiento Alto.
*   **Evidencia**: Inyección de dependencias a través de constructores en `GestorClientes` (aunque `Sesion` se accede vía Singleton estático, lo cual es una violación aceptada por simplicidad académica).

---

## 2. Evaluación de Patrones GRASP

### 2.1. Information Expert
*   `Cliente`: Gestiona su propia lógica de seguimiento y estado de solicitudes.
*   `Diccionario`: Encapsula la lógica de hashing y resolución de colisiones.

### 2.2. Creator
*   `GestorClientes`: Responsable del ciclo de vida de `Cliente` y `Accion`.

### 2.3. Controller
*   `Menu`: Actúa como controlador de fachada, coordinando la interacción entre el usuario y la capa de servicios.

### 2.4. Low Coupling / High Cohesion
*   **Logro**: Desacoplamiento efectivo entre la lógica de persistencia (JsonLoader) y el modelo de dominio.
*   **Riesgo**: Acoplamiento directo a `java.util.Scanner` en la capa de Vista, dificultando pruebas automatizadas de UI.

---

## 3. Oportunidades de Refactorización

### 3.1. Abstracción de I/O
*   **Problema**: Dependencia explícita de `System.in/out`.
*   **Propuesta**: Introducir interfaz `IEntradaSalida` para permitir testabilidad y futura migración a GUI.

### 3.2. Patrón Repository
*   **Problema**: Mezcla de lógica de negocio y acceso a datos en `GestorClientes`.
*   **Propuesta**: Extraer `RepositorioClientes` para aislar operaciones CRUD puras.

### 3.3. Patrón Observer
*   **Problema**: Falta de reactividad ante cambios de estado.
*   **Propuesta**: Implementar sistema de eventos para notificar a la UI sobre cambios en el modelo (e.g., al completar una carga masiva).

---

## 4. Conclusión Técnica

La arquitectura actual demuestra una madurez adecuada para los requerimientos funcionales de la Iteración 1. Las violaciones detectadas (acoplamiento I/O, Singleton) son decisiones conscientes de compensación (trade-offs) entre pureza académica y viabilidad de implementación en los plazos establecidos.
