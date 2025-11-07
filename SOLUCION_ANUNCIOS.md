# 🔥 PROBLEMA IDENTIFICADO: Anuncios sin Empresa

## El Problema Real

Tus anuncios de `user1@gmail.com` **NO aparecen en búsquedas** porque:

### Estructura de Tablas
```
users (mail, username, password)
  ↓ (relacionada por mail)
empresa (nif [PK], empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
  ↓ (relacionada por nif)
anuncios (id, descripcion, categoria, especificacion, nif_empresa [FK], ...) ← FK a empresa(nif)
```

**Columnas de la tabla empresa:**
- `nif` (PK) - character varying
- `empresa` - character varying (nombre de la empresa)
- `sector` - character varying
- `ubicacion` - character varying
- `mail` - character varying (email del usuario dueño)
- `calidad` - real (puede ser NULL)
- `num_trabajos` - integer (puede ser NULL)
- `verificado` - boolean

### El Query de Búsqueda
```sql
SELECT a.*, e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE categoria = ? AND especificacion = ?
```

### ⚠️ Por Qué No Funcionan Tus Anuncios

Cuando creas un anuncio:
1. Se guarda con un `nif_empresa`
2. Pero ese `nif` **NO existe** en la tabla `empresa`
3. El LEFT JOIN encuentra el anuncio pero `empresa_email` es NULL
4. Los anuncios sí aparecen, pero sin botón de chat

## 🔍 Diagnóstico

Ejecuta este SQL en tu base de datos:

```sql
-- Ver si tienes empresa
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';
```

**Resultado esperado:**
- ❌ Si no devuelve nada → **NO tienes empresa creada** (ESTE ES TU PROBLEMA)
- ✅ Si devuelve una fila → Tienes empresa

Luego verifica tus anuncios:

```sql
-- Ver tus anuncios
SELECT a.id, a.descripcion, a.nif_empresa
FROM anuncios a;

-- Ver si ese NIF existe en empresa
SELECT nif FROM empresa WHERE nif = 'EL_NIF_DE_TU_ANUNCIO';
```

## ✅ SOLUCIÓN
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
### Paso 1: Crear Tu Empresa
('NIF_USER1', 'Mi Empresa User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com', NULL, 0, false);
Primero, verifica que tu usuario existe:
```sql
SELECT * FROM users WHERE mail = 'user1@gmail.com';
```

Luego crea tu empresa vinculada a tu usuario:
```sql
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail)
VALUES 
('NIF_USER1', 'Mi Empresa User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com');
```

**IMPORTANTE**: El campo `mail` debe ser exactamente `'user1@gmail.com'` (el mismo de tu usuario).

### Paso 2: Actualizar Tus Anuncios

Primero, encuentra los IDs de tus anuncios:
```sql
SELECT id, descripcion FROM anuncios 
ORDER BY creado_en DESC 
LIMIT 5;
```

Luego actualízalos para que usen el NIF de tu empresa:
```sql
UPDATE anuncios 
SET nif_empresa = 'NIF_USER1'
WHERE id IN ('ID_ANUNCIO_1', 'ID_ANUNCIO_2');
```

**Reemplaza**:
- `'NIF_USER1'` → El NIF que pusiste al crear la empresa
- `'ID_ANUNCIO_1'`, `'ID_ANUNCIO_2'` → Los IDs reales de tus anuncios

### Paso 3: Verificar que Funcionó

```sql
SELECT 
    a.id,
    a.descripcion,
    a.categoria,
    a.especificacion,
    a.nif_empresa,
    e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail = 'user1@gmail.com';
```
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
VALUES ('NIF_USER1_2025', 'Empresa User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com', NULL, 0, false)
ON CONFLICT (nif) DO NOTHING;
## 🎯 Solución Completa (Copy-Paste)

```sql
-- 1. Verificar usuario
SELECT * FROM users WHERE mail = 'user1@gmail.com';

-- 2. Crear empresa (si no existe)
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail)
VALUES ('NIF_USER1_2025', 'Empresa User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com')
ON CONFLICT DO NOTHING;

-- 3. Ver los IDs de tus anuncios
SELECT id, descripcion, nif_empresa 
FROM anuncios 
ORDER BY creado_en DESC;

-- 4. Actualizar TODOS los anuncios que no tengan empresa válida
UPDATE anuncios 
SET nif_empresa = 'NIF_USER1_2025'
WHERE nif_empresa NOT IN (SELECT nif FROM empresa)
   OR nif_empresa IS NULL;

-- O actualizar solo tus anuncios específicos (reemplaza los IDs):
-- UPDATE anuncios 
-- SET nif_empresa = 'NIF_USER1_2025'
-- WHERE id IN ('abc123', 'def456');

-- 5. Verificar que ahora funcionan
SELECT 
    a.id,
    a.descripcion,
    e.mail AS empresa_email,
    CASE 
        WHEN e.mail IS NOT NULL THEN 'OK - Aparecerá en búsquedas'
        ELSE 'ERROR - No tiene empresa'
    END AS estado
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC;
```

## 📱 Después de Ejecutar el SQL

1. **Reinicia la aplicación** (para limpiar cualquier caché)
2. Inicia sesión con cualquier usuario
3. Ve a **🔎 Búsquedas**
4. Selecciona la categoría que usaste en tus anuncios (ej: "Hogar y reparaciones")
5. Tus anuncios deberían aparecer con el botón **💬 Chatear**

## 🔄 Para Futuros Anuncios

Antes de crear anuncios nuevos:
1. Asegúrate de tener empresa creada (ve a "Mi Empresa" en la app)
2. Completa el perfil de empresa
3. Luego crea anuncios

La app debería usar automáticamente el NIF de tu empresa.

## ❓ Si Sigue Sin Funcionar

Ejecuta esto y envíame el resultado:

```sql
-- Ver TODO para diagnosticar
SELECT 'USUARIOS:' AS tabla;
SELECT * FROM users WHERE mail = 'user1@gmail.com';

SELECT 'EMPRESAS:' AS tabla;
SELECT * FROM empresa;

SELECT 'ANUNCIOS CON EMPRESA:' AS tabla;
SELECT a.id, a.descripcion, a.nif_empresa, e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC;
```

Y también mira los logs de la consola cuando ejecutes la app y hagas una búsqueda (debería mostrar cuántos resultados encuentra).

