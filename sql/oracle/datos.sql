-- Oracle · Paso 3: carga los 18 tipos. Ejecútalo conectado como pokerap,
-- con el archivo abierto en UTF-8 (por las tildes).
-- Los Pokémon NO se cargan aquí: la aplicación los importa desde
-- datos/pokedex-inicial.json la primera vez que encuentra la tabla vacía.
INSERT INTO tipo (nombre) VALUES ('Normal');
INSERT INTO tipo (nombre) VALUES ('Lucha');
INSERT INTO tipo (nombre) VALUES ('Volador');
INSERT INTO tipo (nombre) VALUES ('Veneno');
INSERT INTO tipo (nombre) VALUES ('Tierra');
INSERT INTO tipo (nombre) VALUES ('Roca');
INSERT INTO tipo (nombre) VALUES ('Bicho');
INSERT INTO tipo (nombre) VALUES ('Fantasma');
INSERT INTO tipo (nombre) VALUES ('Acero');
INSERT INTO tipo (nombre) VALUES ('Fuego');
INSERT INTO tipo (nombre) VALUES ('Agua');
INSERT INTO tipo (nombre) VALUES ('Planta');
INSERT INTO tipo (nombre) VALUES ('Eléctrico');
INSERT INTO tipo (nombre) VALUES ('Psíquico');
INSERT INTO tipo (nombre) VALUES ('Hielo');
INSERT INTO tipo (nombre) VALUES ('Dragón');
INSERT INTO tipo (nombre) VALUES ('Siniestro');
INSERT INTO tipo (nombre) VALUES ('Hada');

-- En Oracle los INSERT no quedan guardados hasta confirmar.
COMMIT;
