A Custom Resource Definition (CRD) is a powerful feature that allows you to extend the Kubernetes API by creating your 
own domain-specific, custom object types. By default, Kubernetes includes native resources like Pods, Services, and 
Deployments. A CRD essentially teaches Kubernetes a "new word," making the API server capable of handling the entire 
lifecycle of a custom data structure.

# creating a custom resource
https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/?ref=techblog&utm_source=copilot.com


1. create CRD (custom type):          `kubectl apply -f crontabs_crd.yaml`
2. create instance(s) of CR:          `kubectl apply -f my-crontabs.yaml`
3. get instances:                     `kubectl get crontab`  or   `kubectl get ct -o yaml`

misc: 
delete existing instance `kubectl delete crontab <instance-name>`
delete instances that are not defined in .yaml. Important: labels must be defined in my-crontabs.yaml first:
`kubectl apply -f my-crontabs.yaml --prune -l app=cron-manager`