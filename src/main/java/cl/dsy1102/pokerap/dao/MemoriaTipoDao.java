package cl.dsy1102.pokerap.dao;

import cl.dsy1102.pokerap.model.Tipo;

import java.util.ArrayList;
import java.util.List;

/**
 * TipoDao del código inicial: los 18 tipos están escritos en el programa.
 * En la versión con base de datos se leen de la tabla tipo (JdbcTipoDao).
 */
public class MemoriaTipoDao implements TipoDao {

    private static final String[] NOMBRES = {
            "Acero", "Agua", "Bicho", "Dragón", "Eléctrico", "Fantasma", "Fuego", "Hada", "Hielo",
            "Lucha", "Normal", "Planta", "Psíquico", "Roca", "Siniestro", "Tierra", "Veneno", "Volador"};

    @Override
    public List<Tipo> listarTodos() {
        List<Tipo> tipos = new ArrayList<>();
        for (int i = 0; i < NOMBRES.length; i++) {
            tipos.add(new Tipo(i + 1, NOMBRES[i]));
        }
        return tipos;
    }
}
