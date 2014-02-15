#
# Cookbook Name:: kafka
# Provider:: download
#

use_inline_resources

action :create do
  local_file_path = new_resource.name
  known_md5 = new_resource.md5_checksum

  remote_file local_file_path do
    source   new_resource.source
    mode     new_resource.mode
    checksum new_resource.checksum
    notifies :create, 'ruby_block[validate-download]', :immediately
  end

  ruby_block 'validate-download' do
    block do
      if known_md5 && !known_md5.empty?
        unless (checksum = Digest::MD5.file(local_file_path).hexdigest) == known_md5
          Chef::Application.fatal! %(Downloaded tarball checksum (#{checksum}) does not match known checksum (#{known_md5}))
        end
      else
        Chef::Log.debug 'No MD5 checksum set, not validating downloaded archive'
      end

      new_resource.updated_by_last_action(true)
    end
    action :nothing
  end
end
