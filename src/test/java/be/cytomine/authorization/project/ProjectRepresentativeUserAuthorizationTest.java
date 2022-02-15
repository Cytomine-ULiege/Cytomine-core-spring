package be.cytomine.authorization.project;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.User;
import be.cytomine.service.PermissionService;
import be.cytomine.service.ontology.AnnotationTermService;
import be.cytomine.service.project.ProjectRepresentativeUserService;
import be.cytomine.service.security.SecurityACLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ProjectRepresentativeUserAuthorizationTest extends CRDAuthorizationTest {


    private ProjectRepresentativeUser projectRepresentativeUser = null;

    @Autowired
    ProjectRepresentativeUserService projectRepresentativeUserService;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    SecurityACLService securityACLService;

    @Autowired
    PermissionService permissionService;

    @BeforeEach
    public void before() throws Exception {
        if (projectRepresentativeUser == null) {
            projectRepresentativeUser = builder.given_an_project_representative_user();
            initUser();
            initACL(projectRepresentativeUser.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_project_representative_user() {
        expectOK (() -> { projectRepresentativeUserService
                .listByProject(projectRepresentativeUser.getProject()); });
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_can_list_project_representative_user(){
        expectOK (() -> { projectRepresentativeUserService
                .listByProject(projectRepresentativeUser.getProject()); });
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_project_representative_user(){
        expectForbidden(() -> {
            projectRepresentativeUserService
                    .listByProject(projectRepresentativeUser.getProject());
        });
    }

    @Override
    public void when_i_get_domain() {
        projectRepresentativeUserService.get(projectRepresentativeUser.getId());
        projectRepresentativeUserService.find(
                projectRepresentativeUser.getProject(), projectRepresentativeUser.getUser());
    }

    @Override
    protected void when_i_add_domain() {
        User user = builder.given_a_user();
        builder.addUserToProject(projectRepresentativeUser.getProject(), user.getUsername());
        projectRepresentativeUserService.add(
                builder.given_a_not_persisted_project_representative_user(
                        projectRepresentativeUser.getProject(), user
                ).toJsonObject()
        );
    }

    @Override
    protected void when_i_delete_domain() {
        User user = projectRepresentativeUser.getUser();
        builder.addUserToProject(projectRepresentativeUser.getProject(), user.getUsername());
        ProjectRepresentativeUser projectRepresentativeUserToDelete = builder.given_a_not_persisted_project_representative_user(projectRepresentativeUser.getProject(),
                user);
        builder.persistAndReturn(projectRepresentativeUserToDelete);
        projectRepresentativeUserService.delete(projectRepresentativeUserToDelete, null, null, true);
    }
    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.WRITE);
    }


    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_USER");
    }
}