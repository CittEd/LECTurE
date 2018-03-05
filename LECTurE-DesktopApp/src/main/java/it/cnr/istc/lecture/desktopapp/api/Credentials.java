/*
 * Copyright (C) 2018 Riccardo De Benedictis
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
package it.cnr.istc.lecture.desktopapp.api;

import java.util.Collections;
import java.util.Map;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 *
 * @author Riccardo De Benedictis
 */
public class Credentials {

    private final String email;
    private final String password;
    private final Map<Parameter, Map<String, String>> parameters;

    @JsonbCreator
    public Credentials(@JsonbProperty("email") String email, @JsonbProperty("password") String password, @JsonbProperty("parameters") Map<Parameter, Map<String, String>> parameters) {
        this.email = email;
        this.password = password;
        this.parameters = parameters;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Map<Parameter, Map<String, String>> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}