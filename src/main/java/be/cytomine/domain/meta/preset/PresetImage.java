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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * The preset for storing the selected czt in an image.
 */
@Entity
@Getter
@Setter
public class PresetImage extends CytomineDomain {

    /**
     * The image related to the channel.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageInstance image;

    /**
     * The preset profile.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id")
    private Preset preset;

    /**
     * The current rotation of the image.
     */
    private Double rotation;

    /**
     * The current c index.
     */
    private Integer c;

    /**
     * The current z index.
     */
    private Integer z;

    /**
     * The current t index.
     */
    private Integer t;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        PresetImage pi = this;

        pi.id = json.getJSONAttrLong("id", null);
        pi.created = json.getJSONAttrDate("created");
        pi.updated = json.getJSONAttrDate("updated");

        pi.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        pi.preset = (Preset) json.getJSONAttrDomain(entityManager, "preset", new Preset(), true);

        pi.rotation = json.getJSONAttrDouble("rotation", null);
        pi.c = json.getJSONAttrInteger("c", null);
        pi.z = json.getJSONAttrInteger("z", null);
        pi.t = json.getJSONAttrInteger("t", null);

        return pi;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        PresetImage pi = (PresetImage) domain;

        returnArray.put("image", pi.getImage().getId());
        returnArray.put("preset", pi.getPreset().getId());

        returnArray.put("rotation", pi.getRotation());
        returnArray.put("c", pi.getC());
        returnArray.put("z", pi.getZ());
        returnArray.put("t", pi.getT());

        return returnArray;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return preset.container();
    }
}
