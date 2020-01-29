https://www.yunforum.net/pdf/kubernetes-in-action.pdf
COMMANDS
-
- kubectl config view
- kubectl config get-contexts
- kubectl config use-context 
- gcloud container clusters list
- gcloud container clusters create kubia --num-nodes 3 --machine-type f1-micro

- kubectl run kubia --image=luksa/kubia --port=8080 --generator=run/v1

Scale out replicationcontrollers
- kubectl scale rc kubia --replicas=3

Expose service
- kubectl expose rc kubia --type=LoadBalancer --name kubia-http

Explain the docs
-
- k explain pods.spec ...
- kubectl cluster-info | grep dashboard
- gcloud container clusters describe kubia | grep -E "(username|password):"

https://gist.github.com/DanielBerman/0724195d977f97d68fc2c7bc4a4e0419

INFO
-
- kubectl cluster-info
- kubectl get nodes|no
- kubectl get pods|po [-o wide|yaml]
- kubectl get replicationcontrollers|rc
- kubectl describe node <node>
- kubectl describe pod|po
- kubectl describe pod|po
- kubectl describe servise|svc  

LOGS
-
- kubectl logs mypod
- kubectl logs mypod --previous `if died, to check reason`

LABELS
-
Listing labels
- kubectl get po --show-labels
- kubectl get po -L creation_method,env

Listing subsets of pods through label selectors
- kubectl get po -l creation_method=manual; kubectl get po -l env=debug; kubectl get po -l creation_method=manual,env=debug;
- kubectl get po -l 'env in (prod,devel)'
- kubectl get po -l creation_method!=manual; kubectl get po -l env!=debug
- kubectl get po -l creation_method
- kubectl get po -l '!env'

Modifying/adding labels of existing pods
- kubectl label po kubia-manual creation_method=manual
- kubectl label po kubia-manual-v2 env=debug --overwrite
- kubectl create -f kubia-manual.yaml

ANNOTATIONS
-
>For example, an annotation used to specify the name of the person who
created the object can make collaboration between everyone working on the cluster
much easier.

- kubectl get pods|po {name} -o yaml
- kubectl annotate pod kubia-manual mycompany.com/someannotation="foo bar"

NAMESPACES
-
>**!IMPORTANT**: Node is not possible namespaced

>**TIP** alias kcd='kubectl config set-context $(kubectl config current-
context) --namespace
- kubectl get ns
- kubectl get po --namespace|n {namespace}
- kubectl create -f custom-namespace.yaml
- kubectl create namespace custom-namespace
- kubectl create -f kubia-manual.yaml -n custom-namespace `OR` add `namespace: custom-
namespace entry to the metadata`

STOPPING AND REMOVING PODS
-
- kubectl delete po {name1} {name2}
- k delete po -l creation_method=manual `by label`
- k delete po -l creation_method=manual -l some-other-label `by labels`
- kubectl delete ns custom-namespace `by namespace`
- kubectl delete po --all `all pods`
- kubectl delete all --all `ALL OBJECTs`  
> NOTE Deleting everything with the all keyword doesn’t delete absolutely
everything. Certain resources (like Secrets, which we’ll introduce in chapter 7)
are preserved and need to be deleted explicitly.

---
# Replication and other controllers: deploying managed pods
ReplicationController
-
It has three essential parts:
> * A `label selector`, which determines what pods are in the ReplicationController’s scope
> * A `replica count`, which specifies the desired number of pods that should be running
> * A `pod template`, which is used when creating new pod replicas

>`IMPORTANT` only changes to the replica count affect existing pods.

>`TIP` Don’t specify a pod selector when defining a ReplicationController. Let
Kubernetes extract it from the pod template. This will keep your YAML
shorter and simpler.
- gcloud compute ssh {GKE node}
- sudo ifconfig eth0 down `kill the node on GKE`
- gcloud compute instances reset {nodeId}
>`TIP` If you know a pod is malfunctioning, you can take it out of the Replication-
Controller’s scope, let the controller replace it with a new one, and then debug or
play with the pod in any way you want.
- kubectl edit rc kubia `edit already create rc template`
- kubectl scale rc kubia --replicas=10
>`TIP` Deleting a replication controller with `--cascade=false` leaves pods unmanaged
- kubectl delete rc kubia --cascade=false

ReplicaSet
-
>`INFO` A ReplicaSet behaves exactly like a ReplicationController, but it has more expressive
pod selectors.
- kubectl get rs
- kubectl describe rs

