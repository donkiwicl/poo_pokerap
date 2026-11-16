package cl.dsy1102.pokerap.dao;

/**
 * Falla al leer o guardar datos (base de datos o archivo JSON). Su mensaje
 * está pensado para mostrarse al usuario; el error técnico queda como causa.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
