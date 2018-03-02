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
package it.cnr.istc.lecture.webapp.api.model;

import java.util.Collection;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 *
 * @author Riccardo De Benedictis
 */
public class URLEventTemplate extends EventTemplate {

    private final String content;
    private final String url;

    @JsonbCreator
    public URLEventTemplate(@JsonbProperty("name") String name, @JsonbProperty("role") String role, @JsonbProperty("triggerCondition") Condition trigger_condition, @JsonbProperty("executionCondition") Condition execution_condition, @JsonbProperty("events") Collection<String> events, @JsonbProperty("relations") Collection<Relation> relations, @JsonbProperty("content") String content, @JsonbProperty("url") String url) {
        super(EventTemplateType.URLEventTemplate, name, role, trigger_condition, execution_condition, events, relations);
        this.content = content;
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }
}
