def call(def imageTag, def environment = 'DEV') {
  def encodedImageTag = java.net.URLEncoder.encode(imageTag, "UTF-8")
  def deploymentJobLink = "${JENKINS_URL}job/tbaem-ecom-deploy"

  echo 'Deploy link:'
  echo "${deploymentJobLink}/parambuild/?ENVIRONMENT=${environment}imageTag=${encodedImageTag}"
}