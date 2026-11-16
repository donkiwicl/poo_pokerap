package cl.dsy1102.pokerap.dao;

import cl.dsy1102.pokerap.model.Pokemon;

import java.util.List;

/**
 * Contrato de acceso a los Pokémon. El repositorio y los controladores solo
 * conocen esta interfaz, no dónde ni cómo se guardan los datos.
 */
public interface PokemonDao {

    /** Todos los Pokémon, ordenados por número. Lista vacía si no hay datos. */
    List<Pokemon> listarTodos() throws PersistenciaException;

    /**
     * Guarda un Pokémon nuevo y le asigna su id.
     * Un número repetido lanza PersistenciaException("Ya existe un Pokémon con el número 25.").
     */
    void insertar(Pokemon pokemon) throws PersistenciaException;

    /** Actualiza el Pokémon con ese id. Retorna false si ya no existe. */
    boolean actualizar(Pokemon pokemon) throws PersistenciaException;

    /** Elimina el Pokémon. Retorna false si ya no existía. */
    boolean eliminar(int id) throws PersistenciaException;

    /**
     * Carga masiva: inserta los Pokémon cuyo número todavía no está en la tabla
     * y omite el resto (así no se pisan los que el usuario ya editó).
     * Es todo o nada: si una fila falla, no se guarda ninguna.
     *
     * @return cuántos Pokémon se insertaron
     */
    int importar(List<Pokemon> pokemones) throws PersistenciaException;
}
