#!/usr/bin/ruby
# encoding: UTF-8

message_hash = Hash::new

open($*[0], "rb:UTF-16LE:UTF-8") do |f|
  f.each { |line|
    pattern = '\/\* Class = "(.*)"; .* = "(.*)"; ObjectID = "(.*)"; \*\/'
    r = Regexp.new(pattern, Regexp::FIXEDENCODING)
    r.match(line) { |m|
      key = m[2]
      value = "/* %s (%s) : <title:%s> (oid:%s) */" % [m[1], m[2], m[2], m[3]]
      message_hash.store(key, value)
    }
  }
end

# Write Byte Order Mark
print "\uFEFF".encode("UTF-16LE")

message_hash.each { |key, comment|
  output = "%s\n\"%s\" = \"%s\";\n\n" % [comment, key, key]
  print output.encode("UTF-16LE")
}
