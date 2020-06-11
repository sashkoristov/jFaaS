package dps.invoker;

import VMInvokerResources.FunctionInput;
import VMInvokerResources.OperatingSystem;
import VMInvokerResources.SSHClient;
import VMInvokerResources.TaskInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jcraft.jsch.Session;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VMInvoker implements FaaSInvoker {

    private List<TaskInfo> tasksInfo;
    private List<String> values;

    /**
     * This method invokes the task
     *
     * @param function has the form "IP:VM:OS:TaskID:Parameter1:...:ParameterN"
     * @param functionInputs contains the file paths to Tasks.yaml and private key
     * @return
     * @throws Exception
     */
    @Override
    public String invokeFunction(String function, Map<String, Object> functionInputs) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        try {
            tasksInfo = yamlMapper.readValue(new File(functionInputs.get(FunctionInput.TASK_DESCRIPTION_PATH).toString()), new TypeReference<>() {});
            values = getValues(function);
            if (!values.contains("VM")) {
                throw new IllegalArgumentException("TYPES ACCEPTED: VM");
            }
            String publicIP = getPublicIP();
            OperatingSystem operatingSystem = getOperatingSystem();
            String task = getTask();
            if (task.endsWith(".sh")) {
                executeTaskOnVM(publicIP, operatingSystem, functionInputs.get(FunctionInput.PRIVATE_KEY_PATH).toString(), task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method gets a String, the "value", with the form:
     * "IP:ResourceType:OperatingSystem:TaskID:Param1:...:ParamN"
     * and it returns all fields separated by : as a list
     *
     * @param value
     * @return
     */
    private List<String> getValues(String value) {
        List<String> values = new ArrayList<>();
        while(value.contains(":")) {
            values.add(value.substring(0, value.indexOf(":")));
            value = value.substring(value.indexOf(":")+1);
        }
        values.add(value);
        return values;
    }

    /**
     * This method checks if a valid IP address is in the given list
     * and returns it
     *
     * @return
     */
    private String getPublicIP() {
        return values.stream()
                .filter(v -> InetAddressValidator.getInstance().isValidInet4Address(v))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NO VALID PUBLIC IP ADDRESS"));
    }

    /**
     * This method checks if a valid operating system is in the given list
     * and returns it
     *
     * @return
     */
    private OperatingSystem getOperatingSystem() {
        return values.stream()
                .filter(v -> (v.equals(OperatingSystem.UBUNTU.toString()) || v.equals(OperatingSystem.CENTOS.toString())))
                .findFirst()
                .map(os -> OperatingSystem.valueOf(os))
                .orElseThrow(() -> new IllegalArgumentException("OPERATING SYSTEMS ACCEPTED: UBUNTU, CENTOS"));
    }

    /**
     * This method checks if the 4th field is a valid task ID
     * and returns the filename of the respective task
     *
     * @return
     */
    private String getTask() {
        if (values.get(3).chars().allMatch(Character::isDigit)) {
            int taskID = Integer.parseInt(values.get(3));
            return tasksInfo.stream()
                    .filter(t -> t.getId() == taskID)
                    .map(t -> t.getTaskName())
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("NO VALID TASK ID"));
        }
        throw new IllegalArgumentException("NO VALID TASK ID AS FOURTH FIELD");
    }

    /**
     * This method gets all fields with an index > 3
     * and combines them to one string as follows: " param1 param2 paramN"
     * If there were no parameters given the method returns ""
     *
     * @return
     */
    private String getParameterString() {
        String parameters = "";
        for (int i = 4; i < values.size(); i++) {
            parameters = parameters + " " + values.get(i);
        }
        return parameters;
    }

    /**
     * This method executes the task on the VM
     *
     * @param publicIP
     * @param operatingSystem
     * @param keyFilePath
     * @param task
     */
    private void executeTaskOnVM(String publicIP, OperatingSystem operatingSystem, String keyFilePath, String task) {
        String parameters = getParameterString();
        Session session = SSHClient.createSession(publicIP, 22, operatingSystem.toString().toLowerCase(), keyFilePath);
        if (session != null) {
            SSHClient.executeCommand("sh " + task + parameters, publicIP, session);
            SSHClient.closeSession(publicIP, session);
        }
    }



}
