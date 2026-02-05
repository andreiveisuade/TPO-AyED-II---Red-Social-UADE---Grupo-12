# Reporte de Revisión de Calidad de Código
**Proyecto**: TPO_AyED_II  
**Fecha**: 2026-02-05  
**Archivos Analizados**: 41 archivos Java

---

## Resumen Ejecutivo

**Calificación General**: 8.0/10 (mejorado desde 7.5)

**Fortalezas**:
- Arquitectura en capas bien definida
- Uso correcto de patrones (Singleton, Facade)
- TDAs implementados correctamente con complejidad O(1)
- Documentación de invariantes actualizada ✅

**Correcciones Implementadas** (2026-02-05):
- ✅ Invariante de `Cliente.java` actualizado
- ✅ Validación de null en `Diccionario.hash()`
- ✅ Comentarios duplicados eliminados

**Áreas Pendientes de Mejora**:
- Manejo de excepciones silencioso en `buscarOCargar()`
- Conversión `String → int` innecesaria en `getSiguiendo()`

---

## 1. Problemas Críticos (Prioridad Alta)

### 1.1 Invariante Desactualizado en `Cliente.java` ✅ CORREGIDO

**Archivo**: `src/modelo/Cliente.java` (líneas 5-16)

**Problema**: Invariante describía array de tamaño 2, pero la implementación usa `Diccionario` sin límite.

**Solución Implementada**:
```java
INVARIANTE DE REPRESENTACIÓN:
- id > 0
- nombre != null && !nombre.trim().isEmpty()
- 0 <= scoring <= 100
- siguiendo != null
- No existen duplicados en siguiendo (garantizado por Diccionario)
- Ningún cliente se sigue a sí mismo
- solicitudesPendientes != null
```

**Estado**: ✅ Implementado + documentación actualizada en `docs/07_Invariantes.md`

---

### 1.2 Casting Inseguro en `GestorClientes.java` ⚠️ NO IMPLEMENTADO

**Archivo**: `src/servicio/GestorClientes.java` (líneas 120-139, 160-177, 190-194)

**Decisión**: No implementado por ser **sobre-ingeniería**. El `Diccionario<Integer, Cliente>` garantiza por diseño que solo contiene objetos `Cliente`. Agregar `instanceof` sería código defensivo innecesario que viola el principio de confianza en tipos genéricos.

**Justificación**: 
- El diccionario es privado y controlado
- No hay forma de insertar objetos no-Cliente
- El casting es seguro por construcción

---

### 1.3 Falta Validación de Null en `Diccionario.hash()` ✅ CORREGIDO

**Archivo**: `src/tda/Diccionario.java` (línea 32-37)

**Solución Implementada**:
```java
private int hash(K clave) {
    if (clave == null) {
        throw new IllegalArgumentException("La clave no puede ser null");
    }
    return Math.abs(clave.hashCode() % tabla.length);
}
```

**Estado**: ✅ Implementado. Previene `NullPointerException` con mensaje claro.

---

## 2. Problemas Moderados (Prioridad Media)

### 2.1 Código Comentado en `Cliente.java` ✅ CORREGIDO

**Archivo**: `src/modelo/Cliente.java` (línea 96-97)

**Estado**: ✅ Eliminado durante limpieza de comentarios duplicados.

---

### 2.2 Constantes Duplicadas en `Cliente.java`

**Archivo**: `src/modelo/Cliente.java` (líneas 21-23)

**Problema**:
```java
/* Constantes */
/* Constantes */
// Ya no hay límite máximo de usuarios a seguir
```

**Impacto**: Comentario duplicado sin valor.

**Solución**: Eliminar duplicado.

---

### 2.3 Método `getSiguiendo()` Ineficiente

**Archivo**: `src/modelo/Cliente.java` (líneas 81-94)

**Problema**:
```java
public int[] getSiguiendo() {
    String[] claves = siguiendo.obtenerClaves();  // O(n)
    int[] ids = new int[claves.length];
    for (int i = 0; i < claves.length; i++) {
        if (claves[i] != null) {
            try {
                ids[i] = Integer.parseInt(claves[i]);  // ⚠️ Conversión innecesaria
            } catch (NumberFormatException e) {
                ids[i] = 0;
            }
        }
    }
    return ids;
}
```

**Problema**: 
- `Diccionario<Integer, Boolean>` almacena `Integer` como clave, pero `obtenerClaves()` retorna `String[]`
- Conversión `String → int` innecesaria

**Solución**: Modificar `IDiccionario.obtenerClaves()` para soportar genéricos:
```java
// En IDiccionario
K[] obtenerClaves();

// En Cliente
public int[] getSiguiendo() {
    Integer[] claves = siguiendo.obtenerClaves();
    int[] ids = new int[claves.length];
    for (int i = 0; i < claves.length; i++) {
        ids[i] = claves[i];
    }
    return ids;
}
```

---

### 2.4 Manejo de Excepciones Silencioso

**Archivo**: `src/servicio/GestorClientes.java` (líneas 70-76)

