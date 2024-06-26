# Back-end for rApp Managers

## Local Helm Chart Setup

### Setting up the Environment

1. Install Kind: Visit this link to install [kind](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)

2. Install Power tools for kubectl: Visit this link to install [kubectl](https://github.com/ahmetb/kubectx?tab=readme-ov-file#installation).

3. Install Helm: Visit this link to install [helm](https://helm.sh/docs/intro/install/).

4. Setup local registry:

    Visit this link [here](https://kind.sigs.k8s.io/docs/user/local-registry/) and download the script into your system. Run the script using the following command:

    ```bash
        bash <script-name>>.sh
    ```
    
    **NOTE:** If the script is not running run it in git bash on windows.

### Running the chart

1. Pull the Docker image from GCP using the following command:

    ```bash
        docker pull europe-north1-docker.pkg.dev/rapp-studio/rapp/rapp-manager-backend:<version-tag> 
    ```

2. Tag image to localhost:

    ```bash
        docker tag europe-north1-docker.pkg.dev/rapp-studio/rapp/rapp-manager-backend:<version-tag> localhost:5001/europe-north1-docker.pkg.dev/rapp-studio/rapp/rapp-manager-backend:<version-tag>
    ```

3. Push the Docker image to localhost:

    ```bash
        docker push localhost:5001/europe-north1-docker.pkg.dev/rapp-studio/rapp/rapp-manager-backend:<version-tag>
    ```

4. Add credentials to Helm Chart: Add your GCP credentials to the `charts/rapp-manager-backend-local/app/config.json` file.

5. Install Helm Chart

    ```bash
        helm install rapp-manager-backend-local ./charts/rapp-manager-backend-local
    ```

    The following output will be thrown on success:

    ```bash
    export POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=rapp-manager-backend-local,app.kubernetes.io/instance=rapp-manager-backend-local" -o jsonpath="{.items[0].metadata.name}")
    export CONTAINER_PORT=$(kubectl get pod --namespace default $POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
    echo "Visit http://127.0.0.1:8080 to use your application"
    kubectl --namespace default port-forward $POD_NAME 8080:$CONTAINER_PORT
    ```

    **NOTE:** The `export` command does not work on Windows terminal please use git bash to run these commands above.

6. Verify that the Helm Chart is running the pod:

    ```bash
        kubectl get pods
    ```

    On success, the chart will be running:

    ```bash
        rapp-manager-backend-local-<pod-id>   1/1     Running   0          2m
    ```

7. To use the service run the following command:

    ```bash
        kubectl --namespace default port-forward $POD_NAME 8080:$CONTAINER_PORT
    ```

    **NOTE:** Make sure you run the `export` command (Step 5's output) before running the port-forward command.

**API Documentation can be found [here](https://zinkworks.atlassian.net/wiki/spaces/ZINKWORKS/blog/2024/04/24/702578698/r-APP+BackEnd+API+Developer+Guide).**
