package cl.dsy1102.pokerap.controller;

import cl.dsy1102.pokerap.dao.PersistenciaException;
import cl.dsy1102.pokerap.json.ArchivoPokedex;
import cl.dsy1102.pokerap.json.PokeApiCliente;
import cl.dsy1102.pokerap.json.PokemonJson;
import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.model.Tipo;
import cl.dsy1102.pokerap.repository.PokemonRepository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Pantalla principal: la Pokédex en una TableView con búsqueda y filtro.
 *
 * El controlador NO sabe de SQL ni de JDBC: habla solo con el repositorio.
 * Tampoco modifica la tabla directamente: el repositorio cambia su
 * ObservableList y la tabla reacciona sola.
 *
 *   ObservableList (repositorio) -> FilteredList -> SortedList -> TableView
 */
public class PrincipalController {

    /** Opción del ComboBox que significa "sin filtro". No existe en la base de datos. */
    private static final Tipo TODOS = new Tipo(0, "Todos los tipos");
    private static final int CANTIDAD_DESCARGA = 151;

    @FXML private Label lblMotor;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Tipo> cmbTipo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnImportar;
    @FXML private Button btnDescargar;

    @FXML private TableView<Pokemon> tblPokemon;
    @FXML private TableColumn<Pokemon, Integer> colNumero;
    @FXML private TableColumn<Pokemon, String> colNombre;
    @FXML private TableColumn<Pokemon, Tipo> colTipo1;
    @FXML private TableColumn<Pokemon, Tipo> colTipo2;
    @FXML private TableColumn<Pokemon, Double> colAltura;
    @FXML private TableColumn<Pokemon, Double> colPeso;
    @FXML private TableColumn<Pokemon, Integer> colHp;
    @FXML private TableColumn<Pokemon, Integer> colAtaque;
    @FXML private TableColumn<Pokemon, Integer> colDefensa;
    @FXML private TableColumn<Pokemon, Integer> colVelocidad;
    @FXML private TableColumn<Pokemon, Integer> colTotal;

    @FXML private Label lblEstado;
    @FXML private Label lblMensaje;
    @FXML private ProgressBar barProgreso;

    private PokemonRepository repositorio;
    private ArchivoPokedex archivo;
    private FilteredList<Pokemon> filtrados;

