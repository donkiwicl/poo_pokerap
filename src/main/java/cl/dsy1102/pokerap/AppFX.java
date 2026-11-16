package cl.dsy1102.pokerap;

import cl.dsy1102.pokerap.controller.PrincipalController;
import cl.dsy1102.pokerap.dao.MemoriaPokemonDao;
import cl.dsy1102.pokerap.dao.MemoriaTipoDao;
import cl.dsy1102.pokerap.json.ArchivoPokedex;
import cl.dsy1102.pokerap.repository.PokemonRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Pokerap. Ejecuta con: mvn javafx:run
 *
 * Aquí se arman las capas: es el único lugar que conoce las implementaciones
 * concretas de los DAO.
 *
 *   Vista (FXML) -> Controlador -> PokemonRepository -> PokemonDao -> MemoriaPokemonDao (lista en memoria)
 */
public class AppFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // TODO R6: crea la ConexionBD desde "/db.properties" (si la configuración
        //  es inválida, avisa con Alertas.error y cierra con Platform.exit()),
        //  reemplaza los DAO en memoria por JdbcPokemonDao y JdbcTipoDao, y
        //  entrega conexion.getMotor() al controlador en vez de "memoria".
        PokemonRepository repositorio =
                new PokemonRepository(new MemoriaPokemonDao(), new MemoriaTipoDao());
        ArchivoPokedex archivo = new ArchivoPokedex(DescargarPokedex.ARCHIVO);

        FXMLLoader cargador = new FXMLLoader(getClass().getResource("view/principal-view.fxml"));
        Parent raiz = cargador.load();
        PrincipalController principal = cargador.getController();

        stage.setTitle("Pokerap · Pokédex");
        stage.setScene(new Scene(raiz, 1040, 640));
        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.show();

        principal.inicializar(repositorio, archivo, "memoria");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
