-- ========================================
-- SCRIPT RÁPIDO PARA ARREGLAR user1@gmail.com
-- Ejecuta esto en tu base de datos PostgreSQL
-- ========================================

-- Paso 1: Ver si existe tu usuario
SELECT '1. Verificando usuario user1@gmail.com...' AS paso;
SELECT * FROM users WHERE mail = 'user1@gmail.com';

-- Paso 2: Ver si tienes empresa
SELECT '2. Verificando empresa de user1@gmail.com...' AS paso;
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';

-- Paso 3: Crear empresa si no existe
SELECT '3. Creando empresa (si no existe)...' AS paso;
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
VALUES ('NIF_USER1_2025', 'Empresa User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com', NULL, 0, false)
ON CONFLICT (nif) DO NOTHING;

-- Verificar que se creó
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';

-- Paso 4: Ver tus anuncios actuales
SELECT '4. Tus anuncios actuales...' AS paso;
SELECT id, descripcion, categoria, especificacion, nif_empresa
FROM anuncios
ORDER BY creado_en DESC
LIMIT 10;

-- Paso 5: Actualizar TODOS los anuncios sin empresa válida
SELECT '5. Actualizando anuncios...' AS paso;
UPDATE anuncios
SET nif_empresa = 'NIF_USER1_2025'
WHERE nif_empresa NOT IN (SELECT nif FROM empresa)
   OR nif_empresa IS NULL;

-- Mostrar cuántos se actualizaron
SELECT '   → Anuncios actualizados' AS info;

-- Paso 6: Verificar resultado final
SELECT '6. Verificación final - Anuncios con empresa:' AS paso;
SELECT
    a.id,
    LEFT(a.descripcion, 40) AS descripcion,
    a.categoria,
    a.especificacion,
    a.nif_empresa,
    e.mail AS empresa_email,
    CASE
        WHEN e.mail IS NOT NULL THEN '✓ OK'
        ELSE '✗ ERROR'
    END AS estado
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC
LIMIT 10;

-- ========================================
-- LISTO! Ahora:
-- 1. Reinicia la aplicación
-- 2. Ve a Búsquedas
-- 3. Tus anuncios deberían aparecer
-- ========================================

