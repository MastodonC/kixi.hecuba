# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "kixi-base"

  #TODO - validate this is a sensible box to use
  config.vm.box_url = "http://grahamc.com/vagrant/ubuntu-12.04-omnibus-chef.box"


  config.vm.provider :virtualbox do |vb, override|
    # headless mode
    vb.gui = false

    # Use VBoxManage to customize the VM.
    # See http://www.virtualbox.org/manual/ch08.html#idp56624480
    vb.customize ["modifyvm", :id, "--memory", "1024"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    override.vm.network :forwarded_port, guest: 9092, host: 9092 #Kafka
    override.vm.network :forwarded_port, guest: 2181, host: 2181 #Zookeeper
    override.vm.network :forwarded_port, guest: 9042, host: 9042 #Cassandra CQL
    override.vm.network :forwarded_port, guest: 9160, host: 9160 #Cassandra Thrift
    override.vm.network :forwarded_port, guest: 7199, host: 7199 #Cassandra JMX (TODO - confirm)
 
  end

  config.vm.provider :aws do |aws, override|
    aws.access_key_id = ENV['AWS_ACCESS_KEY']
    aws.secret_access_key = ENV['AWS_SECRET_KEY']
    aws.keypair_name = ENV['AWS_KEYPAIR']
    aws.instance_type = "m1.small"
    aws.security_groups = "kixi"

    aws.ami = "ami-7747d01e" # for m1.small

    aws.user_data = "#cloud-config\nbootcmd:\n - echo 'manual' > /etc/init/ssh.override\npackages:\n - ruby-1.9.3\nruncmd:\n - [ wget, 'https://www.opscode.com/chef/install.sh', -O, /tmp/install_chef.sh ]\n - [sh, /tmp/install_chef.sh]\n - [ 'rm', '/etc/init/ssh.override' ]\n - [ 'service', 'ssh', 'start' ]\n"


    override.vm.box = "dummy"
    override.ssh.username = "ubuntu"
    override.ssh.private_key_path = ENV['SSH_KEY_PATH']
  end

  # Enable provisioning with chef solo, specifying a cookbooks path, roles
  # path, and data_bags path (all relative to this Vagrantfile), and adding
  # some recipes and/or roles.
  #
  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = "vagrant/cookbooks"
    chef.json.merge!({
                       :java => {
                         :install_flavor => "oracle",
                         :oracle => {
                           "accept_oracle_download_terms" => true
                         },
                         :jdk_version => "7",
                         :jdk => {
                           :"7" => {
                             :x86_64 => {
                               :url => "http://download.oracle.com/otn-pub/java/jdk/7u40-b43/jdk-7u40-linux-x64.tar.gz",
                               :checksum => "72f6e010592cad5e994276eee7db5f0b0d7c15c06949bd81f0e14811048bcf2c"
                             }
                           }
                         }
                       },
                       :cassandra => {
                         :version => "1.2.15"
                       },
                       :kafka => {
                         :version => "0.8.0",
                         :init_style => :upstart
                       }
    })

    chef.add_recipe "apt"
    chef.add_recipe "java"
    chef.add_recipe "kafka::zookeeper"
    chef.add_recipe "cassandra::datastax"
  end

end
