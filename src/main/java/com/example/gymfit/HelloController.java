
package com.example.gymfit;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HelloController {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<String[]> socios =
            FXCollections.observableArrayList();

    private int filaEnEdicion = -1;
    private List<Control> obligatorios;

    @FXML private MenuItem miNuevo, miGuardar, miEditar, miDuplicar;
    @FXML private MenuItem miBuscar, miBaja, miSalir, miAyuda;
    @FXML private MenuItem ciEditar, ciDuplicar, ciBaja;
    @FXML private CheckMenuItem miVerBarra;
    @FXML private ToolBar barra;

    @FXML private TextField nombre, apellidos, iban;
    @FXML private DatePicker fecha;
    @FXML private ComboBox<String> provincia;
    @FXML private ToggleGroup grupoSexo, grupoCuota;
    @FXML private FlowPane clases;
    @FXML private VBox panelAviso;
    @FXML private Label avisoTexto, estado;

    @FXML private TableView<String[]> tabla;
    @FXML private TableColumn<String[], String> colNombre;
    @FXML private TableColumn<String[], String> colApellidos;
    @FXML private TableColumn<String[], String> colFecha;
    @FXML private TableColumn<String[], String> colCuota;

    @FXML
    private void initialize() {

        acel(miNuevo, "Shortcut+N");
        acel(miGuardar, "Shortcut+S");
        acel(miEditar, "Shortcut+E");
        acel(ciEditar, "Shortcut+E");
        acel(miDuplicar, "Shortcut+D");
        acel(ciDuplicar, "Shortcut+D");
        acel(miBuscar, "Shortcut+F");
        acel(miBaja, "Delete");
        acel(ciBaja, "Delete");
        acel(miSalir, "Shortcut+Q");
        acel(miAyuda, "F1");

        fecha.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : FMT.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                try {
                    return s == null || s.isBlank()
                            ? null : LocalDate.parse(s.trim(), FMT);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });

        provincia.getItems().addAll(
                "Barcelona", "Madrid", "Málaga",
                "Sevilla", "Valencia", "Zaragoza"
        );

        grupoCuota.selectToggle(grupoCuota.getToggles().get(0));

        colNombre.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue()[0]));

        colApellidos.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue()[1]));

        colFecha.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue()[2]));

        colCuota.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue()[3]));

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setItems(socios);

        socios.add(new String[]{
                "Ana", "García López", "12/03/1995", "Premium"
        });

        socios.add(new String[]{
                "Luis", "Pérez Ruiz", "21/07/2001", "Joven"
        });

        socios.add(new String[]{
                "Marta", "Soler Vidal", "30/11/1988", "Plus"
        });

        socios.add(new String[]{
                "José", "Martín Gil", "02/02/1950", "Senior"
        });

        obligatorios = List.of(
                nombre, apellidos, fecha, provincia, iban
        );

        Platform.runLater(() -> nombre.requestFocus());
    }

    private void acel(MenuItem m, String combinacion) {
        m.setAccelerator(
                KeyCombination.keyCombination(combinacion)
        );
    }

    @FXML
    private void onNuevo() {
        limpiar();
        nombre.requestFocus();
        estado.setText("Formulario listo para un socio nuevo.");
    }

    @FXML
    private void onGuardar() {

        quitarMarcas();

        if (nombre.getText().isBlank()) {
            error(nombre,
                    "Falta el nombre. Escríbelo en el campo Nombre.");
            return;
        }

        if (apellidos.getText().isBlank()) {
            error(apellidos,
                    "Faltan los apellidos. Ejemplo: García López.");
            return;
        }

        LocalDate f = fecha.getConverter().fromString(
                fecha.getEditor().getText()
        );

        if (f == null) {
            error(fecha,
                    "La fecha no es correcta. Elígela en el calendario "
                            + "o utiliza el formato dd/mm/aaaa.");
            return;
        }

        fecha.setValue(f);

        if (provincia.getValue() == null) {
            error(provincia,
                    "Selecciona una provincia en la lista.");
            return;
        }

        if (!iban.getText().replace(" ", "").matches("ES\\d{22}")) {
            error(iban,
                    "El IBAN debe empezar por ES y contener 24 caracteres.");
            return;
        }

        if (grupoCuota.getSelectedToggle() == null) {
            error(nombre, "Selecciona un tipo de cuota.");
            return;
        }

        String[] fila = {
                nombre.getText().trim(),
                apellidos.getText().trim(),
                FMT.format(f),
                ((RadioButton) grupoCuota.getSelectedToggle()).getText()
        };

        if (filaEnEdicion >= 0) {
            socios.set(filaEnEdicion, fila);
        } else {
            socios.add(fila);
        }

        limpiar();
        estado.setText("Socio guardado: " + fila[0] + " " + fila[1]);
    }

    @FXML
    private void onEditar() {

        int f = filaElegida();
        if (f < 0) return;

        String[] s = socios.get(f);

        limpiar();

        nombre.setText(s[0]);
        apellidos.setText(s[1]);
        fecha.setValue(LocalDate.parse(s[2], FMT));

        for (Toggle t : grupoCuota.getToggles()) {
            if (((RadioButton) t).getText().equals(s[3])) {
                grupoCuota.selectToggle(t);
            }
        }

        filaEnEdicion = f;
        estado.setText("Editando a " + s[0]);
    }

    @FXML
    private void onDuplicar() {

        int f = filaElegida();
        if (f < 0) return;

        socios.add(f + 1, socios.get(f).clone());
        tabla.getSelectionModel().select(f + 1);
        estado.setText("Socio duplicado.");
    }

    @FXML
    private void onBaja() {

        int f = filaElegida();
        if (f < 0) return;

        String quien = socios.get(f)[0] + " " + socios.get(f)[1];

        if (confirmar(
                "¿Dar de baja a " + quien + "?",
                "Dar de baja",
                "Sí, dar de baja",
                "No, mantener"
        )) {
            socios.remove(f);
            estado.setText(quien + " ha sido dado de baja.");
        }
    }

    @FXML
    private void onBuscar() {

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Buscar socio");
        d.setHeaderText(null);
        d.setContentText("Nombre o apellidos:");

        d.showAndWait().ifPresent(texto -> {

            String t = texto.trim().toLowerCase();

            for (int i = 0; i < socios.size(); i++) {
                String[] s = socios.get(i);

                if (!t.isEmpty()
                        && (s[0] + " " + s[1]).toLowerCase().contains(t)) {
                    tabla.getSelectionModel().select(i);
                    estado.setText("Socio encontrado.");
                    return;
                }
            }

            estado.setText("No se ha encontrado el socio.");
        });
    }

    @FXML
    private void onEtiqueta() {

        int f = filaElegida();
        if (f < 0) return;

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Añadir etiqueta");
        d.setHeaderText(null);
        d.setContentText("Escribe una etiqueta:");

        d.showAndWait().ifPresent(t -> {
            if (!t.isBlank()) {
                estado.setText("Etiqueta añadida.");
            }
        });
    }

    @FXML
    private void onNotas() {
        info("Notas", "Este socio todavía no tiene notas.");
    }

    @FXML
    private void onExportar() {
        info("Exportar lista",
                "Aquí se guardaría la lista de socios en un archivo.");
    }

    @FXML
    private void onAyuda() {
        info("Guía de uso",
                "1. Rellena el formulario.\n"
                        + "2. Pulsa Guardar socio.\n"
                        + "3. Selecciona un socio para editarlo o darlo de baja.");
    }

    @FXML
    private void onSalir() {
        Platform.exit();
    }

    @FXML
    private void onVerBarra() {
        barra.setVisible(miVerBarra.isSelected());
        barra.setManaged(miVerBarra.isSelected());
    }

    @FXML
    private void onLimpiar() {
        if (confirmar(
                "¿Quieres vaciar todo el formulario?",
                "Limpiar formulario",
                "Sí, limpiar",
                "No, volver"
        )) {
            limpiar();
            estado.setText("Formulario vacío.");
        }
    }

    @FXML
    private void onCancelar() {
        boolean hayDatos =
                !nombre.getText().isBlank()
                        || !apellidos.getText().isBlank()
                        || !iban.getText().isBlank();

        if (hayDatos) {
            onLimpiar();
        }
    }

    private int filaElegida() {

        int f = tabla.getSelectionModel().getSelectedIndex();

        if (f < 0) {
            estado.setText("Primero selecciona un socio.");
        }

        return f;
    }

    private void error(Control campo, String mensaje) {

        avisoTexto.setText(mensaje);
        panelAviso.setVisible(true);
        panelAviso.setManaged(true);

        campo.getStyleClass().add("campo-error");
        campo.requestFocus();

        estado.setText("Revisa el campo marcado.");
    }

    private void quitarMarcas() {
        for (Control c : obligatorios) {
            c.getStyleClass().remove("campo-error");
        }
    }

    private void limpiar() {

        filaEnEdicion = -1;

        nombre.clear();
        apellidos.clear();
        iban.clear();

        fecha.setValue(null);
        fecha.getEditor().clear();

        provincia.setValue(null);
        grupoSexo.selectToggle(null);

        grupoCuota.selectToggle(grupoCuota.getToggles().get(0));

        for (Node n : clases.getChildren()) {
            ((CheckBox) n).setSelected(false);
        }

        panelAviso.setVisible(false);
        panelAviso.setManaged(false);

        quitarMarcas();
    }

    private boolean confirmar(
            String mensaje,
            String titulo,
            String si,
            String no
    ) {

        ButtonType bSi = new ButtonType(
                si, ButtonBar.ButtonData.OK_DONE
        );

        ButtonType bNo = new ButtonType(
                no, ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert a = new Alert(
                Alert.AlertType.WARNING, mensaje, bSi, bNo
        );

        a.setTitle(titulo);
        a.setHeaderText(null);

        return a.showAndWait()
                .filter(b -> b == bSi)
                .isPresent();
    }

    private void info(String titulo, String mensaje) {

        Alert a = new Alert(
                Alert.AlertType.INFORMATION, mensaje, ButtonType.OK
        );

        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}

