# 🔍 DIAGNÓSTICO: Botón de Chat No Aparece

## ❓ Problema

El botón "💬 Chatear" no aparece en las tarjetas de búsqueda, solo se ve el precio y "Ver detalles".

## 🎯 Causa Más Probable

Los anuncios **NO tienen `empresaEmail`** configurado. Esto sucede cuando:

1. El anuncio no está vinculado a ninguna empresa en la BD
2. La empresa existe pero no tiene campo `mail`
3. El JOIN entre `anuncios` y `empresa` no encuentra coincidencias

## 🔍 Cómo Verificar

### Paso 1: Ejecutar la aplicación

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="icai.dtc.isw.ui.JVentana"
```

### Paso 2: Hacer una búsqueda

1. Inicia sesión con cualquier usuario
2. Ve a 🔎 Búsquedas
3. Selecciona cualquier categoría

### Paso 3: Mirar la consola

Verás algo como esto para cada anuncio:

```
DEBUG Anuncio ID: abc123
  - NIF Anuncio: NIF_EMPRESA_1
  - NIF Usuario actual: null
  - Es propio: false
  - Email empresa: null  ← ⚠️ ESTE ES EL PROBLEMA
  - Mostrar botón: false
  → ❌ BOTÓN NO AGREGADO
```

## ✅ Solución

### Si `Email empresa: null`

El anuncio NO tiene empresa con email. Necesitas:

#### Opción A: Ejecutar el script SQL de arreglo

```sql
-- 1. Ver qué anuncios NO tienen empresa con email
SELECT a.id, a.descripcion, a.nif_empresa, e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail IS NULL;

-- 2. Crear empresas para esos anuncios
-- Ejemplo: Si hay un anuncio con nif_empresa = 'NIF123' sin empresa
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
VALUES ('NIF123', 'Empresa Test', 'Hogar y reparaciones', 'Madrid', 'empresa@test.com', NULL, 0, false);

-- 3. O actualizar los anuncios para usar una empresa existente
UPDATE anuncios 
SET nif_empresa = (SELECT nif FROM empresa WHERE mail = 'empresa@existente.com' LIMIT 1)
WHERE nif_empresa NOT IN (SELECT nif FROM empresa);
```

#### Opción B: Usar el script automático

Ejecuta el archivo **`ARREGLAR_USER1.sql`** que creé anteriormente:

```bash
psql -h ec2-13-60-40-17.eu-north-1.compute.amazonaws.com -U postgres -d postgres -f ARREGLAR_USER1.sql
```

### Si `Email empresa: empresa@algo.com` (tiene email)

Entonces el problema es otro. Verifica:

```
  - Es propio: true  ← Si es true, no se muestra el botón
```

Esto significa que el anuncio ES TUYO. El botón no se muestra en tus propios anuncios.

**Solución**: Busca anuncios de OTRAS empresas para ver el botón.

## 📋 Script SQL Completo de Diagnóstico

```sql
-- Ver TODOS los anuncios con su estado de email
SELECT 
    a.id,
    LEFT(a.descripcion, 30) AS descripcion,
    a.nif_empresa,
    e.mail AS empresa_email,
    CASE 
        WHEN e.mail IS NULL THEN '❌ SIN EMAIL - No habrá botón'
        WHEN e.mail IS NOT NULL THEN '✅ CON EMAIL - Habrá botón'
    END AS estado_boton
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC;
```

## 🔧 Solución Rápida para Testing

Si quieres probar el botón rápidamente:

```sql
-- 1. Crear una empresa de prueba
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
VALUES ('NIF_PRUEBA_001', 'Empresa Prueba', 'Hogar y reparaciones', 'Madrid', 'prueba@test.com', NULL, 0, false);

-- 2. Crear un anuncio de esa empresa
INSERT INTO anuncios (id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa, creado_en, actualizado_en)
VALUES ('ANUNCIO_PRUEBA_001', 'Servicio de prueba para testing', 50.00, 'Hogar y reparaciones', 'Electricidad', 'Madrid', 'NIF_PRUEBA_001', NOW(), NOW());

-- 3. Ahora cuando busques, ESTE anuncio SÍ tendrá botón de chat
```

## 📊 Checklist de Verificación

- [ ] Ejecuté la aplicación y vi los logs en consola
- [ ] Vi el mensaje "DEBUG Anuncio ID: ..."
- [ ] Identifiqué si `Email empresa` es `null` o tiene valor
- [ ] Si es null, ejecuté el script SQL para crear/vincular empresas
- [ ] Reinicié la aplicación
- [ ] Hice una búsqueda de nuevo
- [ ] Ahora SÍ veo el botón "💬 Chatear"

## 🎯 Resumen

**El botón NO aparece porque:**
- `empresaEmail` del anuncio es `null`
- La condición `!esPropio && anuncio.getEmpresaEmail() != null` es `false`

**Solución:**
1. Asegúrate de que los anuncios tienen `nif_empresa` válido
2. Asegúrate de que existe una empresa con ese NIF en la tabla `empresa`
3. Asegúrate de que esa empresa tiene un `mail` configurado
4. El JOIN automáticamente pondrá el email en los anuncios

---

**Ejecuta la aplicación, mira los logs y verás exactamente cuál es el problema para cada anuncio.** Los logs te dirán si falta el email o si el anuncio es tuyo.

