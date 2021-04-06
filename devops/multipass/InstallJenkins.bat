multipass stop jenkins
multipass delete jenkins
multipass purge

multipass launch --cpus 2 --name jenkins --cloud-init jenkins.cloud-init

multipass exec jenkins -- cloud-init status --wait

multipass mount ../jenkins_home jenkins:/var/lib/jenkins

multipass exec jenkins -- sudo apt-get -y install jenkins

rem Pause to Let Jenkins start
timeout /t 30 /nobreak

multipass exec jenkins -- sudo cat /var/lib/jenkins/secrets/initialAdminPassword

start "" http://jenkins.mshome.net:8080/
