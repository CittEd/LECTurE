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
package it.cnr.istc.lecture.desktopapp.api.model;

import java.util.Collection;
import java.util.Collections;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 *
 * @author Riccardo De Benedictis
 */
public class OrCondition extends Condition {

    private final Collection<Condition> conditions;

    @JsonbCreator
    public OrCondition(@JsonbProperty("conditions") Collection<Condition> conditions) {
        super(ConditionType.OrCondition);
        this.conditions = conditions;
    }

    public Collection<Condition> getConditions() {
        return Collections.unmodifiableCollection(conditions);
    }
}