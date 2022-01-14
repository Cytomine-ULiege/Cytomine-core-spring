package be.cytomine.repository.ontology;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.AnnotationIndex;
import be.cytomine.domain.security.SecUser;
import be.cytomine.service.dto.AnnotationIndexLightDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

public interface AnnotationDomainRepository extends JpaRepository<AnnotationDomain, Long>, JpaSpecificationExecutor<AnnotationDomain>  {


    @Query(value = "SELECT annotation.id as annotation,user_id as user\n" +
            "FROM user_annotation annotation\n" +
            "WHERE annotation.image_id = :image\n" +
            "AND user_id IN (:layers)\n" +
            "AND ST_Intersects(annotation.location,ST_GeometryFromText(:location,0))", nativeQuery = true)
    List<Tuple> findAllIntersectForUserAnnotations(Long image, List<Long> layers, String location);

    @Query(value = "SELECT annotation.id as annotation,user_id as user\n" +
            "FROM reviewed_annotation annotation\n" +
            "WHERE annotation.image_id = :image\n" +
            "AND ST_Intersects(annotation.location,ST_GeometryFromText(:location,0))", nativeQuery = true)
    List<Tuple> findAllIntersectForReviewedAnnotations(Long image, String location);

}