**Problema**:
```java
try {
    new JsonLoader().cargarClienteEspecifico(id, DATA_PATH, this);
    cliente = clientes.obtener(id);
} catch (IOException e) {
    // Cliente no encontrado  ⚠️ Excepción silenciada
}
```

**Impacto**: Dificulta debugging. No distingue entre "archivo no existe" vs "error de lectura".

**Solución**:
```java
} catch (IOException e) {
    System.err.println("Error al cargar cliente " + id + ": " + e.getMessage());
}
```

---

## 3. Mejoras de Diseño (Prioridad Baja)

### 3.1 Violación Potencial de SRP en `GestorClientes`

**Archivo**: `src/servicio/GestorClientes.java`

**Problema**: `GestorClientes` tiene 25 métodos con responsabilidades mixtas:
- Gestión de clientes (CRUD)
- Gestión de relaciones (seguir/dejar de seguir)
- Gestión de historial (undo/redo)
- Búsquedas (por nombre, scoring, ID)

**Sugerencia**: Separar en:
- `RepositorioClientes`: CRUD + búsquedas
- `GestorRelaciones`: seguir/dejar de seguir
- `GestorHistorial`: undo/redo (ya existe `HistorialAcciones`, pero no se usa directamente)

---

### 3.2 Magic Numbers en `Diccionario.java`

**Archivo**: `src/tda/Diccionario.java` (línea 16)

**Problema**:
```java
private static final int CAPACIDAD_INICIAL = 64;
```

**Sugerencia**: Documentar por qué 64:
```java
// Capacidad inicial de 64 (2^6) para:
// - Distribución uniforme con operador módulo
// - Suficiente para carga típica del TP (< 100 clientes en memoria)
private static final int CAPACIDAD_INICIAL = 64;
```

---

### 3.3 Falta Encapsulación en `GestorClientes.getClientes()`

**Archivo**: `src/servicio/GestorClientes.java` (línea 179-181)

**Problema**:
```java
public Diccionario<Integer, Cliente> getClientes() {
    return clientes;  // ⚠️ Expone estructura interna
}
```

**Impacto**: Permite modificación externa sin control.

**Solución**: Retornar copia o hacer método privado si solo se usa internamente.

---

## 4. Buenas Prácticas Detectadas ✅

### 4.1 Uso Correcto de Singleton
```java
// Sesion.java
private static Sesion instancia;
private Sesion() {}  // Constructor privado
public static Sesion getInstancia() {
    if (instancia == null) {
        instancia = new Sesion();
    }
    return instancia;
}
```

### 4.2 Validación Centralizada
```java
// Validador.java - Evita duplicación
public static ResultadoValidacion validarNombre(String nombre) {
    if (nombre == null) {
        return ResultadoValidacion.error("El nombre no puede ser nulo");
    }
    // ...
}
```

### 4.3 Invariantes Documentados
```java
// Cliente.java
INVARIANTE DE REPRESENTACIÓN:
- id > 0
- nombre != null && !nombre.trim().isEmpty()
```

### 4.4 Complejidad O(1) en TDAs
```java
// Diccionario.java
public V obtener(K clave) {
    int indice = hash(clave);  // O(1)
    // Búsqueda en lista enlazada (O(1) amortizado con buen factor de carga)
}
```

---

## 5. Métricas de Código

| Métrica | Valor | Estado |
|---------|-------|--------|
| Total de archivos Java | 41 | ✅ |
| Archivos con tests | 9 | ⚠️ (22% cobertura) |
| Uso de `@SuppressWarnings` | 2 | ✅ (justificado) |
| TODOs pendientes | 0 | ✅ |
| FIXMEs pendientes | 0 | ✅ |
| Líneas promedio por método | ~15 | ✅ |
| Métodos por clase (promedio) | ~8 | ✅ |

---

## 6. Recomendaciones Priorizadas

### ✅ Completado (2026-02-05)
1. ✅ Actualizar invariante en `Cliente.java`
2. ✅ Agregar validación de null en `Diccionario.hash()`
3. ✅ Eliminar código comentado y duplicados

### Corto Plazo (Próxima iteración)
4. Mejorar manejo de excepciones en `buscarOCargar()`
5. Agregar tests para `Validador.java` y `ResultadoValidacion.java`
6. Optimizar `getSiguiendo()` con genéricos en `obtenerClaves()`

### Largo Plazo (Refactoring futuro)
7. Separar responsabilidades de `GestorClientes`
8. Hacer `obtenerClaves()` genérico en `IDiccionario`
9. Encapsular `getClientes()` o hacerlo privado

---

## 7. Conclusión

El proyecto tiene una **arquitectura sólida** con buenas prácticas de diseño (Singleton, Facade, validación centralizada). Los TDAs están bien implementados con complejidad O(1).

**Principales riesgos**:
- Invariantes desactualizados pueden causar confusión
- Casting inseguro puede generar excepciones en runtime
- Falta de validación de null en puntos críticos

**Próximo paso**: Implementar las 3 correcciones inmediatas (10-15 min de trabajo).
