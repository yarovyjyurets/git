def call(def imageTag, def clusterName) {
  def encodedImageTag = java.net.URLEncoder.encode(imageTag, "UTF-8")
  def promoteToDevJobName = "${JENKINS_URL}job/deploy-to-dev"

  echo 'DEV link'
  echo "${promoteToDevJobName}/parambuild/?imageTag=${encodedImageTag}&clusterName=${clusterName}"

  currentBuild.description = """
  <div>
    <a href='${promoteToDevJobName}/parambuild/?imageTag=${encodedImageTag}&clusterName=${clusterName}'>
      Promote to DEV
    </a>
  </div>
  """
}