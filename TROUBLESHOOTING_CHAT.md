# Guía de Resolución de Problemas - Chat

## Error al Crear Chat

Si ves "Error al crear chat" al presionar el botón 💬 Chatear, sigue estos pasos:

### 1. Ejecutar la aplicación y ver los logs

La aplicación ahora tiene logging detallado. Cuando presiones el botón de chat, verás en la consola:

```
Iniciando chat:
  Cliente: tu@email.com
  Empresa: empresa@email.com
  Anuncio ID: 123

ChatDAO.getOrCreateChat llamado:
  clienteEmail: tu@email.com
  empresaEmail: empresa@email.com
  anuncioId: 123
```

### 2. Posibles Causas del Error

#### Causa 1: El anuncio no tiene email de empresa
**Síntoma**: El mensaje dice "La empresa no tiene email configurado"

**Solución**: Verifica que la empresa está correctamente creada en la tabla `empresa`:

```sql
-- Ver empresas
SELECT nif, mail FROM empresa;

-- Ver anuncios y sus empresas
SELECT a.id, a.descripcion, a.nif_empresa, e.mail 
FROM anuncios a 
LEFT JOIN empresa e ON a.nif_empresa = e.nif;
```

Si algún anuncio tiene `nif_empresa` pero no hay empresa con ese NIF, debes:
1. Crear la empresa en la tabla `empresa`
2. O actualizar el `nif_empresa` del anuncio para que coincida con una empresa existente

#### Causa 2: Error de clave foránea (FK)
**Síntoma**: El log muestra error SQL al hacer INSERT

**Solución**: Verifica que:
- El `cliente_email` existe en la tabla `users`
- El `empresa_email` existe en la tabla `users`  
- El `anuncio_id` existe en la tabla `anuncios`

```sql
-- Verificar que los emails existen en users
SELECT mail FROM users WHERE mail = 'email@a.verificar';

-- Verificar que el anuncio existe
SELECT id FROM anuncios WHERE id = 'id_del_anuncio';
```

#### Causa 3: La tabla chats no existe
**Síntoma**: Error SQL "relation chats does not exist"

**Solución**: Ejecuta el script SQL para crear las tablas:

```sql
-- Crear tabla chats
CREATE TABLE IF NOT EXISTS chats (
    id SERIAL PRIMARY KEY,
    cliente_email VARCHAR(255) NOT NULL,
    empresa_email VARCHAR(255) NOT NULL,
    anuncio_id VARCHAR(255) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_email) REFERENCES users(mail) ON DELETE CASCADE,
    FOREIGN KEY (empresa_email) REFERENCES users(mail) ON DELETE CASCADE,
    FOREIGN KEY (anuncio_id) REFERENCES anuncios(id) ON DELETE CASCADE,
    UNIQUE(cliente_email, empresa_email, anuncio_id)
);

-- Crear tabla mensajes_chat
CREATE TABLE IF NOT EXISTS mensajes_chat (
    id SERIAL PRIMARY KEY,
    chat_id INTEGER NOT NULL,
    remitente_email VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    enviado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leido BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    FOREIGN KEY (remitente_email) REFERENCES users(mail) ON DELETE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_chats_cliente ON chats(cliente_email);
CREATE INDEX IF NOT EXISTS idx_chats_empresa ON chats(empresa_email);
CREATE INDEX IF NOT EXISTS idx_mensajes_chat ON mensajes_chat(chat_id);
CREATE INDEX IF NOT EXISTS idx_mensajes_enviado ON mensajes_chat(enviado_en);
```

### 3. Script de Diagnóstico

Ejecuta este SQL para diagnosticar el problema:

```sql
-- 1. Ver todos los anuncios con sus empresas
SELECT 
    a.id AS anuncio_id,
    a.descripcion,
    a.nif_empresa,
    e.mail AS empresa_email,
    CASE 
        WHEN e.mail IS NULL THEN 'SIN EMAIL - NO SE PUEDE CHATEAR'
        ELSE 'OK'
    END AS estado
FROM anuncios a
LEFT JOIN empresa e ON a.nif_empresa = e.nif
ORDER BY a.creado_en DESC;

-- 2. Ver empresas sin mail
SELECT nif, empresa 
FROM empresa 
WHERE mail IS NULL OR mail = '';

-- 3. Ver chats existentes
SELECT 
    c.id,
    c.cliente_email,
    c.empresa_email,
    c.anuncio_id,
    c.creado_en
FROM chats c
ORDER BY c.creado_en DESC;
```

### 4. Solución Rápida

Si tienes anuncios sin empresa_email, puedes:

**Opción A**: Crear empresas para esos anuncios
```sql
-- Ejemplo: Crear empresa para un NIF que no existe
INSERT INTO empresa (nif, empresa, sector, ubicacion, mail)
VALUES ('12345678A', 'Mi Empresa', 'Servicios', 'Madrid', 'empresa@email.com');
```

**Opción B**: Actualizar anuncios para que usen empresas existentes
```sql
-- Ver empresas disponibles
SELECT nif, mail FROM empresa;

-- Actualizar anuncio para usar una empresa existente
UPDATE anuncios 
SET nif_empresa = 'NIF_EXISTENTE'
WHERE id = 'ID_DEL_ANUNCIO';
```

### 5. Verificar que Funcionó

Después de arreglar, recarga la aplicación y:
1. Ve a Búsquedas
2. Busca anuncios
3. Deberías ver el botón "💬 Chatear" solo en anuncios con empresa_email
4. Click en "💬 Chatear" debería abrir el chat sin errores

### 6. Si Sigue Sin Funcionar

Revisa los logs en la consola cuando presiones el botón. El error específico te dirá exactamente qué está fallando.

Los mensajes más comunes son:
- `empresaEmail es null o vacío` → El anuncio no tiene empresa asociada
- `Error SQL: constraint violation` → Problema con claves foráneas
- `chatCtrl.getOrCreateChat devolvió null` → Error en la creación del chat en BD

