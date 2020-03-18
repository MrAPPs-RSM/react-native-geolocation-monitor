require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-geolocation-monitor"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.swift_version = '4.2'
  s.framework = 'Foundation'
  s.platforms    = { :ios => "11.0" }
  s.source       = { :git => "https://github.com/MrAPPs-RSM/react-native-geolocation-monitor.git", :tag => "#{s.version}" }
  s.source_files = "ios/**/*.{h,m,swift}"

  s.dependency "React"
end
