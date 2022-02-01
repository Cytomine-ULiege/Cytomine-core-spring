package be.cytomine.domain.social;

import be.cytomine.utils.DateUtils;
import be.cytomine.utils.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Info on user connection for a project
 * ex : User x connect to project y the 2013/01/01 at time y
 */
@Getter
@Setter
@Document
@Entity
//@CompoundIndex(def = "{'user' : 1, 'image' : 1, 'created': -1}")
public class PersistentImageConsultation implements Cloneable {

        // TODO:
//    version false
//    stateless true //don't store data in memory after read&co. These data don't need to be update.

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    protected Date created;

    Long user;

    @Indexed
    Long image;

    Long project;

    Long projectConnection;

    String session;

    String imageName;

    String imageThumb;

    String mode;

    Long time;

    Integer countCreatedAnnotations;

    @Transient // we need both
    @org.springframework.data.annotation.Transient
    Map<String, Object> extraProperties = new LinkedHashMap<>();

    public static JsonObject getDataFromDomain(PersistentImageConsultation domain) {
        JsonObject returnArray = new JsonObject();
        PersistentImageConsultation connection = (PersistentImageConsultation)domain;
        returnArray.put("class", domain.getClass());
        returnArray.put("id", domain.getId());
        returnArray.put("created", DateUtils.getTimeToString(domain.created));
        returnArray.put("user", connection.getUser());
        returnArray.put("image", connection.getImage());
        returnArray.put("imageName", connection.getImageName());
        returnArray.put("imageThumb", connection.getImageThumb());
        returnArray.put("mode", connection.getMode());
        returnArray.put("project", connection.getProject());
        returnArray.put("projectConnection", connection.getProjectConnection());
        returnArray.put("time", connection.getTime());
        returnArray.put("countCreatedAnnotations", connection.getCountCreatedAnnotations());
        returnArray.putAll(connection.getExtraProperties());
        return returnArray;
    }

    @Override
    public Object clone() {
        PersistentImageConsultation result = new PersistentImageConsultation();
        result.user = user;
        result.project = project;
        result.projectConnection = projectConnection;
        result.time = time;
        result.image = image;
        result.imageName = imageName;
        result.imageThumb = imageThumb;
        result.mode = mode;
        result.countCreatedAnnotations = countCreatedAnnotations;
        result.id = id;
        result.created = created;
        return result;
    }

    public void propertyMissing(String name, Object value) {
        extraProperties.put(name, value);
    }


    public Long computeDateInMillis() {
        return created != null ? created.getTime() - new Date(0).getTime() : null;
    }

    @Override
    public String toString() {
        return "PersistentImageConsultation{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", createdTime=" + (created!=null? created.getTime() : null) +
                ", user=" + user +
                ", project=" + project +
                ", image=" + image +
                ", time=" + time +
                '}';
    }
}
