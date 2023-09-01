package be.cytomine.domain.meta.preset;

/*
 * Copyright (c) 2009-2023. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * The preset profile for a specific image with a user.
 */
@Entity
@Getter
@Setter
public class Preset extends CytomineDomain {

    /**
     * The project related to the preset
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * The owner of the preset
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The name to identify the preset
     */
    private String name;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        Preset preset = this;

        preset.setProject((Project) json.getJSONAttrDomain(entityManager, "project", new Project(), true));
        preset.setUser((User) json.getJSONAttrDomain(entityManager, "user", new User(), true));

        return preset;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        Preset preset = (Preset) domain;

        returnArray.put("project", preset.getProject().getId());
        returnArray.put("user", preset.getUser().getId());

        return returnArray;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return project.container();
    }
}
