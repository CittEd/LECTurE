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

import it.cnr.istc.lecture.api.Credentials;
import it.cnr.istc.lecture.api.InitResponse;
import it.cnr.istc.lecture.api.NewUserRequest;
import it.cnr.istc.lecture.api.Parameter;
import it.cnr.istc.lecture.api.User;
import it.cnr.istc.lecture.api.messages.NewParameter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author Riccardo De Benedictis
 */
public class Context {

    private static final Logger LOG = Logger.getLogger(Context.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static Context ctx;

    public static Context getContext() {
        if (ctx == null) {
            ctx = new Context();
        }
        return ctx;
    }
    private final Properties properties = new Properties();
    private final Client client = ClientBuilder.newClient();
    private final WebTarget target;
    private MqttClient mqtt;
    private Stage stage;
    /**
     * The current user.
     */
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();
    /**
     * The current user's parameter types.
     */
    private final Map<String, Parameter> par_types = new HashMap<>();
    /**
     * The current user's parameter values.
     */
    private final Map<String, Map<String, StringProperty>> par_vals = new HashMap<>();
    /**
     * The current user's parameter values as a list, to be displayed on tables.
     * Notice that each parameter can aggregate more than a single value.
     */
    private final ObservableList<ParameterValue> par_values = FXCollections.observableArrayList();

    private Context() {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.target = client.target("http://" + properties.getProperty("host", "localhost") + ":" + properties.getProperty("service-port", "8080")).path("LECTurE-WebApp").path("LECTurE");
        user.addListener((ObservableValue<? extends User> observable, User oldValue, User newValue) -> {
            if (oldValue != null) {
                // we clear the current data..
                try {
                    mqtt.disconnect();
                    mqtt.close();
                    par_values.clear();
                    par_vals.clear();
                    par_types.clear();
                } catch (MqttException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            if (newValue != null) {
                // we set up a new user..
                try {
                    mqtt = new MqttClient("tcp://" + properties.getProperty("host", "localhost") + ":" + properties.getProperty("mqtt-port", "1883"), String.valueOf(newValue.id), new MemoryPersistence());
                    mqtt.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            LOG.log(Level.SEVERE, null, cause);
                            user.set(null);
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            LOG.log(Level.WARNING, "message arrived: {0} - {1}", new Object[]{topic, message});
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    options.setAutomaticReconnect(true);
                    mqtt.connect(options);

                    for (Parameter par : newValue.par_types.values()) {
                        par_types.put(par.name, par);
                        // we broadcast the existence of a new parameter..
                        mqtt.publish(newValue.id + "/output", JSONB.toJson(new NewParameter(par)).getBytes(), 1, false);
                    }
                    for (Map.Entry<String, Map<String, String>> par_val : newValue.par_values.entrySet()) {
                        Map<String, StringProperty> c_vals = new HashMap<>();
                        for (Map.Entry<String, String> val : par_val.getValue().entrySet()) {
                            SimpleStringProperty val_prop = new SimpleStringProperty(val.getValue());
                            c_vals.put(val.getKey(), val_prop);
                            par_values.add(new ParameterValue(par_val.getKey() + "." + val.getKey(), val_prop));
                        }
                        par_vals.put(par_val.getKey(), c_vals);
                        // we broadcast the the new value of the parameter..
                        mqtt.publish(newValue.id + "/output/" + par_val.getKey(), JSONB.toJson(par_val.getValue()).getBytes(), 1, true);
                    }
                } catch (MqttException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public void login(String email, String password) {
        Map<String, Parameter> par_tps = load_pars();
        Map<String, Map<String, String>> par_vls = load_par_vals();
        Credentials credentials = new Credentials(email, password, par_tps, par_vls);
        InitResponse init = target.path("login").request(MediaType.APPLICATION_JSON).post(Entity.json(credentials), InitResponse.class);
        init.user.par_types = par_tps;
        init.user.par_values = par_vls;
        user.set(init.user);
    }

    public void logout() {
        user.set(null);
    }

    public void newUser(String email, String password, String first_name, String last_name) {
        Map<String, Parameter> par_tps = load_pars();
        Map<String, Map<String, String>> par_vls = load_par_vals();
        NewUserRequest new_user = new NewUserRequest(email, password, first_name, last_name, par_tps, par_vls);
        User u = target.path("newUser").request(MediaType.APPLICATION_JSON).post(Entity.json(new_user), User.class);
        u.par_types = par_tps;
        u.par_values = par_vls;
        user.set(u);
    }

    private static Map<String, Parameter> load_pars() {
        Collection<Parameter> pars = JSONB.fromJson(Context.class.getResourceAsStream("/parameters/types.json"), new ArrayList<Parameter>() {
        }.getClass().getGenericSuperclass());
        return pars.stream().collect(Collectors.toMap(p -> p.name, p -> p));
    }

    private static Map<String, Map<String, String>> load_par_vals() {
        Map<String, Map<String, String>> par_vals = JSONB.fromJson(Context.class.getResourceAsStream("/parameters/values.json"), new HashMap<String, Map<String, String>>() {
        }.getClass().getGenericSuperclass());
        return par_vals;
    }

    public static class ParameterValue {

        private final StringProperty name;
        private final StringProperty value;

        ParameterValue(String name, StringProperty value) {
            this.name = new SimpleStringProperty(name);
            this.value = value;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty valueProperty() {
            return value;
        }
    }
}
