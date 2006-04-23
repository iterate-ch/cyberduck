#!/usr/local/bin/python

import cgi
import smtplib
import email
from email.MIMEText import MIMEText

print "Content-type: text/html\n\n"
try:
	form = cgi.FieldStorage()
	if form.has_key("crashlog"):
		mail = MIMEText(form["crashlog"].value)
		mail["To"] = "bugs@cyberduck.ch"
	 	mail["From"]= "noreply@cyberduck.ch"
	 	mail["Subject"] ="Cyberduck Crash Report"
	 	mail["Date"] = email.Utils.formatdate(localtime=1)
	 	mail["Message-ID"] = email.Utils.make_msgid()
		s = smtplib.SMTP()
		s.connect("localhost")
		s.sendmail("noreply@cyberduck.ch", "bugs@cyberduck.ch", mail.as_string())
		s.quit()
except:
    cgi.print_exception()
