package cl.dsy1102.pokerap.model;

/**
 * Una fila de la tabla tipo (Planta, Fuego, Agua, ...).
 *
 * Es un record: dos tipos son iguales si tienen el mismo id y nombre.
 * toString() retorna el nombre porque así lo muestran los ComboBox.
 */
public record Tipo(int id, String nombre) {

    @Override
    public String toString() {
        return nombre;
    }
}
