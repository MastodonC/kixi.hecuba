#
# Cookbook Name:: zookeeper
# Recipe:: default
#
# Copyright 2013, Simple Finance Technology Corp.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


actions :create, :delete, :create_if_missing
default_action :create if defined?(default_action)

attribute :path, :kind_of => String, :name_attribute => true
attribute :connect_str, :kind_of => String, :required => true
attribute :data, :kind_of => String

# zookeeper_node "/jones" do
#   action :create_if_missing
#   connect_str  "localhost:2181"
#   data "my-id"
# end
