-- Oracle · Paso 1: crea el usuario (esquema) del ejercicio, una sola vez.
-- Ejecútalo conectado como SYSTEM a la PDB (XEPDB1 en Oracle XE 21c,
-- FREEPDB1 en Oracle Free 23ai), por ejemplo desde SQL Developer.
CREATE USER pokerap IDENTIFIED BY pokerap QUOTA UNLIMITED ON users;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO pokerap;
