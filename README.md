# Trabajo Práctico Obligatorio: Red Social

**Materia:** Algoritmos y Estructuras de Datos II  
**Institución:** UADE  
**Lenguaje:** Java 17+  

---

## 1. Descripción del Proyecto

Este proyecto consiste en la implementación de una red social simplificada capaz de gestionar un volumen masivo de usuarios (1.000.000+) y sus interacciones. El objetivo principal es demostrar la correcta aplicación de **Tipos Abstractos de Datos (TDAs)** propios, optimización algorítmica y patrones de diseño orientados a objetos.

La arquitectura prioriza la eficiencia en tiempo de ejecución, logrando una complejidad **O(1)** para las operaciones más frecuentes mediante el uso extensivo de tablas hash y estructuras enlazadas en memoria.

---

## 2. Arquitectura y Diseño

El sistema sigue una arquitectura en capas con flujo unidireccional para garantizar el desacoplamiento y la mantenibilidad.

### 2.1. Estructura de Capas

1.  **Vista (UI)**: Interfaz de consola que captura la interacción del usuario.
2.  **Lógica (Servicio/Modelo)**: Implementa las reglas de negocio, gestión de sesiones y validaciones.
3.  **Datos (TDA)**: Estructuras de datos genéricas implementadas desde cero.

### 2.2. Patrones de Diseño Aplicados

*   **Singleton (`Sesion`)**: Centraliza el estado de autenticación y el historial de acciones, garantizando una única instancia activa.
*   **Facade (`GestorClientes`)**: Provee una interfaz simplificada para subsistemas complejos, ocultando la gestión interna de los TDAs.
*   **Value Object**: Utilizado en entidades inmutables como `SolicitudSeguimiento` para asegurar la integridad referencial.
*   **Command (Historial)**: Implementado mediante pilas para permitir la reversión (`undo`) y repetición (`redo`) de acciones.

---

## 3. Estructuras de Datos (TDAs)

Se han implementado las siguientes estructuras genéricas sin utilizar el framework de colecciones de Java (`java.util`):

| TDA | Implementación | Complejidad Temporal | Uso en el Sistema |
| :--- | :--- | :--- | :--- |
| **Diccionario** | Tabla Hash con encadenamiento | **O(1)** (promedio) | Indexación de usuarios por ID. |
| **Pila** | Lista enlazada simple (LIFO) | **O(1)** | Historial de acciones y mecanismo de deshacer/rehacer. |
| **Cola** | Lista enlazada con punteros (FIFO) | **O(1)** | Gestión de solicitudes de seguimiento pendientes. |

---

## 4. Estrategia de Persistencia

Para conciliar el requerimiento de persistencia con la necesidad de alto rendimiento, se adoptó una estrategia de **"Carga Inicial / Guardado Final"**:

1.  **Carga (Startup)**: Al iniciar, el dataset JSON completo se deserializa y carga en memoria RAM. Costo: **O(N)**.
2.  **Ejecución**: Todas las operaciones transaccionales ocurren en memoria, garantizando tiempos de respuesta constantes **O(1)**.
3.  **Persistencia (Shutdown)**: Al finalizar la ejecución, el estado actual de la memoria se serializa y persiste en disco. Costo: **O(N)**.

Esta decisión de diseño elimina la latencia de I/O durante la interacción del usuario.

---

## 5. Instrucciones de Ejecución

### Requisitos Previos
*   JDK 17 o superior.
*   Entorno tipo Unix (Linux/macOS) o Windows con Bash.

### Compilación y Ejecución
Se provee un script de automatización para compilar el código fuente y ejecutar la clase principal.

```bash
# Otorgar permisos de ejecución (primera vez)
chmod +x run.sh

# Ejecutar
./run.sh
```

### Ejecución de Pruebas Unitarias
El proyecto incluye una suite de tests exhaustiva para validar la lógica de negocio y las estructuras de datos.

```bash
./test.sh
```

---

## 6. Documentación Adicional

La documentación técnica detallada se encuentra en el directorio `/docs`:

*   `docs/ARQUITECTURA.md`: Análisis profundo de decisiones arquitectónicas.
*   `docs/05_Analisis_Algoritmico.md`: Justificación teórica de la complejidad Big O.
*   `docs/TDAs.md`: Especificación formal de las estructuras implementadas.
