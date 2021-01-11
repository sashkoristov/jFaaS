# VMInvoker implementation check

### Test setup - steps

##### 1
Start a VM and make it public accessible (e.g. via the console).
 
##### 2
Create a ``setup.sh``:

````
ssh -i testpair.pem ec2-user@54.236.48.155
touch setup.sh
````

with the following content:

````
#!/bin/bash

echo "{ 'message': 'hello $1' }"
````

##### 3

Create the ``Tasks.yaml`` file:

````yaml
- !!project.TaskInfo
  id: 0
  taskName: setup.sh
  taskFilePath: setup.sh
  metaDataNames:
  metaDataFilePaths:
````

##### 4

Adapt the ``workflow.yaml`` file

from:

````yaml
name: "helloWorld"
dataIns:
  - name: "name"
    type: "string"
    source: "name"
workflowBody:
  - function:
      name: "hello"
      type: "helloType"
      dataIns:
        - name: "name"
          type: "string"
          source: "helloWorld/name"
      dataOuts:
        - name: "message"
          type: "string"
      properties:
        - name: "resource"
          value: "python:https://eu-gb.functions.appdomain.cloud/api/v1/web/pste4444%40gmail.com_dev/default/helloWorld.json"
dataOuts:
  - name: "message"
    type: "string"
    source: "hello/message"
````

to

````yaml
name: "helloWorld"
dataIns:
  - name: "0"
    type: "string"
    source: "0"
workflowBody:
  - function:
      name: "hello"
      type: "helloType"
      dataIns:
        - name: "0"
          type: "string"
          source: "helloWorld/0"
      dataOuts:
        - name: "message"
          type: "string"
      properties:
        - name: "resource"
          value: "sh:54.236.48.155:VM:UBUNTU:0"
dataOuts:
  - name: "message"
    type: "string"
    source: "hello/message"
````

### Limitations

- Key of json can only be number?!
- username bust be the same as operating system?!
- Output Json is never returned