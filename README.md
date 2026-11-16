# EA3 · Pokerap — de la PokeAPI (JSON) a una base de datos relacional

**DSY1102 · Programación Orientada a Objetos · Ejercicio de práctica tipo Evaluación Parcial 3**

> Material de práctica. No corresponde a una evaluación sumativa. Este repositorio contiene el **enunciado y el código inicial**.

| | |
|---|---|
| **Indicadores** | IL 3.1 al IL 3.5 (ver [sección 6](#6-indicadores-de-logro)) |
| **Tiempo estimado** | 180 minutos (como la EP3) |
| **Tecnologías** | Java 21 · Maven · JavaFX 21 · FXML · Gson · JDBC · **MySQL 8 u Oracle** · H2 para las pruebas |
| **Temas** | Consumir una API REST que responde JSON, leer JSON con Gson, `TableView` con `ObservableList`, capas MVC + Repository + DAO, conexión JDBC configurable, `PreparedStatement`, `JOIN`, claves generadas, transacciones, manejo de errores de base de datos en la interfaz |

---

## 1. Contexto

La [PokeAPI](https://pokeapi.co) es una API pública que entrega los datos de cada Pokémon en formato JSON. Con ella se generó `datos/pokedex-inicial.json`: los 151 Pokémon originales, ya simplificados y traducidos.

La aplicación **Pokerap** ya funciona: lee ese JSON, lo muestra en una **TableView** con búsqueda y filtro por tipo, y permite **agregar**, **editar** y **eliminar** Pokémon. El problema es que guarda todo **en memoria**: al cerrar la ventana se pierde cada cambio.

Tu trabajo es reemplazar ese almacenamiento por una **base de datos relacional** (eliges **MySQL u Oracle**). La aplicación está separada en capas (MVC + Repository + DAO), así que el cambio debería tocar **solo la capa de datos** y la clase que arma las capas.

```
Hoy:    Vista (FXML) ─► Controlador ─► PokemonRepository ─► PokemonDao ◄── MemoriaPokemonDao (lista en memoria)
                                                            TipoDao    ◄── MemoriaTipoDao

Meta:   Vista (FXML) ─► Controlador ─► PokemonRepository ─► PokemonDao ◄── JdbcPokemonDao ─┐
                                                            TipoDao    ◄── JdbcTipoDao    ─┴─► ConexionBD ─► MySQL / Oracle
                                                                                                    ▲
                                                                                              db.properties
```

Con la base de datos funcionando, la primera vez que la aplicación encuentre la tabla `pokemon` vacía la llenará con el JSON inicial. Desde ahí, la base de datos es la fuente de los datos.

## 2. Lo que ya existe (código inicial)

| Paquete / archivo | Estado |
|---|---|
| `model/` | **Resuelto.** `Pokemon` (valida sus reglas en el constructor) y `Tipo` (record). |
| `json/` | **Resuelto.** `PokemonJson` (forma del archivo), `ArchivoPokedex` (lee y escribe con Gson), `PokeApiCliente` (consulta la API con `HttpClient`). |
| `dao/PokemonDao`, `TipoDao`, `PersistenciaException` | **Resueltos.** Las interfaces son el contrato que deben cumplir tus clases. |
| `dao/MemoriaPokemonDao`, `MemoriaTipoDao` | Versión en memoria, funcionando. Es lo que vas a reemplazar. |
| `repository/` | **Resuelto.** `PokemonRepository` mantiene la `ObservableList` sincronizada con el DAO. |
| `controller/`, `view/*.fxml`, `styles.css` | **Resueltos.** Tabla, formulario modal, alertas. |
| `AppFX` | Arma las capas con los DAO en memoria (R6). |
| `pom.xml`, `module-info.java` | Sin el driver ni el módulo de JDBC (R1). |
| `sql/<motor>/crear-bd.sql` o `crear-usuario.sql`, `datos.sql` | **Resueltos.** `datos.sql` carga los 18 tipos. |
| `sql/<motor>/tablas.sql` | **Vacío** (R2). |
| `datos/pokedex-inicial.json` | El JSON inicial (151 Pokémon). |
| `src/test/` | 29 pruebas de la capa de datos con H2. **No compilan** hasta que crees las clases de R1, R3 y R4. |

Antes de empezar, ejecuta `mvn javafx:run`. Agrega un Pokémon, edita otro, cierra y vuelve a abrir: los cambios desaparecen.

## 3. Base de datos

Dos tablas. `pokemon` referencia dos veces a `tipo`: el tipo principal es obligatorio y el secundario admite `NULL`.

| Tabla | Columnas |
|---|---|
| `tipo` | `id` (PK, autogenerada) · `nombre` (único, 20) |
| `pokemon` | `id` (PK, autogenerada) · `numero` (único, > 0) · `nombre` (40) · `tipo_principal_id` (FK) · `tipo_secundario_id` (FK, `NULL`) · `altura` y `peso` (un decimal, > 0) · `hp`, `ataque`, `defensa`, `velocidad` (1 a 255) |

### Opción A · MySQL 8

```bash
mysql -u root -p < sql/mysql/crear-bd.sql
```
```bash
mysql -u root -p pokerap < sql/mysql/tablas.sql
```
```bash
mysql -u root -p pokerap < sql/mysql/datos.sql
```

También puedes abrir y ejecutar los tres archivos, en ese orden, desde MySQL Workbench.

### Opción B · Oracle (XE 21c o Free 23ai)

Desde SQL Developer:

1. Conectado como `SYSTEM` a la PDB, ejecuta `sql/oracle/crear-usuario.sql`.
2. Conectado como `pokerap`, ejecuta `sql/oracle/tablas.sql` (la primera vez los dos `DROP` fallan: es esperable) y luego `sql/oracle/datos.sql`.

### Qué cambia entre los dos motores

| | MySQL | Oracle |
|---|---|---|
| Clave autogenerada | `INT AUTO_INCREMENT` | `NUMBER GENERATED ALWAYS AS IDENTITY` |
| Tipos de columna | `INT`, `VARCHAR`, `DECIMAL` | `NUMBER`, `VARCHAR2` |
| Reiniciar tablas | `DROP TABLE IF EXISTS` | `DROP TABLE ... CASCADE CONSTRAINTS` |
| Dónde viven las tablas | una base de datos (`pokerap`) | un usuario o esquema (`pokerap`) |
| URL JDBC | `jdbc:mysql://localhost:3306/pokerap` | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| Driver (Maven) | `com.mysql:mysql-connector-j:9.7.0` | `com.oracle.database.jdbc:ojdbc11:23.6.0.24.10` |
| Confirmar los `INSERT` de un script | automático | `COMMIT` |

Si usas solo SQL estándar en tus sentencias, el código Java es el mismo para los dos motores.

## 4. Ejecutar

```bash
mvn javafx:run
```

Cuando existan tus clases de R1, R3 y R4, ejecuta las pruebas:

```bash
mvn test
```

Las pruebas usan H2 en memoria (no necesitan servidor) y ejecutan el `tablas.sql` de **los dos** motores. Si solo escribiste uno, corre las de ese motor:

```bash
mvn test -Dbd.motor=mysql
```

## 5. Requerimientos

### R1. Biblioteca de conectividad (IL 3.1)
- Agrega al `pom.xml` el driver JDBC del motor que elegiste, con el `scope` correcto.
- Agrega `requires java.sql;` al `module-info.java`.
- Debes poder explicar qué hace el driver y por qué tu código no lo importa.

### R2. Tablas (IL 3.2)
- Escribe `sql/<motor>/tablas.sql` según la sección 3: PK autogenerada, `UNIQUE`, `NOT NULL`, `CHECK` y las dos claves foráneas.
- Crea la base de datos (o el usuario) y ejecuta `tablas.sql` y `datos.sql`.

### R3. Conexión (IL 3.2)
- Crea `src/main/resources/db.properties` con `db.url`, `db.usuario` y `db.clave`.
- Crea `dao/ConexionBD` con:
  - el constructor `ConexionBD(Properties)`, que lanza `IllegalStateException` si falta la URL o el usuario;
  - `static ConexionBD desdeRecurso(String recurso)`;
  - `Connection abrir() throws SQLException`;
  - `String getMotor()`, que retorna `"MySQL"` u `"Oracle"` según la URL.
- Ninguna otra clase debe conocer la URL, el usuario ni la clave.

### R4. Consulta y manipulación de datos (IL 3.3, IL 3.4)
- Crea `dao/JdbcTipoDao` (implementa `TipoDao`) y `dao/JdbcPokemonDao` (implementa `PokemonDao`). Ambas reciben la `ConexionBD` en el constructor.
- `listarTodos`: una sola consulta con `JOIN` para el tipo principal y `LEFT JOIN` para el secundario, ordenada por número.
- `insertar`: recupera el id generado y lo asigna al Pokémon. Un número repetido lanza `PersistenciaException("Ya existe un Pokémon con el número N.")`.
- `actualizar` y `eliminar`: retornan `false` si la fila ya no existe.
- **Todas** las sentencias con `PreparedStatement` y parámetros `?`. Ningún dato del usuario se concatena al SQL.
- **Todas** las conexiones, sentencias y `ResultSet` en `try-with-resources`.
- Las `SQLException` no salen del DAO: se traducen a `PersistenciaException` con un mensaje para el usuario (distingue al menos «no hay conexión» de los demás errores).

### R5. Importación en una transacción (IL 3.3)
- `JdbcPokemonDao.importar(List<Pokemon>)`: inserta los Pokémon cuyo número todavía no existe y retorna cuántos insertó.
- Es todo o nada: si una fila falla, `rollback` y no queda ninguna.

### R6. Integración con la interfaz (IL 3.4, IL 3.5)
- En `AppFX`, reemplaza los DAO en memoria por los DAO JDBC. **No modifiques** el repositorio ni los controladores.
- Si `db.properties` es inválido, avisa con `Alertas.error` y cierra la aplicación.
- El encabezado debe mostrar el motor en uso.
- Comprueba desde la interfaz: la carga inicial desde el JSON, agregar, editar, eliminar, cerrar y volver a abrir (los cambios deben seguir ahí).
- Con el servidor de base de datos detenido la aplicación **no debe caerse**: muestra un mensaje comprensible y permite reintentar con **Recargar**.

### R7. (Desafío)
- Agrega a `PokemonDao` un método `List<Pokemon> buscarPorTipo(int tipoId)` resuelto con SQL y úsalo desde la interfaz en vez del filtro en memoria.
- Haz que la aplicación funcione con el **otro** motor cambiando solo `pom.xml`, `db.properties` y los scripts.

## 6. Indicadores de logro

RA3: *organiza proyectos de software, utilizando estructura y/o dependencia de paquetes, para dar orden lógico y cohesión a los componentes desarrollados.*

| IL | Indicador de logro | Peso | Requerimientos |
|---|---|---|---|
| **3.1** | Incorpora una biblioteca de conectividad a base de datos al proyecto existente, reconociendo cómo establece el puente entre la aplicación y el sistema gestor de base de datos. | 2 % | R1 |
| **3.2** | Configura la conexión entre la aplicación y una base de datos relacional, estableciendo los parámetros necesarios para el acceso persistente a los datos. | 26 % | R2, R3 |
| **3.3** | Implementa operaciones de consulta y manipulación de datos sobre una base de datos relacional, utilizando sentencias parametrizadas para garantizar la integridad y seguridad de los datos. | 41 % | R4, R5 |
| **3.4** | Implementa la capa de acceso a datos de la aplicación, reemplazando el almacenamiento en memoria por una solución con base de datos relacional y valorando la separación de responsabilidades en términos de cohesión y mantenibilidad. | 15 % | R4, R6 |
| **3.5** | Integra la interfaz gráfica de escritorio con la capa de acceso a datos, conectando la interacción del usuario con la lógica de negocio y la persistencia en base de datos. | 16 % | R6 |

## 7. Errores frecuentes

- **`Access denied` o `ORA-01017`**: usuario o clave incorrectos en `db.properties`.
- **«No hay conexión con la base de datos»**: el servicio está detenido o el puerto de la URL no es el correcto.
- **«El tipo 'Eléctrico' … no existe en la tabla tipo»**: no ejecutaste `datos.sql`, o lo cargaste con un cliente que no estaba en UTF-8 y las tildes quedaron mal guardadas.
- **`No suitable driver`**: la URL no empieza con `jdbc:mysql:` ni `jdbc:oracle:`, o falta el driver en el `pom.xml`.
- **Las pruebas no compilan**: es lo esperado hasta que existan `ConexionBD`, `JdbcTipoDao` y `JdbcPokemonDao`.
