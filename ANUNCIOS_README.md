# Documentación del Flujo de Creación de Anuncios

## Descripción General

Se ha implementado un flujo completo para crear anuncios en la aplicación Java Swing, siguiendo el patrón MVC y utilizando la infraestructura de comunicación cliente-servidor existente.

## Componentes Implementados

### 1. Modelo de Dominio
- **Archivo**: `src/main/java/icai/dtc/isw/domain/Anuncio.java`
- **Descripción**: Clase que representa un anuncio con todos sus atributos.
- **Campos**:
  - `id` (String): Identificador único (UUID)
  - `descripcion` (String): Descripción del anuncio
  - `precio` (Double): Precio del servicio/producto
  - `categoria` (String): Categoría del anuncio
  - `especificacion` (String): Especificaciones adicionales
  - `ubicacion` (String): Ubicación del servicio/producto
  - `nifEmpresa` (String): NIF de la empresa que publica
  - `creadoEn` (Timestamp): Fecha de creación
  - `actualizadoEn` (Timestamp): Fecha de última actualización

### 2. Capa de Acceso a Datos (DAO)
- **Archivo**: `src/main/java/icai/dtc/isw/dao/AnuncioDAO.java`
- **Métodos principales**:
  - `insert(Anuncio)`: Inserta un nuevo anuncio
  - `findById(String)`: Busca un anuncio por ID
  - `findByNifEmpresa(String)`: Lista anuncios de una empresa
  - `findAll()`: Lista todos los anuncios

### 3. Controlador
- **Archivo**: `src/main/java/icai/dtc/isw/controler/AnuncioControler.java`
- **Métodos principales**:
  - `createAnuncio(...)`: Crea un anuncio con validaciones
  - `getAnuncio(String)`: Obtiene un anuncio por ID
  - `getAnunciosByEmpresa(String)`: Lista anuncios de una empresa
  - `getAllAnuncios()`: Lista todos los anuncios

### 4. API del Cliente
- **Archivo**: `src/main/java/icai/dtc/isw/ui/AnuncioApi.java`
- **Descripción**: Interfaz para comunicarse con el servidor mediante sockets.
- **Métodos**:
  - `createAnuncio(...)`: Envía petición para crear un anuncio
  - `getAnuncio(String)`: Obtiene un anuncio del servidor
  - `getAnunciosByEmpresa(String)`: Lista anuncios de una empresa

### 5. Interfaz de Usuario
- **Archivo**: `src/main/java/icai/dtc/isw/ui/CrearAnuncioPanel.java`
- **Descripción**: Diálogo modal para crear un nuevo anuncio.
- **Campos del formulario**:
  - Descripción (área de texto)
  - Precio (campo numérico)
  - Categoría (ComboBox con opciones predefinidas)
  - Especificación (campo de texto)
  - Ubicación (campo de texto)
  - NIF Empresa (campo de solo lectura, auto-rellenado)

### 6. Integración en EmpresaPanel
- **Archivo**: `src/main/java/icai/dtc/isw/ui/EmpresaPanel.java`
- **Cambios**: El botón "Poner anuncio" ahora abre el diálogo `CrearAnuncioPanel`.

### 7. Servidor
- **Archivo**: `src/main/java/icai/dtc/isw/server/SocketServer.java`
- **Endpoints añadidos**:
  - `/anuncio/create`: Crea un nuevo anuncio
  - `/anuncio/get`: Obtiene un anuncio por ID
  - `/anuncio/list`: Lista anuncios de una empresa

### 8. Cliente
- **Archivo**: `src/main/java/icai/dtc/isw/client/Client.java`
- **Cambios**: Añadidos casos para manejar las respuestas del servidor:
  - `/anuncioCreateResponse`
  - `/anuncioGetResponse`
  - `/anuncioListResponse`

## Instalación y Configuración

### 1. Base de Datos

**✅ LA TABLA YA EXISTE EN AWS CON EL NOMBRE "anuncios"**

El código ha sido configurado para usar la tabla existente `anuncios` en la base de datos PostgreSQL de AWS.

Si necesitas recrear la tabla o verificar su estructura, puedes consultar el archivo `create_anuncio_table.sql` como referencia (nota: el script usa "anuncio" singular, pero el código Java usa "anuncios" plural que es la tabla real en AWS).

**Estructura esperada de la tabla `anuncios`:**
- `id` VARCHAR(255) PRIMARY KEY
- `descripcion` TEXT NOT NULL
- `precio` DECIMAL NOT NULL
- `categoria` VARCHAR NOT NULL
- `especificacion` TEXT NOT NULL
- `ubicacion` VARCHAR NOT NULL
- `nif_empresa` VARCHAR NOT NULL (FK a empresa.nif)
- `creado_en` TIMESTAMP
- `actualizado_en` TIMESTAMP

