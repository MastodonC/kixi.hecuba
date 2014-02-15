name              'yum'
maintainer        'Chef'
maintainer_email  'cookbooks@getchef.com'
license           'Apache 2.0'
description       'Configures various yum components on Red Hat-like systems'
long_description  IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version           '3.0.6'

supports 'redhat'
supports 'centos'
supports 'amazon'
supports 'fedora'