```
selector
  matchExpressions:
    - key: app
      operator: In
      values:
        - kubia
```
**Valid operators:**
>* `In` — Label’s value must match one of the specified values .
>* `NotIn` — Label’s value must not match any of the specified values .
>* `Exists` — Pod must include a label with the specified key (the value isn’t import-
ant). When using this operator, you shouldn’t specify the values field.
>* `DoesNotExist` — Pod must not include a label with the specified key. The values
property must not be specified.

# DaemonSets
When you want pod to run on each and every node in the cluster
> Nodes can be made unschedulable,
preventing pods from being deployed to them. A DaemonSet will deploy pods
even to such nodes, because the unschedulable attribute is only used by the
Scheduler, whereas pods managed by a DaemonSet bypass the Scheduler
completely. This is usually desirable, because DaemonSets are meant to run
system services, which usually need to run even on unschedulable nodes.
- kubectl label node minikube disk=ssd --overwrite

# Job/CronJob resources

Jobs are useful for ad hoc tasks, where it’s crucial that the task fin-
ishes properly.
- k create -f k8s/kubia-job.yaml
- k get job
>`INFO` Jobs may be configured to create more than one pod instance and run them in paral-
lel or sequentially. This is done by setting the `completions` and the p`arallelism` prop-
erties in the Job spec

# Services

>* `Pods are ephemeral`—They may come and go at any time, whether it’s because a
pod is removed from a node to make room for other pods, because someone
scaled down the number of pods, or because a cluster node has failed.
>* `Kubernetes assigns an IP address to a pod after the pod has been scheduled to a node and before it’s started` — Clients thus can’t know the IP address of the server pod
up front.
>* `Horizontal scaling means multiple pods may provide the same service` — Each of those
pods has its own IP address. Clients shouldn’t care how many pods are backing
the service and what their IPs are. They shouldn’t have to keep a list of all the
individual IPs of pods. Instead, all those pods should be accessible through a
single IP address.

To solve these problems, Kubernetes also provides another resource type — `Services`

A Kubernetes `Service` is a resource you create to make a single, constant point of
entry to a group of pods providing the same service.

Creating services
-
- k create -f kubia-svc.yaml

The `expose` command created a Service resource with the same pod selector as the one
used by the ReplicationController, thereby exposing all its pods through a single IP
address and port.

- kubectl expose rc {name} --type=LoadBalancer --name kubia-http

You can send requests to your service from within the cluster in a few ways:
>* The obvious way is to create a pod that will send the request to the service’s
cluster IP and log the response. You can then examine the pod’s log to see
what the service’s response was.
>* You can ssh into one of the Kubernetes nodes and use the curl command.
>* You can execute the curl command inside one of your existing pods through
the kubectl exec command.

- kubectl exec pod -- curl -s {url}

`sessionAffinity`: `ClientIP`|Node -  makes the service proxy redirect all requests originating from the same client IP
to the same pod.
>`INFO` Kubernetes supports only two types of service session affinity: None and ClientIP.
You may be surprised it doesn’t have a cookie-based session affinity option, but you
need to understand that Kubernetes services don’t operate at the HTTP level. Services
deal with TCP and UDP packets and don’t care about the payload they carry. Because
cookies are a construct of the HTTP protocol, services don’t know about them, which
explains why session affinity cannot be based on cookies.

>`NOTE` When creating a service with multiple ports, you must specify a name
for each port.
```
spec:
  ports:
    - name: http
      port: 80
      targetPort: 8080
    - name: https
      port: 443
      targetPort: 8443
  selector:
    app: kubia
```
>`TIP` But why should you even bother with **naming ports**? The biggest benefit of doing so is
that it enables you to change port numbers later without having to change the service
spec.

Discovering services
-
*DISCOVERING SERVICES THROUGH ENVIRONMENT VARIABLES* (useless)
*DISCOVERING SERVICES THROUGH DNS*

- kubectl exec -it {name} bash
- cat /etc/resolv.conf (result: default.svc.cluster.local)
- curl http://{svc-name}
>`NOTE` YOU CAN’T PING A SERVICE IP

Connecting to services living outside the cluster (5.2.1)
---
- kubectl describe svc {svc-name}

An Endpoints resource (yes, plural) is a list of IP addresses and ports exposing a service.
The Endpoints resource is like any other Kubernetes resource, so you can display
its basic info with `kubectl`
- kubectl get endpoints {svc-name}
>`NOTE` Although the pod selector is defined in the service spec, it’s not used directly when
redirecting incoming connections. Instead, the selector is used to build a list of IPs
and ports, which is then stored in the Endpoints resource.

# Exposing services to external clients (5.3)
>`NOTE` Services have default type: `ClusterIP`

