/*
 * Copyright (C) 2018 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
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
package it.cnr.istc.ale.client;

import com.fasterxml.jackson.core.type.TypeReference;
import it.cnr.istc.ale.client.context.Context;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Riccardo De Benedictis
 */
public class Config {

    private static final Logger LOG = Logger.getLogger(Config.class.getName());
    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    private Map<String, String> config;

    private Config() {
        try {
            config = Context.MAPPER.readValue(getClass().getResourceAsStream("/config.json"), new TypeReference<Map<String, String>>() {
            });
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public String getParam(Param param) {
        switch (param) {
            case Host:
                return config.get("host");
            case ServicePort:
                return config.get("service-port");
            case MQTTPort:
                return config.get("mqtt-port");
            default:
                throw new AssertionError(param.name());
        }
    }

    public enum Param {
        Host,
        ServicePort,
        MQTTPort
    }
}
