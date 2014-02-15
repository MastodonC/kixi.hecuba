# encoding: utf-8

require 'spec_helper'

describe 'kafka::_setup' do
  let :chef_run do
    ChefSpec::Runner.new.converge(described_recipe)
  end

  context 'group and user' do
    context 'by default' do
      it 'creates a kafka group' do
        expect(chef_run).to create_group('kafka')
      end

      it 'creates a kafka user' do
        expect(chef_run).to create_user('kafka').with({
          shell: '/sbin/nologin',
          gid: 'kafka'
        })
      end
    end

    context 'when overridden' do
      let :chef_run do
        ChefSpec::Runner.new do |node|
          node.set[:kafka][:user] = 'spec'
          node.set[:kafka][:group] = 'spec'
        end.converge(described_recipe)
      end

      it 'creates a group with set name' do
        expect(chef_run).to create_group('spec')
      end

      it 'creates a user with set name' do
        expect(chef_run).to create_user('spec').with({
          shell: '/sbin/nologin',
          gid: 'spec'
        })
      end
    end
  end

  it 'creates installation directory' do
    expect(chef_run).to create_directory('/opt/kafka').with({
      owner: 'kafka',
      group: 'kafka',
      mode: '755'
    })
  end

  it 'creates build directory' do
    expect(chef_run).to create_directory('/opt/kafka/build').with({
      owner: 'kafka',
      group: 'kafka',
      mode: '755'
    })
  end

  it 'creates log directory' do
    expect(chef_run).to create_directory('/var/log/kafka').with({
      owner: 'kafka',
      group: 'kafka',
      mode: '755'
    })
  end

  it 'creates config directory' do
    expect(chef_run).to create_directory('/opt/kafka/config').with({
      owner: 'kafka',
      group: 'kafka',
      mode: '755'
    })
  end
end
