# 🔍 GUÍA: Por qué no aparecen mis anuncios en Búsquedas

## Problema
Los anuncios creados con user1@gmail.com no aparecen en el apartado de Búsquedas.

## Pasos para Diagnosticar

### 1️⃣ Ejecutar la aplicación y ver los logs

He agregado logging detallado. Cuando ejecutes la aplicación:

```bash
# Ejecutar la aplicación
cd "c:\Users\lopez\Documents\comillas\ISW comillas\CONNEXA"
mvn clean compile
mvn exec:java -Dexec.mainClass="icai.dtc.isw.ui.JVentana"
```

### 2️⃣ Hacer una búsqueda

1. Inicia sesión con cualquier usuario
2. Ve a la pestaña **🔎 Búsquedas**
3. Selecciona una categoría (o déjala en la primera)
4. Selecciona un trabajo (o déjalo en el primero)

**Mira la consola**, verás algo como:

```
=== BÚSQUEDA ===
Categoría: Hogar y reparaciones
Trabajo: Electricidad
CalidadMin: 1
Resultados encontrados: 2
  - ID: abc123, Desc: Servicio..., Cat: Hogar y reparaciones, Esp: Electricidad, Email: user1@gmail.com
  - ID: def456, Desc: Otro..., Cat: Hogar y reparaciones, Esp: Electricidad, Email: user2@gmail.com
```

### 3️⃣ Interpretar los resultados

#### ✅ Caso 1: Aparecen resultados pero no los ves en pantalla
```
Resultados encontrados: 2
  - ID: abc123, ...
  - ID: def456, ...
```

**Problema**: Error en la UI al renderizar las tarjetas
**Solución**: Revisa la consola buscando excepciones de Java/Swing

#### ❌ Caso 2: NO aparecen resultados (0 encontrados)
```
Resultados encontrados: 0
```

**Problema**: Los anuncios no están en la BD o no cumplen con los filtros

**Verifica en la base de datos**:

```sql
-- Ver TODOS tus anuncios
SELECT a.id, a.descripcion, a.categoria, a.especificacion, a.nif_empresa
FROM anuncios a
JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail = 'user1@gmail.com';
```

Si esto devuelve tus 2 anuncios, entonces el problema está en los **filtros**.

#### ⚠️ Caso 3: Aparece "Email: null"
```
Resultados encontrados: 2
  - ID: abc123, ..., Email: null
```

**Problema**: La empresa no está vinculada correctamente

**Solución**:

```sql
-- Verificar que tu empresa existe
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';

-- Si no existe, créala:
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail)
VALUES ('TU_NIF', 'Mi Empresa', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com');

-- Luego actualiza tus anuncios para usar ese NIF:
UPDATE anuncios 
SET nif_empresa = 'TU_NIF'
WHERE id IN ('id_anuncio_1', 'id_anuncio_2');
```

### 4️⃣ Verificar los filtros

El sistema filtra por **categoría** y **especificación**. Si tus anuncios tienen valores diferentes a los que seleccionas, no aparecerán.

**Ejemplo**:
- Seleccionas: "Hogar y reparaciones" / "Electricidad"
- Tus anuncios: "Salud, belleza y cuidados" / "Peluquería"
- **Resultado**: ❌ No aparecen

**Solución**: 
- Opción A: Cambia los filtros en la búsqueda
- Opción B: Actualiza la categoría/especificación de tus anuncios en la BD

### 5️⃣ Script de diagnóstico SQL

Ejecuta esto en tu base de datos PostgreSQL:

```sql
-- 1. Ver todos los anuncios
SELECT 
    a.id,
    a.descripcion,
    a.categoria,
    a.especificacion,
    a.nif_empresa,
    e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC;

-- 2. Ver específicamente los de user1@gmail.com
SELECT 
    a.id,
    a.descripcion,
    a.categoria,
    a.especificacion
FROM anuncios a
JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail = 'user1@gmail.com';

-- 3. Ver si existe la empresa de user1@gmail.com
SELECT nif, empresa, mail 
FROM empresa 
WHERE mail = 'user1@gmail.com';

-- 4. Ver anuncios sin empresa vinculada
SELECT a.id, a.descripcion, a.nif_empresa
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.nif IS NULL;
```

## Soluciones Rápidas

### Solución 1: Crear empresa si no existe

```sql
-- Verificar si tu usuario tiene empresa
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';

-- Si no existe, créala
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail)
SELECT 
    'NIF_' || md5(random()::text), -- Genera un NIF único
    'Empresa de ' || username,
    'Hogar y reparaciones',
    'Madrid',
    mail
FROM users 
WHERE mail = 'user1@gmail.com';
```

### Solución 2: Vincular anuncios existentes a la empresa

```sql
-- Obtén el NIF de tu empresa
SELECT nif FROM empresa WHERE mail = 'user1@gmail.com';

-- Actualiza tus anuncios (reemplaza 'TU_NIF' con el valor real)
UPDATE anuncios 
SET nif_empresa = 'TU_NIF'
WHERE nif_empresa IS NULL 
   OR nif_empresa NOT IN (SELECT nif FROM empresa);
```

### Solución 3: Ver qué categorías/especificaciones tienes

```sql
-- Ver las categorías de tus anuncios
SELECT DISTINCT categoria, especificacion
FROM anuncios a
JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail = 'user1@gmail.com';
```

Luego en la aplicación, selecciona exactamente esas categorías/especificaciones en los filtros.

## Checklist de Verificación

- [ ] La empresa de user1@gmail.com existe en la tabla `empresa`
- [ ] Los anuncios tienen `nif_empresa` que coincide con el NIF de la empresa
- [ ] Los anuncios tienen `categoria` y `especificacion` válidos
- [ ] Los filtros de búsqueda coinciden con la categoría/especificación de los anuncios
- [ ] Los logs en consola muestran los anuncios encontrados
- [ ] No hay excepciones en la consola

## Si Nada Funciona

Ejecuta este script Java de diagnóstico:

```bash
mvn compile exec:java -Dexec.mainClass="icai.dtc.isw.dao.DiagnosticarBusqueda"
```

Esto te mostrará:
- Todos los anuncios en la BD
- Cuáles tienen empresa vinculada
- Los anuncios específicos de user1@gmail.com
- Si el método `search()` los encuentra

---

**Nota**: El problema más común es que los anuncios no tienen empresa vinculada o la categoría/especificación no coincide con los filtros seleccionados.

