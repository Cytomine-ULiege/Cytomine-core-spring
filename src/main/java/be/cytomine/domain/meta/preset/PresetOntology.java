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
import be.cytomine.domain.ontology.Term;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * The preset for storing the state of the terms in an image.
 */
@Entity
@Getter
@Setter
public class PresetOntology extends CytomineDomain {

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
     * A term in the ontology.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    /**
     * The opacity of the layer.
     */
    private Double opacity;

    /**
     * Whether the layer is visible or not.
     */
    private Boolean visible;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        PresetOntology po = this;

        po.id = json.getJSONAttrLong("id", null);
        po.created = json.getJSONAttrDate("created");
        po.updated = json.getJSONAttrDate("updated");

        po.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        po.preset = (Preset) json.getJSONAttrDomain(entityManager, "preset", new Preset(), true);
        po.term = (Term) json.getJSONAttrDomain(entityManager, "term", new Term(), true);

        po.opacity = json.getJSONAttrDouble("opacity", null);
        po.visible = json.getJSONAttrBoolean("visible", null);

        return po;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        PresetOntology po = (PresetOntology) domain;

        returnArray.put("image", po.getImage().getId());
        returnArray.put("preset", po.getPreset().getId());
        returnArray.put("term", po.getTerm().getId());

        returnArray.put("opacity", po.getOpacity());
        returnArray.put("visible", po.getVisible());

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
