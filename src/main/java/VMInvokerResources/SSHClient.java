package VMInvokerResources;

import com.jcraft.jsch.*;

import java.io.*;

public abstract class SSHClient {

    /**
     * This method connects to the instance with the given IP and the given SSH key
     *
     * @param publicIP
     * @param port
     * @param username
     * @param keyFilePath
     * @return
     */
    public static Session createSession(String publicIP, int port, String username, String keyFilePath) {
        JSch jSch = new JSch();
        Session session = null;
        try {
            jSch.addIdentity(keyFilePath);
            session = jSch.getSession(username, publicIP, port);
            session.setConfig("StrictHostKeyChecking", "no");
            do {
                System.out.println("Trying to connect to instance with IP " + publicIP + " and username " + username + " ...");
            } while(!connectToSession(session));
            System.out.println("*** Session connected to instance " + username + "@" + publicIP + ": " + port + " ***");
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    private static boolean connectToSession(Session session) {
        try {
            session.connect();
        } catch (JSchException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * This method executes the given command on the instance with the given IP
     *
     * @param command
     * @param publicIP
     * @param session
     */
    public static void executeCommand(String command, String publicIP, Session session) {
        if (!session.isConnected()) {
            System.out.println("Connect to instance with IP " + publicIP + " first before sending command");
            return;
        }
        ChannelExec channel = null;
        try {
            System.out.println("Sending command " + command + " to instance with IP " + publicIP + " ...");
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            channel.connect();
            StringBuilder outputBuffer = new StringBuilder();
            StringBuilder errorBuffer = new StringBuilder();
            int exitStatus = -1;
            byte[] buffer = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0)
                        break;
                    outputBuffer.append(new String(buffer, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(buffer, 0, 1024);
                    if (i < 0)
                        break;
                    errorBuffer.append(new String(buffer, 0, i));
                }
                if (channel.isClosed()) {
                    if ((in.available() > 0) || (err.available() > 0))
                        continue;
                    exitStatus = channel.getExitStatus();
                    System.out.println("Channel exit status on instance with IP " + publicIP + ": " + exitStatus);
                    break;
                }
                try { Thread.sleep(200); } catch (Exception e) {}
            }
            String outputMessages = outputBuffer.toString();
            if (outputMessages.length() > 0) {
                System.out.println("Output: " + outputMessages);
            }
            String errorMessages = errorBuffer.toString();
            if (errorMessages.length() > 0) {
                System.out.println("Error or warning: " + errorMessages);
            }
        } catch (JSchException | IOException e) {
            System.err.println("Something went wrong with executing the command " + command + " on instance with IP " + publicIP);
            e.printStackTrace();
        }
        channel.disconnect();
    }

    /**
     * This method sends the given file to the instance with the given IP
     *
     * @param filePath
     * @param publicIP
     * @param session
     * @return
     */
    public static boolean sendFile(String filePath, String publicIP, Session session) {
        if (!session.isConnected()) {
            System.out.println("Connect to instance with IP " + publicIP + " first before sending file");
            return false;
        }
        File file = new File(filePath);
        System.out.println("Sending " + file.getName() + " to instance with IP " + publicIP + " ...");
        FileInputStream in = null;
        ChannelSftp sftpChannel = null;
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            in = new FileInputStream(file);
            sftpChannel.put(in, file.getName());
            if (file.getPath().endsWith(".sh")) {
                executeCommand("sed -i 's/\r$//' " + file.getName(), publicIP, session);
            }
            if (file.getPath().endsWith(".pem")) {
                executeCommand("chmod 600 " + file.getName(), publicIP, session);
            }
            System.out.println("*** File " + file.getName() + " sent to instance with IP " + publicIP + " ***");
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                sftpChannel.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * This method receives a given file from the instance with the given IP
     *
     * @param remoteFilePath
     * @param localFilePath
     * @param publicIP
     * @param session
     * @return
     */
    public static boolean receiveFile(String remoteFilePath, String localFilePath, String publicIP, Session session) {
        if (!session.isConnected()) {
            System.out.println("Connect to instance with IP " + publicIP + " first before receiving file");
            return false;
        }
        System.out.println("Receiving file from instance with IP " + publicIP + " ...");
        ChannelSftp sftpChannel = null;
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.get(remoteFilePath, localFilePath);
            System.out.println("*** File " + remoteFilePath + " from instance with IP " + publicIP + " has been saved to " + localFilePath + " ***");
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            sftpChannel.disconnect();
        }
        return false;
    }

    /**
     * This method closes the connection to an instance
     *
     * @param publicIP
     * @param session
     * @return
     */
    public static boolean closeSession(String publicIP, Session session) {
        if (!session.isConnected()) {
            System.out.println("Connect to instance with IP " + publicIP + " first");
            return false;
        }
        session.disconnect();
        System.out.println("*** Session of instance with IP " + publicIP + " disconnected ***");
        return true;
    }

}
