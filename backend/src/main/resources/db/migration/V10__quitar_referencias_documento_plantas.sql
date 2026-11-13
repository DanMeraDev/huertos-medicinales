-- Quita de los textos del catálogo inicial de plantas (V8) las referencias al documento del MSP
-- (" (Anexo 5)", " (Anexo 3)", " (Anexo 2.1)" y " (Tabla 3)"), que no deben mostrarse al usuario (issue #133).
-- Se usa REPLACE en lugar de reescribir los textos completos para conservar las ediciones hechas con CP-CU06.

UPDATE plantas
SET usos_medicinales         = REPLACE(REPLACE(REPLACE(REPLACE(usos_medicinales,
                                   ' (Anexo 5)', ''), ' (Anexo 3)', ''), ' (Anexo 2.1)', ''), ' (Tabla 3)', ''),
    preparacion_dosificacion = REPLACE(REPLACE(REPLACE(REPLACE(preparacion_dosificacion,
                                   ' (Anexo 5)', ''), ' (Anexo 3)', ''), ' (Anexo 2.1)', ''), ' (Tabla 3)', ''),
    contraindicaciones       = REPLACE(REPLACE(REPLACE(REPLACE(contraindicaciones,
                                   ' (Anexo 5)', ''), ' (Anexo 3)', ''), ' (Anexo 2.1)', ''), ' (Tabla 3)', '');
