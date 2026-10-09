
Critical Runtime Prerequisite: The Bootstrap JSON

You cannot run an xDS application without configuring a bootstrap environment variables pointer. 
When Grpc.newChannelBuilder executes, it parses the filesystem destination defined inside GRPC_XDS_BOOTSTRAP to locate 
the routing server.
If you are using Istio, its sidecar injection system can automatically mount an ambient agent infrastructure config for you.
If you are building a custom runtime test loop locally on WSL, save this baseline file layout as bootstrap.json

Before running your compiled Java application file inside your terminal console, activate the config tracking property like this:
```bash
export GRPC_XDS_BOOTSTRAP="/path/to/your/bootstrap.json"
java -jar your-app.jar
```
