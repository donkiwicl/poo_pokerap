package cl.dsy1102.pokerap.controller;

import cl.dsy1102.pokerap.dao.PersistenciaException;
import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.model.Tipo;
import cl.dsy1102.pokerap.repository.PokemonRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Formulario para crear o editar un Pokémon (ventana modal).
 *
 * Valida en dos niveles: el formato de lo que se escribió (¿es un número?) lo
 * revisa el controlador; las reglas del negocio (rangos, tipos distintos) las
 * revisa el constructor de Pokemon. Los errores se muestran en el mismo formulario.
 */
public class FormularioController {

    /** Opción "sin tipo secundario". No existe en la base de datos. */
    private static final Tipo NINGUNO = new Tipo(0, "(ninguno)");

    @FXML private Label lblTitulo;
    @FXML private TextField txtNumero;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Tipo> cmbTipoPrincipal;
    @FXML private ComboBox<Tipo> cmbTipoSecundario;
    @FXML private TextField txtAltura;
    @FXML private TextField txtPeso;
    @FXML private TextField txtHp;
    @FXML private TextField txtAtaque;
    @FXML private TextField txtDefensa;
    @FXML private TextField txtVelocidad;
    @FXML private Label lblError;

    private PokemonRepository repositorio;
    private Pokemon original;   // null = se está creando uno nuevo
    private Pokemon guardado;

    public void inicializar(PokemonRepository repositorio, Pokemon aEditar) {
        this.repositorio = repositorio;
        this.original = aEditar;

        cmbTipoPrincipal.setItems(repositorio.tipos());
        ObservableList<Tipo> secundarios = FXCollections.observableArrayList(NINGUNO);
        secundarios.addAll(repositorio.tipos());
        cmbTipoSecundario.setItems(secundarios);
        cmbTipoSecundario.setValue(NINGUNO);

        if (aEditar == null) {
            lblTitulo.setText("Nuevo Pokémon");
            txtNumero.setText(String.valueOf(siguienteNumero()));
        } else {
            lblTitulo.setText("Editar #" + aEditar.getNumero() + " " + aEditar.getNombre());
            txtNumero.setText(String.valueOf(aEditar.getNumero()));
            txtNumero.setDisable(true);   // el número identifica al Pokémon: no se edita
            txtNombre.setText(aEditar.getNombre());
            cmbTipoPrincipal.setValue(aEditar.getTipoPrincipal());
            if (aEditar.getTipoSecundario() != null) {
                cmbTipoSecundario.setValue(aEditar.getTipoSecundario());
            }
            txtAltura.setText(String.format(Locale.US, "%.1f", aEditar.getAltura()));
            txtPeso.setText(String.format(Locale.US, "%.1f", aEditar.getPeso()));
            txtHp.setText(String.valueOf(aEditar.getHp()));
            txtAtaque.setText(String.valueOf(aEditar.getAtaque()));
            txtDefensa.setText(String.valueOf(aEditar.getDefensa()));
            txtVelocidad.setText(String.valueOf(aEditar.getVelocidad()));
        }
    }

    /** Sugerencia para el número de un Pokémon nuevo: el mayor + 1. */
    private int siguienteNumero() {
        int mayor = 0;
        for (Pokemon pokemon : repositorio.listar()) {
            mayor = Math.max(mayor, pokemon.getNumero());
        }
        return mayor + 1;
    }

    /** El Pokémon que quedó guardado, o null si se canceló. */
    public Pokemon getGuardado() {
        return guardado;
    }

    @FXML
    private void guardar() {
        try {
            Tipo secundario = cmbTipoSecundario.getValue();
            Pokemon pokemon = new Pokemon(
                    leerEntero(txtNumero, "El número"),
                    txtNombre.getText(),
                    cmbTipoPrincipal.getValue(),
                    secundario == NINGUNO ? null : secundario,
                    leerDecimal(txtAltura, "La altura"),
                    leerDecimal(txtPeso, "El peso"),
                    leerEntero(txtHp, "HP"),
                    leerEntero(txtAtaque, "Ataque"),
                    leerEntero(txtDefensa, "Defensa"),
                    leerEntero(txtVelocidad, "Velocidad"));

            if (original == null) {
                repositorio.agregar(pokemon);
            } else {
                repositorio.actualizar(original, pokemon);
            }
            guardado = pokemon;
            cerrar();
        } catch (IllegalArgumentException | PersistenciaException e) {
            // Dato inválido o falla de la base de datos: la ventana sigue
            // abierta para que el usuario corrija sin perder lo que escribió.
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private static int leerEntero(TextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + " debe ser un número entero.");
        }
    }

    /** Acepta coma o punto decimal. */
    private static double leerDecimal(TextField campo, String nombre) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + " debe ser un número, por ejemplo 1.7");
        }
    }
}
