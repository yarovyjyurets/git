https://www.yunforum.net/pdf/kubernetes-in-action.pdf
COMMANDS
-

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