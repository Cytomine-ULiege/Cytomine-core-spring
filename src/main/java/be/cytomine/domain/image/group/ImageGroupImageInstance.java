package be.cytomine.domain.image.group;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class ImageGroupImageInstance extends CytomineDomain {

    @ManyToOne(fetch = FetchType.LAZY)
    private ImageGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    private ImageInstance image;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ImageGroupImageInstance igii = this;

        igii.id = json.getJSONAttrLong("id", null);
        igii.created = json.getJSONAttrDate("created");
        igii.updated = json.getJSONAttrDate("updated");

        igii.group = (ImageGroup) json.getJSONAttrDomain(entityManager, "group", new ImageGroup(), true);
        igii.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);

        return igii;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ImageGroupImageInstance igii = (ImageGroupImageInstance) domain;

        returnArray.put("image", igii.getImage().getId());
        returnArray.put("group", igii.getGroup().getId());
        returnArray.put("groupName", igii.getGroup().getName());

        return returnArray;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    @Override
    public CytomineDomain container() {
        return group.container();
    }
}
