-- Script para crear la tabla de anuncios en PostgreSQL
-- Asegúrate de ejecutar este script en tu base de datos de AWS

-- Tabla de anuncios
CREATE TABLE IF NOT EXISTS anuncio (
    id VARCHAR(255) PRIMARY KEY,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio > 0),
    categoria VARCHAR(100) NOT NULL,
    especificacion TEXT NOT NULL,
    ubicacion VARCHAR(255) NOT NULL,
    nif_empresa VARCHAR(50) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Clave foránea hacia la tabla empresa (asumiendo que existe)
    CONSTRAINT fk_anuncio_empresa FOREIGN KEY (nif_empresa)
        REFERENCES empresa(nif)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX IF NOT EXISTS idx_anuncio_nif_empresa ON anuncio(nif_empresa);
CREATE INDEX IF NOT EXISTS idx_anuncio_categoria ON anuncio(categoria);
CREATE INDEX IF NOT EXISTS idx_anuncio_creado_en ON anuncio(creado_en DESC);

-- Trigger para actualizar automáticamente la columna actualizado_en
CREATE OR REPLACE FUNCTION update_actualizado_en_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_anuncio_actualizado_en
    BEFORE UPDATE ON anuncio
    FOR EACH ROW
    EXECUTE FUNCTION update_actualizado_en_column();

-- Comentarios sobre la tabla
COMMENT ON TABLE anuncio IS 'Tabla que almacena los anuncios creados por las empresas';
COMMENT ON COLUMN anuncio.id IS 'Identificador único del anuncio (UUID)';
COMMENT ON COLUMN anuncio.descripcion IS 'Descripción detallada del anuncio';
COMMENT ON COLUMN anuncio.precio IS 'Precio del servicio o producto anunciado';
COMMENT ON COLUMN anuncio.categoria IS 'Categoría del anuncio';
COMMENT ON COLUMN anuncio.especificacion IS 'Especificaciones adicionales del anuncio';
COMMENT ON COLUMN anuncio.ubicacion IS 'Ubicación donde se ofrece el servicio/producto';
COMMENT ON COLUMN anuncio.nif_empresa IS 'NIF de la empresa que publica el anuncio';
COMMENT ON COLUMN anuncio.creado_en IS 'Fecha y hora de creación del anuncio';
COMMENT ON COLUMN anuncio.actualizado_en IS 'Fecha y hora de la última actualización';

