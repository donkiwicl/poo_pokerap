package cl.dsy1102.pokerap.dao;

import cl.dsy1102.pokerap.model.Tipo;

import java.util.List;

/** Contrato de acceso a los tipos (tabla de solo lectura para la aplicación). */
public interface TipoDao {

    /** Todos los tipos, ordenados por nombre. */
    List<Tipo> listarTodos() throws PersistenciaException;
}
