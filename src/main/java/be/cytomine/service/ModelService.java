package be.cytomine.service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.ValidationError;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.exceptions.*;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import static org.springframework.security.acls.domain.BasePermission.READ;

@Transactional
@Slf4j
public abstract class ModelService<T extends CytomineDomain> {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ResponseService responseService;

    @Autowired
    SecurityACLService securityACLService;

//    @Autowired
//    ResponseService responseService;

    @Autowired
    ApplicationContext applicationContext;

//    @Autowired
//    TaskService taskService;
//
    @Autowired
    CommandService commandService;
//    def responseService

//    def cytomineService
//    def grailsApplication
//    def taskService
//    def attachedFileService
//    def descriptionService
//    def propertyService
//    def tagDomainAssociationService
    //def securityACLService
    boolean saveOnUndoRedoStack = true;

    /**
     * Save a domain on database, throw error if cannot save
     */
    public void saveDomain(CytomineDomain newObject) {
        checkDoNotAlreadyExist(newObject);

        List<ValidationError> validationErrors = newObject.validate();
        if (!validationErrors.isEmpty()) {
            for (ValidationError validationError : validationErrors) {
                log.error(validationError.getProperty() + " " + validationError.getMessage());
            }
            throw new WrongArgumentException(validationErrors.toString());
        }

        try {
            if (newObject.getId()!=null && !entityManager.contains(newObject)) {
                // entity is detached, merge it in the session
                newObject = entityManager.merge(newObject);
            }
            entityManager.persist(newObject);
            entityManager.flush();
        }
        catch (OptimisticLockingFailureException e) {
            log.error("CANNOT SAVE OBJECT");
            newObject = entityManager.merge(newObject);
        }
        catch(Exception e) {
            throw new WrongArgumentException("Cannot persists object:" + e);
        }
    }

    /**
     * Delete a domain from database
     */
    public void removeDomain(CytomineDomain oldObject) {
        try {
            entityManager.refresh(oldObject);
            entityManager.remove(oldObject);
            entityManager.flush();
        } catch (Exception e) {
            log.error(e.toString());
            throw new InvalidRequestException(e.toString());
        }
    }

    /**
     * Add command info for the new domain concerned by the command
     */
    CytomineDomain fillDomainWithData(CytomineDomain object, JsonObject json) {
        CytomineDomain domain = entityManager.find(object.getClass(), retrieveLongId(json));
        domain.buildDomainFromJson(json, this.entityManager);
        domain.setId(json.getJSONAttrLong("id"));
        return domain;
    }

    /**
     * Get the name of the service (project,...)
     */
    public String getServiceName() {
        return this.getClass().getSimpleName();
    }

    public CommandResponse executeCommand(Command c, CytomineDomain domain, JsonObject json) {
        return executeCommand(c, domain, json, null);
    }

    public CommandResponse executeCommand(Command c, CytomineDomain domain, JsonObject json, Task task) {
        //bug, cannot do new XXXCommand(domain:domain, json:...) => new XXXCommand(); c.domain = domain; c.json = ...
        c.setDomain(domain);
        c.setJson(json);
        return executeCommand(c,task);
    }


    public CommandResponse executeCommand(Command c) {
        return executeCommand(c, null);
    }
    /**
     * Execute command with JSON data
     */
    public CommandResponse executeCommand(Command c, Task task) {
        log.info("Command " + c.getClass() + " " + c.getDomain());
        if(c instanceof DeleteCommand) {
            CytomineDomain domainToDelete = c.getDomain();
            //Create a backup (for 'undo' op)
            //We create before for deleteCommand to keep data from HasMany inside json (data will be deleted later)
            Object backup = domainToDelete.toJSON();
            ((DeleteCommand) c).setBackup(backup);
            this.deleteDependencies(domainToDelete, c.getTransaction(), task);
            //remove all dependent domains

            // TODO: delete dependency mechanism
//            def allServiceMethods = this.getClass().getMethods()
//            def dependencyMethods = allServiceMethods.findAll{it.name.startsWith("deleteDependent")}.unique({it.name})
//
//            def (ordered, unordered) = dependencyMethods.split { it.annotations.findAll{it instanceof DependencyOrder}.size() > 0  }
//            ordered = ordered.sort{- it.annotations.find{it instanceof DependencyOrder}.order()}
//            dependencyMethods = ordered + unordered
//
//            int numberOfDirectDependence = dependencyMethods.size()
//
//            dependencyMethods*.name.eachWithIndex { method, index ->
//                    taskService.updateTask(task, (int)((double)index/(double)numberOfDirectDependence)*100, "")
//                this."$method"(domainToDelete,c.transaction,task)
//            }
//            task

        }
        initCommandService();
        c.setSaveOnUndoRedoStack(this.isSaveOnUndoRedoStack()); //need to use getter method, to get child value
        c.setServiceName(getServiceName());
        return commandService.processCommand(c, this);
    }

