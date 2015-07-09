# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

KIXI_DEV_VERSION="1.0.6"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "kixi-dev-" + KIXI_DEV_VERSION

  config.vm.box_url = "http://mc-deployments-public.s3-website-us-west-2.amazonaws.com/kixi-dev-" + KIXI_DEV_VERSION + ".box"

  config.vm.provider :virtualbox do |vb, override|
    # headless mode
    vb.gui = false

    # Use VBoxManage to customize the VM.
    # See http://www.virtualbox.org/manual/ch08.html#idp56624480
    vb.customize ["modifyvm", :id, "--memory", "2048"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    config.vm.network "private_network", ip: "192.168.50.4", virtualbox__intnet: "kixi"
    override.vm.network :forwarded_port, guest: 9042, host: 9042 #Cassandra CQL
    override.vm.network :forwarded_port, guest: 9160, host: 9160 #Cassandra Thrift
    override.vm.network :forwarded_port, guest: 7199, host: 7199 #Cassandra JMX (TODO - confirm)
    override.vm.network :forwarded_port, guest: 9200, host: 9200 #ElasticSearch
    override.vm.network :forwarded_port, guest: 9300, host: 9300 #ElasticSearch
    override.vm.network :forwarded_port, guest: 8010, host: 8010 #web

  end

end
