/*******************************************************************************
 * Copyright (c) 2017, 2021 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.job.steps;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.job.engine.JobEngineFactory;
import org.eclipse.kapua.job.engine.JobEngineService;
import org.eclipse.kapua.job.engine.JobStartOptions;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.model.query.predicate.AttributePredicate.Operator;
import org.eclipse.kapua.qa.common.StepData;
import org.eclipse.kapua.qa.common.TestBase;
import org.eclipse.kapua.qa.common.cucumber.CucConfig;
import org.eclipse.kapua.qa.common.cucumber.CucJobStepProperty;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobAttributes;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobListResult;
import org.eclipse.kapua.service.job.JobQuery;
import org.eclipse.kapua.service.job.JobService;
import org.eclipse.kapua.service.job.execution.JobExecution;
import org.eclipse.kapua.service.job.execution.JobExecutionAttributes;
import org.eclipse.kapua.service.job.execution.JobExecutionCreator;
import org.eclipse.kapua.service.job.execution.JobExecutionFactory;
import org.eclipse.kapua.service.job.execution.JobExecutionListResult;
import org.eclipse.kapua.service.job.execution.JobExecutionQuery;
import org.eclipse.kapua.service.job.execution.JobExecutionService;
import org.eclipse.kapua.service.job.step.JobStep;
import org.eclipse.kapua.service.job.step.JobStepAttributes;
import org.eclipse.kapua.service.job.step.JobStepCreator;
import org.eclipse.kapua.service.job.step.JobStepFactory;
import org.eclipse.kapua.service.job.step.JobStepListResult;
import org.eclipse.kapua.service.job.step.JobStepQuery;
import org.eclipse.kapua.service.job.step.JobStepService;
import org.eclipse.kapua.service.job.step.definition.JobStepDefinition;
import org.eclipse.kapua.service.job.step.definition.JobStepDefinitionCreator;
import org.eclipse.kapua.service.job.step.definition.JobStepDefinitionFactory;
import org.eclipse.kapua.service.job.step.definition.JobStepDefinitionService;
import org.eclipse.kapua.service.job.step.definition.JobStepProperty;
import org.eclipse.kapua.service.job.step.definition.JobStepType;
import org.eclipse.kapua.service.job.targets.JobTarget;
import org.eclipse.kapua.service.job.targets.JobTargetAttributes;
import org.eclipse.kapua.service.job.targets.JobTargetCreator;
import org.eclipse.kapua.service.job.targets.JobTargetFactory;
import org.eclipse.kapua.service.job.targets.JobTargetListResult;
import org.eclipse.kapua.service.job.targets.JobTargetQuery;
import org.eclipse.kapua.service.job.targets.JobTargetService;
import org.eclipse.kapua.service.job.targets.JobTargetStatus;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ****************************************************************************************
// * Implementation of Gherkin steps used in JobService.feature scenarios.                *
// *                                                                                      *
// * MockedLocator is used for Location Service. Mockito is used to mock other            *
// * services that the Account services dependent on. Dependent services are:             *
// * - Authorization Service                                                              *
// ****************************************************************************************

@Singleton
public class JobServiceSteps extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceSteps.class);

    private static final String CURRENT_JOB_ID = "CurrentJobId";
    private static final String CURRENT_JOB_STEP_DEFINITION_ID = "CurrentJobStepDefinitionId";
    private static final String CURRENT_STEP_ID = "CurrentStepId";
    private static final String JOB_CREATOR = "JobCreator";
    private static final String JOB_EXECUTION = "JobExecution";
    private static final String JOB_EXECUTION_LIST = "JobExecutionList";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_STEP = "JobStep";
    private static final String JOB_STEP_CREATOR = "JobStepCreator";
    private static final String JOB_STEP_DEFINITION = "JobStepDefinition";
    private static final String JOB_STEP_DEFINITIONS = "JobSetpDefinitions";
    private static final String JOB_STEP_DEFINITION_CREATOR = "JobStepDefinitionCreator";
    private static final String JOB_TARGET = "JobTarget";
    private static final String JOB_TARGET_CREATOR = "JobTargetCreator";
    private static final String JOB_TARGET_LIST = "JobTargetList";
    private static final String TEST_JOB = "Test job";

    // Job service objects
    private JobFactory jobFactory;
    private JobService jobService;

    // Job Step definition service objects
    private JobStepDefinitionService jobStepDefinitionService;
    private JobStepDefinitionFactory jobStepDefinitionFactory;

    // Job Step service objects
    private JobStepService jobStepService;
    private JobStepFactory jobStepFactory;

    // Job Target service objects
    private JobTargetService jobTargetService;
    private JobTargetFactory jobTargetFactory;

    // Job Execution service objects
    private JobExecutionService jobExecutionService;
    private JobExecutionFactory jobExecutionFactory;

    //Job Engine Service objects
    private JobEngineService jobEngineService;
    private JobEngineFactory jobEngineFactory;

    // Default constructor
    @Inject
    public JobServiceSteps(StepData stepData) {
        super(stepData);
    }

    @After(value="@setup")
    public void setServices() {
        KapuaLocator locator = KapuaLocator.getInstance();
        jobService = locator.getService(JobService.class);
        jobFactory = locator.getFactory(JobFactory.class);
        jobStepDefinitionService = locator.getService(JobStepDefinitionService.class);
        jobStepDefinitionFactory = locator.getFactory(JobStepDefinitionFactory.class);
        jobStepService = locator.getService(JobStepService.class);
        jobStepFactory = locator.getFactory(JobStepFactory.class);
        jobTargetService = locator.getService(JobTargetService.class);
        jobTargetFactory = locator.getFactory(JobTargetFactory.class);
        jobExecutionService = locator.getService(JobExecutionService.class);
        jobExecutionFactory = locator.getFactory(JobExecutionFactory.class);
        jobEngineService = locator.getService(JobEngineService.class);
        jobEngineFactory = locator.getFactory(JobEngineFactory.class);
    }

    // ************************************************************************************
    // ************************************************************************************
    // * Definition of Cucumber scenario steps                                            *
    // ************************************************************************************
    // ************************************************************************************

    // ************************************************************************************
    // * Setup and tear-down steps                                                        *
    // ************************************************************************************

    @Before(value="@env_docker", order=10)
    public void beforeScenarioDockerFull(Scenario scenario) {
        updateScenario(scenario);
    }

    @Before(value="@env_docker_base", order=10)
    public void beforeScenarioEmbeddedMinimal(Scenario scenario) {
        updateScenario(scenario);
    }

    @Before(value="@env_none", order=10)
    public void beforeScenarioNone(Scenario scenario) {
        updateScenario(scenario);
    }

    // ************************************************************************************
    // ************************************************************************************
    // * Cucumber Test steps                                                              *
    // ************************************************************************************
    // ************************************************************************************

    // ************************************************************************************
    // * Job service steps                                                              *
    // ************************************************************************************
    @When("I configure the job service")
    public void setJobServiceConfigurationValue(List<CucConfig> cucConfigs) throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        KapuaId accId = getCurrentScopeId();
        KapuaId scopeId = getCurrentParentId();

        for (CucConfig config : cucConfigs) {
            config.addConfigToMap(valueMap);
            if (config.getParentId() != null) {
                scopeId = getKapuaId(config.getParentId());
            }
            if (config.getScopeId() != null) {
                accId = getKapuaId(config.getScopeId());
            }
        }

        primeException();
        try {
            jobService.setConfigValues(accId, scopeId, valueMap);
        } catch (KapuaException ke) {
            verifyException(ke);
        }
    }

    @Given("A regular job creator with the name {string}")
    public void prepareARegularJobCreator(String name) {
        JobCreator jobCreator = jobFactory.newCreator(getCurrentScopeId());
        jobCreator.setName(name);
        jobCreator.setDescription(TEST_JOB);
        stepData.put(JOB_CREATOR, jobCreator);
    }

    @Given("A job creator with a null name")
    public void prepareAJobCreatorWithNullName() {
        JobCreator jobCreator = jobFactory.newCreator(getCurrentScopeId());
        jobCreator.setName(null);
        jobCreator.setDescription(TEST_JOB);
        stepData.put(JOB_CREATOR, jobCreator);
    }

    @Given("A job creator with an empty name")
    public void prepareAJobCreatorWithAnEmptyName() {
        JobCreator jobCreator = jobFactory.newCreator(getCurrentScopeId());
        jobCreator.setName("");
        jobCreator.setDescription(TEST_JOB);
        stepData.put(JOB_CREATOR, jobCreator);
    }

    @Given("I create a job with the name {string}")
    public void createANamedJob(String name) throws Exception {
        prepareARegularJobCreator(name);
        JobCreator jobCreator = (JobCreator) stepData.get(JOB_CREATOR);
        primeException();
        try {
            stepData.remove("Job");
            stepData.remove(CURRENT_JOB_ID);
            Job job = jobService.create(jobCreator);
            stepData.put("Job", job);
            stepData.put(CURRENT_JOB_ID, job.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("I create {int} job items")
    public void createANumberOfJobs(int num) throws Exception {
        primeException();
        try {
            for (int i = 0; i < num; i++) {
                JobCreator tmpCreator = jobFactory.newCreator(getCurrentScopeId());
                tmpCreator.setName(String.format("TestJobNum%d", i));
                tmpCreator.setDescription("TestJobDescription");
                jobService.create(tmpCreator);
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("I create {int} job items with the name {string}")
    public void createANumberOfJobsWithName(int num, String name) throws Exception {
        JobCreator tmpCreator = jobFactory.newCreator(getCurrentScopeId());
        tmpCreator.setDescription("TestJobDescription");
        primeException();
        try {
            for (int i = 0; i < num; i++) {
                tmpCreator.setName(name + "_" + i);
                jobService.create(tmpCreator);
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I create a new job entity from the existing creator")
    public void createJobFromCreator() throws Exception {
        JobCreator jobCreator = (JobCreator) stepData.get(JOB_CREATOR);
        primeException();
        try {
            stepData.remove("Job");
            stepData.remove(CURRENT_JOB_ID);
            Job job = jobService.create(jobCreator);
            stepData.put("Job", job);
            stepData.put(CURRENT_JOB_ID, job.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the job name to {string}")
    public void updateExistingJobName(String newName) throws Exception {
        Job oldJob = (Job) stepData.get("Job");
        oldJob.setName(newName);
        primeException();
        try {
            stepData.remove("Job");
            Job newJob = jobService.update(oldJob);
            stepData.put("Job", newJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the job description to {string}")
    public void updateExistingJobDescription(String newDescription) throws Exception {
        Job oldJob = (Job) stepData.get("Job");
        oldJob.setDescription(newDescription);
        primeException();
        try {
            stepData.remove("Job");
            Job newJob = jobService.update(oldJob);
            stepData.put("Job", newJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the job XML definition to {string}")
    public void updateExistingJobXMLDefinition(String newDefinition) throws Exception {
        Job oldJob = (Job) stepData.get("Job");
        oldJob.setJobXmlDefinition(newDefinition);
        primeException();
        try {
            stepData.remove("Job");
            Job newJob = jobService.update(oldJob);
            stepData.put("Job", newJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I add the current step to the last job")
    public void updateJobWithSteps() throws Exception {
        Job oldJob = (Job) stepData.get("Job");
        List<JobStep> tmpStepList = oldJob.getJobSteps();
        JobStep step = (JobStep) stepData.get("Step");
        tmpStepList.add(step);
        oldJob.setJobSteps(tmpStepList);
        primeException();
        try {
            stepData.remove("Job");
            Job newJob = jobService.update(oldJob);
            stepData.put("Job", newJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I delete the job")
    public void deleteJobFromDatabase() throws Exception {
        Job job = (Job) stepData.get("Job");
        primeException();
        try {
            jobService.delete(job.getScopeId(), job.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search for the job in the database")
    public void findJobInDatabase() throws Exception {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        primeException();
        try {
            stepData.remove("Job");
            Job job = jobService.find(getCurrentScopeId(), currentJobId);
            stepData.put("Job", job);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the jobs in the database")
    public void countJobsInDatabase() throws Exception {
        updateCount(() -> (int)jobService.count(jobFactory.newQuery(getCurrentScopeId())));
    }

    @When("I query for jobs in scope {int}")
    public void countJobsInScope(int id) throws Exception {
        updateCount(() -> jobService.query(jobFactory.newQuery(getKapuaId(id))).getSize());
    }

    @When("I count the jobs with the name starting with {string}")
    public void countJobsWithName(String name) throws Exception {
        JobQuery tmpQuery = jobFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobAttributes.NAME, name, Operator.STARTS_WITH));
        updateCount(() -> (int)jobService.query(tmpQuery).getSize());
    }

    @When("I query for the job with the name {string}")
    public void queryForJobWithName(String name) throws Exception {
        JobQuery tmpQuery = jobFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobAttributes.NAME, name));
        primeException();
        try {
            stepData.remove("Job");
            Job job = jobService.query(tmpQuery).getFirstItem();
            stepData.put("Job", job);
            Assert.assertEquals(name, job.getName());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("The job entity matches the creator")
    public void checkJobAgainstCreator() {
        Job job = (Job) stepData.get("Job");
        JobCreator jobCreator = (JobCreator) stepData.get(JOB_CREATOR);
        Assert.assertEquals("The job scope does not match the creator.", jobCreator.getScopeId(), job.getScopeId());
        Assert. assertEquals("The job name does not match the creator.", jobCreator.getName(), job.getName());
        Assert.assertEquals("The job description does not match the creator.", jobCreator.getDescription(), job.getDescription());
    }

    @Then("The job has int step(s)")
    public void checkNumberOfJobSteps(int num) {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals("The job item has the wrong number of steps", num, job.getJobSteps().size());
    }

    @Then("I find a job item in the database")
    public void checkThatAJobWasFound() {
        Assert.assertNotNull("Unexpected null value for the job.", stepData.get("Job"));
    }

    @Then("There is no such job item in the database")
    public void checkThatNoJobWasFound() {
        Assert.assertNull("Unexpected job item was found!", stepData.get("Job"));
    }

    @Then("The job name is {string}")
    public void checkJobItemName(String name) {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals("The job name does not match!", name, job.getName());
    }

    @Then("The job description is {string}")
    public void checkJobItemDescription(String description) {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals("The job description does not match!", description, job.getDescription());
    }

    @Then("The job XML definition is {string}")
    public void checkJobItemXMLDefinition(String definition) {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals("The job XML definition does not match!", definition, job.getJobXmlDefinition());
    }

    @When("I test the sanity of the job factory")
    public void testJobFactorySanity() {
        primeException();
        Assert.assertNotNull("The job factory returned a null creator!", jobFactory.newCreator(SYS_SCOPE_ID));
        Assert.assertNotNull("The job factory returned a null job object!", jobFactory.newEntity(SYS_SCOPE_ID));
        Assert.assertNotNull("The job factory returned a null job query!", jobFactory.newQuery(SYS_SCOPE_ID));
        Assert.assertNotNull("The job factory returned a null job list result!", jobFactory.newListResult());
    }

    // ************************************************************************************
    // * Job Step Definition Test steps                                                              *
    // ************************************************************************************
    @Given("A regular step definition creator with the name {string}")
    public void prepareARegularStepDefinitionCreator(String name) {
        JobStepDefinitionCreator stepDefinitionCreator = prepareDefaultJobStepDefinitionCreator();
        stepDefinitionCreator.setName(name);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("A regular definition creator with the name {string} and {int} properties")
    public void prepareARegularStepDefinitionCreatorWithProperties(String name, Integer cnt) {
        JobStepDefinitionCreator stepDefinitionCreator = prepareDefaultJobStepDefinitionCreator();
        stepDefinitionCreator.setName(name);
        List<JobStepProperty> tmpPropLst = new ArrayList<>();
        tmpPropLst.add(jobStepDefinitionFactory.newStepProperty("Property1", "Type1", null));
        tmpPropLst.add(jobStepDefinitionFactory.newStepProperty("Property2", "Type2", null));
        tmpPropLst.add(jobStepDefinitionFactory.newStepProperty("Property3", "Type3", null));
        stepDefinitionCreator.setStepProperties(tmpPropLst);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("A regular step definition creator with the name {string} and the following properties")
    public void prepareARegularStepDefinitionCreatorWithPropertyList(String name, List<CucJobStepProperty> list) {
        JobStepDefinitionCreator stepDefinitionCreator = prepareDefaultJobStepDefinitionCreator();
        stepDefinitionCreator.setName(name);
        List<JobStepProperty> tmpPropLst = new ArrayList<>();
        for (CucJobStepProperty prop : list) {
            tmpPropLst.add(jobStepDefinitionFactory.newStepProperty(prop.getName(), prop.getType(), null));
        }
        stepDefinitionCreator.setStepProperties(tmpPropLst);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("A regular step definition with the name {string} and the following properties")
    public void createARegularStepDefinitionWithProperties(String name, List<CucJobStepProperty> list) throws Exception {
        prepareARegularStepDefinitionCreatorWithPropertyList(name, list);
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        primeException();
        try {
            stepData.remove(JOB_STEP_DEFINITION);
            stepData.remove(CURRENT_JOB_STEP_DEFINITION_ID);
            JobStepDefinition stepDefinition = jobStepDefinitionService.create(stepDefinitionCreator);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
            stepData.put(CURRENT_JOB_STEP_DEFINITION_ID, stepDefinition.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("Search for step definition with the name {string}")
    public void searchARegularStepDefinitionWithProperties(String name) throws Exception {
        primeException();
        try {
            stepData.remove(JOB_STEP_DEFINITION);
            stepData.remove(CURRENT_JOB_STEP_DEFINITION_ID);
            JobStepDefinition stepDefinition = jobStepDefinitionService.findByName(name);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
            stepData.put(CURRENT_JOB_STEP_DEFINITION_ID, stepDefinition.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("I set the step definition creator name to null")
    public void setDefinitionCreatorNameToNull() {
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        stepDefinitionCreator.setName(null);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("I set the step definition creator processor name to {string}")
    public void setDefinitionCreatorProcessorNameTo(String name) {
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        stepDefinitionCreator.setProcessorName(name);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("I set the step definition creator processor name to null")
    public void setDefinitionCreatorProcessorNameToNull() {
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        stepDefinitionCreator.setProcessorName(null);
        stepData.put(JOB_STEP_DEFINITION_CREATOR, stepDefinitionCreator);
    }

    @Given("I create {int} step definition items")
    public void createANumberOfStepDefinitions(Integer num) throws Exception {
        JobStepDefinitionCreator tmpCreator;
        primeException();
        try {
            for (int i = 0; i < num; i++) {
                tmpCreator = jobStepDefinitionFactory.newCreator(getCurrentScopeId());
                tmpCreator.setName(String.format("TestStepDefinitionNum%d", random.nextLong()));
                tmpCreator.setProcessorName("TestStepProcessor");
                tmpCreator.setStepType(JobStepType.TARGET);
                jobStepDefinitionService.create(tmpCreator);
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I create a new step definition entity from the existing creator")
    public void createAStepDefinitionFromTheCreator() throws Exception {
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        primeException();
        try {
            stepData.remove(JOB_STEP_DEFINITION);
            stepData.remove(CURRENT_JOB_STEP_DEFINITION_ID);
            JobStepDefinition stepDefinition = jobStepDefinitionService.create(stepDefinitionCreator);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
            stepData.put(CURRENT_JOB_STEP_DEFINITION_ID, stepDefinition.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search for the step definition in the database")
    public void findTheExistingStepDefinitionInTheDatabase() throws Exception {
        KapuaId currentStepDefId = (KapuaId) stepData.get(CURRENT_JOB_STEP_DEFINITION_ID);
        primeException();
        try {
            stepData.remove(JOB_STEP_DEFINITION);
            JobStepDefinition stepDefinition = jobStepDefinitionService.find(getCurrentScopeId(), currentStepDefId);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the step definition in the database")
    public void countStepDefinitionInDatabase() throws Exception {
        updateCount(() -> (int)jobStepDefinitionService.count(jobStepDefinitionFactory.newQuery(getCurrentScopeId())));
    }

    @When("I query for step definitions in scope {int}")
    public void countStepDefinitijonsInScope(Integer id) throws Exception {
        updateCount(() -> jobStepDefinitionService.query(jobStepDefinitionFactory.newQuery(getKapuaId(id))).getSize());
    }

    @When("I delete the step definition")
    public void deleteExistingStepDefinition() throws Exception {
        KapuaId currentStepDefId = (KapuaId) stepData.get(CURRENT_JOB_STEP_DEFINITION_ID);
        primeException();
        try {
            jobStepDefinitionService.delete(getCurrentScopeId(), currentStepDefId);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the step definition name to {string}")
    public void changeExistingStepDefinitionName(String name) throws Exception {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        primeException();
        try {
            stepDefinition.setName(name);
            stepDefinition = jobStepDefinitionService.update(stepDefinition);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the step definition processor name to {string}")
    public void changeExistingStepDefinitionProcessor(String name) throws Exception {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        primeException();
        try {
            stepDefinition.setProcessorName(name);
            stepDefinition = jobStepDefinitionService.update(stepDefinition);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I change the step definition type to {string}")
    public void changeExistingStepDefinitionType(String type) throws Exception {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        primeException();
        try {
            stepDefinition.setStepType(getTypeFromString(type));
            stepDefinition = jobStepDefinitionService.update(stepDefinition);
            stepData.put(JOB_STEP_DEFINITION, stepDefinition);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("The step definition entity matches the creator")
    public void checkTheStepDefinitionAgainstTheCreator() {
        JobStepDefinitionCreator stepDefinitionCreator = (JobStepDefinitionCreator) stepData.get(JOB_STEP_DEFINITION_CREATOR);
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        Assert.assertEquals("The step definition has the wrong name!", stepDefinitionCreator.getName(), stepDefinition.getName());
        Assert.assertEquals("The step definition has the wrong description!", stepDefinitionCreator.getDescription(), stepDefinition.getDescription());
        Assert.assertEquals("The step definition has the wrong reader name!", stepDefinitionCreator.getReaderName(), stepDefinition.getReaderName());
        Assert.assertEquals("The step definition has the wrong processor name!", stepDefinitionCreator.getProcessorName(), stepDefinition.getProcessorName());
        Assert.assertEquals("The step definition has the wrong writer name!", stepDefinitionCreator.getWriterName(), stepDefinition.getWriterName());
        Assert.assertEquals("The step definition has a wrong step type!", stepDefinitionCreator.getStepType(), stepDefinition.getStepType());
        Assert.assertNotNull("The step definition has no properties!", stepDefinition.getStepProperties());
        Assert.assertEquals("The step definition has a wrong number of properties!", stepDefinitionCreator.getStepProperties().size(), stepDefinition.getStepProperties().size());
        for (int i = 0; i < stepDefinitionCreator.getStepProperties().size(); i++) {
            Assert.assertEquals(stepDefinitionCreator.getStepProperties().get(i).getName(), stepDefinition.getStepProperties().get(i).getName());
            Assert.assertEquals(stepDefinitionCreator.getStepProperties().get(i).getPropertyType(), stepDefinition.getStepProperties().get(i).getPropertyType());
            Assert.assertEquals(stepDefinitionCreator.getStepProperties().get(i).getPropertyValue(), stepDefinition.getStepProperties().get(i).getPropertyValue());
        }
    }

    @Then("There is no such step definition item in the database")
    public void checkThatNoStepDefinitionWasFound() {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        Assert.assertNull("Unexpected step definition item was found!", stepDefinition);
    }

    @Then("The step definition name is {string}")
    public void checkStepDefinitionName(String name) {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        Assert.assertEquals("The step definition name does not match!", name, stepDefinition.getName());
    }

    @Then("The step definition type is {string}")
    public void checkStepDefinitionType(String type) {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        Assert.assertEquals("The step definition type does not match!", getTypeFromString(type), stepDefinition.getStepType());
    }

    @Then("The step definition processor name is {string}")
    public void checkStepDefinitionProcessorName(String name) {
        JobStepDefinition stepDefinition = (JobStepDefinition) stepData.get(JOB_STEP_DEFINITION);
        Assert.assertEquals("The step definition processor name does not match!", name, stepDefinition.getProcessorName());
    }

    @When("I test the sanity of the step definition factory")
    public void testTheStepDefinitionFactory() {
        Assert.assertNotNull(jobStepDefinitionFactory.newCreator(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepDefinitionFactory.newEntity(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepDefinitionFactory.newListResult());
        Assert.assertNotNull(jobStepDefinitionFactory.newQuery(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepDefinitionFactory.newStepProperty("TestName", "TestType", "TestValue", "TestExampleValue"));
    }


    // ************************************************************************************
    // * Job Step Service Test steps                                                      *
    // ************************************************************************************

    @Given("A regular step creator with the name {string} and the following properties")
    public void prepareARegularStepCreatorWithPropertyList(String name, List<CucJobStepProperty> list) {
        JobStepCreator stepCreator;
        KapuaId currentStepDefId = (KapuaId) stepData.get(CURRENT_JOB_STEP_DEFINITION_ID);
        stepCreator = prepareDefaultJobStepCreator();
        stepCreator.setName(name);
        stepCreator.setJobStepDefinitionId(currentStepDefId);
        List<JobStepProperty> tmpPropLst = new ArrayList<>();
        for (CucJobStepProperty prop : list) {
            tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
        }
        stepCreator.setJobStepProperties(tmpPropLst);
        stepData.put(JOB_STEP_CREATOR, stepCreator);
    }

    @When("I create a new step entity from the existing creator")
    public void createAStepFromTheCreator() throws Exception {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        JobStepCreator stepCreator = (JobStepCreator) stepData.get(JOB_STEP_CREATOR);
        stepCreator.setJobId(currentJobId);
        primeException();
        try {
            stepData.remove(JOB_STEP);
            stepData.remove(CURRENT_STEP_ID);
            JobStep step = jobStepService.create(stepCreator);
            stepData.put(JOB_STEP, step);
            stepData.put(CURRENT_STEP_ID, step.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search the database for created job steps and I find {int}")
    public void searchJobSteps(int count) throws Exception {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        primeException();
        try {
            JobStepQuery tmpQuery = jobStepFactory.newQuery(getCurrentScopeId());
            tmpQuery.setPredicate(tmpQuery.attributePredicate(JobStepAttributes.JOB_ID, currentJobId, AttributePredicate.Operator.EQUAL));
            JobStepListResult jobStepListResult = jobStepService.query(tmpQuery);
            Assert.assertEquals(count, jobStepListResult.getSize());
        } catch (KapuaException ke) {
            verifyException(ke);
        }

    }

    @When("I change the step name to {string}")
    public void updateStepName(String name) throws Exception {
        JobStep step = (JobStep) stepData.get(JOB_STEP);
        step.setName(name);
        primeException();
        try {
            step = jobStepService.update(step);
            stepData.put(JOB_STEP, step);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the step with a new definition")
    public void updateStepDefinition() throws Exception {
        JobStep step = (JobStep) stepData.get(JOB_STEP);
        KapuaId currentStepDefId = (KapuaId) stepData.get(CURRENT_JOB_STEP_DEFINITION_ID);
        step.setJobStepDefinitionId(currentStepDefId);
        primeException();
        try {
            step = jobStepService.update(step);
            stepData.put(JOB_STEP, step);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search for the last step in the database")
    public void findLastStep() throws Exception {
        JobStep step = (JobStep) stepData.get(JOB_STEP);
        primeException();
        try {
            JobStep newStep = jobStepService.find(step.getScopeId(), step.getId());
            stepData.put(JOB_STEP, newStep);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I query for a step with the name {string}")
    public void queryForNamedStep(String name) throws Exception {
        JobStepQuery tmpQuery = jobStepFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobStepAttributes.NAME, name, AttributePredicate.Operator.EQUAL));
        primeException();
        try {
            stepData.remove("JobStepList");
            JobStepListResult stepList = jobStepService.query(tmpQuery);
            stepData.put("JobStepList", stepList);
            stepData.updateCount(stepList.getSize());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the steps in the scope")
    public void countStepsInScope() throws Exception {
        updateCount(() -> (int)jobStepService.count(jobStepFactory.newQuery(getCurrentScopeId())));
    }

    @When("I delete the last step")
    public void deleteLastStep() throws Exception {
        JobStep step = (JobStep) stepData.get(JOB_STEP);
        primeException();
        try {
            jobStepService.delete(step.getScopeId(), step.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("The step item matches the creator")
    public void checkStepItemAgainstCreator() {
        JobStep step = (JobStep) stepData.get(JOB_STEP);
        JobStepCreator stepCreator = (JobStepCreator) stepData.get(JOB_STEP_CREATOR);
        Assert.assertEquals(stepCreator.getJobId(), step.getJobId());
        Assert.assertEquals(stepCreator.getJobStepDefinitionId(), step.getJobStepDefinitionId());
        Assert.assertEquals(stepCreator.getName(), step.getName());
        Assert.assertEquals(stepCreator.getDescription(), step.getDescription());
        Assert.assertEquals(stepCreator.getStepIndex(), (Integer) step.getStepIndex());
        Assert.assertEquals(stepCreator.getStepProperties().size(), step.getStepProperties().size());
    }

    @Then("There is no such step item in the database")
    public void checkThatNoStepWasFound() {
        Assert.assertNull("Unexpected step item found!", stepData.get(JOB_STEP));
    }

    @When("I test the sanity of the step factory")
    public void testTheStepFactory() {
        Assert.assertNotNull(jobStepFactory.newCreator(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepFactory.newEntity(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepFactory.newListResult());
        Assert.assertNotNull(jobStepFactory.newQuery(SYS_SCOPE_ID));
        Assert.assertNotNull(jobStepFactory.newStepProperty("TestName", "TestType", "TestValue"));
    }

    // ************************************************************************************
    // * Job Target Service Test steps                                                    *
    // ************************************************************************************

    @Given("A regular job target item")
    public void createARegularTarget() throws Exception {
        JobTargetCreator targetCreator = prepareDefaultJobTargetCreator();
        stepData.put(JOB_TARGET_CREATOR, targetCreator);
        primeException();
        try {
            stepData.remove(JOB_TARGET);
            JobTarget target = jobTargetService.create(targetCreator);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("A new job target item")
    public void createANewTarget() throws Exception {
        JobTargetCreator targetCreator = prepareJobTargetCreator();
        stepData.put(JOB_TARGET_CREATOR, targetCreator);
        primeException();
        try {
            stepData.remove(JOB_TARGET);
            JobTarget target = jobTargetService.create(targetCreator);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search for the last job target in the database")
    public void findLastJobTarget() throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        primeException();
        try {
            stepData.remove(JOB_TARGET);
            JobTarget targetFound = jobTargetService.find(target.getScopeId(), target.getId());
            stepData.put(JOB_TARGET, targetFound);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I confirm the step index is {int} and status is {string}")
    public void checkStepIndexAndStatus(int stepIndex, String status) throws KapuaException {
        JobTarget jobTarget = (JobTarget) stepData.get(JOB_TARGET);
        JobTarget target = jobTargetService.find(jobTarget.getScopeId(), jobTarget.getId());
        Assert.assertEquals(stepIndex, target.getStepIndex());
        Assert.assertEquals(status, target.getStatus().toString());
    }

    @When("I delete the last job target in the database")
    public void deleteLastJobTarget() throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        primeException();
        try {
            jobTargetService.delete(target.getScopeId(), target.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the job target target id")
    public void updateJobTargetTargetId() throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        JobTargetCreator targetCreator = (JobTargetCreator) stepData.get(JOB_TARGET_CREATOR);
        targetCreator.setJobTargetId(getKapuaId());
        stepData.put(JOB_TARGET_CREATOR, targetCreator);
        target.setJobTargetId(targetCreator.getJobTargetId());
        primeException();
        try {
            target = jobTargetService.update(target);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the job target step number to {int}")
    public void setTargetStepIndex(int i) throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        target.setStepIndex(i);
        primeException();
        try {
            target = jobTargetService.update(target);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the job target step status to {string}")
    public void setTargetStepStatus(String stat) throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        target.setStatus(parseJobTargetStatusFromString(stat));
        primeException();
        try {
            target = jobTargetService.update(target);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the job target step exception message to {string}")
    public void setTargetStepExceptionMessage(String text) throws Exception {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        Exception kex = new Exception(text);
        target.setException(kex);
        primeException();
        try {
            target = jobTargetService.update(target);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the targets in the current scope")
    public void countTargetsForJob() throws Exception {
        updateCount(() -> (int)jobTargetService.count(jobTargetFactory.newQuery(getCurrentScopeId())));
    }

    @When("I query the targets for the current job")
    public void queryTargetsForJob() throws Exception {
        Job job = (Job) stepData.get("Job");
        JobTargetQuery tmpQuery = jobTargetFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobTargetAttributes.JOB_ID, job.getId(), AttributePredicate.Operator.EQUAL));
        primeException();
        try {
            stepData.remove(JOB_TARGET_LIST);
            JobTargetListResult targetList = jobTargetService.query(tmpQuery);
            stepData.put(JOB_TARGET_LIST, targetList);
            stepData.updateCount(targetList.getSize());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("The target step index is indeed {int}")
    public void checkTargetStepIndex(int i) {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        Assert.assertEquals(String.format("The step index should be %d but is in fact %d.", i, target.getStepIndex()), i, target.getStepIndex());
    }

    @Then("The target step exception message is indeed {string}")
    public void checkTargetStepExceptionMessage(String text) {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        Assert.assertEquals(text, target.getException().getMessage());
    }

    @Then("The target step status is indeed {string}")
    public void checkTargetStepStatus(String stat) {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        Assert.assertEquals(parseJobTargetStatusFromString(stat), target.getStatus());
    }

    @Then("The job target matches the creator")
    public void checkJobTargetItemAgainstCreator() {
        JobTarget target = (JobTarget) stepData.get(JOB_TARGET);
        JobTargetCreator targetCreator = (JobTargetCreator) stepData.get(JOB_TARGET_CREATOR);
        Assert.assertEquals(targetCreator.getJobId(), target.getJobId());
        Assert.assertEquals(targetCreator.getJobTargetId(), target.getJobTargetId());
        Assert.assertEquals(targetCreator.getScopeId(), target.getScopeId());
    }

    @Then("There is no such job target item in the database")
    public void checkThatNoTargetWasFound() {
        Assert.assertNull("Unexpected job target item found!", stepData.get(JOB_TARGET));
    }

    @When("I test the sanity of the job target factory")
    public void testTheJobTargetFactory() {
        Assert.assertNotNull(jobTargetFactory.newCreator(SYS_SCOPE_ID));
        Assert.assertNotNull(jobTargetFactory.newEntity(SYS_SCOPE_ID));
        Assert.assertNotNull(jobTargetFactory.newListResult());
        Assert.assertNotNull(jobTargetFactory.newQuery(SYS_SCOPE_ID));
    }
    // ************************************************************************************
    // * Job Execution Service Test steps                                                 *
    // ************************************************************************************

    @Given("A regular job execution item")
    public void createARegularExecution() throws Exception {
        JobExecutionCreator executionCreator = prepareDefaultJobExecutionCreator();
        stepData.put("JobExecutionCreator", executionCreator);
        primeException();
        try {
            stepData.remove(JOB_EXECUTION);
            JobExecution execution = jobExecutionService.create(executionCreator);
            stepData.put(JOB_EXECUTION, execution);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the job id for the execution item")
    public void updateJobIdForExecution() throws Exception {
        Job job = (Job) stepData.get("Job");
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        execution.setJobId(job.getId());
        primeException();
        try {
            execution = jobExecutionService.update(execution);
            stepData.put(JOB_EXECUTION, execution);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I update the end time of the execution item")
    public void updateJobExecutionEndTime() throws Exception {
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        primeException();
        try {
            execution.setEndedOn(DateTime.now().toDate());
            execution = jobExecutionService.update(execution);
            stepData.put(JOB_EXECUTION, execution);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I search for the last job execution in the database")
    public void findLastJobExecution() throws Exception {
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        primeException();
        try {
            stepData.remove("JobExecutionFound");
            JobExecution foundExecution = jobExecutionService.find(execution.getScopeId(), execution.getId());
            stepData.put("JobExecutionFound", foundExecution);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I delete the last job execution in the database")
    public void deleteLastJobExecution() throws Exception {
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        primeException();
        try {
            jobExecutionService.delete(execution.getScopeId(), execution.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the execution items for the current job")
    public void countExecutionsForJob() throws Exception {
        Job job = (Job) stepData.get("Job");
        JobExecutionQuery tmpQuery = jobExecutionFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobExecutionAttributes.JOB_ID, job.getId(), Operator.EQUAL));
        updateCount(() -> (int)jobExecutionService.count(tmpQuery));
    }

    @Then("I query for the execution items for the current job")
    public void queryExecutionsForJobWithPackages() throws Exception {
        Job job = (Job) stepData.get("Job");
        JobExecutionQuery tmpQuery = jobExecutionFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobExecutionAttributes.JOB_ID, job.getId(), Operator.EQUAL));
        primeException();
        try {
            stepData.remove(JOB_EXECUTION_LIST);
            JobExecutionListResult resultList = jobExecutionService.query(tmpQuery);
            stepData.put(JOB_EXECUTION_LIST, resultList);
            stepData.updateCount(resultList.getSize());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("I query for the execution items for the current job and I count {int}")
    public void queryExecutionsForJob(int num) throws Exception {
        Job job = (Job) stepData.get("Job");
        JobExecutionQuery tmpQuery = jobExecutionFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobExecutionAttributes.JOB_ID, job.getId(), Operator.EQUAL));
        primeException();
        try {
            stepData.remove(JOB_EXECUTION_LIST);
            JobExecutionListResult resultList = jobExecutionService.query(tmpQuery);
            stepData.put(JOB_EXECUTION_LIST, resultList);
            stepData.updateCount(resultList.getSize());
            Assert.assertEquals(num, resultList.getSize());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I confirm the executed job is finished")
    public void confirmJobIsFinished() {
        JobExecutionListResult resultList = (JobExecutionListResult) stepData.get(JOB_EXECUTION_LIST);
        JobExecution jobExecution = resultList.getFirstItem();
        Assert.assertNotNull(jobExecution.getEndedOn());
        Assert.assertNotNull(jobExecution.getLog());
    }

    @Then("The job execution matches the creator")
    public void checkJobExecutionItemAgainstCreator() {
        JobExecutionCreator executionCreator = (JobExecutionCreator) stepData.get("JobExecutionCreator");
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        Assert.assertEquals(executionCreator.getScopeId(), execution.getScopeId());
        Assert.assertEquals(executionCreator.getJobId(), execution.getJobId());
        Assert.assertEquals(executionCreator.getStartedOn(), execution.getStartedOn());
    }

    @Then("The job execution items match")
    public void checkJobExecutionItems() {
        JobExecution execution = (JobExecution) stepData.get(JOB_EXECUTION);
        JobExecution foundExecution = (JobExecution) stepData.get("JobExecutionFound");
        Assert.assertEquals(execution.getScopeId(), foundExecution.getScopeId());
        Assert.assertEquals(execution.getJobId(), foundExecution.getJobId());
        Assert.assertEquals(execution.getStartedOn(), foundExecution.getStartedOn());
        Assert.assertEquals(execution.getEndedOn(), foundExecution.getEndedOn());
    }

    @Then("There is no such job execution item in the database")
    public void checkThatNoExecutionWasFound() {
        Assert.assertNull("Unexpected job execution item found!", stepData.get("JobExecutionFound"));
    }

    @When("I test the sanity of the job execution factory")
    public void testTheJobExecutionFactory() {
        Assert.assertNotNull(jobExecutionFactory.newCreator(SYS_SCOPE_ID));
        Assert.assertNotNull(jobExecutionFactory.newEntity(SYS_SCOPE_ID));
        Assert.assertNotNull(jobExecutionFactory.newListResult());
        Assert.assertNotNull(jobExecutionFactory.newQuery(SYS_SCOPE_ID));
    }

    @When("I start a job")
    public void startJob() throws Exception {
        primeException();
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        try {
            JobStartOptions jobStartOptions = jobEngineFactory.newJobStartOptions();
            jobStartOptions.setEnqueue(true);
            jobEngineService.startJob(getCurrentScopeId(), currentJobId, jobStartOptions);
        } catch (KapuaException ke) {
            verifyException(ke);
        }
    }

    @When("I restart a job")
    public void restartJob() throws Exception {
        primeException();
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        try {
            JobStartOptions jobStartOptions = jobEngineFactory.newJobStartOptions();
            jobStartOptions.setResetStepIndex(true);
            jobStartOptions.setFromStepIndex(0);
            jobStartOptions.setEnqueue(true);
            jobEngineService.startJob(getCurrentScopeId(), currentJobId, jobStartOptions);
        } catch (KapuaException ke) {
            verifyException(ke);
        }
    }

    @And("I add target(s) to job")
    public void addTargetsToJob() throws Exception {
        JobTargetCreator jobTargetCreator = jobTargetFactory.newCreator(getCurrentScopeId());
        Job job = (Job) stepData.get("Job");
        ArrayList<Device> devices = (ArrayList<Device>) stepData.get("DeviceList");
        ArrayList<JobTarget> jobTargetList = new ArrayList<>();
        try {
            primeException();
            for (Device dev : devices) {
                jobTargetCreator.setJobTargetId(dev.getId());
                jobTargetCreator.setJobId(job.getId());
                JobTarget jobTarget = jobTargetService.create(jobTargetCreator);
                stepData.put(JOB_TARGET, jobTarget);
                jobTargetList.add(jobTarget);
            }
            stepData.put(JOB_TARGET_LIST, jobTargetList);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("I search for the job targets in database")
    public void iSearchForTheJobTargetsInDatabase() {
        ArrayList<JobTarget> jobTargets = (ArrayList<JobTarget>) stepData.get(JOB_TARGET_LIST);
        stepData.updateCount(jobTargets.size());
    }

    @And("I search for step definition(s) with the name")
    public void searchForStepDefinitionWithTheName(List<String> list) throws Exception {
        ArrayList<JobStepDefinition> jobStepDefinitions = new ArrayList<>();
        primeException();
        try {
            stepData.remove(JOB_STEP_DEFINITIONS);
            stepData.remove(JOB_STEP_DEFINITION);
            stepData.remove(CURRENT_JOB_STEP_DEFINITION_ID);
            for (String name : list) {
                JobStepDefinition stepDefinition = jobStepDefinitionService.findByName(name);
                jobStepDefinitions.add(stepDefinition);
                stepData.put(JOB_STEP_DEFINITIONS, jobStepDefinitions);
                stepData.put(JOB_STEP_DEFINITION, stepDefinition);
                stepData.put(CURRENT_JOB_STEP_DEFINITION_ID, stepDefinition.getId());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("I create a regular step creator with the name {string} and properties")
    public void aRegularStepCreatorWithTheNameAndProperties(String name, List<CucJobStepProperty> tmpProperty) {
        JobStepCreator stepCreator;
        ArrayList<JobStepDefinition> jobStepDefinitions = (ArrayList<JobStepDefinition>) stepData.get(JOB_STEP_DEFINITIONS);
        ArrayList<JobStepCreator> stepCreators = new ArrayList<>();
        ArrayList<String> bundleIds = new ArrayList<>();
        String firstValue = tmpProperty.get(0).getValue();
        String[] values = firstValue.split(",");
        for (String value : values) {
            bundleIds.add(value);
        }
        for (String bundleId : bundleIds) {
            for (JobStepDefinition stepDefinition : jobStepDefinitions) {
                stepCreator = prepareDefaultJobStepCreator();
                stepCreator.setName(name + stepCreators.size());
                stepCreator.setJobStepDefinitionId(stepDefinition.getId());
                List<JobStepProperty> tmpPropLst = new ArrayList<>();
                for (CucJobStepProperty prop : tmpProperty) {
                    if ((stepDefinition.getName().equals("Bundle Start") && prop.getName().equals("bundleId"))) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), bundleId));
                    } else if (stepDefinition.getName().equals("Bundle Stop") && prop.getName().equals("bundleId")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), bundleId));
                    } else if (stepDefinition.getName().equals("Command Execution") && prop.getName().equals("commandInput")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    } else if (stepDefinition.getName().equals("Configuration Put") && prop.getName().equals("configuration")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    } else if (stepDefinition.getName().equals("Asset Write") && prop.getName().equals("assets")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    } else if (stepDefinition.getName().equals("Package Download / Install") && prop.getName().equals("packageDownloadRequest")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    } else if (stepDefinition.getName().equals("Package Uninstall") && prop.getName().equals("packageUninstallRequest")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    } else if (prop.getName().equals("timeout")) {
                        tmpPropLst.add(jobStepFactory.newStepProperty(prop.getName(), prop.getType(), prop.getValue()));
                    }
                }
                stepCreator.setJobStepProperties(tmpPropLst);
                stepCreators.add(stepCreator);
                stepData.put(JOB_STEP_CREATOR, stepCreator);
                stepData.put("JobStepCreators", stepCreators);
            }
        }
    }

    @And("I create a new step entities from the existing creator")
    public void iCreateANewStepEntitiesFromTheExistingCreator() throws Exception {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        ArrayList<JobStepCreator> jobStepCreators = (ArrayList<JobStepCreator>) stepData.get("JobStepCreators");
        ArrayList<JobStep> jobSteps = new ArrayList<>();
        for (JobStepCreator jobStepCreator : jobStepCreators) {
            jobStepCreator.setJobId(currentJobId);
            primeException();
            try {
                stepData.remove(JOB_STEP);
                stepData.put("JobSteps", jobSteps);
                stepData.remove(CURRENT_STEP_ID);
                JobStep step = jobStepService.create(jobStepCreator);
                jobSteps.add(step);
                stepData.put(JOB_STEP, step);
                stepData.put("JobSteps", jobSteps);
                stepData.put(CURRENT_STEP_ID, step.getId());
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @And("I confirm the step index is different than {int} and status is {string}")
    public void iConfirmTheStepIndexIsDifferentThanAndStatusIs(int stepIndex, String status) {
        JobTarget jobTarget = (JobTarget) stepData.get(JOB_TARGET);
        Assert.assertNotEquals(stepIndex, jobTarget.getStepIndex());
        Assert.assertEquals(status, jobTarget.getStatus().toString());
    }

    @And("I stop the job")
    public void iStopTheJob() throws Exception {
        Job job = (Job) stepData.get("Job");
        try {
            primeException();
            jobEngineService.stopJob(getCurrentScopeId(), job.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    // ************************************************************************************
    // * Private helper functions                                                         *
    // ************************************************************************************
    private JobStepType getTypeFromString(String type) {
        if (type.trim().toUpperCase().equals("TARGET")) {
            return JobStepType.TARGET;
        } else {
            return JobStepType.GENERIC;
        }
    }

    private JobStepDefinitionCreator prepareDefaultJobStepDefinitionCreator() {
        JobStepDefinitionCreator tmpCr = jobStepDefinitionFactory.newCreator(getCurrentScopeId());
        tmpCr.setName(String.format("DefinitionName_%d", random.nextInt()));
        tmpCr.setDescription("DefinitionDescription");
        tmpCr.setReaderName(null);
        tmpCr.setProcessorName(TestProcessor.class.getName());
        tmpCr.setWriterName(null);
        tmpCr.setStepType(JobStepType.TARGET);
        return tmpCr;
    }

    private JobStepCreator prepareDefaultJobStepCreator() {
        JobStepCreator tmpCr = jobStepFactory.newCreator(getCurrentScopeId());
        tmpCr.setName(String.format("StepName_%d", random.nextInt()));
        tmpCr.setDescription("StepDescription");
        return tmpCr;
    }

    private JobTargetCreator prepareDefaultJobTargetCreator() {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        JobTargetCreator tmpCr = jobTargetFactory.newCreator(getCurrentScopeId());
        tmpCr.setJobId(currentJobId);
        tmpCr.setJobTargetId(getKapuaId());
        return tmpCr;
    }

    private JobTargetCreator prepareJobTargetCreator() {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        Device device = (Device) stepData.get("LastDevice");
        JobTargetCreator tmpCr = jobTargetFactory.newCreator(getCurrentScopeId());
        tmpCr.setJobId(currentJobId);
        tmpCr.setJobTargetId(device.getId());
        return tmpCr;
    }

    private JobTargetStatus parseJobTargetStatusFromString(String stat) {
        switch (stat.toUpperCase().trim()) {
            case "PROCESS_AWAITING":
                return JobTargetStatus.PROCESS_AWAITING;
            case "PROCESS_OK":
                return JobTargetStatus.PROCESS_OK;
            case "PROCESS_FAILED":
            default:
                return JobTargetStatus.PROCESS_FAILED;
        }
    }

    private JobExecutionCreator prepareDefaultJobExecutionCreator() {
        KapuaId currentJobId = (KapuaId) stepData.get(CURRENT_JOB_ID);
        JobExecutionCreator tmpCr = jobExecutionFactory.newCreator(getCurrentScopeId());
        tmpCr.setJobId(currentJobId);
        tmpCr.setStartedOn(DateTime.now().toDate());
        return tmpCr;
    }

    @Then("I find a job with name {string}")
    public void iFindAJobWithName(String jobName) {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals(job.getName(), jobName);
    }

    @Then("I try to delete the job with name {string}")
    public void iDeleteTheJobWithName(String jobName) throws Exception {
        Job job = (Job) stepData.get("Job");
        try {
            primeException();
            if (job.getName().equals(jobName)) {
                jobService.delete(getCurrentScopeId(), job.getId());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("I try to edit job to name {string}")
    public void iTryToEditJobToName(String jobName) throws Throwable {
        Job job = (Job) stepData.get("Job");
        job.setName(jobName);
        try {
            primeException();
            Job newJob = jobService.update(job);
            stepData.put("Job", newJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("I query for the execution items for the current job and I count {int} or more")
    public void iQueryForTheExecutionItemsForTheCurrentJobAndICountOrMore(int numberOfExecutions) throws Exception {
        Job job = (Job) stepData.get("Job");
        JobExecutionQuery tmpQuery = jobExecutionFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobExecutionAttributes.JOB_ID, job.getId(), Operator.EQUAL));
        primeException();
        try {
            stepData.remove(JOB_EXECUTION_LIST);
            JobExecutionListResult resultList = jobExecutionService.query(tmpQuery);
            stepData.put(JOB_EXECUTION_LIST, resultList);
            stepData.updateCount(resultList.getSize());
            Assert.assertTrue(resultList.getSize() >= numberOfExecutions);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I query for the job with the name {string} and I find it")
    public void iQueryForTheJobWithTheNameAndIFoundIt(String jobName) throws Exception {
        JobQuery tmpQuery = jobFactory.newQuery(getCurrentScopeId());
        tmpQuery.setPredicate(tmpQuery.attributePredicate(JobAttributes.NAME, jobName));
        primeException();
        try {
            stepData.remove("Job");
            Job job = jobService.query(tmpQuery).getFirstItem();
            stepData.put("Job", job);
            Assert.assertEquals(jobName, job.getName());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("I create a new job target item")
    public void iCreateANewJobTargetItem() throws Exception {
        JobTargetCreator targetCreator = prepareJobTargetCreator();
        stepData.put(JOB_TARGET_CREATOR, targetCreator);
        primeException();
        try {
            stepData.remove(JOB_TARGET);
            JobTarget target = jobTargetService.create(targetCreator);
            stepData.put(JOB_TARGET, target);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I count the targets in the current scope and I count {int}")
    public void iCountTheTargetsInTheCurrentScopeAndICount(int targetNum) throws Exception {
        updateCountAndCheck(() -> (int)jobTargetService.count(jobTargetFactory.newQuery(getCurrentScopeId())), targetNum);
    }

    @And("I confirm job target has step index {int} and status {string}")
    public void iConfirmJobTargetHasStatus(int stepIndex, String jobStatus) throws Exception {
        try {
            iConfirmJobTargetHasStatus(100, 2, stepIndex, jobStatus);
        } catch (InterruptedException ex) {
            logger.warn("Waiting interrupted!", ex);
        }
    }

    public void iConfirmJobTargetHasStatus(int secondsToWait, int secondsToTry, int stepIndex, String jobTargetStatus) throws InterruptedException, KapuaException {
        JobTarget jobTarget = (JobTarget) stepData.get(JOB_TARGET);
        long endWaitTime = System.currentTimeMillis() + secondsToWait * 1000;
        JobTarget targetFound = null;
        do {
            targetFound = jobTargetService.find(jobTarget.getScopeId(), jobTarget.getId());
            if (targetFound.getStepIndex() == stepIndex && jobTargetStatus.equals(targetFound.getStatus().name())) {
                return;
            }
            Thread.sleep(secondsToTry * 1000);
        }
        while (System.currentTimeMillis() < endWaitTime);
        //lets the test fail for the right reason
        targetFound = jobTargetService.find(jobTarget.getScopeId(), jobTarget.getId());
        Assert.assertEquals(jobTargetStatus, targetFound.getStatus().toString());
        Assert.assertEquals(stepIndex, targetFound.getStepIndex());
    }

    @Given("I prepare a job with name {string} and description {string}")
    public void iPrepareAJobWithNameAndDescription(String name, String description) {
        JobCreator jobCreator = jobFactory.newCreator(SYS_SCOPE_ID);
        jobCreator.setName(name);
        jobCreator.setDescription(description);
        stepData.put(JOB_CREATOR, jobCreator);
    }

    @When("I try to create job with permitted symbols {string} in name")
    public void iTryToCreateJobWithPermittedSymbolsInName(String validCharacters) throws Exception {
        tryToCreateJob(validCharacters);
    }

    @When("I try to create job with invalid symbols {string} in name")
    public void iTryToCreateJobWithInvalidSymbolsInName(String invalidCharacters) throws Exception {
        tryToCreateJob(invalidCharacters);
    }

    private void tryToCreateJob(String characters) throws Exception {
        JobCreator jobCreator = jobFactory.newCreator(getCurrentScopeId());
        for (int i = 0; i < characters.length(); i++) {
            String jobName = JOB_NAME + characters.charAt(i);
            jobCreator.setName(jobName);
            try {
                primeException();
                Job job = jobService.create(jobCreator);
                stepData.put("Job", job);
                stepData.put(CURRENT_JOB_ID, job.getId());
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @Then("I find a job with description {string}")
    public void iFindAJobWithDescription(String jobDescription) throws Throwable {
        Job job = (Job) stepData.get("Job");
        Assert.assertEquals(job.getDescription(), jobDescription);
    }

    @Then("I try to update job name with permitted symbols {string} in name")
    public void iTryToUpdateJobNameWithPermittedSymbolsInName(String validCharacters) throws Exception {
        tryToUpdateJobName(validCharacters);
    }

    @When("I try to update job name with invalid symbols {string} in name")
    public void iTryToUpdateJobNameWithInvalidSymbolsInName(String invalidCharacters) throws Exception {
        tryToUpdateJobName(invalidCharacters);
    }

    private void tryToUpdateJobName(String characters) throws Exception {
        JobCreator jobCreator = jobFactory.newCreator(getCurrentScopeId());
        //are we sure works as expected with invalid characters?
        for (int i = 0; i < characters.length(); i++) {
            String jobName = JOB_NAME + characters.charAt(i);
            jobCreator.setName(JOB_NAME + i);
            try {
                primeException();
                stepData.remove("Job");
                Job job = jobService.create(jobCreator);
                job.setName(jobName);
                jobService.update(job);
                stepData.put(CURRENT_JOB_ID, job.getId());
                stepData.put("Job", job);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @Then("I change name of job from {string} to {string}")
    public void iChangeNameOfJobFromTo(String oldName, String newName) throws Throwable {
        try {
            JobQuery query = jobFactory.newQuery(getCurrentScopeId());
            query.setPredicate(query.attributePredicate(JobAttributes.NAME, oldName, Operator.EQUAL));
            JobListResult queryResult = jobService.query(query);
            Job job = queryResult.getFirstItem();
            job.setName(newName);
            jobService.update(job);
            stepData.put("Job", job);
        } catch (Exception e) {
            verifyException(e);
        }
    }

    @And("There is no job with name {string} in database")
    public void thereIsNoJobWithNameInDatabase(String jobName) throws Throwable {
        Job job = (Job) stepData.get("Job");
        Assert.assertNotEquals(job.getName(), jobName);
    }

    @When("I change the job description from {string} to {string}")
    public void iChangeTheJobDescriptionFromTo(String oldDescription, String newDescription) throws Throwable {
        try {
            JobQuery query = jobFactory.newQuery(getCurrentScopeId());
            query.setPredicate(query.attributePredicate(JobAttributes.DESCRIPTION, oldDescription, Operator.EQUAL));
            JobListResult queryResult = jobService.query(query);
            Job job = queryResult.getFirstItem();
            job.setDescription(newDescription);
            jobService.update(job);
            stepData.put("Job", job);
        } catch (Exception e) {
            verifyException(e);
        }
    }

}
