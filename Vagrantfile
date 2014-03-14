# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

KIXI_DEV_VERSION="1.0.3"

$extras_script = <<SCRIPT
echo provisioning extras...
cd /
tar xzvf /vagrant/vagrant/extras/kafka-multi-setup.tar.gz
SCRIPT

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "kixi-dev-" + KIXI_DEV_VERSION

  config.vm.box_url = "http://mc-deployments-public.s3-website-us-west-2.amazonaws.com/kixi-dev-" + KIXI_DEV_VERSION + ".box"

  config.vm.provider :virtualbox do |vb, override|
    # headless mode
    vb.gui = false

    # Use VBoxManage to customize the VM.
    # See http://www.virtualbox.org/manual/ch08.html#idp56624480
    vb.customize ["modifyvm", :id, "--memory", "1024"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    config.vm.network "private_network", ip: "192.168.50.4", virtualbox__intnet: "kixi"
    override.vm.network :forwarded_port, guest: 9092, host: 9092 #Kafka
    override.vm.network :forwarded_port, guest: 9093, host: 9093 #Kafka
    override.vm.network :forwarded_port, guest: 10000, host: 10000 #Kafka JMX
    override.vm.network :forwarded_port, guest: 10001, host: 10001 #Kafka JMX
    override.vm.network :forwarded_port, guest: 2181, host: 2181 #Zookeeper
    override.vm.network :forwarded_port, guest: 9042, host: 9042 #Cassandra CQL
    override.vm.network :forwarded_port, guest: 9160, host: 9160 #Cassandra Thrift
    override.vm.network :forwarded_port, guest: 7199, host: 7199 #Cassandra JMX (TODO - confirm)

  end

  config.vm.provision :shell, inline: $extras_script

end
