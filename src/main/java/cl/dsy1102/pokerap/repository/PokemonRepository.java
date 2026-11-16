package cl.dsy1102.pokerap.repository;

import cl.dsy1102.pokerap.dao.PersistenciaException;
import cl.dsy1102.pokerap.dao.PokemonDao;
import cl.dsy1102.pokerap.dao.TipoDao;
import cl.dsy1102.pokerap.json.PokemonJson;
import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.model.Tipo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene las listas observables sincronizadas con los DAO: primero guarda y,
 * solo si el DAO no falla, modifica la lista que ve la interfaz.
 */
public class PokemonRepository implements Repository<Pokemon> {

    private final PokemonDao pokemonDao;
    private final TipoDao tipoDao;
    private final ObservableList<Pokemon> pokemones = FXCollections.observableArrayList();
    private final ObservableList<Tipo> tipos = FXCollections.observableArrayList();

    public PokemonRepository(PokemonDao pokemonDao, TipoDao tipoDao) {
        this.pokemonDao = pokemonDao;
        this.tipoDao = tipoDao;
    }

    @Override
    public void cargar() throws PersistenciaException {
        tipos.setAll(tipoDao.listarTodos());
        pokemones.setAll(pokemonDao.listarTodos());
    }

    @Override
    public ObservableList<Pokemon> listar() {
        return pokemones;
    }

    /** Los tipos disponibles, para los ComboBox. Se llenan con cargar(). */
    public ObservableList<Tipo> tipos() {
        return tipos;
    }

    @Override
    public void agregar(Pokemon pokemon) throws PersistenciaException {
        pokemonDao.insertar(pokemon);
        pokemones.add(pokemon);
    }

    @Override
    public void actualizar(Pokemon original, Pokemon actualizado) throws PersistenciaException {
        int indice = pokemones.indexOf(original);
        if (indice < 0) {
            throw new IllegalArgumentException("El Pokémon a actualizar no está en la lista.");
        }
        actualizado.setId(original.getId());
        if (!pokemonDao.actualizar(actualizado)) {
            pokemones.remove(indice);
            throw new PersistenciaException(original.getNombre() + " ya no existe en la base de datos.");
        }
        pokemones.set(indice, actualizado);
    }

    @Override
    public void eliminar(Pokemon pokemon) throws PersistenciaException {
        pokemonDao.eliminar(pokemon.getId());
        pokemones.remove(pokemon);
    }

    /**
     * Guarda en la base de datos los Pokémon leídos de un JSON y recarga la lista.
     * Los números que ya existen se omiten.
     *
     * @return cuántos Pokémon nuevos se insertaron
     */
    public int importar(List<PokemonJson> leidos) throws PersistenciaException {
        if (tipos.isEmpty()) {
            tipos.setAll(tipoDao.listarTodos());
        }
        List<Pokemon> nuevos = new ArrayList<>();
        for (PokemonJson json : leidos) {
            nuevos.add(convertir(json));
        }
        int insertados = pokemonDao.importar(nuevos);
        cargar();   // los insertados aún no tienen id: se leen de nuevo
        return insertados;
    }

    /** El JSON trae nombres de tipo; el modelo necesita las filas de la tabla tipo. */
    private Pokemon convertir(PokemonJson json) throws PersistenciaException {
        List<String> nombres = json.getTipos();
        if (nombres.isEmpty() || nombres.size() > 2) {
            throw new PersistenciaException("El Pokémon n.° " + json.getNumero() + " del JSON debe tener 1 o 2 tipos.");
        }
        try {
            return new Pokemon(json.getNumero(), json.getNombre(),
                    buscarTipo(nombres.get(0), json),
                    nombres.size() == 2 ? buscarTipo(nombres.get(1), json) : null,
                    json.getAltura(), json.getPeso(),
                    json.getHp(), json.getAtaque(), json.getDefensa(), json.getVelocidad());
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Dato inválido en el JSON (Pokémon n.° " + json.getNumero() + "): "
                    + e.getMessage(), e);
        }
    }

    private Tipo buscarTipo(String nombre, PokemonJson json) throws PersistenciaException {
        for (Tipo tipo : tipos) {
            if (tipo.nombre().equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        throw new PersistenciaException("El tipo '" + nombre + "' del Pokémon n.° " + json.getNumero()
                + " no existe en la tabla tipo. ¿Ejecutaste datos.sql?");
    }
}
