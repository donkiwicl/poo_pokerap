package cl.dsy1102.pokerap.controller;

import cl.dsy1102.pokerap.model.Pokemon;
import cl.dsy1102.pokerap.model.Tipo;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

import java.util.Map;

/**
 * Celda que dibuja el tipo como una etiqueta de color (setCellFactory).
 * La celda decide CÓMO se ve el dato; el dato sigue siendo un Tipo.
 */
public class CeldaTipo extends TableCell<Pokemon, Tipo> {

    private static final Map<String, String> COLORES = Map.ofEntries(
            Map.entry("Normal", "#8a8a6b"), Map.entry("Lucha", "#b0342c"), Map.entry("Volador", "#7f6fd0"),
            Map.entry("Veneno", "#933d93"), Map.entry("Tierra", "#b99a3f"), Map.entry("Roca", "#a08a32"),
            Map.entry("Bicho", "#8a9a1c"), Map.entry("Fantasma", "#63508a"), Map.entry("Acero", "#8d8da6"),
            Map.entry("Fuego", "#d9601f"), Map.entry("Agua", "#4a78d8"), Map.entry("Planta", "#4f9e37"),
            Map.entry("Eléctrico", "#c9a511"), Map.entry("Psíquico", "#e0457a"), Map.entry("Hielo", "#4fa9a6"),
            Map.entry("Dragón", "#5c2ee0"), Map.entry("Siniestro", "#5f4a3d"), Map.entry("Hada", "#c9638f"));

    private final Label etiqueta = new Label();

    public CeldaTipo() {
        etiqueta.getStyleClass().add("etiqueta-tipo");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Tipo tipo, boolean vacia) {
        super.updateItem(tipo, vacia);
        // Las celdas se reutilizan al hacer scroll: siempre hay que limpiar.
        if (vacia || tipo == null) {
            setGraphic(null);
        } else {
            etiqueta.setText(tipo.nombre());
            etiqueta.setStyle("-fx-background-color: " + COLORES.getOrDefault(tipo.nombre(), "#666666") + ";");
            setGraphic(etiqueta);
        }
    }
}
