package cl.dsy1102.pokerap;

import cl.dsy1102.pokerap.dao.JdbcPokemonDao;
import cl.dsy1102.pokerap.dao.JdbcTipoDao;
import cl.dsy1102.pokerap.dao.PersistenciaException;
import cl.dsy1102.pokerap.json.ArchivoPokedex;
import cl.dsy1102.pokerap.json.PokemonJson;
import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.repository.PokemonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El camino completo: archivo JSON -> repositorio -> base de datos -> lista observable. */
class ImportacionJsonTest {

    private final ArchivoPokedex inicial = new ArchivoPokedex(DescargarPokedex.ARCHIVO);

    static String[] motores() {
        return BaseDePrueba.motores();
    }

    private static PokemonRepository repositorio(String motor) throws Exception {
        var conexion = BaseDePrueba.crear(motor);
        return new PokemonRepository(new JdbcPokemonDao(conexion), new JdbcTipoDao(conexion));
    }

    @Test
    void elJsonInicialTieneLos151Pokemon() throws Exception {
        List<PokemonJson> leidos = inicial.leer();
        assertEquals(151, leidos.size());
        assertEquals("Bulbasaur", leidos.get(0).getNombre());
        assertEquals(List.of("Planta", "Veneno"), leidos.get(0).getTipos());
        assertEquals("Mew", leidos.get(150).getNombre());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void elJsonInicialSeImportaCompleto(String motor) throws Exception {
        PokemonRepository repositorio = repositorio(motor);

        assertEquals(151, repositorio.importar(inicial.leer()));
        assertEquals(151, repositorio.listar().size());

        Pokemon charizard = repositorio.listar().get(5);
        assertEquals("Charizard", charizard.getNombre());
        assertEquals("Fuego", charizard.getTipoPrincipal().nombre());
        assertEquals("Volador", charizard.getTipoSecundario().nombre());
        assertTrue(charizard.getId() > 0);

        // Importar por segunda vez no duplica nada.
        assertEquals(0, repositorio.importar(inicial.leer()));
        assertEquals(151, repositorio.listar().size());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void unTipoDesconocidoSeInformaYNoSeImportaNada(String motor) throws Exception {
        PokemonRepository repositorio = repositorio(motor);
        List<PokemonJson> leidos = List.of(
                new PokemonJson(1, "Bulbasaur", List.of("Planta", "Veneno"), 0.7, 6.9, 45, 49, 49, 45),
                new PokemonJson(2, "Raro", List.of("Cósmico"), 1.0, 1.0, 1, 1, 1, 1));

        PersistenciaException e = assertThrows(PersistenciaException.class, () -> repositorio.importar(leidos));
        assertTrue(e.getMessage().contains("Cósmico"), e.getMessage());
        repositorio.cargar();
        assertTrue(repositorio.listar().isEmpty());
    }

    @Test
    void guardarYLeerConservanLosDatos(@TempDir Path carpeta) throws Exception {
        ArchivoPokedex archivo = new ArchivoPokedex(carpeta.resolve("sub").resolve("pokedex.json"));
        archivo.guardar(List.of(new PokemonJson(25, "Pikachu", List.of("Eléctrico"), 0.4, 6.0, 35, 55, 40, 90)));

        PokemonJson leido = archivo.leer().get(0);
        assertEquals(25, leido.getNumero());
        assertEquals(List.of("Eléctrico"), leido.getTipos());
        assertEquals(90, leido.getVelocidad());
    }

    @Test
    void archivoInexistenteVacioOCorrupto(@TempDir Path carpeta) throws Exception {
        assertThrows(PersistenciaException.class, () -> new ArchivoPokedex(carpeta.resolve("no-existe.json")).leer());

        Path vacio = Files.createFile(carpeta.resolve("vacio.json"));
        assertTrue(new ArchivoPokedex(vacio).leer().isEmpty());

        Path corrupto = Files.writeString(carpeta.resolve("corrupto.json"), "{ esto no es una lista");
        assertThrows(PersistenciaException.class, () -> new ArchivoPokedex(corrupto).leer());
    }
}
