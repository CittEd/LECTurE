/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
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
package it.cnr.istc.ale.server;

import static it.cnr.istc.ale.server.Context.HOST;
import static it.cnr.istc.ale.server.Context.MQTT_PORT;
import static it.cnr.istc.ale.server.Context.SERVICE_PORT;
import it.cnr.istc.ale.server.resources.LessonResource;
import it.cnr.istc.ale.server.resources.UserResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.UriBuilder;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());
    public static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ALE_PU");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(UriBuilder.fromUri("http://" + HOST + ":" + SERVICE_PORT).build(), new ResourceConfig(UserResource.class, LessonResource.class));

        BrokerService broker = new BrokerService();
        broker.addConnector(UriBuilder.fromUri("mqtt://" + HOST + ":" + MQTT_PORT).build());
        broker.setPlugins(new BrokerPlugin[]{new BrokerPlugin() {
            @Override
            public Broker installPlugin(Broker broker) throws Exception {
                return new BrokerFilter(broker) {
                    @Override
                    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
                        LOG.log(Level.INFO, "New connection: {0}", info);
                        if (!info.getClientId().equals(Context.SERVER_ID)) {
                            Context.getContext().addConnection(info.getClientId());
                        }
                        super.addConnection(context, info);
                    }

                    @Override
                    public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error) throws Exception {
                        LOG.log(Level.INFO, "Lost connection: {0}", info);
                        if (!info.getClientId().equals(Context.SERVER_ID)) {
                            Context.getContext().removeConnection(info.getClientId());
                        }
                        super.removeConnection(context, info, error);
                    }
                };
            }
        }});
        broker.start();
    }
}
