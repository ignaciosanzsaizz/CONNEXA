-- ================================================
-- SCRIPT DE DIAGNÓSTICO PARA BÚSQUEDAS
-- Ejecuta cada sección en tu cliente PostgreSQL
-- ================================================

-- === 1. VERIFICAR USUARIO user1@gmail.com ===
SELECT 'Usuario user1@gmail.com:' AS info;
SELECT * FROM users WHERE mail = 'user1@gmail.com';

-- === 2. VERIFICAR EMPRESA DE user1@gmail.com ===
SELECT 'Empresa de user1@gmail.com:' AS info;
SELECT nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado
FROM empresa
WHERE mail = 'user1@gmail.com';

-- === 3. ANUNCIOS DE user1@gmail.com (directo por NIF) ===
SELECT 'Anuncios de user1@gmail.com (directo):' AS info;
SELECT
    a.id,
    a.descripcion,
    a.categoria,
    a.especificacion,
    a.precio,
    a.ubicacion,
    a.nif_empresa,
    a.creado_en
FROM anuncios a
WHERE a.nif_empresa IN (SELECT nif FROM empresa WHERE mail = 'user1@gmail.com')
ORDER BY a.creado_en DESC;

-- === 4. ANUNCIOS CON LEFT JOIN (como lo hace el search) ===
SELECT 'Anuncios con LEFT JOIN:' AS info;
SELECT
    a.id,
    a.descripcion,
    a.categoria,
    a.especificacion,
    a.nif_empresa,
    e.mail AS empresa_email,
    e.empresa AS empresa_nombre
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC
LIMIT 10;

-- === 5. ANUNCIOS SIN EMPRESA VINCULADA ===
SELECT 'Anuncios SIN empresa vinculada (nif_empresa no existe en tabla empresa):' AS info;
SELECT
    a.id,
    a.descripcion,
    a.nif_empresa AS nif_no_existe
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.nif IS NULL;

-- === 6. TODAS LAS EMPRESAS ===
SELECT 'Todas las empresas:' AS info;
SELECT nif, empresa, mail
FROM empresa
ORDER BY empresa;

-- === 7. CATEGORÍAS Y ESPECIFICACIONES DISPONIBLES ===
SELECT 'Categorías y especificaciones en anuncios:' AS info;
SELECT DISTINCT categoria, especificacion
FROM anuncios
ORDER BY categoria, especificacion;

-- ================================================
-- SOLUCIÓN: Si user1@gmail.com NO tiene empresa
-- ================================================
/*
-- Paso 1: Verificar si existe la empresa
SELECT * FROM empresa WHERE mail = 'user1@gmail.com';

-- Si NO existe, créala (reemplaza 'MI_NIF' con un NIF único):
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail, calidad, num_trabajos, verificado)
VALUES ('NIF_USER1_123', 'Empresa de User1', 'Hogar y reparaciones', 'Madrid', 'user1@gmail.com', NULL, 0, false);

-- Paso 2: Actualiza tus anuncios para usar ese NIF
-- (reemplaza 'ID1' e 'ID2' con los IDs reales de tus anuncios)
UPDATE anuncios
SET nif_empresa = 'NIF_USER1_123'
WHERE id IN ('ID_ANUNCIO_1', 'ID_ANUNCIO_2');

-- Paso 3: Verifica que ahora funcionan
SELECT a.id, a.descripcion, e.mail AS empresa_email
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
WHERE e.mail = 'user1@gmail.com';
*/

