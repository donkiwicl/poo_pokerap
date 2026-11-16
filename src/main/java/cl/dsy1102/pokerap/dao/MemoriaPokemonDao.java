package cl.dsy1102.pokerap.dao;

import cl.dsy1102.pokerap.model.Pokemon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * PokemonDao del código inicial: guarda los Pokémon en una lista en memoria.
 * Todo lo que agregues o edites SE PIERDE al cerrar la aplicación.
 *
 * Tu trabajo es reemplazarlo por JdbcPokemonDao (misma interfaz, pero con una
 * base de datos relacional) sin modificar el repositorio ni los controladores.
 */
public class MemoriaPokemonDao implements PokemonDao {

    private final List<Pokemon> pokemones = new ArrayList<>();
    private int ultimoId;

    @Override
    public List<Pokemon> listarTodos() {
        List<Pokemon> copia = new ArrayList<>(pokemones);
        copia.sort(Comparator.comparingInt(Pokemon::getNumero));
        return copia;
    }

    @Override
    public void insertar(Pokemon pokemon) throws PersistenciaException {
        if (existeNumero(pokemon.getNumero(), 0)) {
            throw new PersistenciaException("Ya existe un Pokémon con el número " + pokemon.getNumero() + ".");
        }
        pokemon.setId(++ultimoId);
        pokemones.add(pokemon);
    }

    @Override
    public boolean actualizar(Pokemon pokemon) throws PersistenciaException {
        if (existeNumero(pokemon.getNumero(), pokemon.getId())) {
            throw new PersistenciaException("Ya existe un Pokémon con el número " + pokemon.getNumero() + ".");
        }
        for (int i = 0; i < pokemones.size(); i++) {
            if (pokemones.get(i).getId() == pokemon.getId()) {
                pokemones.set(i, pokemon);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        return pokemones.removeIf(pokemon -> pokemon.getId() == id);
    }

    @Override
    public int importar(List<Pokemon> nuevos) {
        int insertados = 0;
        for (Pokemon pokemon : nuevos) {
            if (!existeNumero(pokemon.getNumero(), 0)) {
                pokemon.setId(++ultimoId);
                pokemones.add(pokemon);
                insertados++;
            }
        }
        return insertados;
    }

    /** ¿Hay OTRO Pokémon (distinto del id indicado) con ese número? */
    private boolean existeNumero(int numero, int idPropio) {
        for (Pokemon pokemon : pokemones) {
            if (pokemon.getNumero() == numero && pokemon.getId() != idPropio) {
                return true;
            }
        }
        return false;
    }
}