    /** Lo llama FXMLLoader después de inyectar los @FXML. Aún no hay repositorio. */
    @FXML
    private void initialize() {
        // PropertyValueFactory busca getNumero(), getNombre(), ... por reflexión.
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAltura.setCellValueFactory(new PropertyValueFactory<>("altura"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colHp.setCellValueFactory(new PropertyValueFactory<>("hp"));
        colAtaque.setCellValueFactory(new PropertyValueFactory<>("ataque"));
        colDefensa.setCellValueFactory(new PropertyValueFactory<>("defensa"));
        colVelocidad.setCellValueFactory(new PropertyValueFactory<>("velocidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Con lambdas no hay reflexión y el compilador revisa los tipos.
        colTipo1.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTipoPrincipal()));
        colTipo2.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTipoSecundario()));
        colTipo1.setCellFactory(columna -> new CeldaTipo());
        colTipo2.setCellFactory(columna -> new CeldaTipo());

        // Editar y Eliminar solo se habilitan cuando hay una fila seleccionada.
        var sinSeleccion = tblPokemon.getSelectionModel().selectedItemProperty().isNull();
        btnEditar.disableProperty().bind(sinSeleccion);
        btnEliminar.disableProperty().bind(sinSeleccion);

        // Doble clic en una fila = editar.
        tblPokemon.setRowFactory(tabla -> {
            TableRow<Pokemon> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFormulario(fila.getItem());
                }
            });
            return fila;
        });

        cmbTipo.getItems().setAll(TODOS);
        cmbTipo.setValue(TODOS);
        barProgreso.setVisible(false);
    }

    /** Lo llama AppFX después de cargar el FXML: aquí llegan las dependencias. */
    public void inicializar(PokemonRepository repositorio, ArchivoPokedex archivo, String motor) {
        this.repositorio = repositorio;
        this.archivo = archivo;
        lblMotor.setText("Base de datos: " + motor);

        filtrados = new FilteredList<>(repositorio.listar(), pokemon -> true);
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> actualizarFiltro());
        cmbTipo.valueProperty().addListener((obs, anterior, nuevo) -> actualizarFiltro());

        // La SortedList usa el comparador de la tabla (clic en los encabezados).
        SortedList<Pokemon> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblPokemon.comparatorProperty());
        tblPokemon.setItems(ordenados);

        lblEstado.textProperty().bind(Bindings.createStringBinding(
                () -> "Mostrando " + filtrados.size() + " de " + repositorio.listar().size() + " Pokémon",
                filtrados, repositorio.listar()));

        // Primera ejecución: si la tabla está vacía, se llena con el JSON inicial.
        if (cargarDatos() && repositorio.listar().isEmpty() && archivo.existe()) {
            importarArchivo(true);
        }
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        Tipo tipo = cmbTipo.getValue();
        filtrados.setPredicate(pokemon ->
                (texto.isEmpty()
                        || pokemon.getNombre().toLowerCase().contains(texto)
                        || String.valueOf(pokemon.getNumero()).equals(texto))
                && (tipo == null || tipo == TODOS || pokemon.esDeTipo(tipo)));
    }

    /** Lee todo desde la base de datos. Retorna false si falló (ya se avisó al usuario). */
    private boolean cargarDatos() {
        try {
            repositorio.cargar();
            // El ComboBox de filtro se arma con los tipos que hay en la base de datos.
            ObservableList<Tipo> opciones = FXCollections.observableArrayList(TODOS);
            opciones.addAll(repositorio.tipos());
            cmbTipo.setItems(opciones);
            cmbTipo.setValue(TODOS);
            return true;
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los Pokémon", e.getMessage()
                    + "\n\nCuando el servidor esté disponible, pulsa Recargar.");
            return false;
        }
    }

    @FXML
    private void recargar() {
        if (cargarDatos()) {
            lblMensaje.setText("Datos leídos nuevamente desde la base de datos.");
        }
    }

    @FXML
    private void nuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void editar() {
        Pokemon seleccionado = tblPokemon.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    @FXML
    private void eliminar() {
        Pokemon seleccionado = tblPokemon.getSelectionModel().getSelectedItem();
        if (seleccionado == null
                || !Alertas.confirmar("¿Eliminar a " + seleccionado.getNombre() + "?",
                "Se borrará de la base de datos. Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            repositorio.eliminar(seleccionado);
            lblMensaje.setText(seleccionado.getNombre() + " fue eliminado.");
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo eliminar", e.getMessage());
        }
    }

    /** Abre el formulario en una ventana modal. Con null crea; con un Pokémon, edita. */
    private void abrirFormulario(Pokemon aEditar) {
        if (repositorio.tipos().isEmpty()) {
            Alertas.error("No hay tipos cargados", "La tabla tipo está vacía o no hay conexión."
                    + " Ejecuta datos.sql y pulsa Recargar.");
            return;
        }
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/cl/dsy1102/pokerap/view/formulario-view.fxml"));
            Parent raiz = cargador.load();
            FormularioController formulario = cargador.getController();
            formulario.inicializar(repositorio, aEditar);

            Stage ventana = new Stage();
            ventana.setTitle(aEditar == null ? "Nuevo Pokémon" : "Editar " + aEditar.getNombre());
            ventana.initOwner(tblPokemon.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setResizable(false);
            ventana.setScene(new Scene(raiz));
            ventana.showAndWait();   // el código sigue cuando la ventana se cierra

            Pokemon guardado = formulario.getGuardado();
            if (guardado != null) {
                tblPokemon.getSelectionModel().select(guardado);
                tblPokemon.scrollTo(guardado);
                lblMensaje.setText(guardado.getNombre() + (aEditar == null ? " fue agregado." : " fue actualizado."));
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar formulario-view.fxml", e);
        }
    }

    @FXML
    private void importar() {
        importarArchivo(false);
    }

    /** Lee el archivo JSON y guarda en la base de datos los Pokémon que falten. */
    private void importarArchivo(boolean cargaInicial) {
        try {
            List<PokemonJson> leidos = archivo.leer();
            int insertados = repositorio.importar(leidos);
            lblMensaje.setText((cargaInicial ? "Carga inicial desde el JSON: " : "JSON importado: ")
                    + insertados + " nuevos, " + (leidos.size() - insertados) + " ya existían.");
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo importar " + archivo.getArchivo(), e.getMessage());
        }
    }

    /**
     * Descarga la Pokédex desde la PokeAPI. Son 151 peticiones HTTP: si se
     * hicieran en el hilo de JavaFX, la ventana quedaría congelada. Un Task
     * hace el trabajo en otro hilo y avisa cuando termina.
     */
    @FXML
    private void descargar() {
        if (!Alertas.confirmar("¿Descargar desde la PokeAPI?",
                "Se consultarán " + CANTIDAD_DESCARGA + " Pokémon en pokeapi.co (requiere internet), se"
                + " reemplazará " + archivo.getArchivo() + " y se importarán los que falten en la base de datos.")) {
            return;
        }
        Task<List<PokemonJson>> tarea = new Task<>() {
            @Override
            protected List<PokemonJson> call() throws Exception {
                // Esto corre en el hilo secundario: aquí NO se tocan controles.
                return new PokeApiCliente().descargar(CANTIDAD_DESCARGA, this::updateProgress);
            }
        };
        barProgreso.progressProperty().bind(tarea.progressProperty());
        barProgreso.visibleProperty().bind(tarea.runningProperty());
        btnDescargar.disableProperty().bind(tarea.runningProperty());
        btnImportar.disableProperty().bind(tarea.runningProperty());
        lblMensaje.setText("Descargando desde pokeapi.co ...");

        // setOnSucceeded y setOnFailed se ejecutan de vuelta en el hilo de JavaFX.
        tarea.setOnSucceeded(evento -> {
            try {
                archivo.guardar(tarea.getValue());
                importarArchivo(false);
            } catch (PersistenciaException e) {
                Alertas.error("No se pudo guardar la descarga", e.getMessage());
            }
        });
        tarea.setOnFailed(evento -> {
            lblMensaje.setText("La descarga falló.");
            Alertas.error("No se pudo descargar desde la PokeAPI",
                    "Revisa tu conexión a internet.\n\nDetalle: " + tarea.getException().getMessage());
        });

        Thread hilo = new Thread(tarea, "descarga-pokeapi");
        hilo.setDaemon(true);   // no impide cerrar la aplicación
        hilo.start();
    }
}
