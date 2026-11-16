package cl.dsy1102.pokerap;

import cl.dsy1102.pokerap.dao.ConexionBD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Crea una base de datos de prueba con los scripts de la carpeta sql/.
 *
 * Por defecto usa H2 en memoria (una base nueva por prueba, no necesita
 * servidor) imitando al motor indicado: "mysql" u "oracle". Así las mismas
 * pruebas verifican los scripts y el DAO para los dos motores.
 *
 * Para correr las pruebas contra tu servidor real (¡reinicia las tablas!):
 *   mvn test -Dbd.motor=mysql  -Dbd.url=jdbc:mysql://localhost:3306/pokerap -Dbd.usuario=root -Dbd.clave=...
 *   mvn test -Dbd.motor=oracle -Dbd.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1 -Dbd.usuario=pokerap -Dbd.clave=pokerap
 */
final class BaseDePrueba {

    private static int contador;

    private BaseDePrueba() {
    }

    /** Los motores que se prueban: los dos en H2, o solo el del servidor real. */
    static String[] motores() {
        String motor = System.getProperty("bd.motor");
        return motor == null ? new String[]{"mysql", "oracle"} : new String[]{motor};
    }

    static ConexionBD crear(String motor) throws IOException, SQLException {
        Properties propiedades = new Properties();
        String url = System.getProperty("bd.url");
        if (url == null) {
            String modo = motor.equals("oracle") ? "MODE=Oracle" : "MODE=MySQL;DATABASE_TO_LOWER=TRUE";
            propiedades.setProperty("db.url", "jdbc:h2:mem:prueba" + (++contador) + ";" + modo + ";DB_CLOSE_DELAY=-1");
            propiedades.setProperty("db.usuario", "sa");
        } else {
            propiedades.setProperty("db.url", url);
            propiedades.setProperty("db.usuario", System.getProperty("bd.usuario", "root"));
            propiedades.setProperty("db.clave", System.getProperty("bd.clave", ""));
        }
        ConexionBD conexion = new ConexionBD(propiedades);
        ejecutar(conexion, Path.of("sql", motor, "tablas.sql"));
        ejecutar(conexion, Path.of("sql", motor, "datos.sql"));
        return conexion;
    }

    /** Ejecuta un script: quita los comentarios -- y separa las sentencias por ';'. */
    static void ejecutar(ConexionBD conexion, Path script) throws IOException, SQLException {
        String sinComentarios = Files.readAllLines(script).stream()
                .filter(linea -> !linea.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        try (Connection con = conexion.abrir(); Statement st = con.createStatement()) {
            for (String sentencia : sinComentarios.split(";")) {
                if (sentencia.isBlank()) {
                    continue;
                }
                try {
                    st.execute(sentencia);
                } catch (SQLException e) {
                    // En Oracle no hay DROP TABLE IF EXISTS: la primera vez el DROP falla.
                    if (!sentencia.trim().toUpperCase().startsWith("DROP")) {
                        throw e;
                    }
                }
            }
        }
    }
}
