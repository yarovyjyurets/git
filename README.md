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
- sudo ifconfig eth0 down `kill the node on GKE`
- gcloud compute instances reset {nodeId}