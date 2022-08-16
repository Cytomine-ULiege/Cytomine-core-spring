package be.cytomine.api.controller.ontology;

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

import be.cytomine.api.controller.RestCytomineController;
import be.cytomine.domain.image.CompanionFile;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.image.CompanionFileRepository;
import be.cytomine.service.image.AbstractImageService;
import be.cytomine.service.middleware.ImageServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.*;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestAnnotationProfileController extends RestCytomineController {

    private final EntityManager entityManager;

    private final AbstractImageService abstractImageService;

    private final ImageServerService imageServerService;

    private final CompanionFileRepository companionFileRepository;


    @GetMapping("/api/annotation/{id}/profile/projections.json")
    public ResponseEntity<String> projections(@PathVariable Long id, @RequestParam String format) {
        return profile(id, null, "projections", format, null);
    }

    @GetMapping("/api/annotation/{id}/profile/min-projection.json")
    public ResponseEntity<String> minProjection(@PathVariable Long id, @RequestParam String format) {
        return profile(id, null, "image-projection", format, "min");
    }

    @GetMapping("/api/annotation/{id}/profile/max-projection.json")
    public ResponseEntity<String> maxProjection(@PathVariable Long id, @RequestParam String format) {
        return profile(id, null, "image-projection", format, "max");
    }

    @GetMapping("/api/annotation/{id}/profile/average-projection.json")
    public ResponseEntity<String> averageProjection(@PathVariable Long id, @RequestParam String format) {
        return profile(id, null, "image-projection", format, "average");
    }

    @GetMapping("/api/annotation/{id}/profile.json")
    public ResponseEntity<String> profile(
            @PathVariable Long id,
            @RequestParam String axis,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String projection

    ) {
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(entityManager, id);

        if (!abstractImageService.hasProfile(annotation.getImage().getBaseImage())) {
            throw new ObjectNotFoundException("No profile for abstract image " + annotation.getImage().getBaseImage());
        }

        CompanionFile cf = companionFileRepository.findByImageAndType(annotation.getImage().getBaseImage(), "HDF5");
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        if (type.equals("projections")) {
            boolean hasAxis = Arrays.asList("x,y", "xy", "spatial").contains(axis);
            if (hasAxis) {
                if (annotation.getImage().getBaseImage().getDuration() > 1) {
                    params.put("dimension", "time");
                } else if (annotation.getImage().getBaseImage().getDepth() > 1) {
                    params.put("dimension", "zStack");
                } else if (annotation.getImage().getBaseImage().getChannels() > 1) {
                    params.put("dimension", "channel");
                } else {
                    params.put("dimension", "slice");
                }
            }

            Map<String, Object> projections = imageServerService.profileProjections(cf, annotation, params);
            /*if (params.format == "csv") {
                response.contentType = grailsApplication.config.grails.mime.types[params.format]
                response.setHeader("Content-disposition", "attachment; filename=projections-annotation-${annotation.id}.${params.format}")

                Map labels
                def csvClosure
                if (params.axis == 'xy') {
                    labels = [(params.dimension): params.dimension]
                    csvClosure = { it -> [(params.dimension): it[params.dimension], min: it.min, max: it.max, average: it.average] }
                }
                else {
                    labels = [x: "X", y: "Y"]
                    csvClosure = { it -> [x: it.point[0], y: it.point[1], min: it.min, max: it.max, average: it.average] }
                }

                labels += [min: "minimum intensity", max: "maximum intensity", average: "average intensity"]
                def fields = labels.keySet() as List
                def csvData = projections.collect(csvClosure)

                exportService.export("csv", response.outputStream, csvData, fields, labels, null, ["csv.encoding": "UTF-8", "separator": ";"])
            }*/

            return responseSuccess(projections);
        }

        if (type.equals("image-projection")) {
            return responseSuccess(imageServerService.profileImageProjection(cf, annotation, params));
        }

        return responseSuccess(imageServerService.profile(cf, annotation, params));
    }
}
