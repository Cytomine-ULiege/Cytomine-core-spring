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

import javax.persistence.*;
import javax.validation.constraints.Min;

/**
 * The preset for storing a channel's information in an image.
 */
@Entity
@Getter
@Setter
public class PresetChannel extends CytomineDomain {

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
     * Channel inversion.
     */
    private Boolean inverted;

    /**
     * Intensities adjustment. Either min/max or brightness/contrast.
     */
    private Boolean intMinMax;

    /**
     * Histogram scale. Either linear or logarithmic.
     */
    private Boolean logScale;

    /**
     * The visibility of the channel.
     */
    private Boolean visible;

    /**
     * Gamma correction.
     */
    private Double gamma;

    /**
     * Channel index.
     */
    @Min(0)
    private Integer channel;

    /**
     * Minimum bounds set by the user.
     */
    @Min(0)
    private Integer min;

    /**
     * Maximum bounds set by the user.
     */
    private Integer max;

    /**
     * The color of the channel.
     */
    private String color;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        PresetChannel pc = this;

        pc.id = json.getJSONAttrLong("id", null);
        pc.created = json.getJSONAttrDate("created");
        pc.updated = json.getJSONAttrDate("updated");

        pc.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        pc.preset = (Preset) json.getJSONAttrDomain(entityManager, "preset", new Preset(), true);

        pc.inverted = json.getJSONAttrBoolean("inverted", false);
        pc.intMinMax = json.getJSONAttrBoolean("intMinMax", true);
        pc.logScale = json.getJSONAttrBoolean("logScale", true);
        pc.visible = json.getJSONAttrBoolean("visible", true);
        pc.gamma = json.getJSONAttrDouble("gamma", null);
        pc.channel = json.getJSONAttrInteger("channel", 0);
        pc.min = json.getJSONAttrInteger("min", 0);
        pc.max = json.getJSONAttrInteger("max", null);
        pc.color = json.getJSONAttrStr("color", null);

        return pc;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        PresetChannel pc = (PresetChannel) domain;

        returnArray.put("image", pc.getImage().getId());
        returnArray.put("preset", pc.getPreset().getId());

        returnArray.put("inverted", pc.getInverted());
        returnArray.put("intMinMax", pc.getIntMinMax());
        returnArray.put("logScale", pc.getLogScale());
        returnArray.put("visible", pc.getVisible());
        returnArray.put("gamma", pc.getGamma());
        returnArray.put("channel", pc.getChannel());
        returnArray.put("min", pc.getMin());
        returnArray.put("max", pc.getMax());
        returnArray.put("color", pc.getColor());

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
