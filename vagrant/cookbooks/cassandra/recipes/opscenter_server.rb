include_recipe "java"

case node["platform_family"]
when "debian"

  if node['cassandra']['dse']
    dse = node['cassandra']['dse']
    if dse['credentials']['databag']
      dse_credentials = Chef::EncryptedDataBagItem.load(dse['credentials']['databag']['name'], dse['credentials']['databag']['item'])[dse['credentials']['databag']['entry']]
    else
      dse_credentials = dse['credentials']
    end
    apt_repository "datastax" do
      uri          "http://#{dse_credentials['username']}:#{dse_credentials['password']}@debian.datastax.com/enterprise"
      distribution "stable"
      components   ["main"]
      key          "https://debian.datastax.com/debian/repo_key"
      action :add
    end
  else
    apt_repository "datastax" do
      uri          "https://debian.datastax.com/community"
      distribution "stable"
      components   ["main"]
      key          "https://debian.datastax.com/debian/repo_key"
      action :add
    end
  end

when "rhel"
  include_recipe "yum"

  yum_repository "datastax" do
    repo_name "datastax"
    description "DataStax Repo for Apache Cassandra"
    url "http://rpm.datastax.com/community"
    action :add
  end
end
package "#{node[:cassandra][:opscenter][:server][:package_name]}" do
  action :install
end

service "opscenterd" do
  supports :restart => true, :status => true
  action [:enable, :start]
end

template "/etc/opscenter/opscenterd.conf" do
  source "opscenterd.conf.erb"
  mode 0644
  notifies :restart, resources(:service => "opscenterd"), :delayed
end

