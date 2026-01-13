Trigger a POST refresh call to http://localhost:8888/actuator/refresh will return latest configurations at runtime.

## create config repo
```bash
 cd $HOME
 mkdir config-repo
 cd config-repo
 git init .
 echo info.foo: bar > application.properties
 git add -A .
 git commit -m "Add application.properties"
```