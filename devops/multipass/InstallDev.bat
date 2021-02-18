multipass stop dev
multipass delete dev
multipass purge

multipass launch --cpus 2 --disk 20G --mem 8G --name dev --cloud-init dev.cloud-init

multipass exec dev -- cloud-init status --wait

multipass mount "C:\Users\Martin Nordberg\Documents" dev:/home/ubuntu/Documents/FromWindows

echo Enter the password for user "ubuntu" ...
multipass exec dev -- sudo passwd ubuntu
