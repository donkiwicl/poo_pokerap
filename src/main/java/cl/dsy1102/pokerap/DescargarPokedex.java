package cl.dsy1102.pokerap;

import cl.dsy1102.pokerap.json.ArchivoPokedex;
import cl.dsy1102.pokerap.json.PokeApiCliente;
import cl.dsy1102.pokerap.json.PokemonJson;

import java.nio.file.Path;
import java.util.List;

/**
 * Programa de consola que genera datos/pokedex-inicial.json desde la PokeAPI.
 * Necesita internet. Ejecuta con:
 *
 *   mvn compile exec:java                      (151 Pokémon)
 *   mvn compile exec:java -Dexec.args="251"    (otra cantidad)
 *
 * El archivo resultante viene incluido en el proyecto, así que la aplicación
 * funciona sin internet: este programa solo sirve para regenerarlo.
 */
public class DescargarPokedex {

    public static final Path ARCHIVO = Path.of("datos", "pokedex-inicial.json");
    public static final int CANTIDAD = 151;

    public static void main(String[] args) throws Exception {
        int cantidad = args.length > 0 ? Integer.parseInt(args[0]) : CANTIDAD;

        System.out.println("Descargando " + cantidad + " Pokémon desde pokeapi.co ...");
        List<PokemonJson> pokemones = new PokeApiCliente().descargar(cantidad, (descargados, total) -> {
            if (descargados % 25 == 0 || descargados == total) {
                System.out.println("  " + descargados + " de " + total);
            }
        });

        new ArchivoPokedex(ARCHIVO).guardar(pokemones);
        System.out.println("Listo: " + ARCHIVO.toAbsolutePath());
    }
}
