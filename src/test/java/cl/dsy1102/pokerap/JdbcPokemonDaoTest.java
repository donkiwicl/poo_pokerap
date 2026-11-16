package cl.dsy1102.pokerap;

import cl.dsy1102.pokerap.dao.JdbcPokemonDao;
import cl.dsy1102.pokerap.dao.JdbcTipoDao;
import cl.dsy1102.pokerap.dao.PersistenciaException;
import cl.dsy1102.pokerap.dao.PokemonDao;
import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.model.Tipo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cada prueba se ejecuta dos veces: con los scripts de MySQL y con los de Oracle. */
class JdbcPokemonDaoTest {

    private PokemonDao dao;
    private Map<String, Tipo> tipos;

    static String[] motores() {
        return BaseDePrueba.motores();
    }

    private void preparar(String motor) throws Exception {
        var conexion = BaseDePrueba.crear(motor);
        dao = new JdbcPokemonDao(conexion);
        tipos = new HashMap<>();
        for (Tipo tipo : new JdbcTipoDao(conexion).listarTodos()) {
            tipos.put(tipo.nombre(), tipo);
        }
    }

    private Pokemon pikachu() {
        return new Pokemon(25, "Pikachu", tipos.get("Eléctrico"), null, 0.4, 6.0, 35, 55, 40, 90);
    }

    private Pokemon bulbasaur() {
        return new Pokemon(1, "Bulbasaur", tipos.get("Planta"), tipos.get("Veneno"), 0.7, 6.9, 45, 49, 49, 45);
    }

    @ParameterizedTest
    @MethodSource("motores")
    void losScriptsCarganLos18Tipos(String motor) throws Exception {
        preparar(motor);
        assertEquals(18, tipos.size());
        assertTrue(dao.listarTodos().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void insertarAsignaElIdYSePuedeLeer(String motor) throws Exception {
        preparar(motor);
        Pokemon nuevo = bulbasaur();
        dao.insertar(nuevo);

        assertTrue(nuevo.getId() > 0, "insertar debe asignar el id generado");
        Pokemon leido = dao.listarTodos().get(0);
        assertEquals(nuevo.getId(), leido.getId());
        assertEquals("Bulbasaur", leido.getNombre());
        assertEquals(tipos.get("Planta"), leido.getTipoPrincipal());
        assertEquals(tipos.get("Veneno"), leido.getTipoSecundario());
        assertEquals(0.7, leido.getAltura());
        assertEquals(6.9, leido.getPeso());
        assertEquals(188, leido.getTotal());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void elTipoSecundarioPuedeSerNull(String motor) throws Exception {
        preparar(motor);
        dao.insertar(pikachu());
        assertNull(dao.listarTodos().get(0).getTipoSecundario());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void listarOrdenaPorNumero(String motor) throws Exception {
        preparar(motor);
        dao.insertar(pikachu());
        dao.insertar(bulbasaur());
        List<Pokemon> todos = dao.listarTodos();
        assertEquals(List.of(1, 25), List.of(todos.get(0).getNumero(), todos.get(1).getNumero()));
    }

    @ParameterizedTest
    @MethodSource("motores")
    void numeroRepetidoSeRechazaConMensajeClaro(String motor) throws Exception {
        preparar(motor);
        dao.insertar(pikachu());
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> dao.insertar(pikachu()));
        assertEquals("Ya existe un Pokémon con el número 25.", e.getMessage());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void actualizarCambiaLosDatos(String motor) throws Exception {
        preparar(motor);
        Pokemon original = pikachu();
        dao.insertar(original);

        Pokemon editado = new Pokemon(25, "Pikachu de Ash", tipos.get("Eléctrico"), tipos.get("Hada"),
                0.5, 6.5, 40, 60, 45, 95);
        editado.setId(original.getId());
        assertTrue(dao.actualizar(editado));

        Pokemon leido = dao.listarTodos().get(0);
        assertEquals("Pikachu de Ash", leido.getNombre());
        assertEquals(tipos.get("Hada"), leido.getTipoSecundario());
        assertEquals(95, leido.getVelocidad());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void actualizarYEliminarAlgoQueNoExisteRetornanFalse(String motor) throws Exception {
        preparar(motor);
        Pokemon fantasma = pikachu();
        fantasma.setId(9999);
        assertFalse(dao.actualizar(fantasma));
        assertFalse(dao.eliminar(9999));
    }

    @ParameterizedTest
    @MethodSource("motores")
    void eliminarBorraLaFila(String motor) throws Exception {
        preparar(motor);
        Pokemon nuevo = pikachu();
        dao.insertar(nuevo);
        assertTrue(dao.eliminar(nuevo.getId()));
        assertTrue(dao.listarTodos().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void elNombreConComillasNoRompeElSql(String motor) throws Exception {
        preparar(motor);
        // Con SQL concatenado esto sería una inyección; con parámetros es solo texto.
        String nombre = "x'); DROP TABLE pokemon; --";
        dao.insertar(new Pokemon(7, nombre, tipos.get("Agua"), null, 0.5, 9.0, 44, 48, 65, 43));
        assertEquals(nombre, dao.listarTodos().get(0).getNombre());
    }

    @ParameterizedTest
    @MethodSource("motores")
    void importarOmiteLosNumerosQueYaExisten(String motor) throws Exception {
        preparar(motor);
        Pokemon editadoPorElUsuario = new Pokemon(25, "Mi Pikachu", tipos.get("Eléctrico"), null, 0.4, 6.0, 99, 99, 99, 99);
        dao.insertar(editadoPorElUsuario);

        assertEquals(1, dao.importar(List.of(bulbasaur(), pikachu())));

        List<Pokemon> todos = dao.listarTodos();
        assertEquals(2, todos.size());
        assertEquals("Mi Pikachu", todos.get(1).getNombre(), "importar no debe pisar lo que ya estaba");
    }

    @ParameterizedTest
    @MethodSource("motores")
    void importarEsTodoONada(String motor) throws Exception {
        preparar(motor);
        // El segundo apunta a un tipo que no existe: la clave foránea lo rechaza.
        Pokemon invalido = new Pokemon(2, "Roto", new Tipo(9999, "Inexistente"), null, 1.0, 1.0, 1, 1, 1, 1);
        assertThrows(PersistenciaException.class, () -> dao.importar(List.of(bulbasaur(), invalido)));
        assertTrue(dao.listarTodos().isEmpty(), "el rollback debe deshacer también el primer INSERT");
    }
}
