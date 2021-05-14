package jFaaS.invokers;

import VMInvokerResources.OperatingSystem;
import VMInvokerResources.SSHClient;
import VMInvokerResources.TaskInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonObject;
import com.jcraft.jsch.Session;
import jFaaS.utils.PairResult;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class VMInvoker implements FaaSInvoker {

    private static final String KEY_FILE_PATH = "src/main/resources/keys/private-key.pem";
    private static final String TASKS_FILE_PATH = "src/main/resources/resourceFiles/Tasks.yaml";
    private CountDownLatch latch;

    /**
     * This method invokes the task
     *
     * @param function       has the form "IP:VM:OS:TaskID"
     * @param functionInputs contains the parameters for invoking the task on the machine
     *
     * @return
     */
    @Override
    public PairResult<String, Long> invokeFunction(String function, Map<String, Object> functionInputs) {
        long start = System.currentTimeMillis();
        latch = new CountDownLatch(1);
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        try {
            List<TaskInfo> tasksInfo = yamlMapper.readValue(new File(TASKS_FILE_PATH), new TypeReference<List<TaskInfo>>() {});
            List<String> valuesOfFunction = getValues(function);
            String task = getTask(valuesOfFunction, tasksInfo);
            if (task.endsWith(".sh")) {
                executeScriptOnVM(getPublicIP(valuesOfFunction), getOperatingSystem(valuesOfFunction), task, getParameterString(functionInputs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PairResult<>(new JsonObject().toString(), System.currentTimeMillis() - start);
    }

    /**
     * This method gets a String, the "value", with the form: "IP:ResourceType:OperatingSystem:TaskID" and it returns
     * all fields separated by : as a list
     *
     * @param value
     *
     * @return
     */
    private List<String> getValues(String value) {
        List<String> values = new ArrayList<>();
        while (value.contains(":")) {
            values.add(value.substring(0, value.indexOf(":")));
            value = value.substring(value.indexOf(":") + 1);
        }
        values.add(value);
        return values;
    }

    /**
     * This method checks if a valid IP address is in the given list and returns it
     *
     * @param valuesOfFunction
     *
     * @return
     */
    private String getPublicIP(List<String> valuesOfFunction) {
        return valuesOfFunction.stream()
                .filter(v -> InetAddressValidator.getInstance().isValidInet4Address(v))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NO VALID PUBLIC IP ADDRESS"));
    }

    /**
     * This method checks if a valid operating system is in the given list and returns it
     *
     * @param valuesOfFunction
     *
     * @return
     */
    private OperatingSystem getOperatingSystem(List<String> valuesOfFunction) {
        return valuesOfFunction.stream()
                .filter(v -> (v.equals(OperatingSystem.UBUNTU.toString()) || v.equals(OperatingSystem.CENTOS.toString())))
                .findFirst()
                .map(os -> OperatingSystem.valueOf(os))
                .orElseThrow(() -> new IllegalArgumentException("OPERATING SYSTEMS ACCEPTED: UBUNTU, CENTOS"));
    }

    /**
     * This method checks if the 4th field is a valid task ID and returns the filename of the respective task
     *
     * @param valuesOfFunction
     * @param tasksInfo
     *
     * @return
     */
    private String getTask(List<String> valuesOfFunction, List<TaskInfo> tasksInfo) {
        if (valuesOfFunction.get(3).chars().allMatch(Character::isDigit)) {
            int taskID = Integer.parseInt(valuesOfFunction.get(3));
            return tasksInfo.stream()
                    .filter(t -> t.getId() == taskID)
                    .map(t -> t.getTaskName())
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("NO VALID TASK ID"));
        }
        throw new IllegalArgumentException("NO VALID TASK ID AS FOURTH FIELD");
    }

    /**
     * This method returns all given parameters as single string in the following form: " parameter1 parameter2
     * parameterN"
     *
     * @param functionInputs
     *
     * @return
     */
    private String getParameterString(Map<String, Object> functionInputs) {
        String parameterString = "";
        if (functionInputs != null && !functionInputs.isEmpty()) {
            Map<Integer, Object> functionInputsSorted = new HashMap<>();
            for (Map.Entry entry : functionInputs.entrySet()) {
                functionInputsSorted.put(Integer.valueOf((String) entry.getKey()), entry.getValue());
            }
            functionInputsSorted = new TreeMap<>(functionInputsSorted);
            for (Map.Entry entry : functionInputsSorted.entrySet()) {
                parameterString = parameterString + " " + entry.getValue();
            }
        }
        return parameterString;
    }

    /**
     * This method executes the shell script on the VM
     *
     * @param publicIP
     * @param operatingSystem
     * @param parameterString
     * @param task
     */
    private void executeScriptOnVM(String publicIP, OperatingSystem operatingSystem, String task, String parameterString) {
        Session session = SSHClient.createSession(publicIP, 22, operatingSystem.toString().toLowerCase(), KEY_FILE_PATH);
        if (session != null) {
            SSHClient.executeCommand("sh " + task + parameterString, publicIP, session);
            SSHClient.closeSession(publicIP, session);
            latch.countDown();
        }
    }


}
