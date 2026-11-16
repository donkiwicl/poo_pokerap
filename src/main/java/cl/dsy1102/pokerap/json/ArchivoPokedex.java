package cl.dsy1102.pokerap.json;

import cl.dsy1102.pokerap.dao.PersistenciaException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Lee y escribe el archivo JSON con la Pokédex inicial usando Gson.
 */
public class ArchivoPokedex {

    // Gson necesita saber el tipo completo de la lista: List<PokemonJson>.
    private static final Type TIPO_LISTA = new TypeToken<List<PokemonJson>>() { }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path archivo;

    public ArchivoPokedex(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() {
        return archivo;
    }

    public boolean existe() {
        return Files.isRegularFile(archivo);
    }

    public List<PokemonJson> leer() throws PersistenciaException {
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<PokemonJson> pokemones = gson.fromJson(lector, TIPO_LISTA);
            // Un archivo vacío no es un error de Gson: retorna null.
            return pokemones == null ? List.of() : pokemones;
        } catch (NoSuchFileException e) {
            throw new PersistenciaException("No existe el archivo " + archivo + ".", e);
        } catch (JsonParseException e) {
            throw new PersistenciaException("El archivo " + archivo + " no contiene un JSON válido de Pokémon.", e);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer " + archivo + ": " + e.getMessage(), e);
        }
    }

    public void guardar(List<PokemonJson> pokemones) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                gson.toJson(pokemones, TIPO_LISTA, escritor);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar " + archivo + ": " + e.getMessage(), e);
        }
    }
}