### 2. Compilación

Compila el proyecto usando Maven:

```bash
mvn clean compile
```

### 3. Ejecución

1. **Iniciar el servidor**:
   ```bash
   java -cp target/classes icai.dtc.isw.server.SocketServer
   ```

2. **Iniciar el cliente**:
   ```bash
   java -cp target/classes icai.dtc.isw.ui.AppMovilMock
   ```

## Flujo de Uso

1. El usuario inicia sesión en la aplicación.
2. Navega a "Mi Empresa".
3. Si ya tiene el perfil de empresa completado, verá el botón "Poner anuncio".
4. Al hacer clic en "Poner anuncio", se abre un diálogo modal.
5. Rellena todos los campos obligatorios:
   - **Descripción**: Texto largo describiendo el anuncio
   - **Precio**: Valor numérico (ej: 99.99)
   - **Categoría**: Selección de una lista desplegable
   - **Especificación**: Detalles adicionales
   - **Ubicación**: Dónde se ofrece el servicio/producto
   - **NIF Empresa**: Auto-rellenado (solo lectura)
6. Hace clic en "Crear anuncio".
7. El sistema valida los campos:
   - Ningún campo puede estar vacío
   - El precio debe ser un número válido mayor que cero
8. Si todo es correcto, se envía al servidor.
9. El servidor crea el anuncio en la base de datos.
10. Se muestra un mensaje de éxito y se cierra el diálogo.

## Validaciones Implementadas

### En el Cliente (CrearAnuncioPanel)
- Campos obligatorios no pueden estar vacíos
- El precio debe ser un número válido
- El precio debe ser mayor que cero

### En el Controlador (AnuncioControler)
- Validación de campos nulos o vacíos
- Validación de precio positivo
- Generación automática de UUID para el ID

### En la Base de Datos
- Restricción CHECK para precio > 0
- Clave foránea hacia la tabla empresa
- Timestamps automáticos para creado_en y actualizado_en
- Trigger para actualizar actualizado_en automáticamente

## Categorías Disponibles

1. Hogar y reparaciones
**Nombre de la tabla:** `anuncios` (plural)

2. Salud, belleza y cuidados
CREATE TABLE anuncios (
4. Eventos y ocio
5. Negocio y administración
6. Logística y movilidad
7. Tecnología y digital

## Estructura de la Tabla en PostgreSQL

```sql
CREATE TABLE anuncio (
    id VARCHAR(255) PRIMARY KEY,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio > 0),
    categoria VARCHAR(100) NOT NULL,
    especificacion TEXT NOT NULL,
    ubicacion VARCHAR(255) NOT NULL,
    nif_empresa VARCHAR(50) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nif_empresa) REFERENCES empresa(nif)
);
```

## Gestión de Errores

- Si falta algún campo: mensaje de advertencia
- Si el precio no es numérico: mensaje de error
- Si hay error en el guardado: mensaje de error con detalles
- Si todo es correcto: mensaje de éxito

## Notas Técnicas

- **Patrón de diseño**: MVC (Model-View-Controller)
- **Comunicación**: Cliente-Servidor mediante sockets
- **Serialización**: Java Serialization para objetos de dominio
- **Generación de ID**: UUID aleatorio en el controlador
- **Conexión a BD**: Lee configuración de `properties.xml`
- **Thread-safe**: Cada petición se maneja en un hilo separado en el servidor

## Extensiones Futuras

Posibles mejoras que se pueden implementar:

1. **Listado de anuncios**: Panel para ver todos los anuncios de una empresa
2. **Edición de anuncios**: Permitir modificar anuncios existentes
3. **Eliminación de anuncios**: Permitir borrar anuncios
4. **Búsqueda y filtrado**: Buscar anuncios por categoría, precio, ubicación
5. **Imágenes**: Añadir soporte para imágenes en los anuncios
6. **Estadísticas**: Panel con métricas sobre los anuncios publicados
7. **Notificaciones**: Alertar cuando se crea un nuevo anuncio en una categoría

## Solución de Problemas

### Error de conexión a la base de datos
- Verifica que la IP en `properties.xml` sea correcta
- Confirma que el servidor PostgreSQL esté activo
- Comprueba las credenciales de acceso

### El botón "Poner anuncio" no aparece
- Asegúrate de que el usuario tenga el perfil de empresa completado
- Verifica que el NIF de la empresa esté guardado en la BD

### Error al crear anuncio
- Revisa los logs del servidor para más detalles
- Verifica que la tabla `anuncio` exista en la base de datos
- Confirma que exista la clave foránea hacia la tabla `empresa`

## Contacto y Soporte

Para más información o soporte, contacta con el equipo de desarrollo.

