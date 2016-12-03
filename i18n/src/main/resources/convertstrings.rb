#!/usr/bin/ruby
# encoding: UTF-8

base_language_hash = Hash::new
target_language_hash = Hash::new
comment_hash = Hash::new

# Open base language
open($*[0], "rb:UTF-16LE:UTF-8") do |f|
  f.each { |line|
    pattern = '^"(.*)\..*" = "(.*)";$'
    r = Regexp.new(pattern, Regexp::FIXEDENCODING)
    r.match(line) { |m|
      key = m[1]
      base_language_hash.store(key, m[2])
    }
  }
end
# Open target language
open($*[1], "rb:UTF-16LE:UTF-8") do |f|
  f.each { |line|
    pattern = '^\/\* Class = "(.*)"; .* = "(.*)"; ObjectID = "(.*)"; \*\/$'
    r = Regexp.new(pattern, Regexp::FIXEDENCODING)
    r.match(line) { |m|
      key = m[3]
      translation = m[2]
      unless translation.strip.length == 0 or base_language_hash[key].eql?("OtherViews")
        target_language_hash.store(key, translation)
        comment_hash.store(key, "/* %s (%s) : <title:%s> (oid:%s) */" % [m[1], translation, translation, key])
      end
    }
  }
end

# Write Byte Order Mark
print "\uFEFF".encode("UTF-16LE")

target_language_hash.each { |key, value|
  comment = comment_hash[key]
  base = base_language_hash[key]
  output = "%s\n\"%s\" = \"%s\";\n\n" % [comment, base, value]
  print output.encode("UTF-16LE")
}