You have a few ways to make a service accessible externally:
>* `Setting the service type to NodePort` —For a NodePort service, each cluster node
opens a port on the node itself (hence the name) and redirects traffic received
on that port to the underlying service. The service isn’t accessible only at the
internal cluster IP and port, but also through a dedicated port on all nodes.
>* `Setting the service type to LoadBalancer` , an extension of the NodePort type—This
makes the service accessible through a dedicated load balancer, provisioned
from the cloud infrastructure Kubernetes is running on. The load balancer redi-
rects traffic to the node port across all the nodes. Clients connect to the service
through the load balancer’s IP.
>* `Creating an Ingress resource`, a radically different mechanism for exposing multiple ser-
vices through a single IP address—It operates at the HTTP level (network layer 7)
and can thus offer more features than layer 4 services can. We’ll explain Ingress
resources in section 5.4.

Ingerss
-
>`NOTE` Ingress controllers on cloud providers (in GKE, for example) require
the Ingress to point to a NodePort service. But that’s not a requirement of
Kubernetes itself.
Multiple services can be exposed through a single Ingress.
- minikube addons list
- minikube addons enable ingress

# Signaling when a pod is ready to accept connections
Readiness probes
-
The readiness probe is invoked periodically and determines whether the specific
pod should receive client requests or not.
>`NOTE` Unlike liveness probes, if a container fails the readiness check, it won’t be killed or
restarted. This is an important distinction between liveness and readiness probes.

>`NOTE` DON’T INCLUDE POD SHUTDOWN LOGIC INTO YOUR READINESS PROBES

Headless service
-
Can provide you all IPs of pods managed by service.

Troubleshooting services
-
>* First, make sure you’re connecting to the service’s cluster IP from within the
cluster, not from the outside.
>* Don’t bother pinging the service IP to figure out if the service is accessible
(remember, the service’s cluster IP is a virtual IP and pinging it will never work).
>* If you’ve defined a readiness probe, make sure it’s succeeding; otherwise the
pod won’t be part of the service.
>* To confirm that a pod is part of the service, examine the corresponding Endpoints
object with kubectl get endpoints.
>* If you’re trying to access the service through its FQDN or a part of it (for example,
myservice.mynamespace.svc.cluster.local or myservice.mynamespace) and
it doesn’t work, see if you can access it using its cluster IP instead of the FQDN.
>* Check whether you’re connecting to the port exposed by the service and not
the target port.
>* Try connecting to the pod IP directly to confirm your pod is accepting connections
on the correct port.
>* If you can’t even access your

# Volumes
Type of volumes:
>* `emptyDir` — A simple empty directory used for storing transient data.
>* `hostPath` — Used for mounting directories from the worker node’s filesystem into the pod.
>* `gitRepo` — A volume initialized by checking out the contents of a Git repository.
>* `nfs` — An NFS share mounted into the pod.
>* `gcePersistentDisk (Google Compute Engine Persistent Disk), awsElastic-
BlockStore (Amazon Web Services Elastic Block Store Volume), azureDisk
(Microsoft Azure Disk Volume)` — Used for mounting cloud provider-specific
storage.Using volumes to share data between containers
>* `cinder, cephfs , iscsi , flocker , glusterfs , quobyte , rbd , flexVolume , vsphere-
Volume , photonPersistentDisk , scaleIO` — Used for mounting other types of
network storage.
>* `configMap , secret , downwardAPI` — Special types of volumes used to expose certain Kubernetes resources and cluster information to the pod.
>* `persistentVolumeClaim` — A way to use a pre- or dynamically provisioned persistent storage. (We’ll talk about them in the last section of this chapter.)

`emptyDir`
-
The volume’s lifetime is tied to that of the pod, the volume’s contents are
lost when the pod is deleted.
An emptyDir volume is especially useful for sharing files between containers
running in the same pod. But it can also be used by a single container for when a container needs to write data to disk temporarily, such as when performing a sort
operation on a large dataset, which can’t fit into the available memory.
- k apply -f k8s/volumes/fortune-pod.yaml

An emptyDir volume is the simplest type of volume, but other types build upon it.
After the empty directory is created, they populate it with data. One such volume type
is the `gitRepo` volume type, which we’ll introduce next.

`gitRepo`
-
A `gitRepo` volume is basically an `emptyDir` volume that gets populated by cloning a
Git repository and checking out a specific revision when the pod is starting up (but
before its containers are created). A gitRepo volume, like the emptyDir volume, is basically a dedicated directory cre-
ated specifically for, and used exclusively by, the pod that contains the volume. When
the pod is deleted, the volume and its contents are deleted.

