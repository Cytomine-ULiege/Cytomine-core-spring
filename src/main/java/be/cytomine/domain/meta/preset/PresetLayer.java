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
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * The preset for storing the selected layers in an image.
 */
@Entity
@Getter
@Setter
public class PresetLayer extends CytomineDomain {

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
     * The owner of the layer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The opacity of the layer.
     */
    private Double opacity;

    /**
     * Whether the layer can be modified or not.
     */
    private Boolean drawOn;

    /**
     * Whether the layer is visible or not.
     */
    private Boolean visible;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        PresetLayer pl = this;

        pl.id = json.getJSONAttrLong("id", null);
        pl.created = json.getJSONAttrDate("created");
        pl.updated = json.getJSONAttrDate("updated");

        pl.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        pl.preset = (Preset) json.getJSONAttrDomain(entityManager, "preset", new Preset(), true);
        pl.user = (User) json.getJSONAttrDomain(entityManager, "user", new User(), true);

        pl.opacity = json.getJSONAttrDouble("opacity", null);
        pl.drawOn = json.getJSONAttrBoolean("drawOn", null);
        pl.visible = json.getJSONAttrBoolean("visible", null);

        return pl;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        PresetLayer pl = (PresetLayer) domain;

        returnArray.put("image", pl.getImage().getId());
        returnArray.put("preset", pl.getPreset().getId());
        returnArray.put("user", pl.getUser().getId());

        returnArray.put("opacity", pl.getOpacity());
        returnArray.put("drawOn", pl.getDrawOn());
        returnArray.put("visible", pl.getVisible());

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