    private void initCommandService() {
        if (commandService == null) {
            commandService = (CommandService)applicationContext.getBean("commandService");
        }

    }

    public Boolean isSaveOnUndoRedoStack() {
        return true;
    }

    public CommandResponse add(JsonObject jsonObject) {
        throw new CytomineMethodNotYetImplementedException("No add method implemented");
    }

    public abstract Class currentDomain();

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    public abstract CytomineDomain createFromJSON(JsonObject json);

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    CommandResponse create(JsonObject json, boolean printMessage) {
        return create(createFromJSON(json), printMessage);
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    public CommandResponse create(CytomineDomain domain, boolean printMessage) {
        //Save new object
        beforeAdd(domain);
        saveDomain(domain);

        CommandResponse response = responseService.createResponseMessage(domain, getStringParamsI18n(domain), printMessage, "Add", domain.getCallBack());
        afterAdd(domain,response);
        //Build response message
        return response;
    }


    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    public CommandResponse edit(JsonObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        try {
            return edit(fillDomainWithData(((CytomineDomain)currentDomain().getDeclaredConstructor().newInstance()), json), printMessage);
        } catch (Exception e) {
            throw new ObjectNotFoundException("Cannot create instance of object: " + json + " Exception " + e);
        }
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    public CommandResponse edit(CytomineDomain domain, boolean printMessage) {
        //Build response message
        beforeUpdate(domain);
        saveDomain(domain);
        CommandResponse response = responseService.createResponseMessage(domain, getStringParamsI18n(domain), printMessage, "Edit", domain.getCallBack());
        afterUpdate(domain, response);
        return response;
    }


    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    public CommandResponse destroy(JsonObject json, boolean printMessage) {
        //Get object to delete
        return destroy((CytomineDomain)entityManager.find(currentDomain(), retrieveLongId(json)), printMessage);
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    public CommandResponse destroy(CytomineDomain domain, boolean printMessage) {
        //Build response message
        CommandResponse response = responseService.createResponseMessage(domain, getStringParamsI18n(domain), printMessage, "Delete", domain.getCallBack());
        beforeDelete(domain);
        //Delete object
        removeDomain(domain);
        afterDelete(domain,response);
        return response;
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @return domain retrieve thanks to json
     */
    public CytomineDomain retrieve(JsonObject json) {

        CytomineDomain domain = null;
        if(json.containsKey("id") && !json.get("id").toString().equals("null")) {
            domain = (CytomineDomain) entityManager.find(currentDomain(), retrieveLongId(json)); // cast to long = issue ? TODO
        }

        if (domain == null) {
            throw new ObjectNotFoundException(currentDomain() + " " + json.get("id") + " not found");
        }
        CytomineDomain container = domain.container();
        if (container!=null) {
            //we only check security if container is defined
            securityACLService.check(container,READ);
        }
        return domain;
    }

    protected Long retrieveLongId(JsonObject json) {
        Long id;
        if (json.get("id") instanceof String) {
            id = Long.parseLong((String) json.get("id"));
        } else if (json.get("id") instanceof Integer) {
            id = ((Integer) json.get("id")).longValue();
        }  else {
            id = (Long) json.get("id");
        }
        return id;
    }

    protected void beforeAdd(CytomineDomain domain) {
    }

    protected void beforeDelete(CytomineDomain domain) {

    }

    protected void beforeUpdate(CytomineDomain domain) {

    }

    protected void afterAdd(CytomineDomain domain, CommandResponse response) {
    }

    protected void afterDelete(CytomineDomain domain, CommandResponse response) {

    }

    protected void afterUpdate(CytomineDomain domain, CommandResponse response) {

    }

    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        throw new ServerException("getStringParamsI18n must be implemented for " + this.getClass().toString());
    }

    /**
     * Build callback data for a domain (by default null)
     * Callback are metadata used by client
     * You need to override getCallBack() in domain class
     * @return Callback data
     */
    public Map<String, Object> getCallBack() {
        return null;
    }

    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData) {
        return update(domain, jsonNewData, null);
    }


    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        throw new CytomineMethodNotYetImplementedException("No update method implemented");
    }

    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        throw new CytomineMethodNotYetImplementedException("No delete method implemented");
    }

    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        // do nothing by default
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void deleteDependencies(CytomineDomain domain, Transaction transaction, Task task) {
        return;
    }

    public void updateTask(Task task, int index, int numberOfDirectDependence) {
        //taskService.updateTask(task, (int)((double)index/(double)numberOfDirectDependence)*100, "")
        // TODO
    }

    public CytomineDomain getCytomineDomain(String domainClassName, Long domainIdent) {
        try {
            return (CytomineDomain)getEntityManager()
                    .find(Class.forName(domainClassName), domainIdent);
        } catch (ClassNotFoundException e) {
            throw new ObjectNotFoundException(domainClassName, domainIdent);
        }
    }

}
