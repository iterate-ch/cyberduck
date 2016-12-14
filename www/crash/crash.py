#!/usr/bin/python26
# -*- coding: utf-8 -*-

import cgi
import os
# Regular Expressions
import re
import smtplib
import email
from email.MIMEText import MIMEText
import sqlite3

from traceback import format_exception
from sys import exc_info
import logging

logging.basicConfig(level=logging.DEBUG,
                    format="%(asctime)s %(levelname)-8s %(message)s",
                    datefmt="%a, %d %b %Y %H:%M:%S",
                    filename="crash.log",
                    filemode="a")

# create table crash(ip TEXT, crashlog TEXT);
db = 'crash.sqlite'


def mailreport(crashlog, ip, revision):
    mail = MIMEText(crashlog)
    mail["To"] = "bugs@cyberduck.ch"
    mail["From"] = "noreply@cyberduck.ch"
    if revision:
        mail["Subject"] = "Cyberduck Crash Report (r" + revision + ") from " + ip
    else:
        mail["Subject"] = "Cyberduck Crash Report from " + ip
    mail["Date"] = email.Utils.formatdate(localtime=1)
    mail["Message-ID"] = email.Utils.make_msgid()
    s = smtplib.SMTP()
    s.connect("localhost")
    s.sendmail("noreply@cyberduck.ch", "bugs@cyberduck.ch", mail.as_string())
    s.quit()


if __name__ == "__main__":
    print "Content-type: text/html"
    print
    try:
        form = cgi.FieldStorage()
        revision = None
        if form.has_key("revision"):
            revision = form["revision"].value
        platform = None
        if form.has_key("os"):
            platform = form["os"].value
        if not revision:
            useragent = cgi.escape(os.environ["HTTP_USER_AGENT"])
            revision = re.match(r"Cyberduck \(([0-9]+)\)", useragent).group(1)
        ip = cgi.escape(os.environ["REMOTE_ADDR"])
        if form.has_key("crashlog"):
            logging.info("Crash Report from %s for OS %s and revision %s", ip, platform, revision)
            crashlog = form["crashlog"].value

            #add database entry
            conn = sqlite3.connect(db)
            c = conn.cursor()

            row = (ip, crashlog, revision, platform)
            try:
                c.execute('insert into crash values(?,?,?,?)', row)
            except sqlite3.IntegrityError, (ErrorMessage):
                logging.error('Error adding crashlog from IP %s:%s', ip, ErrorMessage)
                pass
            finally:
                # Save (commit) the changes
                conn.commit()
                # We can also close the cursor if we are done with it
                c.close()

            #send mail
            if revision:
                mailreport(crashlog, ip, revision)
    except:
        logging.error("Unexpected error:".join(format_exception(*exc_info())))
        cgi.print_exception()
