/*
 * Copyright (C) 2018 ISTC - CNR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.lecture.desktopapp;

import it.cnr.istc.lecture.api.model.LessonModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import static javafx.scene.layout.GridPane.setHgrow;
import static javafx.scene.layout.GridPane.setVgrow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 *
 * @author Riccardo De Benedictis
 */
public class AddLessonDialog extends Dialog<AddLessonDialog.AddLessonResult> {

    private static final FileChooser FILE_CHOOSER = new FileChooser();
    private final GridPane grid = new GridPane();
    private final ComboBox<LessonModel> lesson_types = new ComboBox<>();
    private final TextField lesson_name = new TextField();
    private final Button open_button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.FILE_CODE_ALT));
    private final ObservableList<StudentRole> roles = FXCollections.observableArrayList();
    private final TableView<StudentRole> roles_table_view = new TableView<>(roles);
    private final TableColumn<StudentRole, String> role_column = new TableColumn<>(Context.LANGUAGE.getString("ROLE"));
    private final TableColumn<StudentRole, StudentContext> student_column = new TableColumn<>(Context.LANGUAGE.getString("STUDENT"));
    private final ButtonType add_button = new ButtonType(Context.LANGUAGE.getString("ADD"), ButtonBar.ButtonData.OK_DONE);
    private LessonModel lesson_model;

    @SuppressWarnings("unchecked")
    public AddLessonDialog() {
        grid.setHgap(10);
        grid.setVgap(10);
        setHgrow(lesson_types, Priority.ALWAYS);
        setHgrow(lesson_name, Priority.ALWAYS);
        setVgrow(roles_table_view, Priority.ALWAYS);
        setHgrow(roles_table_view, Priority.ALWAYS);

        setTitle(Context.LANGUAGE.getString("ADD LESSON"));

        grid.add(new Label(Context.LANGUAGE.getString("LESSON TYPE") + ":"), 0, 0);
        lesson_types.setPromptText(Context.LANGUAGE.getString("LESSON TYPE"));
        lesson_types.setEditable(false);
        lesson_types.setItems(Context.getContext().modelsProperty());
        lesson_types.valueProperty().addListener((ObservableValue<? extends LessonModel> observable, LessonModel oldValue, LessonModel newValue) -> {
            lesson_model = newValue;
            roles.clear();
            if (newValue != null) {
                lesson_model.roles.forEach(role -> roles.add(new StudentRole(role, null)));
            }
            getDialogPane().lookupButton(add_button).disableProperty().unbind();
            getDialogPane().lookupButton(add_button).disableProperty().bind(lesson_types.valueProperty().isNull().or(lesson_name.textProperty().isEmpty()).or(new StudentRoleBinding()));
        });
        lesson_types.setCellFactory((ListView<LessonModel> param) -> new ListCell<LessonModel>() {
            @Override
            public void updateItem(LessonModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name);
                }
            }
        });
        lesson_types.setConverter(new StringConverter<LessonModel>() {
            @Override
            public String toString(LessonModel object) {
                return object.name;
            }

            @Override
            public LessonModel fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet..");
            }
        });
        grid.add(lesson_types, 1, 0);
        grid.add(open_button, 2, 0);
        grid.add(new Label(Context.LANGUAGE.getString("LESSON NAME") + ":"), 0, 1);
        lesson_name.setPromptText(Context.LANGUAGE.getString("LESSON NAME"));
        grid.add(lesson_name, 1, 1, 2, 1);
        grid.add(roles_table_view, 0, 2, 3, 1);

        roles_table_view.getColumns().addAll(role_column, student_column);
        roles_table_view.setEditable(true);

        roles_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        role_column.setCellValueFactory(new PropertyValueFactory<>("role"));
        student_column.setCellValueFactory(new PropertyValueFactory<>("student"));
        student_column.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<StudentContext>() {
            @Override
            public String toString(StudentContext std_ctx) {
                if (std_ctx == null) {
                    return Context.LANGUAGE.getString("SELECT ONE");
                } else {
                    return std_ctx.getStudent().first_name + " " + std_ctx.getStudent().last_name;
                }
            }

            @Override
            public StudentContext fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet..");
            }
        }, Context.getContext().studentsProperty()));
        student_column.setEditable(true);
        student_column.setOnEditCommit((TableColumn.CellEditEvent<StudentRole, StudentContext> event) -> {
            roles.get(event.getTablePosition().getRow()).student.set(event.getNewValue());
        });

        getDialogPane().setContent(grid);

        open_button.setOnAction((ActionEvent event) -> {
            FILE_CHOOSER.setTitle(Context.LANGUAGE.getString("OPEN LESSON FILE"));
            FILE_CHOOSER.setInitialDirectory(new File(System.getProperty("user.home")));
            FILE_CHOOSER.getExtensionFilters().clear();
            FILE_CHOOSER.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(Context.LANGUAGE.getString("LESSON"), "*.json"),
                    new FileChooser.ExtensionFilter(Context.LANGUAGE.getString("ALL FILES"), "*.*")
            );
            File lesson_file = FILE_CHOOSER.showOpenDialog(Context.getContext().getStage());
            if (lesson_file != null) {
                try {
                    lesson_types.setValue(Context.JSONB.fromJson(new FileInputStream(lesson_file), LessonModel.class));
                } catch (IOException ex) {
                    Logger.getLogger(AddLessonDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        getDialogPane().getButtonTypes().add(add_button);
        getDialogPane().lookupButton(add_button).disableProperty().set(true);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ((Stage) getDialogPane().getScene().getWindow()).getIcons().addAll(Context.getContext().getStage().getIcons());
        setResultConverter((ButtonType param) -> param == add_button ? new AddLessonResult(lesson_model, lesson_name.getText(), roles.stream().collect(Collectors.toMap(StudentRole::getRole, StudentRole::getStudentId))) : null);
    }

    public class StudentRoleBinding extends BooleanBinding {

        public StudentRoleBinding() {
            roles.forEach(role -> bind(role.student));
        }

        @Override
        protected boolean computeValue() {
            return roles.stream().anyMatch(role -> role.student.get() == null);
        }
    }

    public static class StudentRole {

        public final StringProperty role;
        public final ObjectProperty<StudentContext> student;

        public StudentRole(String name, StudentContext student) {
            this.role = new SimpleStringProperty(name);
            this.student = new SimpleObjectProperty<>(student);
        }

        public String getRole() {
            return role.get();
        }

        public StringProperty roleProperty() {
            return role;
        }

        public long getStudentId() {
            return student.get().getStudent().id;
        }

        public ObjectProperty<StudentContext> studentProperty() {
            return student;
        }
    }

    public static class AddLessonResult {

        private final LessonModel model;
        private final String lesson_name;
        private final Map<String, Long> roles;

        private AddLessonResult(LessonModel model, String lesson_name, Map<String, Long> roles) {
            this.model = model;
            this.lesson_name = lesson_name;
            this.roles = roles;
        }

        public LessonModel getModel() {
            return model;
        }

        public String getLessonName() {
            return lesson_name;
        }

        public Map<String, Long> getRoles() {
            return Collections.unmodifiableMap(roles);
        }
    }
}
