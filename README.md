COMMANDS
-

- gcloud container clusters create kubia --num-nodes 3 --machine-type f1-micro

- kubectl run kubia --image=luksa/kubia --port=8080 --generator=run/v1

Scale out replicationcontrollers
- kubectl scale rc kubia --replicas=3

Expose service
- kubectl expose rc kubia --type=LoadBalancer --name kubia-http


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

INFO
-
- kubectl cluster-info
- kubectl get nodes|no
- kubectl get pods|po [-o wide|yaml]
- kubectl get replicationcontrollers|rc
- kubectl describe node <node>
- kubectl describe pod|po
- kubectl describe pod|po


Explain the docs
-
k explain pods.spec ...
kubectl cluster-info | grep dashboard
gcloud container clusters describe kubia | grep -E "(username|password):"

https://gist.github.com/DanielBerman/0724195d977f97d68fc2c7bc4a4e0419