`hostPath`
-
A hostPath volume points to a specific file or directory on the node’s filesystem. Pods running on the same node and using the same path in their host-
Path volume see the same files.

`hostPath` volumes are the first type of persistent storage we’re introducing, because
both the gitRepo and emptyDir volumes’ contents get deleted when a pod is torn
down, whereas a hostPath volume’s contents don’t. Because the volume’s contents are stored on a specific
node’s filesystem, when the database pod gets rescheduled to another node, it will no
longer see the data. This explains why it’s not a good idea to use a hostPath volume
for regular pods, because it makes the pod sensitive to what node it’s scheduled to.
>`TIP` Remember to use hostPath volumes only if you need to read or write sys-
tem files on the node. Never use them to persist data across pods.

# Deployment
You have two ways of updating all those pods. You can do one of the following:
>* Delete all existing pods first and then start the new ones.
>* Start new ones and, once they’re up, delete the old ones. You can do this either
by adding all the new pods and then deleting all the old ones at once (`blue-green deployment`), or
sequentially (`rolling update`), by adding new pods and removing old ones gradually.

At the beginning of this section, I mentioned an even better way of doing updates
than through `kubectl rolling-update` .

- kubectl `rolling-update` kubia-v1 kubia-v2 --image=luksa/kubia:v2 --v 6
>`NOTE` Using the --v 6 option increases the logging level enough to let you see
the requests kubectl is sending to the API server.

But why is it such a bad thing that the update process is being performed by the client
instead of on the server? Well, in your case, the update went smoothly, but what if you
lost network connectivity while kubectl was performing the update? The update pro-
cess would be interrupted mid-way. Pods and ReplicationControllers would end up in
an intermediate state.

>`TIP` Use the verbose logging option when running other kubectl commands,
to learn more about the communication between kubectl and the API server.

Creating Deployment
-
Creating a Deployment isn’t that different from creating a ReplicationController. A
Deployment is also composed of a label selector, a desired replica count, and a pod
template. In addition to that, it also contains a field, which specifies a deployment
strategy that defines how an update should be performed when the Deployment
resource is modified.
- kubectl apply -f k8s/kubia-deployment.yaml --record

*DISPLAYING THE STATUS OF THE DEPLOYMENT ROLLOUT*
- kubectl get deployment
- kubectl describe deployment
- kubectl rollout status deployment kubia

`UNDERSTANDING THE AVAILABLE DEPLOYMENT STRATEGIES`

- `RollingUpdate`: [default] (removes old pods one by one,
while adding new ones at the same time, keeping the application available throughout
the whole process, and ensuring there’s no drop in its capacity to handle requests.)
The upper and lower limits for the number of pods above
or below the desired replica count are configurable. 
>`NOTE` **You should use `RollingUpdate` strategy only
when your app can handle running both the old and new version at the same time.**
- `Recreate` (deletes all the old pods at once and then creates new ones, similar to modifying a
ReplicationController’s pod template and then deleting all the pods. The `Recreate` strategy causes all old pods to be deleted before the new ones are
created.)

Slow down update
- kubectl patch deployment kubia -p '{"spec": {"minReadySeconds": 10}}'
>`TIP` The kubectl patch command is useful for modifying a single property
or a limited number of properties of a resource without having to edit its definition
in a text editor. This doesn’t
cause any kind of update to the pods, because you didn’t change the pod template.
Changing other Deployment properties, like the desired replica count or the deployment
strategy, also doesn’t trigger a rollout, because it doesn’t affect the existing individual
pods in any way.
- kubectl set image deployment kubia kubia=luksa/kubia:v2

>`NOTE` Be aware that if the pod template in the Deployment references a
ConfigMap (or a Secret), modifying the ConfigMap will not trigger an
update. One way to trigger an update when you need to modify an app’s config
is to create a new ConfigMap and modify the pod template so it references
the new ConfigMap.

**UNDOING A ROLLOUT**
- kubectl rollout undo deployment kubia
- kubectl rollout history deployment kubia (`history`)
- kubectl rollout undo deployment kubia --to-revision=1

**CONTROLLING THE RATE OF THE ROLLOUT**
```
spec:
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
    type: RollingUpdate
```

The `minReadySeconds` property specifies how long a newly created pod should be
ready before the pod is treated as available. Until the pod is available, the rollout process
will not continue (remember the `maxUnavailable` property?). A pod is ready
when readiness probes of all its containers return a success. If a new pod isn’t functioning
properly and its readiness probe starts failing before `minReadySeconds` have
passed, the rollout of the new version will effectively be blocked.