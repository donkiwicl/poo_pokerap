package cl.dsy1102.pokerap.json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Consulta la PokeAPI (https://pokeapi.co), una API REST pública que responde JSON.
 *
 * La API entrega mucha más información de la que necesitamos y con otra forma
 * (nombres en inglés, alturas en decímetros, estadísticas en una lista). Las
 * clases internas Detalle, RanuraTipo, etc. copian SOLO la parte del JSON que
 * interesa: Gson ignora las claves que no tienen un campo con su nombre.
 */
public class PokeApiCliente {

    /** Recibe el avance de la descarga (por ejemplo, para una barra de progreso). */
    public interface Progreso {
        void avance(int descargados, int total);
    }

    private static final String URL_POKEMON = "https://pokeapi.co/api/v2/pokemon/";

    private static final Map<String, String> TIPOS = Map.ofEntries(
            Map.entry("normal", "Normal"), Map.entry("fighting", "Lucha"), Map.entry("flying", "Volador"),
            Map.entry("poison", "Veneno"), Map.entry("ground", "Tierra"), Map.entry("rock", "Roca"),
            Map.entry("bug", "Bicho"), Map.entry("ghost", "Fantasma"), Map.entry("steel", "Acero"),
            Map.entry("fire", "Fuego"), Map.entry("water", "Agua"), Map.entry("grass", "Planta"),
            Map.entry("electric", "Eléctrico"), Map.entry("psychic", "Psíquico"), Map.entry("ice", "Hielo"),
            Map.entry("dragon", "Dragón"), Map.entry("dark", "Siniestro"), Map.entry("fairy", "Hada"));

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();

    /**
     * Descarga los Pokémon del 1 al indicado. Es lento (una petición por
     * Pokémon): en una aplicación JavaFX debe llamarse desde un hilo secundario.
     */
    public List<PokemonJson> descargar(int cantidad, Progreso progreso) throws IOException, InterruptedException {
        List<PokemonJson> pokemones = new ArrayList<>();
        for (int numero = 1; numero <= cantidad; numero++) {
            pokemones.add(descargarUno(numero));
            progreso.avance(numero, cantidad);
        }
        return pokemones;
    }

    public PokemonJson descargarUno(int numero) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_POKEMON + numero))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IOException("La PokeAPI respondió " + respuesta.statusCode() + " para el Pokémon " + numero + ".");
        }
        try {
            return convertir(gson.fromJson(respuesta.body(), Detalle.class));
        } catch (JsonParseException | NullPointerException e) {
            throw new IOException("La respuesta de la PokeAPI para el Pokémon " + numero + " no tiene el formato esperado.", e);
        }
    }

    /** Traduce la forma de la API a la forma de nuestro archivo. */
    private static PokemonJson convertir(Detalle detalle) {
        List<String> tipos = new ArrayList<>();
        for (RanuraTipo ranura : detalle.types) {   // vienen ordenados: principal, secundario
            tipos.add(TIPOS.getOrDefault(ranura.type.name, ranura.type.name));
        }
        return new PokemonJson(detalle.id, capitalizar(detalle.name), tipos,
                detalle.height / 10.0,   // decímetros -> metros
                detalle.weight / 10.0,   // hectogramos -> kilos
                estadistica(detalle, "hp"), estadistica(detalle, "attack"),
                estadistica(detalle, "defense"), estadistica(detalle, "speed"));
    }

    private static int estadistica(Detalle detalle, String nombre) {
        for (Estadistica estadistica : detalle.stats) {
            if (nombre.equals(estadistica.stat.name)) {
                return estadistica.valorBase;
            }
        }
        throw new JsonParseException("Falta la estadística " + nombre);
    }

    private static String capitalizar(String texto) {
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    // ---- Forma del JSON de https://pokeapi.co/api/v2/pokemon/{numero} ----
    //
    //   { "id": 1, "name": "bulbasaur", "height": 7, "weight": 69,
    //     "types": [ { "slot": 1, "type": { "name": "grass", "url": "..." } }, ... ],
    //     "stats": [ { "base_stat": 45, "stat": { "name": "hp", "url": "..." } }, ... ],
    //     ... decenas de claves más que no usamos ... }

    private static class Detalle {
        int id;
        String name;
        int height;
        int weight;
        List<RanuraTipo> types;
        List<Estadistica> stats;
    }

    private static class RanuraTipo {
        Nombrado type;
    }

    private static class Estadistica {
        // La clave del JSON no es un nombre válido al estilo Java: se indica con la anotación.
        @SerializedName("base_stat")
        int valorBase;
        Nombrado stat;
    }

    private static class Nombrado {
        String name;
    }
}
